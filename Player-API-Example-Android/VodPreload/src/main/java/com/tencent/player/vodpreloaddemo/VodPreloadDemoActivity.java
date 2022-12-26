package com.tencent.player.vodpreloaddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.tencent.player.common.Constants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.downloader.ITXVodPreloadListener;
import com.tencent.rtmp.downloader.TXVodPreloadManager;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;

/**
 * see: https://cloud.tencent.com/document/product/881/20216#2.E3.80.81.E8.A7.86.E9.A2.91.E9.A2.84.E4.B8.8B.E8.BD.BD
 */
public class VodPreloadDemoActivity extends AppCompatActivity {

    private TXCloudVideoView mPlayerView;
    private TXVodPlayer mVodPlayer;

    private TXVodPreloadManager mDownloadManager;
    private int mCurrTaskID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_preload_demo);

        initVodPreload();

        mPlayerView = findViewById(R.id.video_view);
        mVodPlayer = new TXVodPlayer(this);
        mVodPlayer.setPlayerView(mPlayerView);
        mVodPlayer.startVodPlay(Constants.DEMO_PLAY_M3U8_URL);
    }



    @Override
    protected void onDestroy() {
        //取消预下载
        mDownloadManager.stopPreload(mCurrTaskID);

        mVodPlayer.stopPlay(false);
        super.onDestroy();
    }


    private void initVodPreload() {
        //先设置播放引擎的全局缓存目录和缓存大小
        File sdcardDir = getApplicationContext().getExternalFilesDir(null);
        //设置播放引擎的全局缓存目录和缓存大小
        if (sdcardDir != null) {
            TXPlayerGlobalSetting.setCacheFolderPath(sdcardDir.getPath() + "/PlayerCache");
            TXPlayerGlobalSetting.setMaxCacheSize(200); //单位MB
        }

        //启动预下载
        mDownloadManager = TXVodPreloadManager.getInstance(getApplicationContext());
        mCurrTaskID= mDownloadManager.startPreload(Constants.DEMO_PLAY_M3U8_URL, 3, 1920*1080, new ITXVodPreloadListener() {
            @Override
            public void onComplete(int taskID, String url) {
                Log.d(Constants.TAG, "preload: onComplete: taskID: " + taskID +  ", url: " + url);
            }

            @Override
            public void onError(int taskID, String url, int code, String msg) {
                Log.d(Constants.TAG, "preload: onError:  taskID: " + taskID +  ", url: " + url + ", code: " + code + ", msg: " + msg);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVodPlayer.pause();
    }

}