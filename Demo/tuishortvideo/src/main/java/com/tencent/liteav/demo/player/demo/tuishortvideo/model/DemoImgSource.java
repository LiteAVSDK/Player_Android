package com.tencent.liteav.demo.player.demo.tuishortvideo.model;

import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlaySource;

public class DemoImgSource extends TUIPlaySource {

    private String mImgUrl;

    public DemoImgSource(String imgUrl) {
        mImgUrl = imgUrl;
        // 你可以指定不同的viewType来区分自定义页面类型
        setExtViewType(SVDemoConstants.CustomSourceType.SINGLE_IMG_TYPE);
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.mImgUrl = imgUrl;
    }
}
