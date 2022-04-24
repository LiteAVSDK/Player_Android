package com.tencent.liteav.demo.superplayer.ui.player;


import android.graphics.Bitmap;
import android.view.View;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.List;

/**
 * 播放控制接口
 */
public interface Player {

    /**
     * 设置回调
     *
     * @param callback 回调接口实现对象
     */
    void setCallback(Callback callback);

    /**
     * 设置水印
     *
     * @param bmp 水印图
     * @param x   水印的x坐标
     * @param y   水印的y坐标
     */
    void setWatermark(Bitmap bmp, float x, float y);

    /**
     * 显示控件
     */
    void show();

    /**
     * 隐藏控件
     */
    void hide();

    /**
     * 释放控件的内存
     */
    void release();

    /**
     * 更新播放状态
     *
     * @param playState 正在播放{@link SuperPlayerDef.PlayerState#PLAYING}
     *                  正在加载{@link SuperPlayerDef.PlayerState#LOADING}
     *                  暂停   {@link SuperPlayerDef.PlayerState#PAUSE}
     *                  播放结束{@link SuperPlayerDef.PlayerState#END}
     */
    void updatePlayState(SuperPlayerDef.PlayerState playState);

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    void setVideoQualityList(List<VideoQuality> list);

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    void updateTitle(String title);

    /**
     * 更新视频播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    void updateVideoProgress(long current, long duration);

    /**
     * 更新播放类型
     *
     * @param type 点播     {@link SuperPlayerDef.PlayerType#VOD}
     *             点播     {@link SuperPlayerDef.PlayerType#LIVE}
     *             直播回看  {@link SuperPlayerDef.PlayerType#LIVE_SHIFT}
     */
    void updatePlayType(SuperPlayerDef.PlayerType type);

    /**
     * 设置背景
     *
     * @param bitmap 背景图
     */
    void setBackground(final Bitmap bitmap);

    /**
     * 显示背景
     */
    void showBackground();

    /**
     * 隐藏背景
     */
    void hideBackground();

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
    void updateVideoQuality(VideoQuality videoQuality);

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    void updateImageSpriteInfo(PlayImageSpriteInfo info);

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    void updateKeyFrameDescInfo(List<PlayKeyFrameDescInfo> list);

    /**
     * 播放控制回调接口
     */
    interface Callback {

        /**
         * 切换播放模式回调
         *
         * @param playMode 切换后的播放模式：
         *                 窗口模式      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                 全屏模式      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                 悬浮窗模式    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onSwitchPlayMode(SuperPlayerDef.PlayerMode playMode);

        /**
         * 返回点击事件回调
         *
         * @param playMode 当前播放模式：
         *                 窗口模式      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                 全屏模式      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                 悬浮窗模式    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onBackPressed(SuperPlayerDef.PlayerMode playMode);

        /**
         * 悬浮窗位置更新回调
         *
         * @param x 悬浮窗x坐标
         * @param y 悬浮窗y坐标
         */
        void onFloatPositionChange(int x, int y);

        /**
         * 播放暂停回调
         */
        void onPause();

        /**
         * 播放继续回调
         */
        void onResume();

        /**
         * 播放跳转回调
         *
         * @param position 跳转的位置(秒)
         */
        void onSeekTo(int position);

        /**
         * 恢复直播回调
         */
        void onResumeLive();

        /**
         * 弹幕开关回调
         *
         * @param isOpen 开启：true 关闭：false
         */
        void onDanmuToggle(boolean isOpen);

        /**
         * 屏幕截图回调
         */
        void onSnapshot();

        /**
         * 更新画质回调
         *
         * @param quality 画质
         */
        void onQualityChange(VideoQuality quality);

        /**
         * 更新播放速度回调
         *
         * @param speedLevel 播放速度
         */
        void onSpeedChange(float speedLevel);

        /**
         * 镜像开关回调
         *
         * @param isMirror 开启：true 关闭：close
         */
        void onMirrorToggle(boolean isMirror);

        /**
         * 硬件加速开关回调
         *
         * @param isAccelerate 开启：true 关闭：false
         */
        void onHWAccelerationToggle(boolean isAccelerate);

        /**
         * 当用户点击了 开通VIP会员按钮的回调事件
         */
        void onClickHandleVip();

        /**
         * 当点击了VIP试看界面的返回按钮的的回调
         *  * @param playMode 当前播放模式：
         *                       窗口模式      {@link SuperPlayerDef.PlayerMode#WINDOW  }
         *                       全屏模式      {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }
         *                       悬浮窗模式    {@link SuperPlayerDef.PlayerMode#FLOAT  }
         */
        void onClickVipTitleBack(SuperPlayerDef.PlayerMode playMode);

        /**
         * 但点击了VIP页面的重新试看按钮
         */
        void onClickVipRetry();

        /**
         * 当点击了提示语的关闭按钮
         */
        void onCloseVipTip();

        void playNext();
    }
}
