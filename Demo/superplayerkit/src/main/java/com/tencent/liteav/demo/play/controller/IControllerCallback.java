package com.tencent.liteav.demo.play.controller;

import com.tencent.liteav.demo.play.bean.TCVideoQuality;

/**
 * 播放控制回调接口
 */
public interface IControllerCallback {

    /**
     * 切换播放模式回调
     *
     * @param playMode 切换后的播放模式：
     *                 窗口模式  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_WINDOW}
     *                 全屏模式  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_FULLSCREEN}
     *                 悬浮窗模式{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_FLOAT}
     */
    void onSwitchPlayMode(int playMode);

    /**
     * 返回点击事件回调
     *
     * @param playMode 当前播放模式：
     *                 窗口模式  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_WINDOW}
     *                 全屏模式  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_FULLSCREEN}
     *                 悬浮窗模式{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYMODE_FLOAT}
     */
    void onBackPressed(int playMode);

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
    void onQualityChange(TCVideoQuality quality);

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
}
