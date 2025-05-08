package com.tencent.liteav.demo.player.demo.tuishortvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUILivePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVideoRenderView;

public class TUIShortVideoLiveActivity extends AppCompatActivity {

    public static final int LIVE_CALL_BACK = 1001;
    private static ITUILivePlayer currentLivePlayer;
    private static boolean isStarting = false;

    public static void startLiveActivity(ITUILivePlayer player, Context context) {
        if (!isStarting) {
            isStarting = true;
            currentLivePlayer = player;
            Intent intent = new Intent(context, TUIShortVideoLiveActivity.class);
            ((Activity) context).startActivityForResult(intent, 0);
            isStarting = false;
        }
    }

    private TUIVideoRenderView mVideoView;
    private ImageView mFullscreenBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuishort_video_live);
        findViewById(R.id.ib_back_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mVideoView = findViewById(R.id.txc_vv_live);
        mFullscreenBtn = findViewById(R.id.iv_tui_fullscreen);
        mFullscreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TUILandScapeActivity.startLandScapeActivity(currentLivePlayer, TUIShortVideoLiveActivity.this);
            }
        });
        applyPlayRenderView();
    }

    private void applyPlayRenderView() {
        if (null != currentLivePlayer) {
            if (!currentLivePlayer.isPlaying()) {
                currentLivePlayer.resumePlay();
            }
            currentLivePlayer.setDisplayView(mVideoView);
        } else {
            Toast.makeText(getApplicationContext(), "lose player", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TUILandScapeActivity.LAND_SCAPE_CALL_BACK) {
            mVideoView.handleRenderRecycle();
            applyPlayRenderView();
        }
    }

    @Override
    public void finish() {
        if (null != currentLivePlayer) {
            currentLivePlayer = null;
        }
        setResult(LIVE_CALL_BACK);
        super.finish();
    }
}