package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.superplayer.R;

import java.util.List;

/**
 * 一个带有打点的，模仿seekbar的view
 * <p>
 * 除seekbar基本功能外，还具备关键帧信息打点的功能
 * <p>
 * 1、添加打点信息{@link #addPoint(PointParams, int)}
 * <p>
 * 2、自定义thumb{@link TCThumbView}
 * <p>
 * 3、打点view{@link TCPointView}
 * <p>
 * 4、打点信息参数{@link PointParams}
 */
public class PointSeekBar extends RelativeLayout {

    private int                         mWidth;                 // 自身宽度
    private int                         mHeight;                // 自身高度
    private int                         mSeekBarLeft;           // SeekBar的起点位置
    private int                         mSeekBarRight;          // SeekBar的终点位置
    private int                         mBgTop;                 // 进度条距离父布局上边界的距离
    private int                         mBgBottom;              // 进度条距离父布局下边界的距离
    private int                         mRoundSize;             // 进度条圆角大小
    private int                         mViewEnd;               // 自身的右边界
    private Paint                       mNormalPaint;           // seekbar背景画笔
    private Paint                       mProgressPaint;         // seekbar进度条画笔
    private Paint                       mPointerPaint;          // 打点view画笔
    private Paint                       mPlayablePaint;         // 缓存画笔
    private Drawable                    mThumbDrawable;         // 拖动块图片
    private int                         mHalfDrawableWidth;     // Thumb图片宽度的一半
    private float                       mThumbLeft;             // thumb的marginLeft值
    private float                       mThumbRight;            // thumb的marginRight值
    private float                       mThumbTop;              // thumb的marginTop值
    private float                       mThumbBottom;           // thumb的marginBottom值
    private boolean                     mIsOnDrag;              // 是否处于拖动状态
    private float                       mCurrentLeftOffset = 0; // thumb距离打点view的偏移量
    private float                       mLastX;                 // 上一次点击事件的横坐标，用于计算偏移量
    private int                         mCurrentProgress;       // 当前seekbar的数值
    private int                         mCurrentPlayableProgress; // 当前playable的数值
    private int                         mMaxProgress       = 100;     // seekbar最大数值
    private float                       mBarHeightPx       = 0;       // seekbar的高度大小 px
    private TCThumbView                 mThumbView;             // 滑动ThumbView
    private List<PointParams>           mPointList;             // 打点信息的列表
    private OnSeekBarPointClickListener mPointClickListener;    // 打点view点击回调
    private boolean                     mIsChangePointViews;    // 打点信息是否更新过
    private LayoutParams                mLayoutParams;
    private LayoutParams                mThumbViewParams;
    private LayoutParams                mTcPointViewLayoutParams;
    private RectF                       mRectF;
    private RectF                       mPRectF;
    private RectF                       mTRectF;
    private float                       mPlayableRight;

    public PointSeekBar(Context context) {
        super(context);
        init(null);
    }

