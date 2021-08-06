package com.tencent.liteav.demo.superplayer.model.utils;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * 手势控制视频播放进度、调节亮度音量的工具
 */

public class VideoGestureDetector {
    // 手势类型
    private static final int NONE           = 0;    // 无效果
    private static final int VOLUME         = 1;    // 音量
    private static final int BRIGHTNESS     = 2;    // 亮度
    private static final int VIDEO_PROGRESS = 3;    // 播放进度

    private int                        mScrollMode  = NONE;     // 手势类型
    private VideoGestureListener       mVideoGestureListener;  // 回调
    private int                        mVideoWidth;            // 视频宽度px
    private float                      mBrightness  = 1;        // 当前亮度(0.0~1.0)
    private Window                     mWindow;                // 当前window
    private WindowManager.LayoutParams mLayoutParams;          // 用于获取和设置屏幕亮度
    private ContentResolver            mResolver;              // 用于获取当前屏幕亮度
    private AudioManager               mAudioManager;          // 音频管理器，用于设置音量
    private int                        mMaxVolume   = 0;         // 最大音量值
    private int                        mOldVolume   = 0;         // 记录调节音量之前的旧音量值
    private int                        mVideoProgress;         // 记录滑动后的进度，在回调中抛出
    private int                        mDownProgress;          // 滑动开始时的视频播放进度
    private int                        offsetX      = 20; //手势临界值，当两滑动事件坐标的水平差值>20时判定为{@link #VIDEO_PROGRESS}, 否则判定为{@link #VOLUME}或者{@link #BRIGHTNESS}
    private float                      mSensitivity = 0.3f;    // 调节音量、亮度的灵敏度   //手势灵敏度 0.0~1.0

    public VideoGestureDetector(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (context instanceof Activity) {
            mWindow = ((Activity) context).getWindow();
            mLayoutParams = mWindow.getAttributes();
            mBrightness = mLayoutParams.screenBrightness;
        }
        mResolver = context.getContentResolver();
    }

    /**
     * 设置回调
     *
     * @param videoGestureListener
     */
    public void setVideoGestureListener(VideoGestureListener videoGestureListener) {
        mVideoGestureListener = videoGestureListener;
    }

    /**
     * 重置数据以开始新的一次滑动
     *
     * @param videoWidth   视频宽度px
     * @param downProgress 手势按下时视频的播放进度(秒)
     */
    public void reset(int videoWidth, int downProgress) {
        mVideoProgress = 0;
        mVideoWidth = videoWidth;
        mScrollMode = NONE;
        mOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mBrightness = mLayoutParams.screenBrightness;
        if (mBrightness == -1) {
            //一开始是默认亮度的时候，获取系统亮度，计算比例值
            mBrightness = getBrightness() / 255.0f;
        }
        mDownProgress = downProgress;
    }

    /**
     * 获取当前是否是视频进度滑动手势
     *
     * @return
     */
    public boolean isVideoProgressModel() {
        return mScrollMode == VIDEO_PROGRESS;
    }

    /**
     * 获取滑动后对应的视频进度
     *
     * @return
     */
    public int getVideoProgress() {
        return mVideoProgress;
    }

    /**
     * 滑动手势操控类别判定
     *
     * @param height    滑动事件的高度px
     * @param downEvent 按下事件
     * @param moveEvent 滑动事件
     * @param distanceX 滑动水平距离
     * @param distanceY 滑动竖直距离
     */
    public void check(int height, MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
        switch (mScrollMode) {
            case NONE:
                //offset是让快进快退不要那么敏感的值
                if (Math.abs(downEvent.getX() - moveEvent.getX()) > offsetX) {
                    mScrollMode = VIDEO_PROGRESS;
                } else {
                    int halfVideoWidth = mVideoWidth / 2;
                    if (downEvent.getX() < halfVideoWidth) {
                        mScrollMode = BRIGHTNESS;
                    } else {
                        mScrollMode = VOLUME;
                    }
                }
                break;
            case VOLUME:
                int value = height / mMaxVolume;
                int newVolume = (int) ((downEvent.getY() - moveEvent.getY()) / value * mSensitivity + mOldVolume);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);

                float volumeProgress = newVolume / Float.valueOf(mMaxVolume) * 100;
                if (mVideoGestureListener != null) {
                    mVideoGestureListener.onVolumeGesture(volumeProgress);
                }
                break;
            case BRIGHTNESS:
                float newBrightness = height == 0 ? 0 : (downEvent.getY() - moveEvent.getY()) / height * mSensitivity;
                newBrightness += mBrightness;

                if (newBrightness < 0) {
                    newBrightness = 0;
                } else if (newBrightness > 1) {
                    newBrightness = 1;
                }
                if (mLayoutParams != null) {
                    mLayoutParams.screenBrightness = newBrightness;
                }
                if (mWindow != null) {
                    mWindow.setAttributes(mLayoutParams);
                }

                if (mVideoGestureListener != null) {
                    mVideoGestureListener.onBrightnessGesture(newBrightness);
                }
                break;
            case VIDEO_PROGRESS:
                float dis = moveEvent.getX() - downEvent.getX();
                float percent = dis / mVideoWidth;
                mVideoProgress = (int) (mDownProgress + percent * 100);
                if (mVideoGestureListener != null) {
                    mVideoGestureListener.onSeekGesture(mVideoProgress);
                }
                break;
        }
    }

    /**
     * 获取当前亮度
     *
     * @return
     */
    private int getBrightness() {
        if (mResolver != null) {
            return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
        } else {
            return 255;
        }
    }

    /**
     * 回调
     */
    public interface VideoGestureListener {
        /**
         * 亮度调节回调
         *
         * @param newBrightness 滑动后的新亮度值
         */
        void onBrightnessGesture(float newBrightness);

        /**
         * 音量调节回调
         *
         * @param volumeProgress 滑动后的新音量值
         */
        void onVolumeGesture(float volumeProgress);

        /**
         * 播放进度调节回调
         *
         * @param seekProgress 滑动后的新视频进度
         */
        void onSeekGesture(int seekProgress);
    }
}
