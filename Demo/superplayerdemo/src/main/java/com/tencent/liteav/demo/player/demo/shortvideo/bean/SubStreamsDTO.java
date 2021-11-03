package com.tencent.liteav.demo.player.demo.shortvideo.bean;

import java.io.Serializable;

public class SubStreamsDTO implements Serializable {

    public SubStreamsDTO(String type) {
        this.type = type;
    }

    public String type;
    public int width;
    public int height;
    public String resolutionName;
}