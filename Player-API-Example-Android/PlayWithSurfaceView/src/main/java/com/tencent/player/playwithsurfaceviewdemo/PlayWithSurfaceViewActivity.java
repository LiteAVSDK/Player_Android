package com.tencent.player.playwithsurfaceviewdemo;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.player.common.Constants;
import com.tencent.rtmp.TXVodPlayer;

/**
 * see: https://cloud.tencent.com/document/product/881/20217#.E6.AD.A5.E9.AA.A43.EF.BC.9A.E6.B7.BB.E5.8A.A0-view
 */
public class PlayWithSurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final String PLAY_URL = "play_url";
    private TXVodPlayer mVodPlayer;
    private String mPlayUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with_surface_view);
        mVodPlayer = new TXVodPlayer(this);
        mPlayUrl = getIntent().getStringExtra(PLAY_URL);
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mVodPlayer.setSurface(holder.getSurface());
        mVodPlayer.startVodPlay(mPlayUrl != null ? mPlayUrl : Constants.DEMO_PLAY_MP4_URL);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mVodPlayer.stopPlay(false);
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