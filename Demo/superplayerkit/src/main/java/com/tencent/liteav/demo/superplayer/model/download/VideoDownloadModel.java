package com.tencent.liteav.demo.superplayer.model.download;

import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;

/**
 * model for download
 */
public class VideoDownloadModel {

    private SuperPlayerModel playerModel;

    private int qualityId;

    private String userName;

    private long mPreferResolution = SuperPlayerGlobalConfig.getInstance().preferResolution;

    public SuperPlayerModel getPlayerModel() {
        return playerModel;
    }

    public void setPlayerModel(SuperPlayerModel playerModel) {
        this.playerModel = playerModel;
    }

    public int getQualityId() {
        return qualityId;
    }

    public void setQualityId(int qualityId) {
        this.qualityId = qualityId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPreferResolution(long resolution) {
        mPreferResolution = resolution;
    }

    public long getPreferResolution() {
        return mPreferResolution;
    }
}
