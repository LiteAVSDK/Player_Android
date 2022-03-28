package com.tencent.liteav.demo.superplayer.model;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.List;

public interface SuperPlayer {


    /**
     * 开始播放
     *
     * @param model 超级播放器模型
     */
    void play(SuperPlayerModel model);


    /**
     * 重播
     */
    void reStart();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 暂停点播视频
     */
    void pauseVod();

    /**
     * 恢复播放
     */
    void resume();

    /**
     * 恢复直播播放，从直播时移播放中，恢复到直播播放。
     */
    void resumeLive();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 销毁播放器
     */
    void destroy();

    void reset();

    void revertSettings();

    /**
     * 切换播放器模式
     *
     * @param playerMode {@link SuperPlayerDef.PlayerMode#WINDOW  }          窗口模式
     *                   {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }      全屏模式
     *                   {@link SuperPlayerDef.PlayerMode#FLOAT  }           悬浮窗模式
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
     * 获取当前播放器模式
     *
     * @return {@link SuperPlayerDef.PlayerMode#WINDOW  }          窗口模式
     * {@link SuperPlayerDef.PlayerMode#FULLSCREEN  }              全屏模式
     * {@link SuperPlayerDef.PlayerMode#FLOAT  }                   悬浮窗模式
     */
    SuperPlayerDef.PlayerMode getPlayerMode();

    /**
     * 获取当前播放器状态
     *
     * @return {@link SuperPlayerDef.PlayerState#PLAYING  }     播放中
     * {@link SuperPlayerDef.PlayerState#PAUSE  }               暂停中
     * {@link SuperPlayerDef.PlayerState#LOADING  }             缓冲中
     * {@link SuperPlayerDef.PlayerState#END  }                 结束播放
     */
    SuperPlayerDef.PlayerState getPlayerState();

    /**
     * 获取当前播放器类型
     *
     * @return {@link SuperPlayerDef.PlayerType#LIVE  }     直播
     * {@link SuperPlayerDef.PlayerType#LIVE_SHIFT  }       直播时移
     * {@link SuperPlayerDef.PlayerType#VOD  }              点播
     */
    SuperPlayerDef.PlayerType getPlayerType();

    /**
     * 设置播放器状态回调
     *
     * @param observer {@link SuperPlayerObserver}
     */
    void setObserver(SuperPlayerObserver observer);

    /**
     * 设置超级播放器中点播事件和直播事件的回调
     * @param superPlayerListener
     */
    void setSuperPlayerListener(ISuperPlayerListener superPlayerListener);

    /**
     * 设置是否循环
     * @param isLoop true循环，false不循环
     */
    void setLoop(boolean isLoop);

    /**
     * 设置开始时间
     * @param startPos 开始时间
     */
    void setStartTime(float startPos);

    /**
     * 设置是否自动播放
     * @param isAutoPlay true自动播放，false不自动播放
     */
    void setAutoPlay(boolean isAutoPlay);

    void setNeedToPause(boolean value);
}