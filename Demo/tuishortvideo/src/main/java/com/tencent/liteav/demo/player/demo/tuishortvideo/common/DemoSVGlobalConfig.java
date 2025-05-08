package com.tencent.liteav.demo.player.demo.tuishortvideo.common;

import android.util.Log;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIBasePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUILivePlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIVodPlayer;
import com.tencent.qcloud.tuiplayer.shortvideo.common.TUIVideoConst;

import java.util.Map;

public class DemoSVGlobalConfig {

    private static final String TAG = "DemoSVGlobalConfig";

    private static class SingletonInstance {
        private static final DemoSVGlobalConfig instance = new DemoSVGlobalConfig();
    }

    public static DemoSVGlobalConfig instance() {
        return SingletonInstance.instance;
    }

    private int mRotation;
    private boolean mIsMirror;
    private boolean mIsMute;
    private int mPlayMode = TUIVideoConst.ListPlayMode.MODE_ONE_LOOP;

    public void initParams(int renderRotation, boolean isMirror, boolean isMute, int playMode) {
        safeAssignRotation(renderRotation);
        mIsMirror = isMirror;
        mIsMute = isMute;
        mPlayMode = playMode;
    }

    public int getRotation() {
        return mRotation;
    }

    public void setRotation(int rotation) {
        safeAssignRotation(rotation);
    }

    public boolean isMirror() {
        return mIsMirror;
    }

    public void setIsMirror(boolean isMirror) {
        this.mIsMirror = isMirror;
    }

    public boolean isMute() {
        return mIsMute;
    }

    public void setIsMute(boolean isMute) {
        this.mIsMute = isMute;
    }

    public int getPlayMode() {
        return mPlayMode;
    }

    public void setPlayMode(int playMode) {
        this.mPlayMode = playMode;
    }

    public void applyPlayerGlobalConfig(ITUIBasePlayer player) {
        player.setMute(mIsMute);
        if (player instanceof ITUIVodPlayer) {
            ((ITUIVodPlayer) player).setMirror(mIsMirror);
            ((ITUIVodPlayer) player).setRenderRotation(mRotation);
        }
        if (player instanceof ITUILivePlayer) {
            V2TXLiveDef.V2TXLiveRotation v2TXLiveRotation = getLiveRenderRotation(mRotation);
            if (null != v2TXLiveRotation) {
                ((ITUILivePlayer) player).setRenderRotation(v2TXLiveRotation);
            }
        }
    }

    public V2TXLiveDef.V2TXLiveRotation getLiveRenderRotation(int rotation) {
        V2TXLiveDef.V2TXLiveRotation result = null;
        switch (rotation) {
            case 0:
                result =  V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0;
                break;
            case 90:
                result =  V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation90;
                break;
            case 180:
                result =  V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation180;
                break;
            case 270:
                result =  V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270;
                break;
        }
        return result;
    }

    private void safeAssignRotation(int rotation) {
        if (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270) {
            mRotation = rotation;
        } else {
            Log.e(TAG, "rotation is invalid!!!");
        }
    }

    public void applyExtInfoConfig(Object extInfo, ITUIBasePlayer player) {
        if (null == player) {
            return;
        }
        if (!(extInfo instanceof Map)) {
            return;
        }
        Map<String, Object> infoMap = (Map<String, Object>) extInfo;
        Integer code = (Integer) infoMap.get(SVDemoConstants.LayerEventKey.EVENT);
        if (null != code) {
            if (code == SVDemoConstants.LayerEventCode.UPDATE_RENDER_ROTATION) {
                int rotation = (int) infoMap.get(SVDemoConstants.LayerEventKey.VALUE);
                if (player instanceof ITUIVodPlayer) {
                    ((ITUIVodPlayer) player).setRenderRotation(rotation);
                }
                if (player instanceof ITUILivePlayer) {
                    V2TXLiveDef.V2TXLiveRotation v2TXLiveRotation = getLiveRenderRotation(rotation);
                    if (null != v2TXLiveRotation) {
                        ((ITUILivePlayer) player).setRenderRotation(v2TXLiveRotation);
                    }
                }
            } else if (code == SVDemoConstants.LayerEventCode.UPDATE_MIRROR) {
                boolean mirror = (boolean) infoMap.get(SVDemoConstants.LayerEventKey.VALUE);
                if (player instanceof ITUIVodPlayer) {
                    ((ITUIVodPlayer) player).setMirror(mirror);
                }
                // ITUILivePlayer not support
            } else if (code == SVDemoConstants.LayerEventCode.UPDATE_MUTE) {
                boolean mute = (boolean) infoMap.get(SVDemoConstants.LayerEventKey.VALUE);
                player.setMute(mute);
            }
        }
    }
}
