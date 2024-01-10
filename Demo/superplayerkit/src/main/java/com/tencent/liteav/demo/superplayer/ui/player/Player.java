package com.tencent.liteav.demo.superplayer.ui.player;


import android.graphics.Bitmap;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.TXTrackInfo;

import java.util.List;
import java.util.Map;

/**
 * Playback control interface
 *
 * 播放控制接口
 */
public interface Player {

    /**
     * Set callback.
     *
     * 设置回调
     */
    void setCallback(Callback callback);

    /**
     * Set watermark
     *
     * 设置水印
     *
     * @param bmp Watermark image
     *            水印图
     * @param x   X coordinate of watermark
     *            水印的x坐标
     * @param y   Y coordinate of watermark
     *            水印的y坐标
     */
    void setWatermark(Bitmap bmp, float x, float y);

    /**
     * Show control
     *
     * 显示控件
     */
    void show();

    /**
     * Hide control
     *
     * 隐藏控件
     */
    void hide();

    /**
     * Release control memory
     *
     * 释放控件的内存
     */
    void release();

    /**
     * Update playback status
     *
     * 更新播放状态
     *
     * @param playState Playing {@link SuperPlayerDef.PlayerState#PLAYING}
     *                  Loading {@link SuperPlayerDef.PlayerState#LOADING}
     *                  Paused   {@link SuperPlayerDef.PlayerState#PAUSE}
     *                  Playback ended {@link SuperPlayerDef.PlayerState#END}
     */
    void updatePlayState(SuperPlayerDef.PlayerState playState);

    /**
     * Set video quality information
     *
     * 设置视频画质信息
     */
    void setVideoQualityList(List<VideoQuality> list);

    /**
     * Update video name
     *
     * 更新视频名称
     */
    void updateTitle(String title);

    /**
     * Update video playback progress
     *
     * 更新视频播放进度
     */
    void updateVideoProgress(long current, long duration, long playable);

    /**
     * Update playback type
     *
     * 更新播放类型
     *
     * @param type VOD     {@link SuperPlayerDef.PlayerType#VOD}
     *             Live     {@link SuperPlayerDef.PlayerType#LIVE}
     *             Live playback  {@link SuperPlayerDef.PlayerType#LIVE_SHIFT}
     */
    void updatePlayType(SuperPlayerDef.PlayerType type);

    /**
     * Set background
     *
     * 设置背景
     */
    void setBackground(final Bitmap bitmap);

    /**
     * Show background
     *
     * 显示背景
     */
    void showBackground();

    /**
     * Hide background
     *
     * 隐藏背景
     */
    void hideBackground();

    /**
     * Update video playback quality
     *
     * 更新视频播放画质
     */
    void updateVideoQuality(VideoQuality videoQuality);

    /**
     * Update sprite information
     *
     * 更新雪碧图信息
     */
    void updateImageSpriteInfo(PlayImageSpriteInfo info);

    /**
     * Update keyframe information
     *
     * 更新关键帧信息
     */
    void updateKeyFrameDescInfo(List<PlayKeyFrameDescInfo> list);

    /**
     * Update VIPTrial Information
     * 更新VIP试看信息
     */
    void updateVipInfo(int position);

    /**
     * Whether to display video quality, default display
     *
     * 是否显示清晰度，默认显示
     */
    void setVideoQualityVisible(boolean isShow);

    /**
     * Playback control callback interface
     *
     * 播放控制回调接口
     */
    interface Callback {

        /**
         * Switch playback mode callback.
         *
         * 切换播放模式回调
         *
         * @param playMode Playback mode after switching
         *                 切换后的播放模式：
         *                 Window mode      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                 Full-screen mode      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                 Floating window mode    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onSwitchPlayMode(SuperPlayerDef.PlayerMode playMode);

        /**
         * backPress event callback
         *
         * 返回点击事件回调
         *
         * @param playMode Playback mode after switching
         *                 切换后的播放模式：
         *                 Window mode      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                 Full-screen mode      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                 Floating window mode    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onBackPressed(SuperPlayerDef.PlayerMode playMode);

        /**
         * Floating window position update callback
         *
         * 悬浮窗位置更新回调
         *
         * @param x Floating window x coordinate
         *          悬浮窗x坐标
         * @param y Floating window y coordinate
         *          悬浮窗y坐标
         */
        void onFloatPositionChange(int x, int y);

