package com.tencent.liteav.demo.player.demo.tuishortvideo.common;

import java.util.HashMap;
import java.util.Map;

public class SVDemoConstants {

    private static boolean isQualityGlobalSwitchOpen = false;

    public synchronized static boolean isQualityGlobalOpen() {
        return isQualityGlobalSwitchOpen;
    }

    public synchronized static void setQualityGlobalSwitch(boolean isOpened) {
        isQualityGlobalSwitchOpen = isOpened;
    }

    public static Map<String, Object> obtainEmptyEvent(int code) {
        Map<String, Object> params = new HashMap<>();
        params.put(LayerEventKey.EVENT, code);
        return params;
    }

    public static Map<String, Object> obtainBooleanEvent(int code, boolean value) {
        Map<String, Object> params = new HashMap<>();
        params.put(LayerEventKey.EVENT, code);
        params.put(LayerEventKey.VALUE, value);
        return params;
    }

    public static Map<String, Object> obtainIntEvent(int code, int value) {
        Map<String, Object> params = new HashMap<>();
        params.put(LayerEventKey.EVENT, code);
        params.put(LayerEventKey.VALUE, value);
        return params;
    }

    public static int getEmptyEvent(Map<String, Object> extInfo) {
        if (null != extInfo) {
            Integer code = (Integer) extInfo.get(LayerEventKey.EVENT);
            return null != code ? code : -1;
        }
        return -1;
    }

    public interface LayerEventKey {
        String EVENT = "event";
        String VALUE = "value";
    }

    public interface LayerEventCode {
        int PLUG_RENDER_VIEW = 1;
        int UPDATE_RENDER_ROTATION = 21;
        int UPDATE_MIRROR = 22;
        int UPDATE_MUTE = 23;
    }

    /**
     * custom source page type
     * must >= 0
     */
    public interface CustomSourceType {
        int SINGLE_IMG_TYPE = 1;
    }
}
