package com.tencent.liteav.demo.superplayer.model.utils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.superplayer.model.entity.PlayInfoStream;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.rtmp.TXBitrateItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yuejiaoli on 2018/7/6.
 *
 * 清晰度转换工具
 */

public class VideoQualityUtils {

    private static final String TAG = "TCVideoQualityUtil";

    /**
     * 从比特流信息转换为清晰度信息
     *
     * @param bitrateItem
     * @return
     */
    public static VideoQuality convertToVideoQuality(TXBitrateItem bitrateItem, int index) {
        VideoQuality quality = new VideoQuality();
        quality.bitrate = bitrateItem.bitrate;
        quality.index = bitrateItem.index;
        switch (index) {
            case 0:
                quality.name = "FLU";
                quality.title = "流畅";
                break;
            case 1:
                quality.name = "SD";
                quality.title = "标清";
                break;
            case 2:
                quality.name = "HD";
                quality.title = "高清";
                break;
            case 3:
                quality.name = "FHD";
                quality.title = "超清";
                break;
            case 4:
                quality.name = "2K";
                quality.title = "2K";
                break;
            case 5:
                quality.name = "4K";
                quality.title = "4K";
                break;
            case 6:
                quality.name = "8K";
                quality.title = "8K";
                break;
        }
        return quality;
    }

    /**
     * 从源视频信息与视频类别信息转换为清晰度信息
     *
     * @param sourceStream
     * @param classification
     * @return
     */
    public static VideoQuality convertToVideoQuality(PlayInfoStream sourceStream, String classification) {
        VideoQuality quality = new VideoQuality();
        quality.bitrate = sourceStream.getBitrate();
        if (classification.equals("FLU")) {
            quality.name = "FLU";
            quality.title = "流畅";
        } else if (classification.equals("SD")) {
            quality.name = "SD";
            quality.title = "标清";
        } else if (classification.equals("HD")) {
            quality.name = "HD";
            quality.title = "高清";
        } else if (classification.equals("FHD")) {
            quality.name = "FHD";
            quality.title = "全高清";
        } else if (classification.equals("2K")) {
            quality.name = "2K";
            quality.title = "2K";
        } else if (classification.equals("4K")) {
            quality.name = "4K";
            quality.title = "4K";
        }
        quality.url = sourceStream.url;
        quality.index = -1;
        return quality;
    }

    /**
     * 从{@link PlayInfoStream}转换为{@link VideoQuality}
     *
     * @param stream
     * @return
     */
    public static VideoQuality convertToVideoQuality(PlayInfoStream stream) {
        VideoQuality qulity = new VideoQuality();
        qulity.bitrate = stream.getBitrate();
        qulity.name = stream.id;
        qulity.title = stream.name;
        qulity.url = stream.url;
        qulity.index = -1;
        return qulity;
    }

    /**
     * 从转码列表转换为清晰度列表
     *
     * @param transcodeList
     * @return
     */
    public static List<VideoQuality> convertToVideoQualityList(HashMap<String, PlayInfoStream> transcodeList) {
        List<VideoQuality> videoQualities = new ArrayList<>();
        for (String classification : transcodeList.keySet()) {
            VideoQuality videoQuality = convertToVideoQuality(transcodeList.get(classification));
            videoQualities.add(videoQuality);
        }
        return videoQualities;
    }

    /**
     * 根据视频清晰度别名表从码率信息转换为视频清晰度
     *
     * @param bitrateItem     码率
     * @param resolutionNames 清晰度别名表
     * @return
     */
    public static VideoQuality convertToVideoQuality(TXBitrateItem bitrateItem, List<ResolutionName> resolutionNames) {
        VideoQuality quality = new VideoQuality();
        quality.bitrate = bitrateItem.bitrate;
        quality.index = bitrateItem.index;
        boolean getName = false;
        for (ResolutionName resolutionName : resolutionNames) {
            if (((resolutionName.width == bitrateItem.width && resolutionName.height == bitrateItem.height) || (resolutionName.width == bitrateItem.height && resolutionName.height == bitrateItem.width))
                    && "video".equalsIgnoreCase(resolutionName.type)) {
                quality.title = resolutionName.name;
                getName = true;
                break;
            }
        }
        if (!getName) {
            TXCLog.i(TAG, "error: could not get quality name!");
        }
        return quality;
    }
}
