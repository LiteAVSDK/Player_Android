package com.tencent.liteav.demo.player.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.data.ShortVideoModel;
import com.tencent.qcloud.tuiplayer.core.TUIPlayerConfig;
import com.tencent.qcloud.tuiplayer.core.TUIPlayerCore;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlaySource;

import java.io.File;
import java.util.List;

public class TUIShortVideoActivity extends FragmentActivity implements  ShortVideoModel.IOnDataLoadFullListener {

    private static final String TAG = "ShortVideoDemo:TUIShortVideoActivity";

    private static final String LICENCE_URL =
            "https://license.vod2.myqcloud.com/license/v2/1252463788_1/v_cube.license";
    private static final String LICENCE_KEY = "Please replace it with your licenseKey";

    private ShortVideoFragment mPlayFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set status background to black
        TUIPlayerConfig config = new TUIPlayerConfig.Builder()
                .enableLog(true)
                .licenseKey(LICENCE_KEY)
                .licenseUrl(LICENCE_URL)
                .build();
        TUIPlayerCore.init(getApplicationContext(), config);
        // copy superResolution resource
        String destPath = getCacheDir().getAbsolutePath() + "/sr_resource";
        File file = new File(destPath);
        if (!file.exists()) {
            file.mkdir();
        }
        TUIPlayerCore.setSuperResolutionResource(getApplicationContext(), destPath);
        Window window = getWindow();
        if(null != window) {
            window.setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.player_activity_shortvideo);
        ShortVideoModel.getInstance().setOnDataLoadFullListener(this);
        initView();
    }

    private void initView() {
        ShortVideoModel.getInstance().setOnDataLoadFullListener(this);
        mPlayFragment = new ShortVideoFragment();
        FragmentManager manager = getSupportFragmentManager();
        // 开始事务 得到事务
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        // 替换操作
        fragmentTransaction.replace(R.id.player_frame_layout, mPlayFragment);
        // 提交
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPlayFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadedSuccess(final List<TUIPlaySource> videoBeanList, final boolean isRefresh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayFragment.onLoaded(videoBeanList, isRefresh);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShortVideoModel.getInstance().removeOnDataLoadFullListenerSafe(this);
    }

    @Override
    public void onLoadedFailed(int errCode) {
        Toast.makeText(getApplicationContext(),
                getString(R.string.short_video_get_data_failed) + errCode, Toast.LENGTH_LONG).show();
    }

}
