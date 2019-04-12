package com.coolapk.superplayertest.drm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DRMActivity extends Activity implements SuperPlayerView.OnSuperPlayerViewCallback {
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
        final String fileId = "5285890787511552106";
        final int appId = 1256468886;
        String testTokenURL = "https://demo.vod2.myqcloud.com/drm/gettoken?fileId=" + fileId + "&appId=" + appId; // 替换成您业务的服务器，获取token
        // 发起网络请求，获取Token
        HttpURLClient.getInstance().get(testTokenURL, new HttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String token) {
                try {
                    Log.i(TAG, "onSuccess: token = " + token);
                    // Token需要进行URLEncoder
                    String encodedToken = URLEncoder.encode(token, "UTF-8");
                    SuperPlayerModel model = new SuperPlayerModel();
                    model.appId = appId;
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
    public void onStartFullScreenPlay() {
    }

    @Override
    public void onStopFullScreenPlay() {
    }

    @Override
    public void onClickFloatCloseBtn() {
        // 点击悬浮窗关闭按钮，那么结束整个播放
        mSuperPlayerView.resetPlayer();
        finish();
    }

    @Override
    public void onClickSmallReturnBtn() {
        // 点击小窗模式下返回按钮，开始悬浮播放
        showFloatWindow();
    }

    @Override
    public void onStartFloatWindowPlay() {
        // 开始悬浮播放后，直接返回到桌面，进行悬浮播放
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * 悬浮窗播放
     */
    private void showFloatWindow() {
        if (mSuperPlayerView.getPlayState() == SuperPlayerConst.PLAYSTATE_PLAY) {
            mSuperPlayerView.requestPlayMode(SuperPlayerConst.PLAYMODE_FLOAT);
        } else {
            mSuperPlayerView.resetPlayer();
            finish();
        }
    }
}
