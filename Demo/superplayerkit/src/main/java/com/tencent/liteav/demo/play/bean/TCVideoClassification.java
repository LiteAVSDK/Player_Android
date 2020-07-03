package com.tencent.liteav.demo.play.bean;

import java.util.List;

/**
 * Created by yuejiaoli on 2018/7/6.
 *
 * 视频画质信息
 */

public class TCVideoClassification {

    private String id;
    private String name;
    private List<Integer> definitionList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getDefinitionList() {
        return definitionList;
    }

    public void setDefinitionList(List<Integer> definitionList) {
        this.definitionList = definitionList;
    }
}
