package com.tencent.liteav.demo.superplayer;

import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.TXLiveConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuejiaoli on 2018/7/4.
 * <p>
 * 超级播放器全局配置类
 */

public class SuperPlayerGlobalConfig {

    private static class Singleton {
        private static SuperPlayerGlobalConfig sInstance = new SuperPlayerGlobalConfig();
    }

    public static SuperPlayerGlobalConfig getInstance() {
        return Singleton.sInstance;
    }

    public SuperPlayerGlobalConfig() {
        txSubtitleRenderModel = createVodDefaultSubtitleRenderModel();
    }

    public TXSubtitleRenderModel txSubtitleRenderModel;

    /**
     * 默认播放填充模式 （ 默认播放模式为 自适应模式 ）
     */
    public int renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;

    /**
     * 播放器最大缓存个数 （ 默认缓存 5 ）
     */
    public int maxCacheItem = 5;

    /**
     * 是否启用悬浮窗 （ 默认开启 true ）
     */
    public boolean enableFloatWindow = true;

    /**
     * 是否开启快进快退手势
     */
    public boolean enableFingerTapFastPlay = true;

    /**
     * 是否开启硬件加速 （ 默认开启硬件加速 ）
     */
    public boolean enableHWAcceleration = true;

    /**
     * 时移域名 （修改为自己app的时移域名）
     */
    public String playShiftDomain = "liteavapp.timeshift.qcloud.com";

    /**
     * 悬浮窗位置 （ 默认在左上角，初始化一个宽为 810，高为 540的悬浮窗口 ）
     */
    public TXRect floatViewRect = new TXRect(0, 0, 810, 540);

    /**
     * 是否静音
     */
    public boolean mute = false;

    /**
     * 是否开启log,与原有逻辑保持一直，默认开启
     */
    public boolean enableLog = true;

    /**
     * 是否开启镜面
     */
    public boolean mirror = false;

    /**
     * 请求header
     */
    public Map<String,String> headers = new HashMap<>();

    public static TXSubtitleRenderModel createVodDefaultSubtitleRenderModel() {
        TXSubtitleRenderModel model = new TXSubtitleRenderModel();
        model.canvasWidth = 1920;  // 字幕渲染画布的宽
        model.canvasHeight = 1080;  // 字幕渲染画布的高
        model.fontColor = 0xFFFFFFFF; // 设置字幕字体颜色，默认白色不同命
        model.isBondFontStyle = false;  // 设置字幕字体是否为粗体
        return model;
    }

    /**
     * 播放速率
     */
    public float playRate = 1.0F;

    /**
     * 是否开启支持画中画功能
     */
    public boolean enablePIP = true;

    public final static class TXRect {
        public int x;
        public int y;
        public int width;
        public int height;

        TXRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public TXRect() {
        }
    }
}
