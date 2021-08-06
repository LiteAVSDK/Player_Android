package com.tencent.liteav.demo.superplayer.model.protocol;

/**
 * 视频信息协议请求回调接口
 */
public interface IPlayInfoRequestCallback {

    /**
     * 成功回调
     *
     * @param protocol 视频信息协议实现类
     * @param param    视频信息协议输入参数
     */
    void onSuccess(IPlayInfoProtocol protocol, PlayInfoParams param);

    /**
     * 错误回调
     *
     * @param errCode 错误码
     * @param message 错误信息
     */
    void onError(int errCode, String message);
}
