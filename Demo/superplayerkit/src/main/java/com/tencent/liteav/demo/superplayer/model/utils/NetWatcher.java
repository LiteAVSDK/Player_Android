package com.tencent.liteav.demo.superplayer.model.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.rtmp.TXLivePlayer;

import java.lang.ref.WeakReference;

/**
 * 网络质量监视工具
 * <p>
 * 当loading次数大于等于3次时，提示用户切换到低清晰度
 */

public class NetWatcher {

    private final static int WATCH_TIME        = 30000; // 监控总时长ms
    private final static int MAX_LOADING_TIME  = 10000; // 一次loading的判定时长ms
    private final static int MAX_LOADING_COUNT = 3;     // 弹出切换清晰度提示框的loading总次数

    private WeakReference<Context>      mContext;
    private WeakReference<TXLivePlayer> mLivePlayer;    // 直播播放器
    private String                      mPlayURL          = "";    // 播放的url
    private int                         mLoadingCount     = 0;     // 记录loading次数
    private long                        mLoadingTime      = 0;     // 记录单次loading的时长
    private long                        mLoadingStartTime = 0;     // loading开始的时间
    private boolean                     mWatching;              // 是否正在监控

    public NetWatcher(Context context) {
        mContext = new WeakReference<>(context);
    }

    /**
     * 开始监控网络
     *
     * @param playUrl 播放的url
     * @param player  播放器
     */
    public void start(String playUrl, TXLivePlayer player) {
        mWatching = true;
        mLivePlayer = new WeakReference<>(player);
        mPlayURL = playUrl;
        mLoadingCount = 0;
        mLoadingTime = 0;
        mLoadingStartTime = 0;
        Log.w("NetWatcher", "net check start watch ");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.w("NetWatcher", "net check loading count = " + mLoadingCount + " loading time = " + mLoadingTime);
                if (mLoadingCount >= MAX_LOADING_COUNT || mLoadingTime >= MAX_LOADING_TIME) {
                    showSwitchStreamDialog();
                }
                mLoadingCount = 0;
                mLoadingTime = 0;
            }
        }, WATCH_TIME);
    }

    /**
     * 停止监控
     */
    public void stop() {
        mWatching = false;
        mLoadingCount = 0;
        mLoadingTime = 0;
        mLoadingStartTime = 0;
        mPlayURL = "";
        mLivePlayer = null;
        Log.w("NetWatcher", "net check stop watch");
    }

    /**
     * 开始loading计时
     */
    public void enterLoading() {
        if (mWatching) {
            mLoadingCount++;
            mLoadingStartTime = System.currentTimeMillis();
        }
    }

    /**
     * 结束loading计时
     */
    public void exitLoading() {
        if (mWatching) {
            if (mLoadingStartTime != 0) {
                mLoadingTime += System.currentTimeMillis() - mLoadingStartTime;
                mLoadingStartTime = 0;
            }
        }
    }

    /**
     * 弹出切换清晰度的提示框
     */
    private void showSwitchStreamDialog() {
        final Context context = mContext.get();
        if (context == null) return;
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage("检测到您的网络较差，建议切换清晰度");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TXLivePlayer player = mLivePlayer != null ? mLivePlayer.get() : null;
                        String videoUrl = mPlayURL.replace(".flv", "_900.flv");
                        if (player != null && !TextUtils.isEmpty(videoUrl)) {
                            int result = player.switchStream(videoUrl);
                            if (result < 0) {
                                Toast.makeText(context, "切换高清清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "正在为您切换为高清清晰度，请稍候...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
