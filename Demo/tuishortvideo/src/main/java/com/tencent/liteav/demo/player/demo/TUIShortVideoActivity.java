package com.tencent.liteav.demo.player.demo;

import android.graphics.Color;
import android.os.Build;
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
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;

import java.util.List;

public class TUIShortVideoActivity extends FragmentActivity implements  ShortVideoModel.IOnDataLoadFullListener {

    private static final String TAG = "TUIShortVideoActivity";

    private static final String LICENCE_URL = "Please replace it with your licenseUrl";
    private static final String LICENCE_KEY = "Please replace it with your licenseKey";

    private ShortVideoFragment mPlayFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TUIPlayerConfig config = new TUIPlayerConfig.Builder()
                .enableLog(true)
                .licenseKey(LICENCE_URL)
                .licenseUrl(LICENCE_KEY)
                .build();
        TUIPlayerCore.init(getApplicationContext(), config);
        // set status background to black
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != window) {
            window.setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.player_activity_shortvideo);
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(this);
        initView();
    }

    private void initView() {
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(this);
        mPlayFragment = new ShortVideoFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.player_frame_layout, mPlayFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onLoadedSuccess(final List<TUIVideoSource> videoBeanList, final boolean isRefresh) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayFragment.onLoaded(videoBeanList, isRefresh);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ShortVideoModel.getInstance(this).setOnDataLoadFullListener(null);
        super.onDestroy();
    }

    @Override
    public void onLoadedFailed(int errCode) {
        Toast.makeText(this,
                getString(R.string.short_video_get_data_failed) + errCode, Toast.LENGTH_LONG).show();
    }

}
