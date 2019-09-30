package com.tencent.liteav.demo.play.utils;

import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;
import com.tencent.liteav.demo.play.view.TCVideoQulity;
import com.tencent.rtmp.TXBitrateItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yuejiaoli on 2018/7/6.
 */

public class SuperPlayerUtil {

    /**
     * 清晰度转换
     *
     * @param bitrateItem
     * @return
     */
    public static TCVideoQulity convertToVideoQuality(TXBitrateItem bitrateItem, int index) {
        TCVideoQulity quality = new TCVideoQulity();
        quality.bitrate = bitrateItem.bitrate;
        quality.index = bitrateItem.index;

        switch (index) {
            case 0:
                quality.name = "SD";
                quality.title = "标清";
                break;
            case 1:
                quality.name = "HD";
                quality.title = "高清";
                break;
            case 2:
                quality.name = "FHD";
                quality.title = "超清";
                break;
        }
        return quality;
    }

    public static TCVideoQulity convertToVideoQuality(TCPlayInfoStream sourceStream, String classification) {
        TCVideoQulity quality = new TCVideoQulity();
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
     * 清晰度转换
     *
     * @param stream
     * @return
     */
    public static TCVideoQulity convertToVideoQuality(TCPlayInfoStream stream) {
        TCVideoQulity qulity = new TCVideoQulity();
        qulity.bitrate = stream.getBitrate();
        qulity.name = stream.id;
        qulity.title = stream.name;
        qulity.url = stream.url;
        qulity.index = -1;
        return qulity;
    }

    public static ArrayList<TCVideoQulity> convertToVideoQualityList(HashMap<String, TCPlayInfoStream> transcodeList) {
        ArrayList<TCVideoQulity> videoQulities = new ArrayList<>();
        for (String classification : transcodeList.keySet()) {
            TCVideoQulity videoQulity = convertToVideoQuality(transcodeList.get(classification));
            videoQulities.add(videoQulity);
        }
        return videoQulities;
    }
}
