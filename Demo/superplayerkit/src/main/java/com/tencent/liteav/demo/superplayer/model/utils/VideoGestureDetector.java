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
 * Gesture control tool for adjusting video playback progress, brightness and volume
 *
 * 手势控制视频播放进度、调节亮度音量的工具
 */
public class VideoGestureDetector {
    private static final int NONE           = 0;
    private static final int VOLUME         = 1;
    private static final int BRIGHTNESS     = 2;
    private static final int VIDEO_PROGRESS = 3;

    private int                        mScrollMode  = NONE;
    private VideoGestureListener       mVideoGestureListener;
    private int                        mVideoWidth;
    private float                      mBrightness  = 1;
    private Window                     mWindow;
    private WindowManager.LayoutParams mLayoutParams;
    private ContentResolver            mResolver;
    private AudioManager               mAudioManager;
    private int                        mMaxVolume   = 0;
    private int                        mOldVolume   = 0;
    private int                        mVideoProgress;
    private int                        mDownProgress;
    // Gesture threshold, when the horizontal difference between two sliding events is >20, it is judged as
    // {@link #VIDEO_PROGRESS}, otherwise it is judged as {@link #VOLUME} or {@link #BRIGHTNESS}
    private int                        offsetX      = 20;
    // Adjust the sensitivity of volume and brightness, range 0.0~1.0
    private float                      mSensitivity = 0.3f;

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

    public int getMaxVolume() {
        return mMaxVolume;
    }

    public void setVideoGestureListener(VideoGestureListener videoGestureListener) {
        mVideoGestureListener = videoGestureListener;
    }

    /**
     * Reset the data to start a new slide
     *
     * 重置数据以开始新的一次滑动
     *
     * @param videoWidth   Video width in pixels
     *                     视频宽度px
     * @param downProgress Video playback progress in seconds when the gesture is pressed
     *                     手势按下时视频的播放进度(秒)
     */
    public void reset(int videoWidth, int downProgress) {
        mVideoProgress = 0;
        mVideoWidth = videoWidth;
        mScrollMode = NONE;
        mOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mBrightness = mLayoutParams.screenBrightness;
        if (mBrightness == -1) {
            // When the default brightness is set, get the system brightness and calculate the ratio value
            mBrightness = getBrightness() / 255.0f;
        }
        mDownProgress = downProgress;
    }

    /**
     * Get whether the current gesture is a video progress sliding gesture
     *
     * 获取当前是否是视频进度滑动手势
     */
    public boolean isVideoProgressModel() {
        return mScrollMode == VIDEO_PROGRESS;
    }

    /**
     * Get the corresponding video progress after sliding
     *
     * 获取滑动后对应的视频进度
     */
    public int getVideoProgress() {
        return mVideoProgress;
    }

    /**
     * Sliding gesture type judgment
     *
     * 滑动手势操控类别判定
     *
     * @param height    Height of sliding event in pixels
     *                  滑动事件的高度px
     * @param downEvent Press event
     *                  按下事件
     * @param moveEvent Sliding event
     *                  滑动事件
     * @param distanceX Horizontal distance of sliding
     *                  滑动水平距离
     * @param distanceY Vertical distance of sliding
     *                  滑动竖直距离
     */
    public void check(int height, MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
        switch (mScrollMode) {
            case NONE:
                // The offset represents the minimum sliding threshold
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
                float value = (float) height / (float) mMaxVolume;
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
            default:
                break;
        }
    }

    /**
     * Get the current brightness
     *
     * 获取当前亮度
     */
    private int getBrightness() {
        if (mResolver != null) {
            return Settings.System.getInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
        } else {
            return 255;
        }
    }

    public interface VideoGestureListener {
        /**
         * Brightness adjustment callback
         *
         * 亮度调节回调
         */
        void onBrightnessGesture(float newBrightness);

        /**
         * Volume adjustment callback
         *
         * 音量调节回调
         */
        void onVolumeGesture(float volumeProgress);

        /**
         * Playback progress adjustment callback
         *
         * 播放进度调节回调
         */
        void onSeekGesture(int seekProgress);
    }
}
