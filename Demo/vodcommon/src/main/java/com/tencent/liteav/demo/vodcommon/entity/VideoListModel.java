package com.tencent.liteav.demo.vodcommon.entity;

import java.util.ArrayList;
import java.util.List;

public class VideoListModel {
    public List<VideoModel> videoModelList = new ArrayList<>();
    public String title;
    public String  icon;
    // 是否可以离线缓存
    public boolean isEnableDownload;

    public void addVideoModel(VideoModel videoModel) {
        videoModelList.add(videoModel);
    }
}
