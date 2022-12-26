package com.tencent.player.baseplayer;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;

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
    private Button mBtnSaveConfig;
    private Button mBtnResetConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_config);
        initView();
        refreshViews();
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
        mSpDecodingStrategy = findViewById(R.id.sp_decoding_strategy);
        mSpLogLevel = findViewById(R.id.sp_log_level);
        mBtnSaveConfig = findViewById(R.id.btn_save_config);
        mBtnResetConfig = findViewById(R.id.btn_reset_config);
        mBtnSaveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
                Toast.makeText(PlayerSettingActivity.this,"保存配置成功！",
                        Toast.LENGTH_SHORT).show();
            }
        });
        mBtnResetConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetConfig();
                Toast.makeText(PlayerSettingActivity.this,"重置配置成功！",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshViews() {
        if (ConfigBean.sPlayConfig == null) {
            mCheckboxAccurateSeek.setChecked(true);
            mCheckboxSmoothChange.setChecked(true);
            mCheckboxAutoRotate.setChecked(true);
            mCheckboxBitrateIndex.setChecked(true);
            mCheckboxDeal.setChecked(true);
            mEtPlayReconnectTime.setText(String.valueOf(3));
            mEtPlayReconnectInterval.setText(String.valueOf(3));
            mEtConnectOverTime.setText(String.valueOf(10));
            mEtProgressInterval.setText(String.valueOf(500));
            mEtCacheFolder.setText("txCache");
            mEtMaxCache.setText(String.valueOf(200));
            mEtPreloadMaxCache.setText(String.valueOf(50));
            mEtBufferingMaxTime.setText(String.valueOf(50));
            mEtPreferredResolution.setText(String.valueOf(720*1280));
            mSpPlayerMediaType.setSelection(0);
            mSpDecodingStrategy.setSelection(ConfigBean.sIsEnableHardWareDecode?0:1);
            mSpLogLevel.setSelection(ConfigBean.sLogLevel);
            ConfigBean.sPlayConfig = new TXVodPlayConfig();
        } else {
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
            mSpDecodingStrategy.setSelection(ConfigBean.sIsEnableHardWareDecode?0:1);
            mSpLogLevel.setSelection(ConfigBean.sLogLevel);
        }
    }

    private void resetConfig() {
        ConfigBean.sPlayConfig = null;
        refreshViews();
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
        ConfigBean.sPlayConfig.setMediaType(mSpPlayerMediaType.getSelectedItemPosition());
        ConfigBean.sIsEnableHardWareDecode = mSpDecodingStrategy.getSelectedItemPosition() == 0 ? true:false;
        ConfigBean.sLogLevel = mSpLogLevel.getSelectedItemPosition();
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