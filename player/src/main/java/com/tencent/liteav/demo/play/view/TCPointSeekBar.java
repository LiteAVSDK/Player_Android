package com.tencent.liteav.demo.play.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.R;

import java.util.List;

/**
 * 一个带有打点的，模仿seekbar的view
 */
public class TCPointSeekBar extends RelativeLayout {
    private static final String TAG = "TCPointSeekBar";
    private int mWidth;
    private int mHeight;

    private int mSeekBarLeft; // SeekBar的起点位置
    private int mSeekBarRight;// 终点微视

    private int mBgTop;    // 最下层进度条的Top
    private int mBgBottom; // 进度条的bottom
    private int mRoundSize; // 圆滑的角度大小

    //View的末尾位置
    private int mViewEnd;

    private Paint mNormalPaint;
    private Paint mPointerPaint;
    private Paint mProgressPaint;


    private Drawable mThumbDrawable;
    private int mHalfDrawableWidth;
    private float mThumbLeft;
    private float mThumbRight;
    private float mThumbTop;
    private float mThumbBottom;

    //是否处于拖动状态
    private boolean mIsOnDrag;
    private float mCurrentLeftOffset = 0;

    private float mLastX;

    private int mCurrentProgress;
    private int mMaxProgress = 100;
    private float mBarHeightPx = 0; // bar的高度大小 px


    private TCThumbView mThumbView; // 滑动ThumbView
    private List<PointParams> mPointList; // 打点的列表
    private OnSeekBarPointClickListener mPointClickListener;
    private boolean mIsChangePointViews;

    public TCPointSeekBar(Context context) {
        super(context);
        init(null);
    }

    public TCPointSeekBar(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TCPointSeekBar(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

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

    public void setMax(int max) {
        mMaxProgress = max;
    }

    public int getProgress() {
        return mCurrentProgress;
    }

    public int getMax() {
        return mMaxProgress;
    }

    private void init(AttributeSet attrs) {
        setWillNotDraw(false);
        int progressColor = Color.parseColor("#FF4081");
        int backgroundColor = Color.parseColor("#BBBBBB");
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TCPointSeekBar);
            mThumbDrawable = a.getDrawable(R.styleable.TCPointSeekBar_psb_thumbBackground);
            mHalfDrawableWidth = mThumbDrawable.getIntrinsicWidth() / 2;
            progressColor = a.getColor(R.styleable.TCPointSeekBar_psb_progressColor,
                    Color.parseColor("#FF4081"));
            backgroundColor = a.getColor(R.styleable.TCPointSeekBar_psb_backgroundColor,
                    Color.parseColor("#BBBBBB"));
            mCurrentProgress = a.getInt(R.styleable.TCPointSeekBar_psb_progress, 0);
            mMaxProgress = a.getInt(R.styleable.TCPointSeekBar_psb_max, 100);

            mBarHeightPx = a.getDimension(R.styleable.TCPointSeekBar_psb_progressHeight, 8);
            a.recycle();
        }
        mNormalPaint = new Paint();
        mNormalPaint.setColor(backgroundColor);

        mPointerPaint = new Paint();
        mPointerPaint.setColor(Color.RED);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        this.post(new Runnable() {
            @Override
            public void run() {
                addThumbView();
            }
        });
    }


    private void changeThumbPos() {
        LayoutParams params = (LayoutParams) mThumbView.getLayoutParams();
        params.leftMargin = (int) mThumbLeft;
        params.topMargin = (int) mThumbTop;
        mThumbView.setLayoutParams(params);
    }

    private void addThumbView() {
        mThumbView = new TCThumbView(getContext(), mThumbDrawable);
        LayoutParams thumbParams = new LayoutParams(mThumbDrawable.getIntrinsicHeight(), mThumbDrawable.getIntrinsicHeight());
        mThumbView.setLayoutParams(thumbParams);
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

    }

