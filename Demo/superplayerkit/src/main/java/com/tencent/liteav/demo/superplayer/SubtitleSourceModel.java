package com.tencent.liteav.demo.superplayer;

import java.io.Serializable;

public class SubtitleSourceModel implements Serializable {

    /**
     * External subtitle name
     *
     * 外挂字幕名称
     */
    public String name;

    /**
     * External subtitle link
     *
     * 外挂字幕连接
     */
    public String url;

    /**
     * External subtitle data type
     * Can choose TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT and TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT
     *
     * 外挂字幕数据类型
     * 可选择 TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT 和 TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT
     */
    public String mimeType;

}

