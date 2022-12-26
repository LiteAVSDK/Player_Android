package com.tencent.player.utils;

import android.content.Context;

import com.tencent.player.voddownloadandplay.R;
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
        put(TXVodDownloadDataSource.QUALITY_FLU, R.string.superplayer_flu);
        put(TXVodDownloadDataSource.QUALITY_SD, R.string.superplayer_sd);
        put(TXVodDownloadDataSource.QUALITY_HD, R.string.superplayer_hd);
        put(TXVodDownloadDataSource.QUALITY_FHD, R.string.superplayer_fhd2);
        put(TXVodDownloadDataSource.QUALITY_2K, R.string.superplayer_2k);
        put(TXVodDownloadDataSource.QUALITY_4K, R.string.superplayer_4k);
        put(TXVodDownloadDataSource.QUALITY_OD, R.string.superplayer_original_picture);
        put(TXVodDownloadDataSource.QUALITY_UNK, -1);
    }};



    public static Integer getNameByCacheQualityId(int cacheQualityId) {
        return DOWNLOAD_QUALITY_MAP.get(cacheQualityId);
    }
}
