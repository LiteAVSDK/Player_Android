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
 * A view with markers that mimics a seekbar
 * <p>
 * In addition to the basic functions of a seekbar, it also has the ability to add keyframe markers.
 * <p>
 * 1、Add marker information {@link #addPoint(PointParams, int)}.
 * <p>
 * 2、Custom thumb {@link TCThumbView}.
 * <p>
 * 3、Marker view {@link TCPointView}.
 * <p>
 * 4、Marker information parameters {@link PointParams}.
 *
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

    private int                         mWidth;
    private int                         mHeight;
    private int                         mSeekBarLeft;
    private int                         mSeekBarRight;
    private int                         mBgTop;
    private int                         mBgBottom;
    private int                         mRoundSize;
    private int                         mViewEnd;
    private Paint                       mNormalPaint;
    private Paint                       mProgressPaint;
    private Paint                       mPointerPaint;
    private Paint                       mPlayablePaint;
    private Drawable                    mThumbDrawable;
    private int                         mHalfDrawableWidth;
    private float                       mThumbLeft;
    private float                       mThumbRight;
    private float                       mThumbTop;
    private float                       mThumbBottom;
    private boolean                     mIsOnDrag;
    private float                       mCurrentLeftOffset = 0;
    private float                       mLastX;
    private int                         mCurrentProgress;
    private int                         mCurrentPlayableProgress;
    private int                         mMaxProgress       = 100;
    private float                       mBarHeightPx       = 0;
    private TCThumbView                 mThumbView;
    private List<PointParams>           mPointList;
    private OnSeekBarPointClickListener mPointClickListener;
    private boolean                     mIsChangePointViews;
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
     * Set the seekbar progress value
     *
     * 设置seekbar进度值
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
     * Set the seekbar playable progress value
     *
     * 设置seekbar playable进度值
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
     * Set the maximum value of the seekbar
     *
     * 设置seekbar最大值
     */
    public void setMax(int max) {
        mMaxProgress = max;
    }

    /**
     * Get the seekbar progress value
     *
     * 获取seekbar进度值
     */
    public int getProgress() {
        return mCurrentProgress;
    }

    /**
     * Get the maximum value of the seekbar
     *
     * 获取seekbar最大值
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
     * Change the position of the thumb
     *
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

        // Solve the problem that the seekbar marker is ahead of the progress origin before playback
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

        // Avoid the white space between the seekbar origin and the left end when starting playback
        float tempRight = mPlayableRight < mSeekBarLeft ? mSeekBarLeft : mPlayableRight;

        // Avoid white space at the right end when switching from VOD to live
        mTRectF.right = (tempRight > mTRectF.left) ? tempRight : mTRectF.left;
        canvas.drawRoundRect(mTRectF, mRoundSize, mRoundSize, mPlayablePaint);

        addThumbAndPointViews();
    }

    /**
     * Add the marker view
     *
     * 添加打点view
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
        // Need to use the x coordinate of the event, use mThumbLeft to make it unresponsive to clicks
        float x = event.getX();
        if (mListener != null) {
            mCurrentProgress = (int) ((x / (float) (mWidth)) * mMaxProgress);
            // Protect against excessive dragging to the left or right
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
     * Update progress
     *
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
     * Update the duration of the windowPlayer and FullscreenPlayer
     *
     * 更新windowPlayer FullscreenPlayer的 duration
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
     * Calculate the coordinates of the dragging block
     *
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

    public void setOnPointClickListener(OnSeekBarPointClickListener listener) {
        mPointClickListener = listener;
    }

    public interface OnSeekBarPointClickListener {
        void onSeekBarPointClick(View view, int pos);
    }

    public void setPointList(List<PointParams> pointList) {
        mPointList = pointList;
        mIsChangePointViews = true;
        invalidate();
    }

    /**
     * Marker information
     *
     * 打点信息
     */
    public static class PointParams {
        int progress = 0;
        int color = Color.RED;

        public PointParams(int progress, int color) {
            this.progress = progress;
            this.color = color;
        }
    }

    /**
     * Marker view
     *
     * 打点view
     */
    private static class TCPointView extends View {
        private int mColor = Color.WHITE;
        private Paint mPaint;
        private RectF mRectF;

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
         * Set the color of the marker.
         *
         * 设置打点颜色
         */
        public void setColor(int color) {
            mColor = color;
            mPaint.setColor(mColor);
        }

        /**
         * Set the position information of the marker view
         *
         * 设置打点view的位置信息
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
     * Dragging block view.
     *
     * 拖动块view
     */
    private static class TCThumbView extends View {
        private Paint mPaint;
        private Rect mRect;
        private Drawable mThumbDrawable;

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
