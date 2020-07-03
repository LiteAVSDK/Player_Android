package com.tencent.liteav.demo.play.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;

/**
 * Created by yuejiaoli on 2018/7/4.
 *
 * 更多选项弹框
 *
 * 1、声音调节seekBar回调{@link #mVolumeChangeListener}
 *
 * 2、亮度调节seekBar回调{@link #mLightChangeListener}
 *
 * 3、倍速选择回调{@link #onCheckedChanged(RadioGroup, int)}
 *
 * 4、镜像、硬件加速开关回调{@link #onCheckedChanged(CompoundButton, boolean)}
 */

public class TCVodMoreView extends RelativeLayout implements RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private Context mContext;

    private SeekBar         mSeekBarVolume;     // 音量seekBar
    private SeekBar         mSeekBarLight;      // 亮度seekBar
    private Switch          mSwitchMirror;      // 镜像开关
    private Switch          mSwitchAccelerate;  // 硬解开关
    private Callback        mCallback;          // 回调
    private AudioManager    mAudioManager;      // 音频管理器
    private RadioGroup      mRadioGroup;        // 倍速选择radioGroup
    private RadioButton     mRbSpeed1;          // 1.0倍速按钮
    private RadioButton     mRbSpeed125;        // 1.25倍速按钮
    private RadioButton     mRbSpeed15;         // 1.5倍速按钮
    private RadioButton     mRbSpeed2;          // 2.0倍速按钮
    private LinearLayout    mLayoutSpeed;       // 倍速按钮所在布局
    private LinearLayout    mLayoutMirror;      // 镜像按钮所在布局

    public TCVodMoreView(Context context) {
        super(context);
        init(context);
    }

    public TCVodMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TCVodMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.superplayer_more_popup_view, this);

        mLayoutSpeed = (LinearLayout) findViewById(R.id.superplayer_ll_speed);
        mRadioGroup = (RadioGroup) findViewById(R.id.superplayer_rg);
        mRbSpeed1 = (RadioButton) findViewById(R.id.superplayer_rb_speed1);
        mRbSpeed125 = (RadioButton) findViewById(R.id.superplayer_rb_speed125);
        mRbSpeed15 = (RadioButton) findViewById(R.id.superplayer_rb_speed15);
        mRbSpeed2 = (RadioButton) findViewById(R.id.superplayer_rb_speed2);

        mRadioGroup.setOnCheckedChangeListener(this);
        mSeekBarVolume = (SeekBar) findViewById(R.id.superplayer_sb_audio);
        mSeekBarLight = (SeekBar) findViewById(R.id.superplayer_sb_light);

        mLayoutMirror = (LinearLayout) findViewById(R.id.superplayer_ll_mirror);
        mSwitchMirror = (Switch) findViewById(R.id.superplayer_switch_mirror);

        mSwitchAccelerate = (Switch) findViewById(R.id.superplayer_switch_accelerate);
        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
        mSwitchAccelerate.setChecked(config.enableHWAcceleration);

        mSeekBarVolume.setOnSeekBarChangeListener(mVolumeChangeListener);
        mSeekBarLight.setOnSeekBarChangeListener(mLightChangeListener);

        mSwitchMirror.setOnCheckedChangeListener(this);
        mSwitchAccelerate.setOnCheckedChangeListener(this);

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        updateCurrentVolume();
        updateCurrentLight();
    }

    private void updateCurrentVolume() {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float percentage = (float) curVolume / maxVolume;

        final int progress = (int) (percentage * mSeekBarVolume.getMax());
        mSeekBarVolume.setProgress(progress);
    }

    private void updateCurrentLight() {
        Activity activity = (Activity) mContext;
        Window window = activity.getWindow();

        WindowManager.LayoutParams params = window.getAttributes();
        if (params.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            params.screenBrightness = getActivityBrightness((Activity) mContext);
            window.setAttributes(params);
            if (params.screenBrightness == -1) {
                mSeekBarLight.setProgress(100);
                return;
            }
            mSeekBarLight.setProgress((int) (params.screenBrightness * 100));
        }
    }

    /**
     * 获取当前亮度
     *
     * @param activity
     * @return
     */
    public static float getActivityBrightness(Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams params = localWindow.getAttributes();
        return params.screenBrightness;
    }

    private SeekBar.OnSeekBarChangeListener mVolumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            updateVolumeProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateVolumeProgress(int progress) {
        float percentage = (float) progress / mSeekBarVolume.getMax();

        if (percentage < 0 || percentage > 1)
            return;

        if (mAudioManager != null) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int newVolume = (int) (percentage * maxVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }

    private SeekBar.OnSeekBarChangeListener mLightChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            updateBrightProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateBrightProgress(int progress) {
        Activity activity = (Activity) mContext;
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = progress * 1.0f / 100;
        if (params.screenBrightness > 1.0f) {
            params.screenBrightness = 1.0f;
        }
        if (params.screenBrightness <= 0.01f) {
            params.screenBrightness = 0.01f;
        }

        window.setAttributes(params);
        mSeekBarLight.setProgress(progress);
    }

    /**
     * 镜像、硬解开关监听
     *
     * @param compoundButton
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.superplayer_switch_mirror) {
            if (mCallback != null) {
                mCallback.onMirrorChange(isChecked);
            }
        } else if (compoundButton.getId() == R.id.superplayer_switch_accelerate) {
            SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
            config.enableHWAcceleration = !config.enableHWAcceleration;
            mSwitchAccelerate.setChecked(config.enableHWAcceleration);
            if (mCallback != null) {
                mCallback.onHWAcceleration(config.enableHWAcceleration);
            }
        }
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 倍速选择监听
     *
     * @param radioGroup
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.superplayer_rb_speed1) {
            mRbSpeed1.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.0f);
            }

        } else if (checkedId == R.id.superplayer_rb_speed125) {
            mRbSpeed125.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.25f);
            }

        } else if (checkedId == R.id.superplayer_rb_speed15) {
            mRbSpeed15.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.5f);
            }

        } else if (checkedId == R.id.superplayer_rb_speed2) {
            mRbSpeed2.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(2.0f);
            }

        }
    }

    /**
     * 更新播放视频类型
     *
     * @param playType
     */
    public void updatePlayType(int playType) {
        if (playType == SuperPlayerConst.PLAYTYPE_VOD) {
            mLayoutSpeed.setVisibility(View.VISIBLE);
            mLayoutMirror.setVisibility(View.VISIBLE);
        } else {
            mLayoutSpeed.setVisibility(View.GONE);
            mLayoutMirror.setVisibility(View.GONE);
        }
    }

    /**
     * 回调
     */
    public interface Callback {
        /**
         * 播放速度更新回调
         *
         * @param speedLevel
         */
        void onSpeedChange(float speedLevel);

        /**
         * 镜像开关回调
         *
         * @param isMirror
         */
        void onMirrorChange(boolean isMirror);

        /**
         * 硬解开关回调
         *
         * @param isAccelerate
         */
        void onHWAcceleration(boolean isAccelerate);
    }

}