    public PointSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PointSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 设置seekbar进度值
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMaxProgress) {
            progress = mMaxProgress;
        }
        if (!mIsOnDrag) {
            mCurrentProgress = progress;
            invalidate();
            callbackProgressInternal(progress, false);
        }
    }

    /**
     * 设置seekbar playable进度值
     *
     * @param secondaryProgress
     */
    public void setSecondaryProgress(int secondaryProgress) {
        if (secondaryProgress < 0) {
            secondaryProgress = 0;
        }
        if (secondaryProgress > mMaxProgress) {
            secondaryProgress = mMaxProgress;
        }
        mCurrentPlayableProgress = secondaryProgress;
        mPlayableRight = (mSeekBarRight - 0) * (mCurrentPlayableProgress * 1.0f / mMaxProgress);
        invalidate();
    }

    /**
     * 设置seekbar最大值
     *
     * @param max
     */
    public void setMax(int max) {
        mMaxProgress = max;
    }

    /**
     * 获取seekbar进度值
     *
     * @return
     */
    public int getProgress() {
        return mCurrentProgress;
    }

    /**
     * 获取seekbar最大值
     *
     * @return
     */
    public int getMax() {
        return mMaxProgress;
    }

    private void init(AttributeSet attrs) {
        setWillNotDraw(false);
        int progressColor = getResources().getColor(R.color.superplayer_default_progress_color);
        int backgroundColor = getResources().getColor(R.color.superplayer_default_progress_background_color);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SuperPlayerTCPointSeekBar);
            mThumbDrawable = a.getDrawable(R.styleable.SuperPlayerTCPointSeekBar_psb_thumbBackground);
            mHalfDrawableWidth = mThumbDrawable.getIntrinsicWidth() / 2;
            progressColor = a.getColor(R.styleable.SuperPlayerTCPointSeekBar_psb_progressColor, progressColor);
            backgroundColor = a.getColor(R.styleable.SuperPlayerTCPointSeekBar_psb_backgroundColor, backgroundColor);
            mCurrentProgress = a.getInt(R.styleable.SuperPlayerTCPointSeekBar_psb_progress, 0);
            mMaxProgress = a.getInt(R.styleable.SuperPlayerTCPointSeekBar_psb_max, 100);

            mBarHeightPx = a.getDimension(R.styleable.SuperPlayerTCPointSeekBar_psb_progressHeight, 8);
            a.recycle();
        }
        mNormalPaint = new Paint();
        mNormalPaint.setColor(backgroundColor);

        mPointerPaint = new Paint();
        mPointerPaint.setColor(Color.RED);

        mPlayablePaint = new Paint();
        mPlayablePaint.setColor(Color.WHITE);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        this.post(new Runnable() {
            @Override
            public void run() {
                addThumbView();
            }
        });
        mRectF = new RectF();
        mPRectF = new RectF();
        mTRectF = new RectF();

        mTcPointViewLayoutParams = new LayoutParams(mThumbDrawable.getIntrinsicWidth(),
                mThumbDrawable.getIntrinsicWidth());
        mThumbView = new TCThumbView(getContext(), mThumbDrawable);
        mThumbViewParams = new LayoutParams(mThumbDrawable.getIntrinsicHeight(), mThumbDrawable.getIntrinsicHeight());
    }

    /**
     * 改变thumb位置
     */
    private void changeThumbPos() {
        mLayoutParams.leftMargin = (int) mThumbLeft;
        mLayoutParams.topMargin = (int) mThumbTop;
        mThumbView.setLayoutParams(mLayoutParams);
    }

    private void addThumbView() {
        mThumbView.setLayoutParams(mThumbViewParams);
        mLayoutParams = (LayoutParams) mThumbView.getLayoutParams();
        addView(mThumbView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mSeekBarLeft = mHalfDrawableWidth;
        mSeekBarRight = mWidth - mHalfDrawableWidth;

        float barPaddingTop = (mHeight - mBarHeightPx) / 2;
        mBgTop = (int) barPaddingTop;
        mBgBottom = (int) (mHeight - barPaddingTop);
        mRoundSize = mHeight / 2;

        mViewEnd = mWidth;

        // 解决播放前 seekbar点就已经领先进度原点的问题
        invalidate();

    }

    private void calProgressDis() {
        mThumbLeft = (mSeekBarRight - mSeekBarLeft) * (mCurrentProgress * 1.0f / mMaxProgress);
        mLastX = mThumbLeft;
        mCurrentLeftOffset = 0;
        calculatePointerRect();
    }


    private void addThumbAndPointViews() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mIsChangePointViews) {
                    PointSeekBar.this.removeAllViews();
                    if (mPointList != null) {
                        for (int i = 0; i < mPointList.size(); i++) {
                            addPoint(mPointList.get(i), i);
                        }
                    }
                    addThumbView();
                    mIsChangePointViews = false;
                }
                if (!mIsOnDrag) {
                    calProgressDis();
                    changeThumbPos();
                }
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw  bg
        mRectF.left = mSeekBarLeft;
        mRectF.right = mSeekBarRight;
        mRectF.top = mBgTop;
        mRectF.bottom = mBgBottom;
        canvas.drawRoundRect(mRectF, mRoundSize, mRoundSize, mNormalPaint);

        //draw progress
        mPRectF.left = mSeekBarLeft;
        mPRectF.top = mBgTop;
        mPRectF.right = mThumbRight - mHalfDrawableWidth;
        mPRectF.bottom = mBgBottom;
        canvas.drawRoundRect(mPRectF,
                mRoundSize, mRoundSize, mProgressPaint);

        // draw playable progress
        mTRectF.left = mThumbRight - mHalfDrawableWidth;
        mTRectF.top = mBgTop;
        mTRectF.bottom = mBgBottom;

        // 避免刚开始播放时候 seekbar原点和最左端的一段白色
        float tempRight = mPlayableRight < mSeekBarLeft ? mSeekBarLeft : mPlayableRight;

        // 避免从点播切换到直播的时候 最右端出现白色
        mTRectF.right = (tempRight > mTRectF.left) ? tempRight : mTRectF.left;
        canvas.drawRoundRect(mTRectF, mRoundSize, mRoundSize, mPlayablePaint);

        addThumbAndPointViews();
    }

    /**
     * 添加打点view
     *
     * @param pointParams
     * @param index
     */
    public void addPoint(PointParams pointParams, final int index) {
        final TCPointView view = new TCPointView(getContext());
        mTcPointViewLayoutParams = new LayoutParams(mThumbDrawable.getIntrinsicWidth(),
                mThumbDrawable.getIntrinsicWidth());
        mTcPointViewLayoutParams.leftMargin = (int)
                ((pointParams.progress * 1.0f / mMaxProgress) * (mSeekBarRight - mSeekBarLeft));
        view.setDrawRect((mThumbDrawable.getIntrinsicWidth() - (mBgBottom - mBgTop)) / 2,
                mBgTop, mBgBottom,
                ((mThumbDrawable.getIntrinsicWidth() - (mBgBottom - mBgTop)) / 2) + (mBgBottom - mBgTop));
        view.setLayoutParams(mTcPointViewLayoutParams);
        view.setColor(pointParams.color);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPointClickListener != null) {
                    mPointClickListener.onSeekBarPointClick(view, index);
                }
            }
        });
        addView(view);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        boolean isHandle = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isHandle = handleDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isHandle = handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isHandle = handleUpEvent(event);
                break;

        }
        return true;
    }

    private boolean handleUpEvent(MotionEvent event) {
        // 需要使用event的x坐标 使用mThumbLeft 是使得点击无反应
        float x = event.getX();
        if (mListener != null) {
            mCurrentProgress = (int) ((x / (float) (mWidth)) * mMaxProgress);
            // 针对 向左向右过度拖动 做保护
            if (mCurrentProgress > 100) {
                mCurrentProgress = 100;
            }
            if (mCurrentProgress < 0) {
                mCurrentProgress = 0;
            }
            calProgressDis();
            changeThumbPos();
            invalidate();
            mListener.onStopTrackingTouch(this);
        }
        requestDisallowInterceptTouchEvent(false);
        mIsOnDrag = false;
        return true;
    }

    private boolean handleMoveEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mCurrentLeftOffset = x - mLastX;
            //计算出标尺的Rect
            calculatePointerRect();
            if (mThumbRight - mHalfDrawableWidth <= mSeekBarLeft) {
                mThumbLeft = 0;
                mThumbRight = mThumbLeft + mThumbDrawable.getIntrinsicWidth();
            }
            if (mThumbLeft + mHalfDrawableWidth >= mSeekBarRight) {
                mThumbRight = mWidth;
                mThumbLeft = mWidth - mThumbDrawable.getIntrinsicWidth();
            }
            changeThumbPos();
            invalidate();
            callbackProgress();
            mLastX = x;
            return true;
        }
        return false;
    }

    /**
     * 更新progress
     */
    private void callbackProgress() {
        if (mThumbLeft == 0) {
            callbackProgressInternal(0, true);
        } else if (mThumbRight == mWidth) {
            callbackProgressInternal(mMaxProgress, true);
        } else {
            float pointerMiddle = mThumbLeft + mHalfDrawableWidth;
            if (pointerMiddle >= mViewEnd) {
                callbackProgressInternal(mMaxProgress, true);
            } else {
                float percent = pointerMiddle / mViewEnd * 1.0f;
                int progress = (int) (percent * mMaxProgress);
                if (progress > mMaxProgress) {
                    progress = mMaxProgress;
                }
                callbackProgressInternal(progress, true);
            }
        }
    }

    /**
     * 更新windowPlayer FullscreenPlayer的 duration
     *
     * @param progress
     * @param isFromUser
     */
    private void callbackProgressInternal(int progress, boolean isFromUser) {
        mCurrentProgress = progress;
        if (mListener != null) {
            mListener.onProgressChanged(this, progress, isFromUser);
        }
    }


    private boolean handleDownEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (x >= mThumbLeft - 100 && x <= mThumbRight + 100) {
            if (mListener != null) {
                mListener.onStartTrackingTouch(this);
            }
            mIsOnDrag = true;
            mLastX = x;
            requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return false;
    }

    /**
     * 计算拖动块的坐标
     */
    private void calculatePointerRect() {
        //draw pointer
        mThumbLeft = mThumbLeft + mCurrentLeftOffset;
        mThumbRight = mThumbLeft + mCurrentLeftOffset + mThumbDrawable.getIntrinsicWidth();
        mThumbTop = 0;
        mThumbBottom = mHeight;
    }

    private OnSeekBarChangeListener mListener;

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mListener = listener;
    }

    public interface OnSeekBarChangeListener {

        void onProgressChanged(PointSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(PointSeekBar seekBar);

        void onStopTrackingTouch(PointSeekBar seekBar);
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setOnPointClickListener(OnSeekBarPointClickListener listener) {
        mPointClickListener = listener;
    }

    /**
     * 打点view点击回调
     */
    public interface OnSeekBarPointClickListener {
        void onSeekBarPointClick(View view, int pos);
    }

    /**
     * 设置打点信息列表
     *
     * @param pointList
     */
    public void setPointList(List<PointParams> pointList) {
        mPointList = pointList;
        mIsChangePointViews = true;
        invalidate();
    }

    /**
     * 打点信息
     */
    public static class PointParams {
        int progress = 0;       // 视频进度值(秒)
        int color = Color.RED;  // 打点view的颜色

        public PointParams(int progress, int color) {
            this.progress = progress;
            this.color = color;
        }
    }

    /**
     * 打点view
     */
    private static class TCPointView extends View {
        private int mColor = Color.WHITE; // view颜色
        private Paint mPaint;               // 画笔
        private RectF mRectF;               // 打点view的位置信息(矩形)

        public TCPointView(Context context) {
            super(context);
            init();
        }

        public TCPointView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public TCPointView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(mColor);
            mRectF = new RectF();
        }

        /**
         * 设置打点颜色
         *
         * @param color
         */
        public void setColor(int color) {
            mColor = color;
            mPaint.setColor(mColor);
        }

        /**
         * 设置打点view的位置信息
         *
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        public void setDrawRect(float left, float top, float right, float bottom) {
            mRectF.left = left;
            mRectF.top = top;
            mRectF.right = right;
            mRectF.bottom = bottom;
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(mRectF, mPaint);
        }
    }

    /**
     * 拖动块view
     */
    private static class TCThumbView extends View {
        private Paint mPaint;        // 画笔
        private Rect mRect;         // 位置信息(矩形)
        private Drawable mThumbDrawable;// thumb图片

        public TCThumbView(Context context, Drawable drawable) {
            super(context);
            mThumbDrawable = drawable;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mRect = new Rect();
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mRect.left = 0;
            mRect.top = 0;
            mRect.right = w;
            mRect.bottom = h;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mThumbDrawable.setBounds(mRect);
            mThumbDrawable.draw(canvas);
        }
    }


}
