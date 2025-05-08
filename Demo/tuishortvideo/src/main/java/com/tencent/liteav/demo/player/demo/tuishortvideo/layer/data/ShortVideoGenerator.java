package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.data;

import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.model.ShortVideoComment;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;

import java.util.ArrayList;
import java.util.List;

public class ShortVideoGenerator {


    public static List<ShortVideoComment> generateComments(int count) {
        List<ShortVideoComment> result = new ArrayList<>();
        for (int i = 0;i < count; i++) {
            ShortVideoComment comment = new ShortVideoComment();
            comment.setName("用户"+count);
            comment.setComment("评论评论评论评论评论评论评论评论评论评论评论评论评论评论评论评论");
            result.add(comment);
        }
        return result;
    }

    public static List<TUIVideoSource> generateSources(int count) {
        List<TUIVideoSource> sources = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sources.add(generateSource());
        }
        return sources;
    }

    public static TUIVideoSource generateSource() {
        TUIVideoSource source = new TUIVideoSource();
        source.setAppId(1500005830);
        source.setFileId("387702294394366256");
        source.setCoverPictureUrl("http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/3d98015b387702294394366256/coverBySnapshot/coverBySnapshot_10_0.jpg");
        return source;
    }

}
