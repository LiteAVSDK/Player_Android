package com.tencent.liteav.demo.superplayer.model;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXTrackInfo;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.rtmp.ui.TXSubtitleView;

public interface SuperPlayer {


    /**
     * Start playing.
     *
     * 开始播放
     */
    void play(SuperPlayerModel model);


    /**
     * Replay
     *
     * 重播
     */
    void reStart();

    /**
     * Pause playback
     *
     * 暂停播放
     */
    void pause();

    /**
     * Pause on-demand video
     *
     * 暂停点播视频
     */
    void pauseVod();

    /**
     * Resume playback
     *
     * 恢复播放
     */
    void resume();

    /**
     * Resume live playback, from live time shift playback to live playback
     *
     * 恢复直播播放，从直播时移播放中，恢复到直播播放。
     */
    void resumeLive();

    /**
     * Stop playing
     *
     * 停止播放
     */
    void stop();

    /**
     * Destroy the player
     *
     * 销毁播放器
     */
    void destroy();

    void reset();

    void revertSettings();

    /**
     * Switch player mode.
     *
     * 切换播放器模式
     *
     * @param playerMode {@link SuperPlayerDef.PlayerMode#WINDOW  }          Window mode
     *                   {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }      Full screen mode
     *                   {@link SuperPlayerDef.PlayerMode#FLOAT  }           Floating window mode
     */
    void switchPlayMode(SuperPlayerDef.PlayerMode playerMode);

    void enableHardwareDecode(boolean enable);

    void setPlayerView(TXCloudVideoView videoView);

    void seek(int position);

    void snapshot(TXLivePlayer.ITXSnapshotListener listener);

    void setRate(float speedLevel);

    void setMirror(boolean isMirror);

    void switchStream(VideoQuality quality);

    String getPlayURL();

    /**
     * Get the total video duration of the current vodPlayer
     *
     * 获取当前点播播放器视频总时长
     */
    float getVodDuration();

    /**
     * Get the current player mode
     *
     * 获取当前播放器模式
     *
     * @return {@link SuperPlayerDef.PlayerMode#WINDOW  }          Window mode
     * {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }              Full screen mode
     * {@link SuperPlayerDef.PlayerMode#FLOAT  }                   Floating window mode for live time shift
     */
    SuperPlayerDef.PlayerMode getPlayerMode();

    /**
     * Get the current player status
     *
     * 获取当前播放器状态
     *
     * @return {@link SuperPlayerDef.PlayerState#PLAYING  }     Playing
     * {@link SuperPlayerDef.PlayerState#PAUSE  }               Paused
     * {@link SuperPlayerDef.PlayerState#LOADING  }             Buffering
     * {@link SuperPlayerDef.PlayerState#END  }                 Playback ended
     */
    SuperPlayerDef.PlayerState getPlayerState();

    /**
     * Get the current player type
     *
     * 获取当前播放器类型
     *
     * @return {@link SuperPlayerDef.PlayerType#LIVE  }     Live
     * {@link SuperPlayerDef.PlayerType#LIVE_SHIFT  }       Live time shift
     * {@link SuperPlayerDef.PlayerType#VOD  }              vod
     */
    SuperPlayerDef.PlayerType getPlayerType();

    /**
     * Set player status callback
     *
     * 设置播放器状态回调
     *
     * @param observer {@link SuperPlayerObserver}
     */
    void setObserver(SuperPlayerObserver observer);

    /**
     * Set callbacks for on-demand events and live events in the super player
     *
     * 设置超级播放器中点播事件和直播事件的回调
     */
    void setSuperPlayerListener(ISuperPlayerListener superPlayerListener);

    /**
     * Set looping
     *
     * 设置是否循环
     * @param isLoop True for looping, false for non-looping.
     *               true循环，false不循环
     */
    void setLoop(boolean isLoop);

    /**
     * Set start time
     *
     * 设置开始时间
     */
    void setStartTime(float startPos);

    /**
     * Set autoplay
     *
     * 设置是否自动播放
     */
    void setAutoPlay(boolean isAutoPlay);

    void setNeedToPause(boolean value);

    void onClickSoundTrackItem(TXTrackInfo clickInfo);

    void onClickSubTitleItem(TXTrackInfo clickInfo);

    void setSubTitleView(TXSubtitleView subTitleView);

    void onSubtitleSettingDone(TXSubtitleRenderModel model);

    /**
     * Seek backwards and play
     *
     * 往回seek并且播放
     */
    void playBackward(int position);

    /**
     * Fast forward
     *
     * 往前倍速播放
     */
    void playForward();

    /**
     * Restore playback speed and play
     *
     * 恢复倍速和播放
     */
    void revertSpeedRate();
}
