package com.tencent.liteav.demo.player.demo.tuishortvideo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;

/**
 * video seek bar
 * Simultaneously possess the ability to drag and display progress.
 * And it can change size by drag.
 */
public class VideoSeekBar extends ViewGroup {

    private static final float DEFAULT_RADIUS = 10;
    // progress bar is twice the size of progress
    private static final float PROGRESS_BAR_FACTOR = 1.5F;
    // attr params
    private int mProgressBackgroundColor;
    private int mProgressColor;
    private int mDragBarReachedColor;
    private int mProgressBarColor;
    private Drawable mDragBarDrawable;
    private float mProgressHeight;
    private float mProgressDragHeight;
    private boolean mIsShowBarOnNormal;
    private float mCurrentProgress;
    private float mCurrentBarProgress;
    private int mDragBarHeight = 0;
    private int mDragBarRadius = 0;
    // draw paint
    private final Paint mBackgroundPaint = new Paint();
    private final Paint mProgressPaint = new Paint();
    private final Paint mProgressBarPaint = new Paint();
    private final Paint mBarReachedPaint = new Paint();
    // draw rect
    private final RectF mBackRectF = new RectF();
    private final RectF mProgressRectF = new RectF();
    private final RectF mBarReachedRectF = new RectF();
    // control param
    private boolean mIsDragging = false;
    private boolean mProgressCanSetOnDrag = true;
    // touch event temporary variable
    private float mCurrentDragOffset;
    private float mLastX;
    // drag bar bound
    private int mDragBarLeft;
    private int mDragBarRight;
    private int mDragBarTop;
    private int mDragBarBottom;
    // progress bar pos
    private float mProgressBarX;
    private float mProgressBarY;
    private float mProgressBarRadius;
    private float mProgressInnerWidth;
    // container size
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mLeft = 0;
    private int mRight = 0;

    private VideoSeekListener mListener;

    public VideoSeekBar(Context context) {
        this(context, null);
    }

