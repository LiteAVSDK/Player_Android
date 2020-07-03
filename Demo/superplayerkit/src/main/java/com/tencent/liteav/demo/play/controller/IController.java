package com.tencent.liteav.demo.play.controller;


import android.graphics.Bitmap;

import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.bean.TCVideoQuality;

import java.util.List;

/**
 * 播放控制接口
 */
public interface IController {

    /**
     * 设置回调
     *
     * @param callback 回调接口实现对象
     */
    void setCallback(IControllerCallback callback);

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
     * @param playState 正在播放{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PLAYING}
     *                  正在加载{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_LOADING}
     *                  暂停   {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_PAUSE}
     *                  播放结束{@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYSTATE_END}
     */
    void updatePlayState(int playState);

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    void setVideoQualityList(List<TCVideoQuality> list);

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    void updateTitle(String title);

    /**
     * 更新是屁播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    void updateVideoProgress(long current, long duration);

    /**
     * 更新播放类型
     *
     * @param type 点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_VOD}
     *             点播     {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE}
     *             直播回看  {@link com.tencent.liteav.demo.play.SuperPlayerConst#PLAYTYPE_LIVE_SHIFT}
     */
    void updatePlayType(int type);

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
    void updateVideoQuality(TCVideoQuality videoQuality);

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    void updateImageSpriteInfo(TCPlayImageSpriteInfo info);

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    void updateKeyFrameDescInfo(List<TCPlayKeyFrameDescInfo> list);
}
