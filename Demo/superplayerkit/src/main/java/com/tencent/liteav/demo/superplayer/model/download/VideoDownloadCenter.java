package com.tencent.liteav.demo.superplayer.model.download;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.rtmp.downloader.ITXVodDownloadListener;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;
import com.tencent.rtmp.downloader.TXVodDownloadManager;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 视频缓存封装
 */
public class VideoDownloadCenter {

    private static final String TAG = "VideoDownloadCenter";

    public static final int DOWNLOAD_EVENT_START    = 1;
    public static final int DOWNLOAD_EVENT_PROGRESS = 2;
    public static final int DOWNLOAD_EVENT_STOP     = 3;
    public static final int DOWNLOAD_EVENT_FINISH   = 4;

    private final TXVodDownloadManager                               mDownloadManager;
    // 监控单个mediaInfo的listener
    private final Map<TXVodDownloadMediaInfo, VideoDonwloadListener> mMediaInfoListeners = new HashMap<>();
    // 监控所有下载任务的listener
    private final List<VideoDonwloadListener>                        mAllMediaListeners  = new ArrayList<>();
    // 确保每个操作入栈按顺序执行
    private final Handler                                            mMainThreadHandler;
    private final Handler                                            mWorkThreadHandler;

    private static class SingletonInstance {
        private static final VideoDownloadCenter instance = new VideoDownloadCenter();
    }

    public static VideoDownloadCenter getInstance() {
        return SingletonInstance.instance;
    }