    public VideoSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        setWillNotDraw(false);
        if (null != attrs) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VideoSeekBar);
            mProgressBackgroundColor = typedArray.getColor(R.styleable.VideoSeekBar_vsb_progress_background_color,
                    Color.GRAY);
            mProgressColor = typedArray.getColor(R.styleable.VideoSeekBar_vsb_progress_color, Color.WHITE);
            mDragBarReachedColor = typedArray.getColor(R.styleable.VideoSeekBar_vsb_drag_bar_reached_color, Color.RED);
            mProgressBarColor = typedArray.getColor(R.styleable.VideoSeekBar_vsb_progress_bar_color, Color.BLUE);
            mDragBarDrawable = typedArray.getDrawable(R.styleable.VideoSeekBar_vsb_drag_bar);
            mProgressHeight = typedArray.getDimension(R.styleable.VideoSeekBar_vsb_progress_height, 5);
            mProgressDragHeight = typedArray.getDimension(R.styleable.VideoSeekBar_vsb_progress_drag_height, 10);
            mIsShowBarOnNormal = typedArray.getBoolean(R.styleable.VideoSeekBar_vsb_drag_bar_show_on_normal, false);
            mCurrentProgress = typedArray.getFloat(R.styleable.VideoSeekBar_vsb_progress, 0);
            mCurrentBarProgress = typedArray.getFloat(R.styleable.VideoSeekBar_vsb_bar_progress, 0);
            typedArray.recycle();
        }
        if (null != mDragBarDrawable) {
            int mDragBarWidth = mDragBarDrawable.getIntrinsicWidth();
            mDragBarHeight = mDragBarDrawable.getIntrinsicHeight();
            mDragBarRadius = mDragBarWidth / 2;
        }
        mBackgroundPaint.setColor(mProgressBackgroundColor);
        mProgressPaint.setColor(mProgressColor);
        mProgressBarPaint.setColor(mProgressBarColor);
        mBarReachedPaint.setColor(mDragBarReachedColor);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mLeft = mDragBarRadius;
        mRight = w - mDragBarRadius;

        mViewWidth = w - (mDragBarRadius * 2);
        mViewHeight = h;

        calculateBack();
        calculateCurrentProgress();
        calculateProgressBar();
        calculateCurrentBarProgress();
        calculateCurrentDragBar();
    }

    private void calculateBack() {
        float proHeight = (mIsDragging || mIsShowBarOnNormal) ? mProgressDragHeight : mProgressHeight;
        mBackRectF.left = mLeft;
        mBackRectF.top = (mViewHeight - proHeight) / 2F;
        mBackRectF.right = mRight;
        mBackRectF.bottom = (mViewHeight + proHeight) / 2F;
    }

    private void calculateCurrentProgress() {
        float proHeight = (mIsDragging || mIsShowBarOnNormal) ? mProgressDragHeight : mProgressHeight;
        mProgressRectF.left = mLeft;
        mProgressRectF.right = mLeft + (mViewWidth * mCurrentProgress);
        mProgressRectF.top = (mViewHeight - proHeight) / 2F;
        mProgressRectF.bottom = (mViewHeight + proHeight) / 2F;
    }

    private void calculateProgressBar() {
        float proHeight = (mIsDragging || mIsShowBarOnNormal) ? mProgressDragHeight : mProgressHeight;
        mProgressBarRadius = (proHeight * PROGRESS_BAR_FACTOR) / 2F;
        mProgressInnerWidth = (int) (mViewWidth - (mProgressBarRadius * 2));
        mProgressBarX = mLeft + mProgressBarRadius + (mProgressInnerWidth * mCurrentProgress);
        mProgressBarY = mViewHeight / 2F;
    }

    private void calculateCurrentBarProgress() {
        float proHeight = (mIsDragging || mIsShowBarOnNormal) ? mProgressDragHeight : mProgressHeight;
        mBarReachedRectF.left = mLeft;
        mBarReachedRectF.right = mLeft + (mViewWidth * mCurrentBarProgress);
        mBarReachedRectF.top = (mViewHeight - proHeight) / 2F;
        mBarReachedRectF.bottom = (mViewHeight + proHeight) / 2F;
    }

    private void calculateCurrentDragBar() {
        int dragCenter = (int) (mLeft + (mViewWidth * mCurrentBarProgress));
        mDragBarLeft = dragCenter - mDragBarRadius;
        mDragBarRight = dragCenter + mDragBarRadius;
        mDragBarTop = (mViewHeight - mDragBarHeight) / 2;
        mDragBarBottom = (mViewHeight + mDragBarHeight) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int height = getHeight();
        final int width = getWidth();
        canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        drawProgressBack(canvas);
        drawProgress(canvas);
        drawProgressBar(canvas);
        drawBarProgress(canvas);
        drawDragBar(canvas);
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        boolean isHandle = false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isHandle = handleDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                requestDisallowInterceptTouchEvent(true);
                isHandle = handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isHandle = handleUpEvent(event);
                break;
            default:
                break;

        }
        return true;
    }

    private boolean handleMoveEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsDragging) {
            mCurrentDragOffset = x - mLastX;
            // calc dragBar bound
            mDragBarLeft += mCurrentDragOffset;
            mDragBarRight += mCurrentDragOffset;
            if (mDragBarLeft < 0) {
                mDragBarLeft = 0;
                mDragBarRight = mDragBarRadius;
            }
            if (mDragBarRight > (mLeft + mViewWidth)) {
                mDragBarLeft = (mLeft + mViewWidth) - mDragBarRadius;
                mDragBarRight = mRight;
            }
            final float center = mDragBarLeft;
            mCurrentBarProgress = center / mViewWidth;
            invalidate();
            mLastX = x;
            if (null != mListener) {
                mListener.onDragBarChanged(this, mCurrentProgress, mCurrentBarProgress);
            }
            return true;
        }
        return false;
    }

    private boolean handleDownEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (x >= mDragBarLeft - 50 && x <= mDragBarRight + 50) {
            mIsDragging = true;
            mLastX = x;
            calculateBack();
            calculateCurrentProgress();
            calculateProgressBar();
            calculateCurrentBarProgress();
            if (null != mListener) {
                mListener.onStartDrag(this);
            }
            return true;
        }
        return false;
    }

    private boolean handleUpEvent(MotionEvent event) {
        if (mIsDragging) {
            // 需要使用event的x坐标 使用mThumbLeft 是使得点击无反应
            float x = event.getX() - mLeft;
            mCurrentBarProgress = (x / (float) (mViewWidth));
            // 针对 向左向右过度拖动 做保护
            if (mCurrentBarProgress > 1) {
                mCurrentBarProgress = 1;
            }
            if (mCurrentBarProgress < 0) {
                mCurrentBarProgress = 0;
            }
            mCurrentDragOffset = 0;
            int dragCenter = (int) (mLeft + (mViewWidth * mCurrentBarProgress));
            mCurrentProgress = mCurrentBarProgress;
            mDragBarLeft = dragCenter - mDragBarRadius;
            mDragBarRight = dragCenter + mDragBarRadius;
            mLastX = mDragBarLeft;
            invalidate();
            mIsDragging = false;
            calculateBack();
            calculateCurrentProgress();
            calculateProgressBar();
            calculateCurrentBarProgress();
            if (null != mListener) {
                mListener.onDragDone(this);
            }
            return true;
        }
        return false;
    }

    private void drawProgressBack(Canvas canvas) {
        canvas.drawRoundRect(mBackRectF, DEFAULT_RADIUS, DEFAULT_RADIUS, mBackgroundPaint);
    }

    private void drawProgress(Canvas canvas) {
        mProgressRectF.right = mLeft + (mViewWidth * mCurrentProgress);
        canvas.drawRoundRect(mProgressRectF, DEFAULT_RADIUS, DEFAULT_RADIUS, mProgressPaint);
    }

    /**
     * progress bar will not over back
     */
    private void drawProgressBar(Canvas canvas) {
        mProgressBarX = mLeft + mProgressBarRadius + (mProgressInnerWidth * mCurrentProgress);
        canvas.drawCircle(mProgressBarX, mProgressBarY, mProgressBarRadius, mProgressBarPaint);
    }

    private void drawBarProgress(Canvas canvas) {
        if ((mIsShowBarOnNormal || mIsDragging)) {
            mBarReachedRectF.right = mLeft + (mViewWidth * mCurrentBarProgress);
            canvas.drawRoundRect(mBarReachedRectF, DEFAULT_RADIUS, DEFAULT_RADIUS, mBarReachedPaint);
        }
    }

    /**
     * drag bar can over back
     */
    private void drawDragBar(Canvas canvas) {
        if (null != mDragBarDrawable && (mIsShowBarOnNormal || mIsDragging)) {
            mDragBarDrawable.setBounds(mDragBarLeft, mDragBarTop, mDragBarRight, mDragBarBottom);
            mDragBarDrawable.draw(canvas);
        }
    }

    /**
     * set progress
     *
     * @param progress value must between 0.0 ~ 1.0
     */
    public void setProgress(final float progress) {
        if (mProgressCanSetOnDrag || !mIsDragging) {
            post(new Runnable() {
                @Override
                public void run() {
                    float tmpProgress = progress;
                    if (progress < 0) {
                        tmpProgress = 0;
                    } else if (progress > 1) {
                        tmpProgress = 1;
                    }
                    mCurrentProgress = tmpProgress;
                    invalidate();
                }
            });
        }
    }

    /**
     * set progress
     *
     * @param progress value must between 0.0 ~ 1.0
     */
    public void setBarProgress(final float progress) {
        if (!mIsDragging) {
            post(new Runnable() {
                @Override
                public void run() {
                    float tmpProgress = progress;
                    if (progress < 0) {
                        tmpProgress = 0;
                    } else if (progress > 1) {
                        tmpProgress = 1;
                    }
                    mCurrentBarProgress = tmpProgress;
                    int dragCenter = (int) (mLeft + (mViewWidth * tmpProgress));
                    mDragBarLeft = dragCenter - mDragBarRadius;
                    mDragBarRight = dragCenter + mDragBarRadius;
                    invalidate();
                }
            });
        }
    }

    public void setAllProgress(final float progress) {
        post(new Runnable() {
            @Override
            public void run() {
                float tmpProgress = progress;
                if (progress < 0) {
                    tmpProgress = 0;
                } else if (progress > 1) {
                    tmpProgress = 1;
                }
                if (mCurrentBarProgress != tmpProgress || mCurrentProgress != tmpProgress) {
                    if (!mIsDragging) {
                        mCurrentBarProgress = tmpProgress;
                        int dragCenter = (int) (mLeft + (mViewWidth * tmpProgress));
                        mDragBarLeft = dragCenter - mDragBarRadius;
                        mDragBarRight = dragCenter + mDragBarRadius;
                    }
                    if (mProgressCanSetOnDrag || !mIsDragging) {
                        mCurrentProgress = tmpProgress;
                    }
                    invalidate();
                }
            }
        });
    }

    public void setProgressCanSetOnDrag(boolean flag) {
        mProgressCanSetOnDrag = flag;
    }

    public float getBarProgress() {
        return mCurrentBarProgress;
    }

    public float getProgress() {
        return mCurrentProgress;
    }

    public void setListener(VideoSeekListener listener) {
        mListener = listener;
    }

    public interface VideoSeekListener {
        void onDragBarChanged(VideoSeekBar seekBar, float progress, float barProgress);

        void onStartDrag(VideoSeekBar seekBar);

        void onDragDone(VideoSeekBar seekBar);
    }
}
