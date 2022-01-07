package com.tencent.liteav.demo.superplayer.ui.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.ui.view.VipWatchView;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.reflect.Field;

/**
 * 悬浮窗模式播放控件
 * <p>
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式{@link #onTouchEvent(MotionEvent)}
 * <p>
 * 2、关闭悬浮窗{@link #onClick(View)}
 */
public class FloatPlayer extends AbsPlayer implements View.OnClickListener, VipWatchView.VipWatchViewClickListener {

    private TXCloudVideoView mFloatVideoView;   // 悬浮窗中的视频播放view

    private int   mStatusBarHeight;   // 系统状态栏的高度
    private float mXDownInScreen;   // 按下事件距离屏幕左边界的距离
    private float mYDownInScreen;   // 按下事件距离屏幕上边界的距离
    private float mXInScreen;       // 滑动事件距离屏幕左边界的距离
    private float mYInScreen;       // 滑动事件距离屏幕上边界的距离
    private float mXInView;         // 滑动事件距离自身左边界的距离
    private float mYInView;         // 滑动事件距离自身上边界的距离

    private SuperPlayerDef.PlayerType mPlayType;        // 当前播放视频类型
    private LinearLayout              dynamicWatermarkLayout;   //存放动态水印的layout


    public FloatPlayer(Context context) {
        super(context);
        initView(context);
    }

    public FloatPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FloatPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化view
     */
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_float, this);
        mFloatVideoView = (TXCloudVideoView) findViewById(R.id.superplayer_float_cloud_video_view);
        ImageView ivClose = (ImageView) findViewById(R.id.superplayer_iv_close);
        ivClose.setOnClickListener(this);
        mVipWatchView = findViewById(R.id.superplayer_vip_watch_view);
        mVipWatchView.setVipWatchViewClickListener(this);
        dynamicWatermarkLayout = findViewById(R.id.superplayer_dynamic_watermark_layout);
    }

    public void addDynamicWatermarkView(View view) {
        if (dynamicWatermarkLayout != null) {
            dynamicWatermarkLayout.addView(view);
        }
    }

    public void removeDynamicWatermarkView() {
        if (dynamicWatermarkLayout != null) {
            dynamicWatermarkLayout.removeAllViews();
        }
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
                mControllerCallback.onBackPressed(SuperPlayerDef.PlayerMode.FLOAT);
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
                        mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
                    }
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void updatePlayType(SuperPlayerDef.PlayerType type) {
        mPlayType = type;
    }

    @Override
    public void updateVideoProgress(long current, long duration) {
        if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVipWatchView.setCurrentTime(current);
        }
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
     * 更新悬浮窗的位置信息，在回调{@link Callback#onFloatPositionChange(int, int)}中实现悬浮窗移动
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

    @Override
    public void onClickVipTitleBack() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickVipTitleBack(SuperPlayerDef.PlayerMode.FLOAT);
            mControllerCallback.onSeekTo(0);
        }
    }

    @Override
    public void onClickVipRetry() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickVipRetry();
        }
    }

    @Override
    public void onShowVipView() {
        if (mControllerCallback != null) {
            mControllerCallback.onPause();
        }
    }

    @Override
    public void onClickVipBtn() {
        if (mControllerCallback != null) {
            mControllerCallback.onClickHandleVip();
        }
    }

    @Override
    public void onCloseVipTip() {
        if (mControllerCallback != null) {
            mControllerCallback.onCloseVipTip();
        }
    }

}
