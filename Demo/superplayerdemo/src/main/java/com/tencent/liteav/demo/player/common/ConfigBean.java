package com.tencent.liteav.demo.player.common;

import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;

public class ConfigBean {
    public static boolean sIsEnableSelfAdaption   = true;
    public static boolean sIsEnableHardWareDecode = true;
    public static int     sLogLevel               = TXLiveConstants.LOG_LEVEL_VERBOSE;
    public static boolean sIsUseDash              = false;
    public static TXVodPlayConfig sPlayConfig     = new TXVodPlayConfig();
    
    static {
        resetPlayConfig();
    }

    public static void resetPlayConfig() {
        sPlayConfig.setEnableAccurateSeek(true);
        sPlayConfig.setSmoothSwitchBitrate(true);
        sPlayConfig.setAutoRotate(true);
        sPlayConfig.setEnableRenderProcess(true);
        sPlayConfig.setProgressInterval(500);
        sPlayConfig.setConnectRetryCount(3);
        sPlayConfig.setConnectRetryInterval(3);
        sPlayConfig.setTimeout(10);
        TXPlayerGlobalSetting.setCacheFolderPath("txCache");
        TXPlayerGlobalSetting.setMaxCacheSize(200);
        sPlayConfig.setMaxPreloadSize(50);
        sPlayConfig.setMaxBufferSize(50);
        sPlayConfig.setPreferredResolution(720 * 1280);
        sPlayConfig.setMediaType(TXVodConstants.MEDIA_TYPE_AUTO);
        TXLiveBase.setLogLevel(TXLiveConstants.LOG_LEVEL_VERBOSE);
    }
}
