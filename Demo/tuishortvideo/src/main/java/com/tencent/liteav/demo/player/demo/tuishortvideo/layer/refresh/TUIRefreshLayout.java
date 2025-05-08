package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;

import java.lang.reflect.Method;

public class TUIRefreshLayout extends ViewGroup {

    protected Context mContext;

    protected int mTouchSlop;

    /*
    触发下拉刷新的最小高度。
    一般来说，触发下拉刷新的高度就是头部View的高度
     */
    private int mHeaderTriggerMinHeight = 100;

    /*
    触发下拉刷新的最大高度。
    一般来说，触发下拉刷新的高度就是头部View的高度
     */
    private int mHeaderTriggerMaxHeight = 400;

    /*
     触发上拉加载的最小高度。
     一般来说，触发上拉加载的高度就是尾部View的高度
      */
    private int mFooterTriggerMinHeight = 100;

    /*
    触发上拉加载的最大高度。
    一般来说，触发上拉加载的高度就是尾部View的高度
     */
    private int mFooterTriggerMaxHeight = 400;

    //头部容器
    private LinearLayout mHeaderLayout;

    //头部View
    private View mHeaderView;

    //尾部容器
    private LinearLayout mFooterLayout;

    //尾部View
    private View mFooterView;

    //标记 无状态（既不是上拉 也 不是下拉）
    private final int STATE_NOT = -1;

    //标记 上拉状态
    private final int STATE_UP = 1;

    //标记 下拉状态
    private final int STATE_DOWN = 2;

    //当前状态
    private int mCurrentState = STATE_NOT;

    //是否处于正在下拉刷新状态
    private boolean mIsRefreshing = false;

    //是否处于正在上拉加载状态
    private boolean mIsLoadingMore = false;

    //是否启用下拉功能（默认开启）
    private boolean mIsRefresh = true;

    /*
    是否启用上拉功能（默认不开启）
    如果设置了上拉加载监听器OnLoadMoreListener，就会自动开启。
     */
    private boolean mIsLoadMore = false;

    //上拉、下拉的阻尼 设置上下拉时的拖动阻力效果
    private int mDamp = 4;

    //头部状态监听器
    private OnHeaderStateListener mOnHeaderStateListener;

    //尾部状态监听器
    private OnFooterStateListener mOnFooterStateListener;

    //下拉刷新监听器
    private OnRefreshListener mOnRefreshListener;

    //上拉加载监听器
    private OnLoadMoreListener mOnLoadMoreListener;

    //是否还有更多数据
    private boolean mHasMore = true;

    //是否显示空布局
    private boolean mIsEmpty = false;

    //滑动到底部，自动触发加载更多
    private boolean mAutoLoadMore = true;

    //是否拦截触摸事件，
    private boolean mInterceptTouchEvent = false;

    //----------------  用于监听手指松开时，屏幕的滑动状态  -------------------//
    //手指松开时，不一定是滑动停止，也有可能是Fling，所以需要监听屏幕滑动的情况。
    // 每隔50毫秒获取一下页面的滑动距离，如果跟上次没有变化，表示滑动停止。
    // 之所以用延时获取滑动距离的方式获取滑动状态，是因为在sdk 23前，无法给所有的View设置OnScrollChangeListener。
    private final int SCROLL_DELAY = 50;
    private Handler mScrollHandler = new Handler();
    private Runnable mScrollChangeListener = new Runnable() {
        @Override
        public void run() {
            if (listenScrollChange()) {
                mScrollHandler.postDelayed(mScrollChangeListener, SCROLL_DELAY);
            } else {
                mFlingOrientation = ORIENTATION_FLING_NONE;
            }
        }
    };

    private int oldOffsetY;
    private int mFlingOrientation;
    private static final int ORIENTATION_FLING_NONE = 0;
    private static final int ORIENTATION_FLING_UP = 1;
    private static final int ORIENTATION_FLING_DOWN = 2;

    //手指触摸屏幕时的触摸点
    int mTouchX = 0;
    int mTouchY = 0;

