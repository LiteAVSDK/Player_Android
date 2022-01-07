package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;

import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_BEGIN;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_END;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_PROGRESS;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED;

public class TXVodPlayerWrapper implements ITXVodPlayListener {
    private static final String TAG = "ShortVideoDemo:TXVodPlayerWrapper";
    private TXVodPlayer mVodPlayer;
    private ISeekBarChangeListener mSeekBarChangeListener;
    private TxVodStatus mStatus;
    private String mUrl;
    private boolean mStartOnPrepare;
    private TXVodPlayConfig mTXVodPlayConfig;

    public TXVodPlayerWrapper(Context context) {
        mVodPlayer = new TXVodPlayer(context);
        mVodPlayer.setVodListener(this);
        mTXVodPlayConfig = new TXVodPlayConfig();
        mTXVodPlayConfig.setProgressInterval(1);
        mTXVodPlayConfig.setSmoothSwitchBitrate(true);
        mTXVodPlayConfig.setMaxBufferSize(5);
        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir != null) {
            mTXVodPlayConfig.setCacheFolderPath(sdcardDir.getPath() + "/txcache");
        }
        mVodPlayer.setConfig(mTXVodPlayConfig);
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle bundle) {
        switch (event) {
            case PLAY_EVT_VOD_PLAY_PREPARED:
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED);
                Log.i(TAG, "[onPlayEvent] , startOnPrepare，" + mStartOnPrepare + "，mVodPlayer " + mVodPlayer.hashCode()+" mUrl " +mUrl);
                if (mStartOnPrepare) {
                    mVodPlayer.resume();
                    mStartOnPrepare = false;
                    playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING);
                }
                break;
            case PLAY_EVT_PLAY_BEGIN:
                Log.i(TAG, "[onPlayEvent] , PLAY_EVT_PLAY_BEGIN，" + mVodPlayer.hashCode() + ",url " + mUrl);
                break;
            case PLAY_EVT_PLAY_PROGRESS:
                if (mSeekBarChangeListener != null) {
                    mSeekBarChangeListener.seekbarChanged(bundle);
                }
                break;
            case PLAY_EVT_PLAY_END:
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_ENDED);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }


    public void pausePlay() {
        mVodPlayer.pause();
        playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PAUSED);
    }


    public void resumePlay() {
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED || mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PAUSED) {
            mVodPlayer.resume();
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING);
        } else {
            mStartOnPrepare = true;
        }
        Log.i(TAG, "[resumePlay] , startOnPrepare， " + mStartOnPrepare + " mVodPlayer " + mVodPlayer.hashCode() + " url " + mUrl);
    }


    public void seekTo(int time) {
        mVodPlayer.seek(time);
    }


    public boolean isPlaying() {
        return mVodPlayer.isPlaying();
    }


    public void stopForPlaying() {
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING) {
            mVodPlayer.stopPlay(true);
        }
    }

    public void stopPlay() {
        mVodPlayer.stopPlay(true);
    }

    public void setPlayerView(TXCloudVideoView txCloudVideoView) {
        mVodPlayer.setPlayerView(txCloudVideoView);
    }


    public void preStartPlay(ShortVideoBean bean) {
        this.mUrl = bean.videoURL;
        this.mStatus = TxVodStatus.TX_VIDEO_PLAYER_STATUS_UNLOAD;
        mStartOnPrepare = false;
        mVodPlayer.setLoop(true);
        mVodPlayer.stopPlay(true);
        Log.i(TAG, "[preStartPlay] , startOnPrepare ，" + mStartOnPrepare + "， mVodPlayer " + mVodPlayer.hashCode());
        mVodPlayer.setAutoPlay(false);
        mVodPlayer.setBitrateIndex(bean.bitRateIndex);
        mVodPlayer.startPlay(bean.videoURL);
    }

    private void playerStatusChanged(TxVodStatus status) {
        this.mStatus = status;
        Log.i(TAG," [playerStatusChanged] mVodPlayer" +mVodPlayer.hashCode()+" mStatus "+mStatus );
    }

    public void setVodChangeListener(ISeekBarChangeListener listener) {
        mSeekBarChangeListener = listener;

    }

    public enum TxVodStatus {
        TX_VIDEO_PLAYER_STATUS_UNLOAD,      // 未加载
        TX_VIDEO_PLAYER_STATUS_PREPARED,    // 准备播放
        TX_VIDEO_PLAYER_STATUS_LOADING,     // 加载中
        TX_VIDEO_PLAYER_STATUS_PLAYING,     // 播放中
        TX_VIDEO_PLAYER_STATUS_PAUSED,      // 暂停
        TX_VIDEO_PLAYER_STATUS_ENDED,       // 播放完成
    }

    public interface ISeekBarChangeListener {
        void seekbarChanged(Bundle bundle);
    }

    public TXVodPlayer getVodPlayer() {
        return mVodPlayer;
    }

    public String getUrl() {
        return mUrl;
    }
}
