package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.model;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoComment {

    private int depth = 1;

    private String name;

    private String comment;

    private List<ShortVideoComment> childComment = new ArrayList<>();

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ShortVideoComment> getChildComment() {
        return childComment;
    }

    public void setChildComment(List<ShortVideoComment> childComment) {
        this.childComment = childComment;
    }
}
