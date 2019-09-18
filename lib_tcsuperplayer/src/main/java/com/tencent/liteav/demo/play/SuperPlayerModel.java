package com.tencent.liteav.demo.play;


import com.tencent.liteav.demo.play.v3.SuperPlayerVideoId;

import java.util.List;


/**
 * Created by hans on 2019/3/25.
 *
 * 使用点播SDK有以下三种方式：
 * 1. 使用腾讯云FileId播放模式，仅需填写appid以及fileId即可简单进行播放。（更多高级用法，详见{@link SuperPlayerVideoId} 以及腾讯云官网文档
 *
 * 2. 使用传统URL模式播放，仅需填写URL即可进行播放。
 *
 * 3. 多码率视频播放模式。
 */
public class SuperPlayerModel {
    /** ------------------------------------------------------------------
     *  公共字段
     *  ------------------------------------------------------------------
     */
    public int              appId;              // 【腾讯云服务专用】appid 播放方式1必填；播放方式2、3只有需要使用腾讯云直播时移功能时候填写

//    public String           token;              // 【腾讯云服务专用】加密视频的Token.需要保证是经过URLEncode的
//                                                // 1.使用VideoId(FileId)模式播放加密视频时，需要指定
//                                                // 2.使用URL【腾讯云URL】播放SampleAES加密的HLS流或WideVine加密DASH流时需要指定

    public String title       = "";             // 视频文件名 （用于显示在UI层）
                                                // 播放方式1：若未指定title，则使用FileId返回的Title
                                                // 播放方式2、3：需要指定，否则title显示为空

    /** ------------------------------------------------------------------
     *  播放方式1： 腾讯云存储对象VideoId（FileId）播放模式  1.appId必填 2.使用V3协议注意填写Token用于播放加密视频
     *  ------------------------------------------------------------------
     */
    public SuperPlayerVideoId videoId;


    /** ------------------------------------------------------------------
     *  播放方式2： 直接使用URL播放  支持直播:RTMP、FLV封装格式  点播：MP4、Dash等常见封装格式 使用腾讯云直播时移功能则需要填写appId
     *  ------------------------------------------------------------------
     */
    public String url         = "";      // 视频URL
    public String qualityName = "原画";   // 码率名称（用于显示在UI层）


    /** ------------------------------------------------------------------
     *  播放方式3： 多码率URL播放  播放方式3是播放方式2的扩展，可同时传入多条URL，用于进行码率切换
     *  ------------------------------------------------------------------
     */
    public List<SuperPlayerURL> multiURLs;

    public int playDefaultIndex; // 指定多码率情况下，默认播放的连接Index

    public static class SuperPlayerURL {
        public SuperPlayerURL(String url, String qualityName) {
            this.qualityName = qualityName;
            this.url = url;
        }

        public SuperPlayerURL() {
        }

        public String qualityName = "原画"; // 清晰度名称（用于显示在UI层）

        public String url         = ""; // 该清晰度对应的地址

    }
}
