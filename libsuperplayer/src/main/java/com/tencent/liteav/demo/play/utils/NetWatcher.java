package com.tencent.liteav.demo.play.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLog;

import java.lang.ref.WeakReference;

/**
 *
 */

public class NetWatcher {

    final static int WATCH_TIME = 30000;
    final static int MAX_LOADING_COUNT = 3;
    final static int MAX_LOADING_TIME = 10000;
    private WeakReference<Context> mContext;
    private WeakReference<TXLivePlayer> mLivePlayer;
    private int mLoadingCount = 0;
    private String mPlayUrl = "";
    private long mLoadingTime = 0;
    private long mLoadingStartTime = 0;
    private boolean mWatching;
    public NetWatcher(Context context) {
        mContext = new WeakReference<>(context);
    }

    public void start(String playUrl, TXLivePlayer player) {
        mWatching = true;
        mLivePlayer = new WeakReference<>(player);
        mPlayUrl = playUrl;
        mLoadingCount = 0;
        mLoadingTime= 0;
        mLoadingStartTime = 0;
        TXLog.w("NetWatcher", "net check start watch ");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TXLog.w("NetWatcher", "net check loading count = "+mLoadingCount+" loading time = "+mLoadingTime);
                if (mLoadingCount >= MAX_LOADING_COUNT || mLoadingTime >= MAX_LOADING_TIME) {
                    showSwitchStreamDialog();
                }
                mLoadingCount = 0;
                mLoadingTime = 0;
            }
        }, WATCH_TIME);
    }

    public void stop() {
        mWatching = false;
        mLoadingCount = 0;
        mLoadingTime= 0;
        mLoadingStartTime = 0;
        mPlayUrl = "";
        mLivePlayer = null;
        TXLog.w("NetWatcher", "net check stop watch");
    }

    public void enterLoading() {
        if (mWatching) {
            mLoadingCount++;
            mLoadingStartTime = System.currentTimeMillis();
        }
    }

    public void exitLoading() {
        if (mWatching) {
            if (mLoadingStartTime != 0) {
                mLoadingTime += System.currentTimeMillis() - mLoadingStartTime;
                mLoadingStartTime = 0;
            }
        }
    }

    private void showSwitchStreamDialog() {
        final Context context = mContext.get();
        if (context == null) return;
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage("检测到您的网络较差，建议切换清晰度");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TXLivePlayer player = mLivePlayer!=null ? mLivePlayer.get() : null;
                        String videoUrl = mPlayUrl.replace(".flv","_900.flv");
                        if (player != null && !TextUtils.isEmpty(videoUrl)) {
                            int result = player.switchStream(videoUrl);
                            if (result < 0) {
                                Toast.makeText(context,"切换高清清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context,"正在为您切换为高清清晰度，请稍候...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
