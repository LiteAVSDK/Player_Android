package com.coolapk.superplayertest.drm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.coolapk.superplayertest.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.play.SuperPlayerModel;
import com.tencent.liteav.demo.play.SuperPlayerView;
import com.tencent.liteav.demo.play.v3.SuperPlayerVideoId;
import com.tencent.rtmp.TXLiveConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DRMActivity extends Activity implements SuperPlayerView.PlayerViewCallback {
    private SuperPlayerView mSuperPlayerView;
    private static final String TAG = "DRMActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        mSuperPlayerView = findViewById(R.id.main_super_player_view);
        mSuperPlayerView.setPlayerViewCallback(this);
        // 播放器配置
        SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
        // 开启悬浮窗播放
        prefs.enableFloatWindow = true;
        // 设置悬浮窗的初始位置和宽高
        SuperPlayerGlobalConfig.TXRect rect = new SuperPlayerGlobalConfig.TXRect();
        rect.x = 0;
        rect.y = 0;
        rect.width = 810;
        rect.height = 540;
        prefs.floatViewRect = rect;
        // 播放器默认缓存个数
        prefs.maxCacheItem = 5;
        // 设置播放器渲染模式
        prefs.enableHWAcceleration = true;
        prefs.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;

        // 通过URL方式的视频信息配置
        /**
         * 如果需要播放加密后的视频，需要使用腾讯云FileId与DRM服务，获取token后即可通过播放器播放
         *
         * 以下是一个简单的demo
         */
        final String fileId = "15517827183850370616";
        String testTokenURL = "https://demo.vod2.myqcloud.com/drm/gettoken?fileId=" + fileId; // 替换成您业务的服务器，获取token
        // 发起网络请求，获取Token
        HttpURLClient.getInstance().get(testTokenURL, new HttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String token) {
                try {
                    Log.i(TAG, "onSuccess: token = " + token);
                    // Token需要进行URLEncoder
                    String encodedToken = URLEncoder.encode(token, "UTF-8");
                    SuperPlayerModel model = new SuperPlayerModel();
                    model.appId = 1253039488;
                    model.token = encodedToken;

                    model.videoId = new SuperPlayerVideoId();
                    model.videoId.fileId = fileId;
                    model.videoId.version = SuperPlayerVideoId.FILE_ID_V3;// DRM需要使用V3协议
                    model.videoId.playDefinition = "20";

                    mSuperPlayerView.playWithModel(model);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError() {
                Toast.makeText(DRMActivity.this, "获取Token失败,播放失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 重新开始播放
        if (mSuperPlayerView.getPlayState() == SuperPlayerConst.PLAYSTATE_PLAY) {
            mSuperPlayerView.onResume();
            if (mSuperPlayerView.getPlayMode() == SuperPlayerConst.PLAYMODE_FLOAT) {
                mSuperPlayerView.requestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止播放
        if (mSuperPlayerView.getPlayMode() != SuperPlayerConst.PLAYMODE_FLOAT) {
            mSuperPlayerView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放
        mSuperPlayerView.release();
        if (mSuperPlayerView.getPlayMode() != SuperPlayerConst.PLAYMODE_FLOAT) {
            mSuperPlayerView.resetPlayer();
        }
    }

    @Override
    public void hideViews() {

    }

    @Override
    public void showViews() {

    }

    @Override
    public void onQuit(int playMode) {
        if (playMode == SuperPlayerConst.PLAYMODE_FLOAT) {
            mSuperPlayerView.resetPlayer();
            finish();
        } else if (playMode == SuperPlayerConst.PLAYMODE_WINDOW) {
            if (mSuperPlayerView.getPlayState() == SuperPlayerConst.PLAYSTATE_PLAY) {
//                // 返回桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            } else {
                mSuperPlayerView.resetPlayer();
                finish();
            }
        }
    }
}
