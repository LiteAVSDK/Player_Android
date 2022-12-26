package com.tencent.player.playwithtextureviewdemo;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.player.common.Constants;
import com.tencent.rtmp.TXVodPlayer;

/**
 * see: https://cloud.tencent.com/document/product/881/20217#.E6.AD.A5.E9.AA.A43.EF.BC.9A.E6.B7.BB.E5.8A.A0-view
 */
public class PlayWithTextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TXVodPlayer mVodPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with_texture_view);
        mVodPlayer = new TXVodPlayer(this);
        TextureView textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        mVodPlayer.setSurface(new Surface(surface));
        mVodPlayer.startVodPlay(Constants.DEMO_PLAY_MP4_URL);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        mVodPlayer.stopPlay(false);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

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