    public TUIRefreshLayout(Context context) {
        this(context, null);
    }

    public TUIRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TUIRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setClipToPadding(false);
        initHeaderLayout();
        initFooterLayout();
        ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    /**
     * 初始化头部
     */
    private void initHeaderLayout() {
        mHeaderLayout = new LinearLayout(mContext);
        LayoutParams lp = new LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mHeaderLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mHeaderLayout.setLayoutParams(lp);
        addView(mHeaderLayout);
    }

    /**
     * 设置头部View
     *
     * @param headerView 头部View。这个View必须实现{@link OnHeaderStateListener}接口。
     */
    public void setHeaderView(@NonNull View headerView) {
        if (headerView instanceof OnHeaderStateListener) {
            mHeaderView = headerView;
            mHeaderLayout.removeAllViews();
            mHeaderLayout.addView(mHeaderView);
            mOnHeaderStateListener = (OnHeaderStateListener) headerView;
        } else {
            // headerView必须实现OnHeaderStateListener接口，
            // 并通过OnHeaderStateListener的回调来更新headerView的状态。
            throw new IllegalArgumentException("headerView must implement the OnHeaderStateListener");
        }
    }

    /**
     * 初始化尾部
     */
    private void initFooterLayout() {
        mFooterLayout = new LinearLayout(mContext);
        LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mFooterLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mFooterLayout.setLayoutParams(lp);
        addView(mFooterLayout);
    }