    private void calProgressDis() {
        float dis = (mSeekBarRight - mSeekBarLeft) * (mCurrentProgress * 1.0f / mMaxProgress);
        mThumbLeft = dis;
        mLastX = mThumbLeft;
        mCurrentLeftOffset = 0;
        calculatePointerRect();
    }


    private void addThumbAndPointViews() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mIsChangePointViews) {
                    TCPointSeekBar.this.removeAllViews();
                    if (mPointList != null) {
                        for (int i = 0; i < mPointList.size(); i++) {
                            PointParams params = mPointList.get(i);
                            addPoint(params, i);
                        }
                    }
                    addThumbView();
                    mIsChangePointViews = false;
                }
                calProgressDis();
                changeThumbPos();
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw  bg
        RectF rectF = new RectF();
        rectF.left = mSeekBarLeft;
        rectF.right = mSeekBarRight;
        rectF.top = mBgTop;
        rectF.bottom = mBgBottom;
        canvas.drawRoundRect(rectF, mRoundSize, mRoundSize, mNormalPaint);

        //draw progress
        RectF pRecf = new RectF();
        pRecf.left = mSeekBarLeft;
        pRecf.top = mBgTop;
        pRecf.right = mThumbRight - mHalfDrawableWidth;
        pRecf.bottom = mBgBottom;
        canvas.drawRoundRect(pRecf,
                mRoundSize, mRoundSize, mProgressPaint);

        addThumbAndPointViews();
    }


    public void addPoint(PointParams pointParams, final int index) {
        float percent = pointParams.progress * 1.0f / mMaxProgress;
        int pointSize = mBgBottom - mBgTop;
        float leftMargin = percent * (mSeekBarRight - mSeekBarLeft);

        float rectLeft = (mThumbDrawable.getIntrinsicWidth() - pointSize) / 2;
        float rectTop = mBgTop;
        float rectBottom = mBgBottom;
        float rectRight = rectLeft + pointSize;

        final TCPointView view = new TCPointView(getContext());
        LayoutParams params = new LayoutParams(mThumbDrawable.getIntrinsicWidth(), mThumbDrawable.getIntrinsicWidth());
        params.leftMargin = (int) leftMargin;
        view.setDrawRect(rectLeft, rectTop, rectBottom, rectRight);
        view.setLayoutParams(params);
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
        return isHandle;
    }

    private boolean handleUpEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mIsOnDrag) {
            mIsOnDrag = false;
            if (mListener != null) {
                mListener.onStopTrackingTouch(this);
            }
            return true;
        }
        return false;
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
            if (mListener != null)
                mListener.onStartTrackingTouch(this);
            mIsOnDrag = true;
            mLastX = x;
            return true;
        }
        return false;
    }

    private void calculatePointerRect() {
        //draw pointer
        float pointerLeft = getPointerLeft(mCurrentLeftOffset);
        float pointerRight = pointerLeft + mThumbDrawable.getIntrinsicWidth();
        mThumbLeft = pointerLeft;
        mThumbRight = pointerRight;
        mThumbTop = 0;
        mThumbBottom = mHeight;
    }


    private float getPointerLeft(float offset) {
        return mThumbLeft + offset;
    }

    private OnSeekBarChangeListener mListener;

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mListener = listener;
    }

    public interface OnSeekBarChangeListener {

        void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(TCPointSeekBar seekBar);

        void onStopTrackingTouch(TCPointSeekBar seekBar);
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

    public static class PointParams {
        int progress = 0;
        int color = Color.RED;

        public PointParams(int progress, int color) {
            this.progress = progress;
            this.color = color;
        }
    }

    private static class TCPointView extends View {
        private int mColor = Color.WHITE;
        private Paint mPaint;
        private RectF mRectF;

        public TCPointView(Context context) {
            super(context);
            init();
        }

        public TCPointView(Context context,  AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public TCPointView(Context context,  AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(mColor);
            mRectF = new RectF();
        }


        public void setColor(int color) {
            mColor = color;
            mPaint.setColor(mColor);
        }


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