    private VideoDownloadCenter() {
        mDownloadManager = TXVodDownloadManager.getInstance();
        mDownloadManager.setListener(new CacheCenterDownloadListener());
        mMainThreadHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                return false;
            }
        });

        HandlerThread mWorkHandlerThread = new HandlerThread(TAG);
        mWorkHandlerThread.start();
        mWorkThreadHandler = new Handler(mWorkHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                return false;
            }
        });
    }

    /**
     * 主线程执行
     */
    private void postMain(Runnable runnable) {
        mMainThreadHandler.post(runnable);
    }

    /**
     * 工作线程执行
     */
    private void postWork(Runnable runnable) {
        mWorkThreadHandler.post(runnable);
    }

    private void callbackMediaInfo(final TXVodDownloadMediaInfo info, final OnMediaInfoFetchListener listener) {
        if (null != listener) {
            postMain(new Runnable() {
                @Override
                public void run() {
                    listener.onReady(info);
                }
            });
        }
    }

    /**
     * 数据量较大的时候可能会有卡顿，建议异步调用
     */
    public List<TXVodDownloadMediaInfo> getDownloadList() {
        return mDownloadManager.getDownloadMediaInfoList();
    }

    /**
     * 数据量较大的时候可能会有卡顿，建议异步调用
     */
    public List<TXVodDownloadMediaInfo> getDownloadList(String name) {
        List<TXVodDownloadMediaInfo> mediaInfoList = getDownloadList();
        List<TXVodDownloadMediaInfo> resultList = new ArrayList<>();

        for (TXVodDownloadMediaInfo mediaInfo : mediaInfoList) {
            if (TextUtils.equals(mediaInfo.getUserName(), name)) {
                resultList.add(mediaInfo);
            }
        }
        return resultList;
    }

    /**
     * get download info by url.
     * this is a async method
     */
    public void getDownloadMediaInfo(final String url, final OnMediaInfoFetchListener listener) {
        postWork(new Runnable() {
            @Override
            public void run() {
                final TXVodDownloadMediaInfo info = mDownloadManager.getDownloadMediaInfo(url);
                callbackMediaInfo(info, listener);
            }
        });
    }

    /**
     * get download info by filedId.
     * this is a async method
     */
    public void getDownloadMediaInfo(final int appId, final String filedId, final int qualityId,
                                     final OnMediaInfoFetchListener listener) {
        postWork(new Runnable() {
            @Override
            public void run() {
                final TXVodDownloadMediaInfo info = mDownloadManager.getDownloadMediaInfo(appId, filedId, qualityId);
                callbackMediaInfo(info, listener);
            }
        });
    }

    /**
     * get download info by SuperPlayerModel.
     * this is a async method
     */
    public void getDownloadMediaInfo(SuperPlayerModel superPlayerModel, int qualityId,
                                     final OnMediaInfoFetchListener listener) {
        if (null != superPlayerModel.videoId && !TextUtils.isEmpty(superPlayerModel.videoId.fileId)) {
            getDownloadMediaInfo(superPlayerModel.appId, superPlayerModel.videoId.fileId, qualityId, listener);
        } else if (!TextUtils.isEmpty(superPlayerModel.url)) {
            if (isSuperPlayerVideo(superPlayerModel.url)) {
                getDownloadMediaInfo(superPlayerModel.appId, superPlayerModel.videoId.fileId, qualityId, listener);
            } else {
                getDownloadMediaInfo(superPlayerModel.url, listener);
            }
        }
    }

    /**
     * register global download listener
     */
    public void registerDownloadListener(final VideoDonwloadListener listener) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != listener && !mAllMediaListeners.contains(listener)) {
                    mAllMediaListeners.add(listener);
                }
            }
        });
    }

    /**
     * register special mediainfo download listener
     */
    public void registerDownloadListener(final TXVodDownloadMediaInfo mediaInfo, final VideoDonwloadListener listener) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != listener && !mMediaInfoListeners.containsValue(listener)) {
                    mMediaInfoListeners.put(mediaInfo, listener);
                }
            }
        });
    }

    /**
     * unregister download listener
     */
    public void unRegisterDownloadListener(final VideoDonwloadListener listener) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Collection<VideoDonwloadListener> mediaCollection = mMediaInfoListeners.values();
                while (mediaCollection.contains(listener)) {
                    mediaCollection.remove(listener);
                }
                mAllMediaListeners.remove(listener);
            }
        });
    }

    /**
     * start download
     */
    public TXVodDownloadMediaInfo startDownload(VideoDownloadModel downloadModel) {
        TXVodDownloadDataSource downloadDataSource = null;
        SuperPlayerModel superPlayerModel = downloadModel.getPlayerModel();
        String userName = downloadModel.getUserName();
        // 默认用v4方式下载，v2方式是弃用接口，下载不会存储
        if (null != superPlayerModel.videoId && !TextUtils.isEmpty(superPlayerModel.videoId.fileId)) {
            downloadDataSource = new TXVodDownloadDataSource(superPlayerModel.appId, superPlayerModel.videoId.fileId,
                    downloadModel.getQualityId(), superPlayerModel.videoId.pSign, userName);
        }

        if (null != downloadDataSource) {
            return mDownloadManager.startDownload(downloadDataSource);
        } else {
            return mDownloadManager.startDownloadUrl(superPlayerModel.url, userName);
        }
    }

    /**
     * stop download
     */
    public void stopDownload(TXVodDownloadMediaInfo mediaInfo) {
        mDownloadManager.stopDownload(mediaInfo);
    }

    /**
     * resume download from stop status
     */
    public void resumeDownload(TXVodDownloadMediaInfo mediaInfo) {
        TXVodDownloadDataSource dataSource = mediaInfo.getDataSource();
        if (dataSource != null) {
            mDownloadManager.startDownload(dataSource);
        } else {
            mDownloadManager.startDownloadUrl(mediaInfo.getUrl(), mediaInfo.getUserName());
        }
    }

    /**
     * delete download
     */
    public boolean deleteDownloadMediaInfo(TXVodDownloadMediaInfo mediaInfo) {
        return mDownloadManager.deleteDownloadMediaInfo(mediaInfo);
    }

    /**
     * set global download headers
     */
    public void setHeaders(Map<String, String> headers) {
        if (null != headers) {
            mDownloadManager.setHeaders(headers);
        }
    }

    /**
     * set the download directory
     *
     * @param path download path
     */
    public void setDownloadDirPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            mDownloadManager.setDownloadPath(path);
        }
    }

    private boolean isSuperPlayerVideo(String videoURL) {
        return videoURL.startsWith("txsuperplayer://play_vod");
    }

    private void updateDownloadEvent(final int event, final TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (VideoDonwloadListener listener : mAllMediaListeners) {
                    listener.onDownloadEvent(event, txVodDownloadMediaInfo);
                }

                Set<TXVodDownloadMediaInfo> mediaInfoSet = mMediaInfoListeners.keySet();
                for (TXVodDownloadMediaInfo mediaInfo : mediaInfoSet) {
                    if (txVodDownloadMediaInfo.getDataSource() != null && mediaInfo.getDataSource() != null) {
                        TXVodDownloadDataSource dataSource = txVodDownloadMediaInfo.getDataSource();
                        TXVodDownloadDataSource tempDataSource = mediaInfo.getDataSource();
                        if (dataSource.getAppId() == tempDataSource.getAppId()
                                && TextUtils.equals(dataSource.getFileId(), tempDataSource.getFileId())
                                && dataSource.getQuality() == tempDataSource.getQuality()) {
                            mMediaInfoListeners.get(mediaInfo).onDownloadEvent(event, txVodDownloadMediaInfo);
                        }
                    } else if (TextUtils.equals(mediaInfo.getUrl(), txVodDownloadMediaInfo.getUrl())) {
                        mMediaInfoListeners.get(mediaInfo).onDownloadEvent(event, txVodDownloadMediaInfo);
                    }
                }
            }
        });
    }

    private void updateDownloadError(final TXVodDownloadMediaInfo txVodDownloadMediaInfo, final int errorCode
            , final String errorMsg) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (VideoDonwloadListener listener : mAllMediaListeners) {
                    listener.onDownloadError(txVodDownloadMediaInfo, errorCode, errorMsg);
                }
                for (VideoDonwloadListener listener : mMediaInfoListeners.values()) {
                    if (mMediaInfoListeners.containsKey(txVodDownloadMediaInfo)) {
                        listener.onDownloadError(txVodDownloadMediaInfo, errorCode, errorMsg);
                    }
                }
            }
        });
    }

    class CacheCenterDownloadListener implements ITXVodDownloadListener {

        @Override
        public void onDownloadStart(TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
            updateDownloadEvent(VideoDownloadCenter.DOWNLOAD_EVENT_START, txVodDownloadMediaInfo);
        }

        @Override
        public void onDownloadProgress(TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
            updateDownloadEvent(VideoDownloadCenter.DOWNLOAD_EVENT_PROGRESS, txVodDownloadMediaInfo);
        }

        @Override
        public void onDownloadStop(TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
            updateDownloadEvent(VideoDownloadCenter.DOWNLOAD_EVENT_STOP, txVodDownloadMediaInfo);
        }

        @Override
        public void onDownloadFinish(TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
            updateDownloadEvent(VideoDownloadCenter.DOWNLOAD_EVENT_FINISH, txVodDownloadMediaInfo);
        }

        @Override
        public void onDownloadError(TXVodDownloadMediaInfo txVodDownloadMediaInfo, int i, String s) {
            updateDownloadError(txVodDownloadMediaInfo, i, s);
        }

        @Override
        public int hlsKeyVerify(TXVodDownloadMediaInfo txVodDownloadMediaInfo, String s, byte[] bytes) {
            return 0;
        }
    }


    public interface OnMediaInfoFetchListener {
        void onReady(TXVodDownloadMediaInfo mediaInfo);
    }
}
