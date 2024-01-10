package com.tencent.liteav.demo.superplayer.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.utils.RandomUtils;


/**
 * Used to display dynamic watermark view
 * Use setData to set the watermark text and text size
 * Use show to display the watermark
 * Use hide to hide the watermark
 * Use release to release the thread resources of the watermark
 *
 * 用于展示动态水印view
 * 使用setData 设置水印文字以及文字大小
 * 使用show 展示水印
 * 使用hide 隐藏水印
 * 使用release 释放水印的线程资源
 */
public class DynamicWatermarkView extends View {

    private       int                watermarkViewHeight = 0;
    private       int                watermarkViewWidth  = 0;
    private       Paint              textPaint           = null;
    private       Paint              bgPaint             = null;
    private       RectF              bgRectF             = null;
    private       boolean            isDrawing           = false;
    private final HandlerThread      handlerThread       = new HandlerThread("DynamicWatermarkView");
    private       Handler            drawHandler         = null;
    private       double             deltaX              = 0;
    private       double             deltaY              = 0;
    private       int                bgRectWidthHalf     = 0;
    private       int                bgRectHeightHalf    = 0;
    private       DynamicWaterConfig dynamicWaterConfig  = null;
    private final int                watermarkSpeed      = RandomUtils.getRandomNumber(3, 5);
    private       int                watermarkCenterY    = 0;
    private       int                watermarkCenterX    = 0;
    private       int                watermarkDegree     = 0;

    private static final int DEGREE_360    = 360;
    private static final int DEGREE_270    = 270;
    private static final int DEGREE_180    = 180;
    private static final int DEGREE_90     = 90;
    private static final int DEGREE_0      = 0;
        private static final int TIME_STEP     = 40;    // The drawing interval of the watermark
    private static final int MSG_TYPE_DRAW = 0;
    private static final int MSG_TYPE_PERIOD_SHOW = 1;
    private static final int MSG_TYPE_PERIOD_HIDE = 2;
    private int screenWidth;
    private int screenHeight;
    private long showPeriodTime = 0;     //in millisecond
    private long hidePeriodTime = 0;     //in millisecond

    private static final int BORDER_LEFT   = 0;
    private static final int BORDER_TOP    = 1;
    private static final int BORDER_RIGHT  = 2;
    private static final int BORDER_BOTTOM = 3;


    public DynamicWatermarkView(Context context) {
        super(context);
        this.init();
    }