        /**
         * Playback pause callback
         *
         * 播放暂停回调
         */
        void onPause();

        /**
         * Playback resume callback
         *
         * 播放继续回调
         */
        void onResume();

        /**
         * Playback jump callback
         *
         * 播放跳转回调
         */
        void onSeekTo(int position);

        /**
         * Restore live playback callback
         *
         * 恢复直播回调
         */
        void onResumeLive();

        /**
         * Bullet screen switch callback
         *
         * 弹幕开关回调
         */
        void onDanmuToggle(boolean isOpen);

        /**
         * Screenshot callback
         *
         * 屏幕截图回调
         */
        void onSnapshot();

        /**
         * Update quality callback
         *
         * 更新画质回调
         */
        void onQualityChange(VideoQuality quality);

        /**
         * Update playback speed callback
         *
         * 更新播放速度回调
         */
        void onSpeedChange(float speedLevel);

        /**
         * Mirror switch callback
         *
         * 镜像开关回调
         */
        void onMirrorToggle(boolean isMirror);

        /**
         * Hardware acceleration switch callback
         *
         * 硬件加速开关回调
         */
        void onHWAccelerationToggle(boolean isAccelerate);

        /**
         * Callback event when the user clicks the "Become VIP Member" button
         *
         * 当用户点击了 开通VIP会员按钮的回调事件
         */
        void onClickHandleVip();

        /**
         * Callback event when the "Return" button on the VIP preview page is clicked
         *
         * 当点击了VIP试看界面的返回按钮的的回调
         * @param playMode Current playback mode
         *                 当前播放模式：
         *                       Window mode      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                       Full-screen mode      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                       Floating window mode    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onClickVipTitleBack(SuperPlayerDef.PlayerMode playMode);

        /**
         * Callback event when the "Retry Preview" button on the VIP page is clicked
         *
         * 但点击了VIP页面的重新试看按钮
         */
        void onClickVipRetry();

        /**
         * Callback event when the "Close" button on the prompt message is clicked
         *
         * 当点击了提示语的关闭按钮
         */
        void onCloseVipTip();

        void playNext();

        /**
         * Enable picture-in-picture
         *
         * 开启画中画
         */
        void enterPictureInPictureMode();

        /**
         * Get the current episode playlist
         *
         * 获得当前剧集播放列表
         */
        List<SuperPlayerModel> getPlayList();

        /**
         * Get the currently playing video
         *
         * 获得当前正在播放的视频
         */
        SuperPlayerModel getPlayingVideoModel();

        /**
         * Click the "Go to Cache List" button in the cache menu on the full-screen page
         *
         * 全屏页面点击了缓存菜单的前往缓存列表按钮
         */
        void onShowDownloadList();

        /**
         * Click the item in the audio track view
         *
         * 点击音轨view的item
         */
        void onClickSoundTrackItem(TXTrackInfo clickInfo);

        /**
         * Click the item in the subtitle view
         *
         * 点击字幕view的item
         */
        void onClickSubtitleItem(TXTrackInfo clickInfo);

        /**
         * Click the "Done" button on the audio track view settings page
         *
         * 点击音轨view的设置页面的done按钮
         */
        void onClickSubtitleViewDoneButton(TXSubtitleRenderModel model);

        /**
         * Fast rewind
         *
         * 快退
         */
        void onPlayBackward();

        /**
         * Fast forward
         *
         * 快进
         */
        void onPlayForward();


        /**
         * Finger lift
         *
         * 手指抬起
         */
        void onActionUp();

    }
}
