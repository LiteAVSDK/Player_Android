package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod.message;

import java.util.ArrayList;
import java.util.List;

public class DemoVodLayerMessenger {

    private final List<DemoVodLayerEvent> mEventList = new ArrayList<>();

    public void sendEmptyMsg(int code) {
        for (DemoVodLayerEvent event : mEventList) {
            event.onLayerEvent(code);
        }
    }

    public void addEvent(DemoVodLayerEvent event) {
        if (!mEventList.contains(event)) {
            mEventList.add(event);
        }
    }

    public void removeEvent(DemoVodLayerEvent event) {
        mEventList.remove(event);
    }

    public void clearEvent() {
        mEventList.clear();
    }

}