    /**
     * 设置尾部View
     *
     * @param footerView 尾部View。这个View必须实现{@link OnFooterStateListener}接口
     */
    public void setFooterView(@NonNull View footerView) {
        if (footerView instanceof OnFooterStateListener) {
            mFooterView = footerView;
            mFooterLayout.removeAllViews();
            mFooterLayout.addView(mFooterView);
            mOnFooterStateListener = (OnFooterStateListener) footerView;
        } else {
            // footerView必须实现OnFooterStateListener接口，
            // 并通过OnFooterStateListener的回调来更新footerView的状态。
            throw new IllegalArgumentException("footerView must implement the OnFooterStateListener");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量头部高度
        View headerView = getChildAt(0);
        measureChild(headerView, widthMeasureSpec, heightMeasureSpec);

        //测量尾部高度
        View footerView = getChildAt(1);
        measureChild(footerView, widthMeasureSpec, heightMeasureSpec);

        //测量内容容器宽高
        int count = getChildCount();
        int contentHeight = 0;
        int contentWidth = 0;
        if (mIsEmpty) {
            //空布局容器
            if (count > 3) {
                View emptyView = getChildAt(3);
                measureChildWithMargins(emptyView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams emptyLp = (MarginLayoutParams) emptyView.getLayoutParams();
                contentHeight = emptyView.getMeasuredHeight() + emptyLp.topMargin + emptyLp.bottomMargin;
                contentWidth = emptyView.getMeasuredWidth() + emptyLp.leftMargin + emptyLp.rightMargin;
            }
        } else {
            //内容布局容器
            if (count > 2) {
                View content = getChildAt(2);
                measureChildWithMargins(content, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams contentLp = (MarginLayoutParams) content.getLayoutParams();
                contentHeight = content.getMeasuredHeight() + contentLp.topMargin + contentLp.bottomMargin;
                contentWidth = content.getMeasuredWidth() + contentLp.leftMargin + contentLp.rightMargin;
            }
        }

        setMeasuredDimension(measureWidth(widthMeasureSpec, contentWidth),
                measureHeight(heightMeasureSpec, contentHeight));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //布局头部
        View headerView = getChildAt(0);
        headerView.layout(getPaddingLeft(), -headerView.getMeasuredHeight(),
                getPaddingLeft() + headerView.getMeasuredWidth(), 0);

        //布局尾部
        View footerView = getChildAt(1);
        footerView.layout(getPaddingLeft(), getMeasuredHeight(),
                getPaddingLeft() + footerView.getMeasuredWidth(),
                getMeasuredHeight() + footerView.getMeasuredHeight());

        int count = getChildCount();
        if (mIsEmpty) {
            //空布局容器
            if (count > 3) {
                View emptyView = getChildAt(3);
                MarginLayoutParams emptyLp = (MarginLayoutParams) emptyView.getLayoutParams();
                emptyView.layout(getPaddingLeft() + emptyLp.leftMargin,
                        getPaddingTop() + emptyLp.topMargin,
                        getPaddingLeft() + emptyLp.leftMargin + emptyView.getMeasuredWidth(),
                        getPaddingTop() + emptyLp.topMargin + emptyView.getMeasuredHeight());
            }
        } else {
            //内容布局容器
            if (count > 2) {
                View content = getChildAt(2);
                MarginLayoutParams contentLp = (MarginLayoutParams) content.getLayoutParams();
                content.layout(getPaddingLeft() + contentLp.leftMargin,
                        getPaddingTop() + contentLp.topMargin,
                        getPaddingLeft() + contentLp.leftMargin + content.getMeasuredWidth(),
                        getPaddingTop() + contentLp.topMargin + content.getMeasuredHeight());
            }
        }
    }

    private int measureWidth(int measureSpec, int contentWidth) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentWidth + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec, int contentHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = contentHeight + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    int oldY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentState = STATE_NOT;
                mTouchX = (int) ev.getX();
                mTouchY = (int) ev.getY();
                oldY = (int) ev.getY();
                mScrollHandler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_MOVE:
                int newY = (int) ev.getY();
                if (isNestedScroll()) {
                    if ((canPullDown() && newY > oldY)
                            || (canPullUp() && newY < oldY)) {
                        nestedScroll(oldY - newY);
                    }

                    if ((getScrollY() > 0 && newY > oldY)
                            || (getScrollY() < 0 && newY < oldY)) {
                        nestedPreScroll(oldY - newY);
                    }
                }

                if (newY > oldY) {
                    mFlingOrientation = ORIENTATION_FLING_UP;
                } else if (newY < oldY) {
                    mFlingOrientation = ORIENTATION_FLING_DOWN;
                }

                oldY = newY;

                break;
            case MotionEvent.ACTION_UP:
                int y = (int) ev.getY();
                int x = (int) ev.getX();
                //是否是点击事件，如果按下和松开的坐标没有改变，就认为是点击事件
                boolean isClick = Math.abs(mTouchX - x) < mTouchSlop && Math.abs(mTouchY - y) < mTouchSlop;

                if (!mInterceptTouchEvent && !isClick && !mIsEmpty) {
                    //监听手指松开时，屏幕的滑动状态
                    //手指松开时，不一定是滑动停止，也有可能是Fling，所以需要监听屏幕滑动的情况。
                    initScrollListen();
                    mScrollHandler.postDelayed(mScrollChangeListener, SCROLL_DELAY);
                }

                mTouchX = 0;
                mTouchY = 0;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchEvent = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                if (isRefreshingOrLoading()) {
                    return false;
                }

                if (pullRefresh() && y - mTouchY > mTouchSlop) {
                    return true;
                }

                if (mHasMore && pullLoadMore() && mTouchY - y > mTouchSlop) {
                    return true;
                }

                return false;
            case MotionEvent.ACTION_UP:
                return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentState == STATE_NOT) {
                    if (pullRefresh() && y - mTouchY > mTouchSlop) {
                        mCurrentState = STATE_DOWN;
                        mInterceptTouchEvent = true;
                    }
                    if (mHasMore && pullLoadMore() && mTouchY - y > mTouchSlop) {
                        mCurrentState = STATE_UP;
                        mInterceptTouchEvent = true;
                    }
                }
                if (mTouchY > y) {
                    if (mCurrentState == STATE_UP && !mIsEmpty) {
                        scroll((mTouchY - y) / mDamp, true);
                    }
                } else if (mCurrentState == STATE_DOWN || mIsEmpty) {
                    scroll((mTouchY - y) / mDamp, true);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mIsRefreshing && !mIsLoadingMore) {
                    int scrollOffset = Math.abs(getScrollY());
                    if (mCurrentState == STATE_DOWN || mIsEmpty) {
                        if (scrollOffset < getHeaderTriggerHeight()) {
                            restore(true);
                        } else {
                            triggerRefresh();
                        }
                    } else if (mCurrentState == STATE_UP) {
                        if (scrollOffset < getFooterTriggerHeight()) {
                            restore(true);
                        } else {
                            triggerLoadMore();
                        }
                    }
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 是否可下拉刷新。
     *
     * @return 启用了下拉刷新功能，并且可以下拉显示头部，返回true；否则返回false。
     */
    protected boolean pullRefresh() {
        return mIsRefresh && canPullDown();
    }

    /**
     * 是否可上拉加载更多。
     *
     * @return 启用了上拉加载更多功能，并且有更多数据，并且可以下拉显示头部，返回true，否则返回false。
     */
    protected boolean pullLoadMore() {
        return mIsLoadMore && mHasMore && canPullUp();
    }

    /**
     * 是否可下拉显示头部。
     *
     * @return 如果是空布局或者内容布局已经滑动到顶部，则返回true，否则返回false。
     */
    protected boolean canPullDown() {

        if (mIsEmpty) {
            return true;
        }

        if (getChildCount() >= 3) {
            return computeVerticalScrollOffset(getChildAt(2)) <= 0;
        }
        return true;
    }

    /**
     * 是否可上拉显示尾部。
     *
     * @return 如果不是空布局，并且内容布局已经滑动到底部，则返回true，否则返回false。
     */
    protected boolean canPullUp() {
        if (mIsEmpty) {
            return false;
        }
        if (getChildCount() >= 3) {
            View view = getChildAt(2);
            /*
            列表当前已经划过的 Y 值距离 + 当前屏幕上显示的列表的高度 >= 列表总高度，包括所有可见和不可见的区域
             */
            return computeVerticalScrollOffset(view) + computeVerticalScrollExtent(view)
                    >= computeVerticalScrollRange(view);
        }
        return false;
    }

    /**
     * 是否正在刷新或者正在加载更多
     */
    private boolean isRefreshingOrLoading() {
        return mIsRefreshing || mIsLoadingMore;
    }

    /**
     * 利用属性动画实现平滑滑动
     *
     * @param start       滑动的开始位置
     * @param end         滑动的结束位置
     * @param duration    滑动的持续时间
     * @param isListening 是否监听滑动变化
     * @param listener
     */
    private void smoothScroll(int start, int end, int duration, final boolean isListening,
                              Animator.AnimatorListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end).setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scroll((int) animation.getAnimatedValue(), isListening);
            }
        });
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }

    /**
     * @param y           滑动y的位置
     * @param isListening 是否监听滑动变化，如果为true，滑动的变化将回调到{@link OnHeaderStateListener}
     *                    或{@link OnFooterStateListener}。
     */
    private void scroll(int y, boolean isListening) {
        scrollTo(0, y);
        if (isListening) {
            int scrollOffset = Math.abs(y);
            if (mCurrentState == STATE_DOWN && mOnHeaderStateListener != null) {
                int height = getHeaderTriggerHeight();
                mOnHeaderStateListener.onScrollChange(mHeaderView, scrollOffset,
                        scrollOffset >= height ? 100 : scrollOffset * 100 / height);
            }

            if (mCurrentState == STATE_UP && mOnFooterStateListener != null && mHasMore) {
                int height = getFooterTriggerHeight();
                mOnFooterStateListener.onScrollChange(mFooterView, scrollOffset,
                        scrollOffset >= height ? 100 : scrollOffset * 100 / height);
            }
        }
    }

    /**
     * 是否要处理嵌套滑动。
     *
     * @return
     */
    private boolean isNestedScroll() {
        return isRefreshingOrLoading() || !mHasMore;
    }

    /**
     * 处理嵌套滑动。
     *
     * @param dy 滑动偏移量
     */
    private void nestedPreScroll(int dy) {
        if (mIsRefreshing) {
            int scrollY = getScrollY();
            if (dy > 0 && scrollY < 0) {
                scrollBy(0, Math.min(dy, -scrollY));
            }
        }

        if (mIsLoadingMore || !mHasMore) {
            int scrollY = getScrollY();
            if (dy < 0 && scrollY > 0) {
                scrollBy(0, Math.max(dy, -scrollY));
            }
        }
    }

    /**
     * 处理嵌套滑动。
     *
     * @param dy 滑动偏移量
     */
    private void nestedScroll(int dy) {
        if (mIsRefreshing) {
            int height = getHeaderTriggerHeight();
            int scrollY = getScrollY();
            if (dy < 0 && scrollY > -height) {
                int offset = -getScrollY() - height;
                if (offset < 0) {
                    scrollBy(0, Math.max(dy, offset));
                }
            }
        }

        if (mIsLoadingMore || !mHasMore) {
            int height = getFooterTriggerHeight();
            int scrollY = getScrollY();
            if (dy > 0 && scrollY < height) {
                scrollBy(0, Math.min(dy, height - scrollY));
            }
        }
    }

    /**
     * 手指松开时，监听滑动状态的初始工作
     */
    private void initScrollListen() {
        if (getChildCount() >= 3) {
            oldOffsetY = computeVerticalScrollOffset(getChildAt(2));
        }
    }

    int scrollVelocity = 0;


    /**
     * 监听手指松开时，屏幕的滑动状态
     *
     * @return 返回true表示正在滑动，继续监听；返回false表示滑动停止或者不需要监听。
     */
    private boolean listenScrollChange() {
        if (getChildCount() >= 3) {
            int offsetY = getScrollTopOffset();
            int interval = Math.abs(offsetY - oldOffsetY);
            if (interval > 0) {
                scrollVelocity = interval;
                oldOffsetY = offsetY;
                if (isNestedScroll()) {
                    if (canPullDown() && mFlingOrientation == ORIENTATION_FLING_UP) {
                        nestedScroll(-scrollVelocity);
                    } else if (canPullUp() && mFlingOrientation == ORIENTATION_FLING_DOWN) {
                        nestedScroll(scrollVelocity);
                    }

                    if (getScrollY() > 0 && mFlingOrientation == ORIENTATION_FLING_UP) {
                        nestedPreScroll(-scrollVelocity);
                    } else if (getScrollY() < 0 && mFlingOrientation == ORIENTATION_FLING_DOWN) {
                        nestedPreScroll(scrollVelocity);
                    }
                }
                return true;
            } else {
                // 滑动停止

                //滑动停止时，如果已经滑动到底部，自动触发加载更多
                if (mFlingOrientation == ORIENTATION_FLING_DOWN && mAutoLoadMore && pullLoadMore()) {
                    autoLoadMore();
                    return false;
                }

                if (scrollVelocity > 30) {
                    if (canPullDown() && mIsRefreshing && mFlingOrientation == ORIENTATION_FLING_UP) {
                        int height = getHeaderTriggerHeight();
                        smoothScroll(getScrollY(), -height, (int) (1.0f * height * SCROLL_DELAY / scrollVelocity),
                                false, null);
                    } else if ((mIsLoadingMore || !mHasMore) && canPullUp()
                            && mFlingOrientation == ORIENTATION_FLING_DOWN) {
                        int height = getFooterTriggerHeight();
                        smoothScroll(getScrollY(), height, (int) (1.0f * height * SCROLL_DELAY / scrollVelocity),
                                false, null);
                    }
                }
                scrollVelocity = 0;
                return false;
            }
        }
        return false;
    }

    /**
     * 获取内容布局滑动到顶部的偏移量
     */
    private int getScrollTopOffset() {
        if (getChildCount() >= 3) {
            View view = getChildAt(2);
            return computeVerticalScrollOffset(view);
        }
        return 0;
    }

    /**
     * 获取内容布局滑动到底部部的偏移量
     */
    private int getScrollBottomOffset() {
        if (getChildCount() >= 3) {
            View view = getChildAt(2);
            return computeVerticalScrollRange(view) - computeVerticalScrollOffset(view)
                    - computeVerticalScrollExtent(view);
        }
        return 0;
    }

    private int computeVerticalScrollOffset(View view) {
        try {
            Method method = View.class.getDeclaredMethod("computeVerticalScrollOffset");
            method.setAccessible(true);
            return (int) method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view.getScrollY();
    }

    private int computeVerticalScrollRange(View view) {
        try {
            Method method = View.class.getDeclaredMethod("computeVerticalScrollRange");
            method.setAccessible(true);
            return (int) method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view.getHeight();
    }

    private int computeVerticalScrollExtent(View view) {
        try {
            Method method = View.class.getDeclaredMethod("computeVerticalScrollExtent");
            method.setAccessible(true);
            return (int) method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view.getHeight();
    }

    /**
     * @param isRefresh 是否开启下拉刷新功能 默认开启
     */
    public void setRefreshEnable(boolean isRefresh) {
        mIsRefresh = isRefresh;
    }

    /**
     * @param isLoadMore 是否开启上拉功能 默认不开启
     */
    public void setLoadMoreEnable(boolean isLoadMore) {
        mIsLoadMore = isLoadMore;
    }

    /**
     * 还原
     */
    private void restore(boolean isListener) {
        smoothScroll(getScrollY(), 0, 200, isListener, null);
    }

    /**
     * 通知刷新完成。它会回调{@link OnHeaderStateListener#onRetract(View, boolean)}方法
     *
     * @param isSuccess 是否刷新成功
     */
    public void finishRefresh(boolean isSuccess) {
        if (mIsRefreshing) {
            mCurrentState = STATE_NOT;
            if (mOnHeaderStateListener != null) {
                mOnHeaderStateListener.onRetract(mHeaderView, isSuccess);
            }
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                //平滑收起头部。
                smoothScroll(getScrollY(), 0, 200, false, null);
                mIsRefreshing = false;
            }
        }, 500);
    }

    /**
     * 通知加载更多完成。它会回调{@link OnFooterStateListener#onRetract(View, boolean)}方法
     * 请使用{@link #finishLoadMore(boolean, boolean)}
     */
    @Deprecated
    //不推荐使用这个方法 因为同时调用它和hasMore(boolean)两个方法时，尾部无法显示“加载完成”的提示。推荐使用finishLoadMore(boolean hasMore);
    public void finishLoadMore() {
        //为了处理先调用finishLoadMore()，后调用hasMore(boolean)的情况;延时调用finishLoadMore(mHasMore);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                finishLoadMore(true, mHasMore);
            }
        }, 0);
    }

    /**
     * 通知加载更多完成。它会回调{@link OnFooterStateListener#onRetract(View, boolean)}方法
     *
     * @param isSuccess 是否加载成功
     * @param hasMore   是否还有更多数据
     */
    public void finishLoadMore(boolean isSuccess, final boolean hasMore) {
        if (mIsLoadingMore) {
            mCurrentState = STATE_NOT;
            if (mOnFooterStateListener != null) {
                mOnFooterStateListener.onRetract(mFooterView, isSuccess);
            }
        }
        // 处理尾部的收起。
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsLoadingMore = false;
                hasMore(hasMore);
                if (getScrollBottomOffset() > 0) {
                    // 如果有新的内容加载出来，就收起尾部，并把新内容显示出来。。
                    if (getChildCount() >= 3) {
                        View v = getChildAt(2);
                        if (v instanceof AbsListView) {
                            AbsListView listView = (AbsListView) v;
                            listView.smoothScrollBy(getScrollY(), 0);
                        } else if (!(v instanceof TUIShortVideoView)) {
                            v.scrollBy(0, getScrollY());
                        }
                    }
                    smoothScroll(getScrollY(), 0, 200, false, null);
                } else if (mHasMore) {
                    smoothScroll(getScrollY(), 0, 200, false, null);
                }
            }
        }, 500);
    }

    /**
     * 自动触发下拉刷新。只有启用了下拉刷新功能时起作用。
     */
    public void autoRefresh() {
        if (!mIsRefresh || isRefreshingOrLoading()) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                mCurrentState = STATE_DOWN;
                smoothScroll(getScrollY(), -getHeaderTriggerHeight(), 200, true, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        triggerRefresh();
                    }
                });
            }
        });
    }

    /**
     * 触发下拉刷新
     */
    private void triggerRefresh() {
        if (!mIsRefresh || isRefreshingOrLoading()) {
            return;
        }

        mIsRefreshing = true;
        mCurrentState = STATE_NOT;
        scroll(-getHeaderTriggerHeight(), false);
        if (mOnHeaderStateListener != null) {
            mOnHeaderStateListener.onRefresh(mHeaderView);
        }

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }

    /**
     * 自动触发上拉加载更多。只有在启用了上拉加载更多功能并且有更多数据时起作用。
     */
    public void autoLoadMore() {
        if (isRefreshingOrLoading() || !mHasMore || !mIsLoadMore || mIsEmpty) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                mCurrentState = STATE_UP;
                smoothScroll(getScrollY(), getFooterTriggerHeight(), 200, true, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        triggerLoadMore();
                    }
                });
            }
        });
    }

    /**
     * 触发上拉加载更多。
     */
    private void triggerLoadMore() {
        if (isRefreshingOrLoading() || !mHasMore || !mIsLoadMore || mIsEmpty) {
            return;
        }
        mIsLoadingMore = true;
        mCurrentState = STATE_NOT;
        scroll(getFooterTriggerHeight(), false);
        if (mOnFooterStateListener != null) {
            mOnFooterStateListener.onRefresh(mFooterView);
        }
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }

    /**
     * 是否自动触发加载更多。只有在启用了上拉加载更多功能时起作用。
     *
     * @param autoLoadMore 如果为true，滑动到底部，自动触发加载更多
     */
    public void setAutoLoadMore(boolean autoLoadMore) {
        mAutoLoadMore = autoLoadMore;
    }

    /**
     * 是否还有更多数据，只有为true是才能上拉加载更多.
     * 它会回调{@link OnFooterStateListener#hasMore(boolean)}方法。
     *
     * @param hasMore 默认为true。
     */
    public void hasMore(boolean hasMore) {
        if (mHasMore != hasMore) {
            mHasMore = hasMore;
            if (mOnFooterStateListener != null) {
                mOnFooterStateListener.onHasMore(mFooterView, hasMore);
            }
        }
    }

    /**
     * 获取触发下拉刷新的下拉高度
     *
     * @return
     */
    public int getHeaderTriggerHeight() {
        int height = mHeaderLayout.getHeight();
        height = Math.max(height, mHeaderTriggerMinHeight);
        height = Math.min(height, mHeaderTriggerMaxHeight);
        return height;
    }

    /**
     * 获取触发上拉加载的上拉高度
     *
     * @return
     */
    public int getFooterTriggerHeight() {
        int height = mFooterLayout.getHeight();
        height = Math.max(height, mFooterTriggerMinHeight);
        height = Math.min(height, mFooterTriggerMaxHeight);
        return height;
    }

    /**
     * 设置触发下拉刷新的最小高度。
     *
     * @param headerTriggerMinHeight
     */
    public void setHeaderTriggerMinHeight(int headerTriggerMinHeight) {
        mHeaderTriggerMinHeight = headerTriggerMinHeight;
    }

    /**
     * 设置触发下拉刷新的最大高度。
     *
     * @param headerTriggerMaxHeight
     */
    public void setHeaderTriggerMaxHeight(int headerTriggerMaxHeight) {
        mHeaderTriggerMaxHeight = headerTriggerMaxHeight;
    }

    /**
     * 设置触发上拉加载的最小高度。
     *
     * @param footerTriggerMinHeight
     */
    public void setFooterTriggerMinHeight(int footerTriggerMinHeight) {
        mFooterTriggerMinHeight = footerTriggerMinHeight;
    }

    /**
     * 设置触发上拉加载的最大高度。
     *
     * @param footerTriggerMaxHeight
     */
    public void setFooterTriggerMaxHeight(int footerTriggerMaxHeight) {
        mFooterTriggerMaxHeight = footerTriggerMaxHeight;
    }

    /**
     * 设置拉动阻力 （1到10）
     *
     * @param damp
     */
    public void setDamp(int damp) {
        if (damp < 1) {
            mDamp = 1;
        } else if (damp > 10) {
            mDamp = 10;
        } else {
            mDamp = damp;
        }
    }

    /**
     * 隐藏内容布局，显示空布局
     */
    public void showEmpty() {
        if (!mIsEmpty) {
            mIsEmpty = true;
            //显示空布局
            if (getChildCount() > 3) {
                getChildAt(3).setVisibility(VISIBLE);
            }
            //隐藏内容布局
            if (getChildCount() > 2) {
                getChildAt(2).setVisibility(GONE);
            }
        }
    }

    /**
     * 隐藏空布局，显示内容布局
     */
    public void hideEmpty() {
        if (mIsEmpty) {
            mIsEmpty = false;
            //隐藏空布局
            if (getChildCount() > 3) {
                getChildAt(3).setVisibility(GONE);
            }
            //显示内容布局
            if (getChildCount() > 2) {
                getChildAt(2).setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 设置加载更多的监听，触发加载时回调。
     * RefreshLayout默认没有启用上拉加载更多的功能，如果设置了OnLoadMoreListener，则自动启用。
     *
     * @param listener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
        if (listener != null) {
            setLoadMoreEnable(true);
        }
    }

    /**
     * 设置刷新监听，触发刷新时回调
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }


    //----------------  监听接口  -------------------//

    /**
     * 头部状态监听器
     */
    public interface OnHeaderStateListener {

        /**
         * 头部滑动变化
         *
         * @param headerView   头部View
         * @param scrollOffset 滑动距离
         * @param scrollRatio  从开始到触发阀值的滑动比率（0到100）如果滑动到达了阀值，就算再滑动，这个值也是100
         */
        void onScrollChange(View headerView, int scrollOffset, int scrollRatio);

        /**
         * 头部处于刷新状态 （触发下拉刷新的时候调用）
         *
         * @param headerView 头部View
         */
        void onRefresh(View headerView);

        /**
         * 刷新完成，头部收起
         *
         * @param headerView 头部View
         * @param isSuccess  是否刷新成功
         */
        void onRetract(View headerView, boolean isSuccess);

    }

    /**
     * 尾部状态监听器
     */
    public interface OnFooterStateListener {

        /**
         * 尾部滑动变化
         *
         * @param footerView   尾部View
         * @param scrollOffset 滑动距离
         * @param scrollRatio  从开始到触发阀值的滑动比率（0到100）如果滑动到达了阀值，就算在滑动，这个值也是100
         */
        void onScrollChange(View footerView, int scrollOffset, int scrollRatio);

        /**
         * 尾部处于加载状态 （触发上拉加载的时候调用）
         *
         * @param footerView 尾部View
         */
        void onRefresh(View footerView);

        /**
         * 加载完成，尾部收起
         *
         * @param footerView 尾部View
         * @param isSuccess  是否加载成功
         */
        void onRetract(View footerView, boolean isSuccess);

        /**
         * 是否还有更多(是否可以加载下一页)
         *
         * @param footerView
         * @param hasMore
         */
        void onHasMore(View footerView, boolean hasMore);
    }

    /**
     * 上拉加载监听器
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * 下拉更新监听器
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}
