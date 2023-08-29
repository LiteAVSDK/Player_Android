package com.tencent.liteav.demo.superplayer.model.protocol;

/**
 * Video information protocol request callback interface
 *
 * 视频信息协议请求回调接口
 */
public interface IPlayInfoRequestCallback {

    /**
     * Success callback.
     *
     * 成功回调
     *
     * @param protocol Video information protocol implementation class
     *                 视频信息协议实现类
     * @param param    Video information protocol input parameters
     *                 视频信息协议输入参数
     */
    void onSuccess(IPlayInfoProtocol protocol, PlayInfoParams param);

    /**
     * Error callback
     *
     * 错误回调
     */
    void onError(int errCode, String message);
}
