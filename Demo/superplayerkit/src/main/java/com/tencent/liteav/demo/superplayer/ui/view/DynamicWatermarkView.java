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
import android.view.View;

import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.utils.RandomUtils;


/**
 * 用于展示动态水印view
 * 使用setData 设置水印文字以及文字大小
 * 使用show 展示水印
 * 使用hide 隐藏水印
 * 使用release 释放水印的线程资源
 */
public class DynamicWatermarkView extends View {

    private       int                watermarkViewHeight = 0;      //此view的高度
    private       int                watermarkViewWidth  = 0;      //此view的宽度
    private       Paint              textPaint           = null;   //绘制文字的画笔
    private       Paint              bgPaint             = null;   //绘制背景矩形的画笔
    private       RectF              bgRectF             = null;   //背景矩形
    private       boolean            isDrawing           = false;
    private final HandlerThread      handlerThread       = new HandlerThread("DynamicWatermarkView");
    private       Handler            drawHandler         = null;
    private       double             deltaX              = 0;   //X轴上的速度
    private       double             deltaY              = 0;   //Y轴上的速度
    private       int                bgRectWidthHalf     = 0;   //背景矩形宽的二分之一
    private       int                bgRectHeightHalf    = 0;   //背景矩形高的二分之一
    private       DynamicWaterConfig dynamicWaterConfig  = null;
    private final int                watermarkSpeed      = RandomUtils.getRandomNumber(3, 5);//速度
    private       int                watermarkCenterY    = 0;         //水印中心点在此view中的位置
    private       int                watermarkCenterX    = 0;
    private       int                watermarkDegree     = 0;

    private static final int DEGREE_360    = 360;
    private static final int DEGREE_270    = 270;
    private static final int DEGREE_180    = 180;
    private static final int DEGREE_90     = 90;
    private static final int DEGREE_0      = 0;
    private static final int TIME_STEP     = 40;    //水印的绘制时间间隔
    private static final int MSG_TYPE_DRAW = 0;
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

    /**
     * 初始化对应的画笔以及矩形区域
     */
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
            //当view的尺寸发生变化时，将水印的位置重置为左上角
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
                if (getVisibility() == VISIBLE && isDrawing) {
                    processData();
                    postInvalidate();
                    drawHandler.sendEmptyMessageDelayed(MSG_TYPE_DRAW, TIME_STEP);
                }
            }
            return true;
        }
    }

    /**
     * 设置数据
     *
     * @param dynamicWaterConfig 动态水印信息
     */
    public void setData(DynamicWaterConfig dynamicWaterConfig) {
        if (dynamicWaterConfig != null && !TextUtils.isEmpty(dynamicWaterConfig.dynamicWatermarkTip)) {
            if (this.dynamicWaterConfig == null) {
                watermarkDegree = RandomUtils.getRandomNumber(25, 65);
                calculateSpeedXY(watermarkDegree);
            }
            this.dynamicWaterConfig = dynamicWaterConfig;
            textPaint.setColor(this.dynamicWaterConfig.tipTextColor);
            calculateBgRectWH();
        } else {
            this.dynamicWaterConfig = null;
            hide();
        }
    }

    /**
     * 根据文本信息计算出背景矩形的宽高
     */
    private void calculateBgRectWH() {
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dynamicWaterConfig.tipTextSize);
        int bgRectPadding = sp2px(getContext(), 4);       //文本的左右内边距之和 或 上下内边距之和
        int bgRectHeight = dynamicWaterConfig.tipTextSize + bgRectPadding;
        String tipStr = dynamicWaterConfig.dynamicWatermarkTip;      //由于规则显示，所以使用两行，一行会超出120个字符
        int bgRectWidth = (int) Layout.getDesiredWidth(tipStr, textPaint) + bgRectPadding;
        watermarkCenterX = bgRectWidth / 2;
        watermarkCenterY = bgRectHeight / 2;
        bgRectHeightHalf = bgRectHeight / 2;
        bgRectWidthHalf = bgRectWidth / 2;
    }

    /**
     * 调用此方法用于展示页面
     */
    public void show() {
        if (dynamicWaterConfig != null) {
            if (!isDrawing) {
                setVisibility(VISIBLE);
                isDrawing = true;
                drawHandler.sendEmptyMessage(MSG_TYPE_DRAW);
            }
        } else {
            hide();
        }
    }


    /**
     * 隐藏此view
     */
    public void hide() {
        isDrawing = false;
        setVisibility(INVISIBLE);
        if (drawHandler != null) {
            drawHandler.removeMessages(MSG_TYPE_DRAW);
        }
    }

    /**
     * 释放线程资源
     */
    public void release() {
        hide();
        handlerThread.getLooper().quit();
    }


    /**
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
     * 碰撞检测
     */
    private void checkBorderCollision() {
        if (watermarkCenterY <= bgRectHeightHalf) {  //表示碰撞到了上边距
            onCollisionBorderResultDegree(BORDER_TOP);
            watermarkCenterY = bgRectHeightHalf;
        } else if (watermarkCenterY >= watermarkViewHeight - bgRectHeightHalf) {  //表示碰撞到了下边距
            onCollisionBorderResultDegree(BORDER_BOTTOM);
            watermarkCenterY = watermarkViewHeight - bgRectHeightHalf;
        }
        if (watermarkCenterX <= bgRectWidthHalf) {    //表示碰撞到了左边距
            onCollisionBorderResultDegree(BORDER_LEFT);
            watermarkCenterX = bgRectWidthHalf;
        } else if (watermarkCenterX >= watermarkViewWidth - bgRectWidthHalf) {   //表示碰撞到了右边距
            onCollisionBorderResultDegree(BORDER_RIGHT);
            watermarkCenterX = watermarkViewWidth - bgRectWidthHalf;
        }
    }


    /**
     * @param border left,top,right,bottom
     * @return
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
     * 计算x 方向和Y方向的速度
     */
    private void calculateSpeedXY(int degree) {
        double tempPI = Math.toRadians(degree);  //然后将角度转化为弧度
        deltaX = Math.sin(tempPI) * watermarkSpeed;
        deltaY = Math.cos(tempPI) * watermarkSpeed;
    }

    /**
     * sp单位转px
     *
     * @param context
     * @param spValue
     * @return
     */
    private int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }
}
