package com.tencent.player.voddownloadandplay.download;

import com.tencent.player.entity.SuperPlayerModel;

/**
 * model for download
 */
public class VideoDownloadModel {

    private SuperPlayerModel playerModel;

    private int qualityId;

    private String userName;

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
}
