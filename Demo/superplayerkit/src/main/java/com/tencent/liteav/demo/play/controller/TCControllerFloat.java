package com.tencent.liteav.demo.play.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.bean.TCVideoQuality;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 悬浮窗模式播放控件
 *
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式{@link #onTouchEvent(MotionEvent)}
 *
 * 2、关闭悬浮窗{@link #onClick(View)}
 */
public class TCControllerFloat extends RelativeLayout implements IController, View.OnClickListener {

    private TXCloudVideoView            mFloatVideoView;        // 悬浮窗中的视频播放view
    private IControllerCallback         mControllerCallback;    // 播放控件回调接口

    private int                         mStatusBarHeight;       // 系统状态栏的高度
    private float                       mXDownInScreen;         // 按下事件距离屏幕左边界的距离
    private float                       mYDownInScreen;         // 按下事件距离屏幕上边界的距离
    private float                       mXInScreen;             // 滑动事件距离屏幕左边界的距离
    private float                       mYInScreen;             // 滑动事件距离屏幕上边界的距离
    private float                       mXInView;               // 滑动事件距离自身左边界的距离
    private float                       mYInView;               // 滑动事件距离自身上边界的距离

    public TCControllerFloat(Context context) {
        super(context);
        initView(context);
    }

    public TCControllerFloat(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TCControllerFloat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     *初始化view
     */
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_controller_float, this);
        mFloatVideoView = (TXCloudVideoView) findViewById(R.id.superplayer_float_cloud_video_view);
        ImageView ivClose = (ImageView) findViewById(R.id.superplayer_iv_close);
        ivClose.setOnClickListener(this);
    }

    /**
     * 获取悬浮窗中的视频播放view
     */
    public TXCloudVideoView getFloatVideoView() {
        return mFloatVideoView;
    }

    /**
     * 设置点击事件监听，实现点击关闭按钮后关闭悬浮窗
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.superplayer_iv_close) {
            if (mControllerCallback != null) {
                mControllerCallback.onBackPressed(SuperPlayerConst.PLAYMODE_FLOAT);
            }
        }
    }

    /**
     * 重写触摸事件监听，实现悬浮窗随手指移动
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXInView = event.getX();
                mYInView = event.getY();
                mXDownInScreen = event.getRawX();
                mYDownInScreen = event.getRawY() - getStatusBarHeight();
                mXInScreen = event.getRawX();
                mYInScreen = event.getRawY() - getStatusBarHeight();

                break;
            case MotionEvent.ACTION_MOVE: //悬浮窗随手指移动
                mXInScreen = event.getRawX();
                mYInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();

                break;
            case MotionEvent.ACTION_UP:
                if (mXDownInScreen == mXInScreen && mYDownInScreen == mYInScreen) {//手指没有滑动视为点击，回到窗口模式
                    if (mControllerCallback != null) {
                        mControllerCallback.onSwitchPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
                    }
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * 获取系统状态栏高度
     */
    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                mStatusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }

    /**
     * 更新悬浮窗的位置信息，在回调{@link IControllerCallback#onFloatPositionChange(int, int)}中实现悬浮窗移动
     */
    private void updateViewPosition() {
        int x = (int) (mXInScreen - mXInView);
        int y = (int) (mYInScreen - mYInView);
        SuperPlayerGlobalConfig.TXRect rect = SuperPlayerGlobalConfig.getInstance().floatViewRect;
        if (rect != null) {
            rect.x = x;
            rect.y = y;
        }
        if (mControllerCallback != null) {
            mControllerCallback.onFloatPositionChange(x, y);
        }
    }

    /**
     * 设置回调
     *
     * @param callback 回调接口实现对象
     */
    @Override
    public void setCallback(IControllerCallback callback) {
        mControllerCallback = callback;
    }

    /**
     * 设置水印
     *
     * @param bmp 水印图
     * @param x   水印的x坐标
     * @param y   水印的y坐标
     */
    @Override
    public void setWatermark(Bitmap bmp, float x, float y) {
    }

    /**
     * 显示控件
     */
    @Override
    public void show() {

    }

    /**
     * 隐藏控件
     */
    @Override
    public void hide() {

    }

    /**
     * 释放控件的内存
     */
    @Override
    public void release() {

    }

    /**
     * 更新播放状态
     *
     * @param playState 正在播放{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PLAYING}
     *                  正在加载{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_LOADING}
     *                  暂停   {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PAUSE}
     *                  播放结束{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_END}
     */
    @Override
    public void updatePlayState(int playState) {

    }

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    @Override
    public void setVideoQualityList(List<TCVideoQuality> list) {

    }

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    @Override
    public void updateTitle(String title) {

    }

    /**
     * 更新是屁播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    @Override
    public void updateVideoProgress(long current, long duration) {

    }

    /**
     * 更新播放类型
     *
     * @param type 点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_VOD}
     *             点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE}
     *             直播回看  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE_SHIFT}
     */
    @Override
    public void updatePlayType(int type) {

    }

    /**
     * 设置背景
     *
     * @param bitmap 背景图
     */
    @Override
    public void setBackground(Bitmap bitmap) {

    }

    /**
     * 显示背景
     */
    @Override
    public void showBackground() {

    }

    /**
     * 隐藏背景
     */
    @Override
    public void hideBackground() {

    }

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
    @Override
    public void updateVideoQuality(TCVideoQuality videoQuality) {

    }

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    @Override
    public void updateImageSpriteInfo(TCPlayImageSpriteInfo info) {

    }

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    @Override
    public void updateKeyFrameDescInfo(List<TCPlayKeyFrameDescInfo> list) {

    }
}
