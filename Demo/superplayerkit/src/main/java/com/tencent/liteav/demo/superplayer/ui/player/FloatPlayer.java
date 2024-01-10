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
 * Floating window mode playback control
 * <p>
 * Slide to move the floating window, click the floating window to return
 * to window mode {@link #onTouchEvent(MotionEvent)}
 * <p>
 * Close the floating window {@link #onClick(View)}
 *
 *
 * 悬浮窗模式播放控件
 * <p>
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式{@link #onTouchEvent(MotionEvent)}
 * <p>
 * 2、关闭悬浮窗{@link #onClick(View)}
 */
public class FloatPlayer extends AbsPlayer implements View.OnClickListener, VipWatchView.VipWatchViewClickListener {

    private TXCloudVideoView mFloatVideoView;

    private int   mStatusBarHeight;   // System status bar height.
    private float mXDownInScreen;   // Distance from the press event to the left edge of the screen.
    private float mYDownInScreen;   // Distance from the press event to the top edge of the screen.
    private float mXInScreen;       // Distance from the sliding event to the left edge of the screen.
    private float mYInScreen;       // Distance from the sliding event to the top edge of the screen.
    private float mXInView;         // Distance from the sliding event to the left edge of itself.
    private float mYInView;         // Distance from the sliding event to the top edge of itself.

    private SuperPlayerDef.PlayerType mPlayType;
    private LinearLayout              dynamicWatermarkLayout;

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
     * Get the video playback view in the floating window.
     *
     * 获取悬浮窗中的视频播放view
     */
    public TXCloudVideoView getFloatVideoView() {
        return mFloatVideoView;
    }

    /**
     * Set click event listener to close the floating window after clicking the close button.
     *
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
     * Override touch event listener to move the floating window with finger.
     *
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
            case MotionEvent.ACTION_MOVE:
                mXInScreen = event.getRawX();
                mYInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();

                break;
            case MotionEvent.ACTION_UP:
                // If the finger does not slide, it is regarded as a click and returns to window mode.
                if (mXDownInScreen == mXInScreen && mYDownInScreen == mYInScreen) {
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
    public void setVideoQualityVisible(boolean isShow) {
    }

    @Override
    public void updateVideoProgress(long current, long duration, long playable) {
        if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVipWatchView.setCurrentTime(current);
        }
    }

    @Override
    public void updateVipInfo(int position) {
        super.updateVipInfo(position);
        if (mPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVipWatchView.setCurrentTime(position);
        }
    }

    /**
     * Get the height of the system status bar.
     *
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
     * Update the position information of the floating window, and implement the floating window movement in
     * the callback {@link Callback#onFloatPositionChange(int, int)}.
     *
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
