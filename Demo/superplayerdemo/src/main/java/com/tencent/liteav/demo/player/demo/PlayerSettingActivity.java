package com.tencent.liteav.demo.player.demo;

import static com.tencent.liteav.demo.player.common.ConfigBean.resetPlayConfig;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.common.ConfigBean;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;

public class PlayerSettingActivity extends FragmentActivity {

    public static final int RESULT_CODE = 102;
    private CheckBox mCheckboxAccurateSeek;
    private CheckBox mCheckboxSmoothChange;
    private CheckBox mCheckboxAutoRotate;
    private CheckBox mCheckboxBitrateIndex;
    private CheckBox mCheckboxDeal;
    private TextView mTvPlayReconnectTime;
    private EditText mEtPlayReconnectTime;
    private TextView mTvPlayReconnectInterval;
    private EditText mEtPlayReconnectInterval;
    private TextView mTvConnectOverTime;
    private EditText mEtConnectOverTime;
    private TextView mTvProgressInterval;
    private EditText mEtProgressInterval;
    private TextView mTvCacheFolder;
    private EditText mEtCacheFolder;
    private TextView mTvMaxCache;
    private EditText mEtMaxCache;
    private TextView mTvPreloadMaxCache;
    private EditText mEtPreloadMaxCache;
    private TextView mTvBufferingMaxTime;
    private EditText mEtBufferingMaxTime;
    private TextView mTvPreferredResolution;
    private EditText mEtPreferredResolution;
    private Spinner mSpPlayerMediaType;
    private Spinner mSpDecodingStrategy;
    private Spinner mSpLogLevel;
    private Spinner mSpVideoType;
    private Button mBtnSaveConfig;
    private Button mBtnResetConfig;
    private int[] mMediaType = {TXVodConstants.MEDIA_TYPE_AUTO,
            TXVodConstants.MEDIA_TYPE_HLS_VOD, TXVodConstants.MEDIA_TYPE_HLS_LIVE};
    private int[] mLogArray = {TXLiveConstants.LOG_LEVEL_VERBOSE, TXLiveConstants.LOG_LEVEL_DEBUG,
            TXLiveConstants.LOG_LEVEL_INFO, TXLiveConstants.LOG_LEVEL_WARN,
            TXLiveConstants.LOG_LEVEL_ERROR, TXLiveConstants.LOG_LEVEL_FATAL, TXLiveConstants.LOG_LEVEL_NULL};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_config);
        initView();
        setConfigView();
    }

    private void initView() {
        mCheckboxAccurateSeek = findViewById(R.id.checkbox_accurate_seek);
        mCheckboxSmoothChange = findViewById(R.id.checkbox_smooth_change);
        mCheckboxAutoRotate = findViewById(R.id.checkbox_auto_rotate);
        mCheckboxBitrateIndex = findViewById(R.id.checkbox_bitrate_index);
        mCheckboxDeal = findViewById(R.id.checkbox_deal);
        mTvPlayReconnectTime = findViewById(R.id.tv_play_reconnect_time);
        mEtPlayReconnectTime = findViewById(R.id.et_play_reconnect_time);
        mTvPlayReconnectInterval = findViewById(R.id.tv_play_reconnect_interval);
        mEtPlayReconnectInterval = findViewById(R.id.et_play_reconnect_interval);
        mTvConnectOverTime = findViewById(R.id.tv_connect_over_time);
        mEtConnectOverTime = findViewById(R.id.et_connect_over_time);
        mTvProgressInterval = findViewById(R.id.tv_progress_interval);
        mEtProgressInterval = findViewById(R.id.et_progress_interval);
        mTvCacheFolder = findViewById(R.id.tv_cache_folder);
        mEtCacheFolder = findViewById(R.id.et_cache_folder);
        mTvMaxCache = findViewById(R.id.tv_max_cache);
        mEtMaxCache = findViewById(R.id.et_max_cache);
        mTvPreloadMaxCache = findViewById(R.id.tv_preload_max_cache);
        mEtPreloadMaxCache = findViewById(R.id.et_preload_max_cache);
        mTvBufferingMaxTime = findViewById(R.id.tv_buffering_max_time);
        mEtBufferingMaxTime = findViewById(R.id.et_buffering_max_time);
        mTvPreferredResolution = findViewById(R.id.tv_preferred_resolution);
        mEtPreferredResolution = findViewById(R.id.et_preferred_resolution);
        mSpPlayerMediaType = findViewById(R.id.sp_player_media_type);
        ArrayAdapter playerMediaAdapter = ArrayAdapter.createFromResource(this,
                R.array.player_media_type, R.layout.super_player_spinner_layout);
        playerMediaAdapter.setDropDownViewResource(R.layout.super_player_drop_down_layout);
        mSpPlayerMediaType.setAdapter(playerMediaAdapter);
        mSpDecodingStrategy = findViewById(R.id.sp_decoding_strategy);
        ArrayAdapter decodingStrategyAdapter = ArrayAdapter.createFromResource(this,
                R.array.decoding_strategy, R.layout.super_player_spinner_layout);
        decodingStrategyAdapter.setDropDownViewResource(R.layout.super_player_drop_down_layout);
        mSpDecodingStrategy.setAdapter(decodingStrategyAdapter);
        mSpLogLevel = findViewById(R.id.sp_log_level);
        ArrayAdapter logLevelAdapter = ArrayAdapter.createFromResource(this,
                R.array.log_level, R.layout.super_player_spinner_layout);
        logLevelAdapter.setDropDownViewResource(R.layout.super_player_drop_down_layout);
        mSpLogLevel.setAdapter(logLevelAdapter);
        mSpVideoType = findViewById(R.id.sp_video_type);
        ArrayAdapter videoTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.video_type, R.layout.super_player_spinner_layout);
        videoTypeAdapter.setDropDownViewResource(R.layout.super_player_drop_down_layout);
        mSpVideoType.setAdapter(videoTypeAdapter);
        mBtnSaveConfig = findViewById(R.id.btn_save_config);
        mBtnResetConfig = findViewById(R.id.btn_reset_config);
        mBtnSaveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
                Toast.makeText(PlayerSettingActivity.this,
                        getResources().getString(R.string.super_player_save_succeed),
                        Toast.LENGTH_SHORT).show();
            }
        });
        mBtnResetConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetConfig();
                Toast.makeText(PlayerSettingActivity.this,
                        getResources().getString(R.string.super_player_reset_succeed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setConfigView() {
        mCheckboxAccurateSeek.setChecked(ConfigBean.sPlayConfig.isEnableAccurateSeek());
        mCheckboxSmoothChange.setChecked(ConfigBean.sPlayConfig.isSmoothSwitchBitrate());
        mCheckboxAutoRotate.setChecked(ConfigBean.sPlayConfig.isAutoRotate());
        mCheckboxBitrateIndex.setChecked(ConfigBean.sIsEnableSelfAdaption);
        mCheckboxDeal.setChecked(ConfigBean.sPlayConfig.isEnableRenderProcess());
        mEtPlayReconnectTime.setText(String.valueOf(ConfigBean.sPlayConfig.getConnectRetryCount()));
        mEtPlayReconnectInterval.setText(String.valueOf(ConfigBean.sPlayConfig.getConnectRetryInterval()));
        mEtConnectOverTime.setText(String.valueOf(ConfigBean.sPlayConfig.getTimeout()));
        mEtProgressInterval.setText(String.valueOf(ConfigBean.sPlayConfig.getProgressInterval()));
        mEtCacheFolder.setText(TXPlayerGlobalSetting.getCacheFolderPath());
        mEtMaxCache.setText(String.valueOf(TXPlayerGlobalSetting.getMaxCacheSize()));
        mEtPreloadMaxCache.setText(String.valueOf(ConfigBean.sPlayConfig.getMaxPreloadSize()));
        mEtBufferingMaxTime.setText(String.valueOf(ConfigBean.sPlayConfig.getMaxBufferSize()));
        mEtPreferredResolution.setText(String.valueOf(ConfigBean.sPlayConfig.getPreferredResolution()));
        mSpPlayerMediaType.setSelection(ConfigBean.sPlayConfig.getMediaType());
        mSpDecodingStrategy.setSelection(ConfigBean.sIsEnableHardWareDecode ? 0 : 1);
        mSpLogLevel.setSelection(ConfigBean.sLogLevel);
        mSpVideoType.setSelection(ConfigBean.sIsUseDash ? 1 : 0);
    }

    private void resetConfig() {
        resetPlayConfig();
        setConfigView();
    }

    private void saveConfig() {
        ConfigBean.sPlayConfig.setEnableAccurateSeek(mCheckboxAccurateSeek.isChecked());
        ConfigBean.sPlayConfig.setSmoothSwitchBitrate(mCheckboxSmoothChange.isChecked());
        ConfigBean.sPlayConfig.setAutoRotate(mCheckboxAutoRotate.isChecked());
        ConfigBean.sIsEnableSelfAdaption = mCheckboxBitrateIndex.isChecked();
        ConfigBean.sPlayConfig.setEnableRenderProcess(mCheckboxDeal.isChecked());
        ConfigBean.sPlayConfig.setProgressInterval(getIntFromEditText(mEtProgressInterval));
        ConfigBean.sPlayConfig.setConnectRetryCount(getIntFromEditText(mEtPlayReconnectTime));
        ConfigBean.sPlayConfig.setConnectRetryInterval(getIntFromEditText(mEtPlayReconnectInterval));
        ConfigBean.sPlayConfig.setTimeout(getIntFromEditText(mEtConnectOverTime));
        TXPlayerGlobalSetting.setCacheFolderPath(mEtCacheFolder.getText().toString());
        TXPlayerGlobalSetting.setMaxCacheSize(getIntFromEditText(mEtMaxCache));
        ConfigBean.sPlayConfig.setMaxPreloadSize(getIntFromEditText(mEtPreloadMaxCache));
        ConfigBean.sPlayConfig.setMaxBufferSize(getIntFromEditText(mEtBufferingMaxTime));
        ConfigBean.sPlayConfig.setPreferredResolution(getLongFromEditText(mEtPreferredResolution));
        ConfigBean.sPlayConfig.setMediaType(mMediaType[mSpPlayerMediaType.getSelectedItemPosition()]);
        ConfigBean.sIsEnableHardWareDecode = mSpDecodingStrategy.getSelectedItemPosition() == 0 ? true : false;
        ConfigBean.sLogLevel = mLogArray[mSpLogLevel.getSelectedItemPosition()];
        ConfigBean.sIsUseDash = mSpVideoType.getSelectedItemPosition() == 0 ? false : true;
        TXLiveBase.setLogLevel(ConfigBean.sLogLevel);
        finish();
    }

    private int getIntFromEditText(EditText editText) {
        int ret = 0;
        String text = editText.getText().toString();
        if (!text.equals("")) {
            ret = Integer.parseInt(text);
        }
        return ret;
    }

    private long getLongFromEditText(EditText editText) {
        long ret = 0;
        String text = editText.getText().toString();
        if (!text.equals("")) {
            ret = Long.parseLong(text);
        }
        return ret;
    }

}