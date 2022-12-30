package com.tencent.liteav.demo.vodcommon.entity;


import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;

import android.text.TextUtils;

import com.tencent.liteav.demo.superplayer.SubtitleSourceModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuejiaoli on 2018/7/4.
 */

public class VideoModel implements Serializable {

    /**
     * 视频标题
     */
    public String title;

    /**
     * 视频URL
     */
    public String videoURL;


    /**
     * 从服务器拉取的封面图片
     */
    public String placeholderImage;


    /**
     * 用户设置图片的接口 如果是本地图片前面加file://
     */
    public String coverPictureUrl;

    /**
     * 视频时长
     */
    public int duration;

    /**
     * appId
     */
    public int appid;

    /**
     * 视频的fileid
     */
    public String fileid;

    /**
     * 签名字串
     */
    public String pSign;

    public int playAction = PLAY_ACTION_AUTO_PLAY;

    public List<VideoQuality> videoQualityList = new ArrayList<>();

    /**
     * 外挂字幕
     */
    public List<SubtitleSourceModel> subtitleSourceModelList = new ArrayList<>();

    /**
     * VIDEO 不同清晰度的URL链接
     */
    public List<VideoPlayerURL> multiVideoURLs;
    public int                  playDefaultIndex; // 指定多码率情况下，默认播放的连接Index
    public VipWatchModel        vipWatchModel = null;

    //feed流视频描述信息
    public String             videoDescription     = null;
    public String             videoMoreDescription = null;
    /**
     * 动态水印文本
     */
    public DynamicWaterConfig dynamicWaterConfig   = null;

    /**
     * 是否启用缓存下载能力
     */
    public boolean isEnableDownload = false;


    public static class VideoPlayerURL implements  Serializable {

        public VideoPlayerURL() {
        }

        public VideoPlayerURL(String title, String url) {
            this.title = title;
            this.url = url;
        }

        /**
         * 视频标题
         */
        public String title;

        /**
         * 视频URL
         */
        public String url;

        @Override
        public String toString() {
            return "SuperPlayerUrl{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public SuperPlayerModel convertToSuperPlayerModel() {
        SuperPlayerModel superPlayerModel = new SuperPlayerModel();
        superPlayerModel.appId = appid;
        superPlayerModel.vipWatchMode = vipWatchModel;
        if (dynamicWaterConfig != null) {
            superPlayerModel.dynamicWaterConfig = dynamicWaterConfig;
        }
        if (!TextUtils.isEmpty(videoURL)) {
            if (isSuperPlayerVideo()) {
                return transToSuperPlayerVideo();
            } else {
                superPlayerModel.title = title;
                superPlayerModel.url = videoURL;

                superPlayerModel.multiURLs = new ArrayList<>();
                if (multiVideoURLs != null) {
                    for (VideoModel.VideoPlayerURL modelURL : multiVideoURLs) {
                        superPlayerModel.multiURLs.add(new SuperPlayerModel.SuperPlayerURL(modelURL.url,
                                modelURL.title));
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(fileid)) {
            superPlayerModel.videoId = new SuperPlayerVideoId();
            superPlayerModel.videoId.fileId = fileid;
            superPlayerModel.videoId.pSign = pSign;
        }

        superPlayerModel.subtitleSourceModelList = subtitleSourceModelList;

        superPlayerModel.playAction = playAction;
        superPlayerModel.placeholderImage = placeholderImage;
        superPlayerModel.coverPictureUrl = coverPictureUrl;
        superPlayerModel.duration = duration;
        superPlayerModel.title = title;
        superPlayerModel.videoQualityList = videoQualityList;
        superPlayerModel.isEnableCache = isEnableDownload;

        return superPlayerModel;
    }

    private boolean isSuperPlayerVideo() {
        return videoURL.startsWith("txsuperplayer://play_vod");
    }

    private SuperPlayerModel transToSuperPlayerVideo() {
        SuperPlayerModel model = new SuperPlayerModel();
        String videoUrl = videoURL;
        String appIdStr = getValueByName(videoUrl, "appId");
        try {
            model.appId = appIdStr.equals("") ? 0 : Integer.valueOf(appIdStr);
            SuperPlayerVideoId videoId = new SuperPlayerVideoId();
            videoId.fileId = getValueByName(videoUrl, "fileId");
            videoId.pSign = getValueByName(videoUrl, "psign");
            model.videoId = videoId;
            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getValueByName(String url, String name) { //txsuperplayer://play_vod?v=4&appId=1400295357&fileId
        // =5285890796599775084&pcfg=Default
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.startsWith(name + "=")) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }


}