    public DynamicWatermarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();

    }

    public DynamicWatermarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        bgRectF = new RectF();
        textPaint = new Paint();

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setColor(Color.TRANSPARENT);

        bgPaint.setStyle(Paint.Style.FILL);
        handlerThread.start();
        drawHandler = new Handler(handlerThread.getLooper(), new HandlerThreadCallback());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        watermarkViewWidth = getMeasuredWidth();
        watermarkViewHeight = getMeasuredHeight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            this.watermarkViewWidth = w;
            this.watermarkViewHeight = h;
            // When the size of the view changes, reset the position of the watermark to the upper left corner
            if (dynamicWaterConfig != null) {
                watermarkCenterX = bgRectWidthHalf;
                watermarkCenterY = bgRectHeightHalf;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getVisibility() == VISIBLE && dynamicWaterConfig != null) {
            canvas.drawRect(bgRectF, bgPaint);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
            float baseline = bgRectF.centerY() + distance;
            textPaint.setTextSize(dynamicWaterConfig.tipTextSize);
            canvas.drawText(dynamicWaterConfig.dynamicWatermarkTip, bgRectF.centerX(), baseline, textPaint);
        }
    }


    private class HandlerThreadCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_TYPE_DRAW) {
                processData();
                drawHandler.sendEmptyMessageDelayed(MSG_TYPE_DRAW, TIME_STEP);
                if (getVisibility() == VISIBLE && isDrawing) {
                    postInvalidate();
                }
            } else if (msg.what == MSG_TYPE_PERIOD_HIDE) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(INVISIBLE);
                    }
                });
                drawHandler.removeMessages(MSG_TYPE_PERIOD_HIDE);
                drawHandler.removeMessages(MSG_TYPE_PERIOD_SHOW);
                drawHandler.sendEmptyMessageDelayed(MSG_TYPE_PERIOD_SHOW, hidePeriodTime);
            }  else if (msg.what == MSG_TYPE_PERIOD_SHOW) {
                watermarkCenterY = RandomUtils.getRandomNumber(50, screenHeight);
                watermarkCenterX = RandomUtils.getRandomNumber(50, screenWidth);
                post(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(VISIBLE);
                    }
                });
                drawHandler.removeMessages(MSG_TYPE_PERIOD_HIDE);
                drawHandler.removeMessages(MSG_TYPE_PERIOD_SHOW);
                drawHandler.sendEmptyMessageDelayed(MSG_TYPE_PERIOD_HIDE, showPeriodTime);
            }
            return true;
        }
    }

    public void setData(DynamicWaterConfig dynamicWaterConfig) {
        if (dynamicWaterConfig != null && !TextUtils.isEmpty(dynamicWaterConfig.dynamicWatermarkTip)) {
            if (this.dynamicWaterConfig == null) {
                watermarkDegree = RandomUtils.getRandomNumber(25, 65);
                calculateSpeedXY(watermarkDegree);
            }
            this.dynamicWaterConfig = dynamicWaterConfig;
            textPaint.setColor(this.dynamicWaterConfig.tipTextColor);
            initGhostPeriodTime();
            calculateBgRectWH();
        } else {
            this.dynamicWaterConfig = null;
            hide();
        }
    }

    private void initGhostPeriodTime() {
        if (dynamicWaterConfig.getShowType() == DynamicWaterConfig.GHOST_RUNNING) {
            if (dynamicWaterConfig.durationInSecond == 0) {
                showPeriodTime = 5000;
                hidePeriodTime = 5000;
            } else {
                long cycle;
                if (dynamicWaterConfig.durationInSecond <= 1 * 60) {
                    cycle = dynamicWaterConfig.durationInSecond / 2;
                } else if (dynamicWaterConfig.durationInSecond <= 30 * 60) {
                    cycle = dynamicWaterConfig.durationInSecond / 4;
                } else  {
                    cycle = 30 * 60 / 4;
                }
                showPeriodTime = 1000 * cycle / 4;
                hidePeriodTime = 1000 * cycle * 3 / 4;;
            }
        }
    }

    /**
     * Calculate the width and height of the background rectangle based on the text information
     *
     * 根据文本信息计算出背景矩形的宽高
     */
    private void calculateBgRectWH() {
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dynamicWaterConfig.tipTextSize);
        // The sum of the left and right padding of the text or the sum of the top and bottom padding
        int bgRectPadding = sp2px(getContext(), 4);
        int bgRectHeight = dynamicWaterConfig.tipTextSize + bgRectPadding;
        // Because of the rule display, two lines are used, one line will exceed 120 characters
        String tipStr = dynamicWaterConfig.dynamicWatermarkTip;
        int bgRectWidth = (int) Layout.getDesiredWidth(tipStr, textPaint) + bgRectPadding;
        watermarkCenterX = bgRectWidth / 2;
        watermarkCenterY = bgRectHeight / 2;
        bgRectHeightHalf = bgRectHeight / 2;
        bgRectWidthHalf = bgRectWidth / 2;
    }

    public void show() {
        if (dynamicWaterConfig != null) {
            if (!isDrawing) {
                setVisibility(VISIBLE);
                isDrawing = true;
                drawHandler.sendEmptyMessage(MSG_TYPE_DRAW);
            }
            if (dynamicWaterConfig.getShowType() == DynamicWaterConfig.GHOST_RUNNING) {
                drawHandler.removeMessages(MSG_TYPE_PERIOD_HIDE);
                drawHandler.removeMessages(MSG_TYPE_PERIOD_SHOW);
                drawHandler.sendEmptyMessageDelayed(MSG_TYPE_PERIOD_HIDE, showPeriodTime);
            }
        } else {
            hide();
        }
    }

    public void hide() {
        isDrawing = false;
        setVisibility(INVISIBLE);
        if (drawHandler != null) {
            drawHandler.removeMessages(MSG_TYPE_DRAW);
        }
    }

    public void release() {
        hide();
        handlerThread.quit();
    }


    /**
     * Process watermark position data
     *
     * 处理水印的位置数据
     */
    private void processData() {
        if (dynamicWaterConfig == null) {
            return;
        }
        watermarkCenterY += deltaY;
        watermarkCenterX += deltaX;
        checkBorderCollision();
        bgRectF.left = watermarkCenterX - bgRectWidthHalf;
        bgRectF.top = watermarkCenterY - bgRectHeightHalf;
        bgRectF.right = watermarkCenterX + bgRectWidthHalf;
        bgRectF.bottom = watermarkCenterY + bgRectHeightHalf;
    }

    /**
     * Collision detection
     *
     * 碰撞检测
     */
    private void checkBorderCollision() {
        if (watermarkCenterY <= bgRectHeightHalf) {  // Indicates collision with the top margin
            onCollisionBorderResultDegree(BORDER_TOP);
            watermarkCenterY = bgRectHeightHalf;
        } else if (watermarkCenterY >= watermarkViewHeight - bgRectHeightHalf) {
            // Indicates collision with the bottom margin
            onCollisionBorderResultDegree(BORDER_BOTTOM);
            watermarkCenterY = watermarkViewHeight - bgRectHeightHalf;
        }
        if (watermarkCenterX <= bgRectWidthHalf) {    // Indicates collision with the left margin
            onCollisionBorderResultDegree(BORDER_LEFT);
            watermarkCenterX = bgRectWidthHalf;
        } else if (watermarkCenterX >= watermarkViewWidth - bgRectWidthHalf) {
            // Indicates collision with the right margin
            onCollisionBorderResultDegree(BORDER_RIGHT);
            watermarkCenterX = watermarkViewWidth - bgRectWidthHalf;
        }
    }


    /**
     * @param border left,top,right,bottom
     */
    private void onCollisionBorderResultDegree(int border) {
        if (watermarkDegree > DEGREE_0 && watermarkDegree <= DEGREE_90) {
            if (border == BORDER_BOTTOM) {
                watermarkDegree = DEGREE_180 - watermarkDegree;
            }
            if (border == BORDER_RIGHT) {
                watermarkDegree = DEGREE_360 - watermarkDegree;
            }
        } else if (watermarkDegree > DEGREE_90 && watermarkDegree <= DEGREE_180) {
            if (border == BORDER_RIGHT) {
                watermarkDegree = DEGREE_360 - watermarkDegree;
            }
            if (border == BORDER_TOP) {
                watermarkDegree = DEGREE_180 - watermarkDegree;
            }
        } else if (watermarkDegree > DEGREE_180 && watermarkDegree <= DEGREE_270) {
            if (border == BORDER_LEFT) {
                watermarkDegree = DEGREE_360 - watermarkDegree;
            }
            if (border == BORDER_TOP) {
                watermarkDegree = DEGREE_270 + DEGREE_270 - watermarkDegree;
            }
        } else if (watermarkDegree > DEGREE_270 && watermarkDegree <= DEGREE_360) {
            if (border == BORDER_BOTTOM) {
                watermarkDegree = DEGREE_360 - watermarkDegree + DEGREE_180;
            }
            if (border == BORDER_LEFT) {
                watermarkDegree = DEGREE_360 - watermarkDegree;
            }
        }
        calculateSpeedXY(watermarkDegree);
    }

    /**
     * Calculate the speed in the X direction and Y direction
     *
     * 计算x 方向和Y方向的速度
     */
    private void calculateSpeedXY(int degree) {
        double tempPI = Math.toRadians(degree);
        deltaX = Math.sin(tempPI) * watermarkSpeed;
        deltaY = Math.cos(tempPI) * watermarkSpeed;
    }

    /**
     * Convert sp unit to px
     *
     * sp单位转px
     */
    private int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }
}
