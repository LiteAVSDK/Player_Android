package com.tencent.liteav.demo.superplayer.model.download;

import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

/**
 * Download callback
 *
 * 下载回调
 */
public interface VideoDownloadListener {

    /**
     * Download event listener
     * Main thread callback
     *
     * 下载事件监听
     * 主线程回调
     *
     * @param event Event
     *              事件
     * @param mediaInfo Download information
     *                  下载信息
     */
    void onDownloadEvent(int event, TXVodDownloadMediaInfo mediaInfo);

    /**
     * Download failure event
     * Main thread callback
     *
     * 下载失败事件
     * 主线程回调
     *
     * @param mediaInfo Download information
     *                  下载信息
     * @param errorCode Error code
     *                  错误码
     * @param errorMsg Error message
     *                 错误信息
     */
    void onDownloadError(TXVodDownloadMediaInfo mediaInfo, int errorCode, String errorMsg);
}
