package com.tencent.liteav.demo.player.feed.model;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.expand.model.utils.SuperVodListLoader;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerVideoId;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedVodListLoader {

    private Handler handler = new Handler(Looper.getMainLooper());

    private List<VideoModel> loadDefaultVodList() {
        List<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "5285890781763144364";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "现有超级播放器demo-点播列表-腾讯云";
        model.videoMoreDescription = "腾讯多年技术沉淀，300+ 款产品共筑腾讯云产品矩阵，从基础设施到行业应用领域，腾讯云提供完善的产品体系，助力您的业务腾飞。";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819220421305";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "腾讯云短视频演示";
        model.videoMoreDescription = "短视频 （User Generated Short Video，UGSV）基于腾讯云强大的上传、存储、转码、分发的云点播能力，提供集成了采集、剪辑、拼接、特效、分享、播放等功能的客户端 SDK。";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071568";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "小直播app基础功能";
        model.videoMoreDescription = "基于云直播服务、即时通信（IM）和对象存储服务（COS）构建，并使用云服务器（CVM）提供简单的后台服务，实现多项直播功能。";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071668";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "利用小直播app实现连麦互动、文字互动和弹幕消息等功能";
        model.videoMoreDescription = "基于云直播服务、即时通信（IM）和对象存储服务（COS）构建，并使用云服务器（CVM）提供简单的后台服务，实现多项直播功能。";
        list.add(model);

        model = new VideoModel();
        model.appid = 1252463788;
        model.fileid = "4564972819219071679";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "可以实现登录、注册、开播、房间列表、连麦互动、文字互动和弹幕消息等功能";
        model.videoMoreDescription = "基于云直播服务、即时通信（IM）和对象存储服务（COS）构建，并使用云服务器（CVM）提供简单的后台服务，实现多项直播功能。";
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "8602268011437356984";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = "一站式 VPaaS （Video Platform as a Service）解决方案";
        model.videoMoreDescription = "腾讯云点播（Video on Demand，VOD）基于腾讯多年技术积累与基础设施建设，为有音视频应用相关需求的客户提供包括音视频存储管理、音视频转码处理、音视频加速播放和音视频通信服务的一站式解决方案。";
        list.add(model);

        return list;
    }

    /**
     * 根据视频ID 获取视频标题
     *
     * @param fileId
     * @return
     */
    private String getTitleByFileId(String fileId) {
        String title = "";
        switch (fileId) {
            case "5285890781763144364":
                title = "腾讯云介绍";
                break;
            case "4564972819220421305":
                title = "小视频app";
                break;
            case "4564972819219071568":
                title = "小直播直播美颜、观众评论点赞等基础功能";
                break;
            case "4564972819219071668":
                title = "小直播app主播连麦";
                break;
            case "4564972819219071679":
                title = "小直播app-在线直播解决方案";
                break;
            case "8602268011437356984":
                title = "2分钟带你认识云点播";
                break;
            default:
                break;
        }
        return title;
    }


    /**
     * 获取数据
     *
     * @param loadDataCallBack
     */
    public void loadListData(int page, final LoadDataCallBack loadDataCallBack) {
        int random = getRandomNumber(1, 5);
        List<VideoModel> videoModelList = page == 0 ? loadDefaultVodList() : loadDefaultVodList().subList(0, random);
        final int size = videoModelList.size();
        SuperVodListLoader mSuperVodListLoader = new SuperVodListLoader();
        mSuperVodListLoader.getVodInfoOneByOne(videoModelList, new SuperVodListLoader.OnVodInfoLoadListener() {
            int count = 0;
            List<VideoModel> resultList = new ArrayList<>();

            @Override
            public void onSuccess(VideoModel videoModel) {
                videoModel.title = getTitleByFileId(videoModel.fileid);
                resultCallBack(videoModel);
            }

            @Override
            public void onFail(int errCode) {
                resultCallBack(null);
            }

            private void resultCallBack(final VideoModel videoModel) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (videoModel != null) {
                            resultList.add(videoModel);
                        }
                        count++;
                        if (count != size) {
                            return;
                        }
                        if (resultList.size() > 0) {
                            loadDataCallBack.onLoadedData(resultList);
                        } else {
                            loadDataCallBack.onError(-1);
                        }
                    }
                });
            }
        });
    }


    public interface LoadDataCallBack {
        void onLoadedData(List<VideoModel> videoModels);

        void onError(int errorCode);
    }


    /**
     * 将VideoModel 转换为SuperPlayerModel
     *
     * @param videoModel
     * @return
     */
    public static SuperPlayerModel conversionModel(VideoModel videoModel) {
        SuperPlayerModel superPlayerModelV3 = new SuperPlayerModel();
        superPlayerModelV3.appId = videoModel.appid;
        superPlayerModelV3.vipWatchMode = videoModel.vipWatchModel;
        if (!TextUtils.isEmpty(videoModel.videoURL)) {
            if (isSuperPlayerVideo(videoModel)) {
                return playSuperPlayerVideo(videoModel);
            } else {
                superPlayerModelV3.title = videoModel.title;
                superPlayerModelV3.url = videoModel.videoURL;

                superPlayerModelV3.multiURLs = new ArrayList<>();
                if (videoModel.multiVideoURLs != null) {
                    for (VideoModel.VideoPlayerURL modelURL : videoModel.multiVideoURLs) {
                        superPlayerModelV3.multiURLs.add(new SuperPlayerModel.SuperPlayerURL(modelURL.url, modelURL.title));
                    }
                }
            }
        } else if (!TextUtils.isEmpty(videoModel.fileid)) {
            superPlayerModelV3.videoId = new SuperPlayerVideoId();
            superPlayerModelV3.videoId.fileId = videoModel.fileid;
            superPlayerModelV3.videoId.pSign = videoModel.pSign;
        }
        superPlayerModelV3.playAction = videoModel.playAction;
        superPlayerModelV3.placeholderImage = videoModel.placeholderImage;
        superPlayerModelV3.coverPictureUrl = videoModel.coverPictureUrl;
        superPlayerModelV3.duration = videoModel.duration;
        superPlayerModelV3.title = videoModel.title;
        return superPlayerModelV3;
    }

    private static boolean isSuperPlayerVideo(VideoModel videoModel) {
        return videoModel.videoURL.startsWith("txsuperplayer://play_vod");
    }

    private static SuperPlayerModel playSuperPlayerVideo(VideoModel videoModel) {
        SuperPlayerModel model = new SuperPlayerModel();
        String videoUrl = videoModel.videoURL;
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

    private static String getValueByName(String url, String name) { //txsuperplayer://play_vod?v=4&appId=1400295357&fileId=5285890796599775084&pcfg=Default
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

    /**
     * 获取范围内的数据
     *
     * @param min
     * @param max
     * @return
     */
    private static Integer getRandomNumber(int min, int max) {
        Random random = new Random();
        int result = random.nextInt(max) % (max - min + 1) + min;
        return result;
    }
}
