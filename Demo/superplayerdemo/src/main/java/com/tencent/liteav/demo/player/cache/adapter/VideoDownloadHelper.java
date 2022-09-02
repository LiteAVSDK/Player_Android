package com.tencent.liteav.demo.player.cache.adapter;

import android.content.Context;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.utils.SuperVodListLoader;
import com.tencent.liteav.demo.player.view.DialogUtils;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * video download list helper.
 * <p>
 * common methods
 * </p>
 */
public class VideoDownloadHelper {

    private static final long MIN_SHOW_ERROR_TIME = 3000L;

    public static final String DEFAULT_DOWNLOAD_VIDEO_COVER =
            "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";

    // 下载状态文字表驱动
    private static final Map<Integer, Integer> PROGRESS_STATE_TEXT_MAP = new HashMap<Integer, Integer>() {{
        put(TXVodDownloadMediaInfo.STATE_INIT, R.string.superplayer_cache_state_cacheing);
        put(TXVodDownloadMediaInfo.STATE_START, R.string.superplayer_cache_state_cacheing);
        put(TXVodDownloadMediaInfo.STATE_STOP, R.string.superplayer_cache_state_pause);
        put(TXVodDownloadMediaInfo.STATE_ERROR, R.string.superplayer_cache_state_pause);
        put(TXVodDownloadMediaInfo.STATE_FINISH, R.string.superplayer_cache_state_finish);
    }};

    // 下载状态图标表驱动
    private static final Map<Integer, Integer> PROGRESS_STATE_ICON_MAP = new HashMap<Integer, Integer>() {{
        put(TXVodDownloadMediaInfo.STATE_INIT, R.drawable.superplayer_cache_circle_status_caching);
        put(TXVodDownloadMediaInfo.STATE_START, R.drawable.superplayer_cache_circle_status_caching);
        put(TXVodDownloadMediaInfo.STATE_STOP, R.drawable.superplayer_cache_circle_status_pause);
        put(TXVodDownloadMediaInfo.STATE_ERROR, R.drawable.superplayer_cache_circle_status_pause);
        put(TXVodDownloadMediaInfo.STATE_FINISH, R.drawable.superplayer_cache_circle_status_finish);
    }};


    private final String             mProgressFormatter;
    private final SuperVodListLoader loader;

    private boolean isRequestSuccess  = true;
    private long    lastShowErrorTime = 0L;

    // 只能包内实例化
    VideoDownloadHelper(String mProgressFormatter,Context context) {
        this.mProgressFormatter = mProgressFormatter;
        loader = new SuperVodListLoader(context);
    }

    public int getProgressStateTextRes(int downloadState) {
        return PROGRESS_STATE_TEXT_MAP.get(downloadState);
    }

    public int getProgressStateIconRes(int downloadState) {
        return PROGRESS_STATE_ICON_MAP.get(downloadState);
    }

    public Integer getDownloadQualityText(int downloadQualityId) {
        return VideoQualityUtils.getNameByCacheQualityId(downloadQualityId);
    }

    public SuperVodListLoader getLoader() {
        return loader;
    }

    public String getProgressFormatter() {
        return mProgressFormatter;
    }

    /**
     * 根据时间戳格式化成 00:00
     */
    public String formattedTime(long second) {
        long h = second / 3600;
        long m = (second % 3600) / 60;
        long s = (second % 3600) % 60;
        String hs;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }
        String ms;
        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }
        String ss;
        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        String formatTime;
        if (h > 0) {
            formatTime = hs + ":" + ms + ":" + ss;
        } else {
            formatTime = ms + ":" + ss;
        }
        return formatTime;
    }

    public void showNoNetWorkTip(Context activity) {
        if (isRequestSuccess && System.currentTimeMillis() - lastShowErrorTime < MIN_SHOW_ERROR_TIME) {
            lastShowErrorTime = System.currentTimeMillis();
            isRequestSuccess = false;
            DialogUtils.getInstance().showTip(activity, false, activity.getString(R.string.superplayer_net_error));
        }
    }

    public void updateRequestStatus() {
        if (!isRequestSuccess) {
            isRequestSuccess = true;
        }
    }
}
