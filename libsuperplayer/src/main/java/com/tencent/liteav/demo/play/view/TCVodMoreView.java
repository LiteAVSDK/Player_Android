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
 * 更多选项弹框
 */

public class TCVodMoreView extends RelativeLayout implements RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private Context mContext;

    private SeekBar mSeekBarVolume;
    private SeekBar mSeekBarLight;
    private Switch mSwitchMirror;
    private Switch mSwitchAccelerate;
    private Callback mCallback;
    private AudioManager mAudioManager;
    private RadioGroup mRadioGroup;
    private RadioButton mRbSpeed1;
    private RadioButton mRbSpeed125;
    private RadioButton mRbSpeed15;
    private RadioButton mRbSpeed2;
    private LinearLayout mLayoutSpeed;
    private LinearLayout mLayoutMirror;

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
        LayoutInflater.from(mContext).inflate(R.layout.player_more_popup_view, this);

        mLayoutSpeed = (LinearLayout) findViewById(R.id.layout_speed);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRbSpeed1 = (RadioButton) findViewById(R.id.rb_speed1);
        mRbSpeed125 = (RadioButton) findViewById(R.id.rb_speed125);
        mRbSpeed15 = (RadioButton) findViewById(R.id.rb_speed15);
        mRbSpeed2 = (RadioButton) findViewById(R.id.rb_speed2);

        mRadioGroup.setOnCheckedChangeListener(this);
        mSeekBarVolume = (SeekBar) findViewById(R.id.seekBar_audio);
        mSeekBarLight = (SeekBar) findViewById(R.id.seekBar_light);

        mLayoutMirror = (LinearLayout) findViewById(R.id.layout_mirror);
        mSwitchMirror = (Switch) findViewById(R.id.switch_mirror);

        mSwitchAccelerate = (Switch) findViewById(R.id.switch_accelerate);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.switch_mirror) {
            if (mCallback != null) {
                mCallback.onMirrorChange(isChecked);
            }
        } else if (compoundButton.getId() == R.id.switch_accelerate) {
            SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
            config.enableHWAcceleration = !config.enableHWAcceleration;
            mSwitchAccelerate.setChecked(config.enableHWAcceleration);
            if (mCallback != null) {
                mCallback.onHWAcceleration(config.enableHWAcceleration);
            }
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (checkedId == R.id.rb_speed1) {
            mRbSpeed1.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.0f);
            }

        } else if (checkedId == R.id.rb_speed125) {
            mRbSpeed125.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.25f);
            }

        } else if (checkedId == R.id.rb_speed15) {
            mRbSpeed15.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(1.5f);
            }

        } else if (checkedId == R.id.rb_speed2) {
            mRbSpeed2.setChecked(true);
            if (mCallback != null) {
                mCallback.onSpeedChange(2.0f);
            }

        }
    }

    public void updatePlayType(int playType) {
        if (playType == SuperPlayerConst.PLAYTYPE_VOD) {
            mLayoutSpeed.setVisibility(View.VISIBLE);
            mLayoutMirror.setVisibility(View.VISIBLE);
        } else {
            mLayoutSpeed.setVisibility(View.GONE);
            mLayoutMirror.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void onSpeedChange(float speedLevel);

        void onMirrorChange(boolean isMirror);

        void onHWAcceleration(boolean isAccelerate);
    }

}
