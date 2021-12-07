package com.tencent.liteav.demo.superplayer.ui.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.ui.view.VipWatchView;

import java.util.List;

/**
 * 播放器公共逻辑
 */
public abstract class AbsPlayer extends RelativeLayout implements Player {

    protected static final int      MAX_SHIFT_TIME = 7200; // demo演示直播时移是MAX_SHIFT_TIMEs，即2小时
    protected Callback     mControllerCallback; // 播放控制回调
    protected VipWatchView mVipWatchView;                           //用于展示VIP 试看的view控件


    protected Runnable mHideViewRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public AbsPlayer(Context context) {
        super(context);
    }

    public AbsPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCallback(Callback callback) {
        mControllerCallback = callback;
    }

    @Override
    public void setWatermark(Bitmap bmp, float x, float y) {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void release() {

    }

    @Override
    public void updatePlayState(SuperPlayerDef.PlayerState playState) {

    }

    @Override
    public void setVideoQualityList(List<VideoQuality> list) {

    }

    @Override
    public void updateTitle(String title) {

    }

    @Override
    public void updateVideoProgress(long current, long duration) {

    }

    @Override
    public void updatePlayType(SuperPlayerDef.PlayerType type) {

    }

    @Override
    public void setBackground(Bitmap bitmap) {

    }

    @Override
    public void showBackground() {

    }

    @Override
    public void hideBackground() {

    }

    @Override
    public void updateVideoQuality(VideoQuality videoQuality) {

    }

    @Override
    public void updateImageSpriteInfo(PlayImageSpriteInfo info) {

    }

    @Override
    public void updateKeyFrameDescInfo(List<PlayKeyFrameDescInfo> list) {

    }


    public void setVipWatchModel(VipWatchModel vipWatchModel) {
        if (mVipWatchView != null) {
            mVipWatchView.setVipWatchMode(vipWatchModel);
        }
    }

    public void hideVipView() {
        if (mVipWatchView != null) {
            mVipWatchView.hideVipView();
        }
    }

    public void hideTipView() {
        if (mVipWatchView != null) {
            mVipWatchView.hideTipView();
        }
    }

    public boolean isShowingVipView() {
        if (mVipWatchView != null) {
            return mVipWatchView.isShowing();
        } else {
            return false;
        }
    }

    /**
     * 设置控件的可见性
     *
     * @param view      目标控件
     * @param isVisible 显示：true 隐藏：false
     */
    protected void toggleView(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 将秒数转换为hh:mm:ss的格式
     *
     * @param second
     * @return
     */
    protected String formattedTime(long second) {
        String formatTime;
        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h == 0) {
            formatTime = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            formatTime = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }
        return formatTime;
    }

    protected String asTwoDigit(long digit) {
        String value = "";
        if (digit < 10) {
            value = "0";
        }
        value += String.valueOf(digit);
        return value;
    }

}
