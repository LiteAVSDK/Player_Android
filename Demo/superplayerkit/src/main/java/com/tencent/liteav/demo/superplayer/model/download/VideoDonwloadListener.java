package com.tencent.liteav.demo.superplayer.model.download;

import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

/**
 * 下载回调
 */
public interface VideoDonwloadListener {

    /**
     * 下载事件监听
     * 主线程回调
     *
     * @param event 事件
     * @param mediaInfo 下载信息
     */
    void onDownloadEvent(int event, TXVodDownloadMediaInfo mediaInfo);

    /**
     * 下载失败时间
     * 主线程回调
     *
     * @param mediaInfo 下载信息
     * @param errorCode 错误码
     * @param errorMsg 错误信息
     */
    void onDownloadError(TXVodDownloadMediaInfo mediaInfo, int errorCode, String errorMsg);
}
