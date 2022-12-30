package com.tencent.liteav.demo.superplayer;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodConstants;

import java.io.Serializable;

public class SubtitleSourceModel implements Serializable {

    /**
     * 外挂字幕名称
     */
    public String name;

    /**
     * 外挂字幕连接
     */
    public String url;

    /**
     * 外挂字幕数据类型
     * 可选择 TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT 和 TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT
     */
    public String mimeType;

}

