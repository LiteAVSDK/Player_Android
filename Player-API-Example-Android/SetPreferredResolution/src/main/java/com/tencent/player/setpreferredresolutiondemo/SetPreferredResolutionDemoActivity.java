package com.tencent.player.setpreferredresolutiondemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.player.common.Constants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class SetPreferredResolutionDemoActivity extends AppCompatActivity {

    private TXCloudVideoView mPlayerView;
    private TXVodPlayer mVodPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_preferred_resolution_demo);
        // init and start play
        initViewAndStartPlay();
    }

    private void initViewAndStartPlay() {
        mPlayerView = findViewById(R.id.video_view);
        mVodPlayer = new TXVodPlayer(this);
        mVodPlayer.setPlayerView(mPlayerView);
        TXVodPlayConfig config = new TXVodPlayConfig();
        config.setPreferredResolution(720 * 1280);
        mVodPlayer.setConfig(config);
        mVodPlayer.startVodPlay(Constants.DEMO_PLAY_MP4_URL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVodPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        mVodPlayer.stopPlay(false);
        super.onDestroy();
    }
}
