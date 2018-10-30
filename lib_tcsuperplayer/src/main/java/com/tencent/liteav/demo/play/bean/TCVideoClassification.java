package com.tencent.liteav.demo.play.bean;

import java.util.ArrayList;

/**
 * Created by yuejiaoli on 2018/7/6.
 */

public class TCVideoClassification {

    private String id;
    private String name;
    private ArrayList<Integer> definitionList;

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

    public ArrayList<Integer> getDefinitionList() {
        return definitionList;
    }

    public void setDefinitionList(ArrayList<Integer> definitionList) {
        this.definitionList = definitionList;
    }
}
