package com.tencent.liteav.demo.player.webdata;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class WebDataInfo {

    public static final String EXTRA_FROM           = "from";
    public static final String EXTRA_TARGET         = "target";
    public static final String EXTRA_PROTOCOL       = "protocol";
    public static final String EXTRA_DATA           = "data";

    public static final String TARGET_SUPERPLAYER   = "superplayer";

    private String from;
    private String target;
    private String protocol;
    private Map<String, String> data;

    public WebDataInfo() {
        this.data = new HashMap<>();
    }

    public WebDataInfo(String from, String target, String protocol) {
        this.from = from;
        this.target = target;
        this.protocol = protocol;
        this.data = new HashMap<>();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public boolean isLegal() {
        return !TextUtils.isEmpty(from) && !TextUtils.isEmpty(target) && !TextUtils.isEmpty(protocol);
    }

    public void start(Context context) {
        start(context, null);
    }

    public void start(Context context, WebDataParser.Callback callback) {
        WebDataParser.get().start(context, this, callback);
    }

    @Override
    public String toString() {
        return "WebDataInfo{" +
                "from='" + from + '\'' +
                ", target='" + target + '\'' +
                ", protocol='" + protocol + '\'' +
                ", data=" + data +
                '}';
    }
}
