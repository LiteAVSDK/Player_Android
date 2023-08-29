package com.tencent.liteav.demo.superplayer.model.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.rtmp.TXLivePlayer;

import java.lang.ref.WeakReference;

/**
 * Network quality monitoring tool.
 * <p>
 * When the loading count is greater than or equal to 3, prompt the user to switch to low video quality
 *
 * 网络质量监视工具
 * <p>
 * 当loading次数大于等于3次时，提示用户切换到低清晰度
 */
public class NetWatcher {

    private final static int WATCH_TIME        = 30000;
    private final static int MAX_LOADING_TIME  = 10000;
    private final static int MAX_LOADING_COUNT = 3;

    private WeakReference<Context>      mContext;
    private WeakReference<TXLivePlayer> mLivePlayer;
    private String                      mPlayURL          = "";
    private int                         mLoadingCount     = 0;
    private long                        mLoadingTime      = 0;
    private long                        mLoadingStartTime = 0;
    private boolean                     mWatching;

    public NetWatcher(Context context) {
        mContext = new WeakReference<>(context);
    }

    /**
     * Start monitoring the network
     *
     * 开始监控网络
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
     * Stop monitoring
     *
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
     * Start loading timer
     *
     * 开始loading计时
     */
    public void enterLoading() {
        if (mWatching) {
            mLoadingCount++;
            mLoadingStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Stop loading timer
     *
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
     * Pop up the prompt to switch video quality
     *
     * 弹出切换清晰度的提示框
     */
    private void showSwitchStreamDialog() {
        final Context context = mContext.get();
        if (context == null) {
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(context.getString(R.string.superplayer_weak_net_tip));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TXLivePlayer player = mLivePlayer != null ? mLivePlayer.get() : null;
                        String videoUrl = mPlayURL.replace(".flv", "_900.flv");
                        if (player != null && !TextUtils.isEmpty(videoUrl)) {
                            int result = player.switchStream(videoUrl);
                            if (result < 0) {
                                Toast.makeText(context,
                                        context.getString(R.string.superplayer_switch_high_quality_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context,
                                        context.getString(R.string.superplayer_switching_high_quality),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
