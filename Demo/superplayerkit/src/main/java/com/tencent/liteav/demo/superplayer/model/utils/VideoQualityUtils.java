package com.tencent.liteav.demo.superplayer.model.utils;

import android.content.Context;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.entity.PlayInfoStream;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuejiaoli on 2018/7/6.
 * <p>
 * 清晰度转换工具
 */

public class VideoQualityUtils {

    private static final String TAG = "TCVideoQualityUtil";

    // 下载文件画质文字表驱动
    private static final Map<Integer, Integer> DOWNLOAD_QUALITY_MAP = new HashMap<Integer, Integer>() {{
        put(TXVodDownloadDataSource.QUALITY_SD, R.string.superplayer_sd);
        put(TXVodDownloadDataSource.QUALITY_HD, R.string.superplayer_hd);
        put(TXVodDownloadDataSource.QUALITY_FHD, R.string.superplayer_fhd2);
        put(TXVodDownloadDataSource.QUALITY_240P, R.string.superplayer_flu);
        put(TXVodDownloadDataSource.QUALITY_480P, R.string.superplayer_sd);
        put(TXVodDownloadDataSource.QUALITY_720P, R.string.superplayer_hd);
        put(TXVodDownloadDataSource.QUALITY_1080P, R.string.superplayer_fhd2);
        put(TXVodDownloadDataSource.QUALITY_2K, R.string.superplayer_2k);
        put(TXVodDownloadDataSource.QUALITY_4K, R.string.superplayer_4k);
        put(TXVodDownloadDataSource.QUALITY_OD, R.string.superplayer_original_picture);
        put(TXVodDownloadDataSource.QUALITY_UNK, -1);
    }};

    /**
     * 从比特流信息转换为清晰度信息
     *
     * @param bitrateItem
     * @return
     */
    public static VideoQuality convertToVideoQuality(Context context, TXBitrateItem bitrateItem) {
        VideoQuality quality = new VideoQuality();
        quality.bitrate = bitrateItem.bitrate;
        quality.index = bitrateItem.index;
        quality.height = bitrateItem.height;
        quality.width = bitrateItem.width;
        formatVideoQuality(context, quality);
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

    private static void formatVideoQuality(Context context, VideoQuality quality) {
        int minValue = Math.min(quality.width, quality.height);
        if (minValue == 240 || minValue == 180) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_flu), minValue);
        } else if (minValue == 480 || minValue == 360) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_sd), minValue);
        } else if (minValue == 540) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_fsd), minValue);
        } else if (minValue == 720) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_hd), minValue);
        } else if (minValue == 1080) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_fhd2), minValue);
        } else if (minValue == 1440) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_2k), minValue);
        } else if (minValue == 2160) {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            context.getString(R.string.superplayer_4k), minValue);
        } else {
            quality.title = context
                    .getString(R.string.superplayer_resolution_name,
                            "", minValue);
        }
    }

    public static String transformToQualityName(String title) {
        if (title == null) {
            return "";
        }
        String qualityName = title;
        if (title.contains("(")) {
            if (title.charAt(0) == ' ' && title.contains(")")) {
                qualityName = title.substring(title.indexOf('(') + 1, title.indexOf(')'));
            } else {
                qualityName = title.substring(0, title.indexOf('('));
            }
        }
        return qualityName;
    }

    /**
     * 根据videoQuality，转化为视频下载需要用到的画质id
     *
     * @param quality 视频画质
     * @return {@link TXVodDownloadDataSource} QUALITY常量
     */
    public static int getCacheVideoQualityIndex(VideoQuality quality) {
        if (null == quality) {
            return TXVodDownloadDataSource.QUALITY_UNK;
        }
        int minValue = Math.min(quality.width, quality.height);
        int cacheQualityIndex = 0;
        if (minValue == 240 || minValue == 180) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_240P;
        } else if (minValue == 480 || minValue == 360) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_480P;
        } else if (minValue == 540) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_540P;
        } else if (minValue == 720) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_720P;
        } else if (minValue == 1080) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_1080P;
        } else if (minValue == 1440) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_2K;
        } else if (minValue == 2160) {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_4K;
        } else {
            cacheQualityIndex = TXVodDownloadDataSource.QUALITY_UNK;
        }
        return cacheQualityIndex;
    }

    public static Integer getNameByCacheQualityId(int cacheQualityId) {
        return DOWNLOAD_QUALITY_MAP.get(cacheQualityId);
    }
}
