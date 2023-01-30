package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.liteav.demo.vodcommon.entity.ConfigBean;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;

import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_BEGIN;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_END;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_PROGRESS;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED;

public class TXVodPlayerWrapper implements ITXVodPlayListener {
    private static final String TAG = "ShortVideoDemo:TXVodPlayerWrapper";
    private TXVodPlayer mVodPlayer;
    private OnPlayEventChangedListener mOnPlayEventChangedListener;
    private TxVodStatus mStatus;
    private String mUrl;
    private boolean mStartOnPrepare;
    private TXVodPlayConfig mTXVodPlayConfig;

    public TXVodPlayerWrapper(Context context) {
        mVodPlayer = new TXVodPlayer(context);
        mVodPlayer.setVodListener(this);
        if (ConfigBean.getInstance().isIsUseDash()) {
            mVodPlayer.setRenderMode(TXVodConstants.RENDER_MODE_ADJUST_RESOLUTION);
        } else {
            mVodPlayer.setRenderMode(TXVodConstants.RENDER_MODE_FULL_FILL_SCREEN);
        }
        mTXVodPlayConfig = new TXVodPlayConfig();
        mTXVodPlayConfig.setProgressInterval(1);
        mTXVodPlayConfig.setSmoothSwitchBitrate(true);
        mTXVodPlayConfig.setMaxBufferSize(5);
        mTXVodPlayConfig.setPreferredResolution(1080 * 1920);
        mTXVodPlayConfig.setMaxCacheItems(8);
        if (TXPlayerGlobalSetting.getCacheFolderPath() == null
                || TXPlayerGlobalSetting.getCacheFolderPath().equals("")) {
            File sdcardDir = context.getExternalFilesDir(null);
            TXPlayerGlobalSetting.setCacheFolderPath(sdcardDir.getPath() + "/txcache");
        }
        mVodPlayer.setConfig(mTXVodPlayConfig);
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle bundle) {
        switch (event) {
            case PLAY_EVT_VOD_PLAY_PREPARED:
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED);
                Log.i(TAG, "[onPlayEvent] , startOnPrepare，" + mStartOnPrepare + "，mVodPlayer "
                        + mVodPlayer.hashCode() + " mUrl " + mUrl);
                if (mStartOnPrepare) {
                    mVodPlayer.resume();
                    mStartOnPrepare = false;
                    playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING);
                }
                break;
            case PLAY_EVT_RCV_FIRST_I_FRAME:
                if (mOnPlayEventChangedListener != null) {
                    mOnPlayEventChangedListener.onRcvFirstFrame();
                }
                break;
            case PLAY_EVT_PLAY_PROGRESS:
                if (mOnPlayEventChangedListener != null) {
                    mOnPlayEventChangedListener.onProgress(bundle);
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
        Log.i(TAG, "[resumePlay] , startOnPrepare， " + mStartOnPrepare
                + " mVodPlayer " + mVodPlayer.hashCode() + " url " + mUrl);
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED) {
            mVodPlayer.setAutoPlay(true);
            mVodPlayer.startVodPlay(mUrl);
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING);
            return;
        }
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED
                || mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PAUSED) {
            mVodPlayer.resume();
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING);
        } else {
            mStartOnPrepare = true;
        }
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
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED);
        }
    }

    public void stopPlay() {
        mVodPlayer.stopPlay(true);
        playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED);
    }

    public void setPlayerView(TXCloudVideoView txCloudVideoView) {
        mVodPlayer.setPlayerView(txCloudVideoView);
    }


    public void preStartPlay(VideoModel bean) {
        this.mUrl = bean.videoURL;
        this.mStatus = TxVodStatus.TX_VIDEO_PLAYER_STATUS_UNLOAD;
        mStartOnPrepare = false;
        mVodPlayer.setLoop(true);
        mVodPlayer.stopPlay(true);
        Log.i(TAG, "[preStartPlay] , startOnPrepare ，" + mStartOnPrepare + "， mVodPlayer " + mVodPlayer.hashCode());
        mVodPlayer.setAutoPlay(false);
        mVodPlayer.startVodPlay(bean.videoURL);
    }

    private void playerStatusChanged(TxVodStatus status) {
        this.mStatus = status;
        Log.i(TAG, " [playerStatusChanged] mVodPlayer" + mVodPlayer.hashCode() + " mStatus " + mStatus);
    }

    public void setVodChangeListener(OnPlayEventChangedListener listener) {
        mOnPlayEventChangedListener = listener;
    }

    public enum TxVodStatus {
        TX_VIDEO_PLAYER_STATUS_UNLOAD,      // 未加载
        TX_VIDEO_PLAYER_STATUS_PREPARED,    // 准备播放
        TX_VIDEO_PLAYER_STATUS_LOADING,     // 加载中
        TX_VIDEO_PLAYER_STATUS_PLAYING,     // 播放中
        TX_VIDEO_PLAYER_STATUS_PAUSED,      // 暂停
        TX_VIDEO_PLAYER_STATUS_ENDED,       // 播放完成
        TX_VIDEO_PLAYER_STATUS_STOPPED      // 手动停止播放
    }

    public interface OnPlayEventChangedListener {
        void onProgress(Bundle bundle);

        void onRcvFirstFrame();
    }

    public TXVodPlayer getVodPlayer() {
        return mVodPlayer;
    }

    public String getUrl() {
        return mUrl;
    }
}
