package com.tencent.liteav.demo.superplayer;

import com.tencent.liteav.demo.superplayer.helper.annoation.SuperPlayerRenderTypeParams;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.TXLiveConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Super player component global configuration class
 * 
 * 超级播放器组件全局配置类
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
     * Default playback fill mode (default playback mode is adaptive mode)
     *
     * 默认播放填充模式 （ 默认播放模式为 自适应模式 ）
     */
    public int renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;

    /**
     * The player's rendering view types can be referred to in {@link SuperPlayerDef.PlayerRenderType}.
     * {@link SuperPlayerDef.PlayerRenderType#SURFACE_VIEW} supports DRM playback.
     * {@link SuperPlayerDef.PlayerRenderType#CLOUD_VIEW} does not support DRM playback.
     * Default is {@link SuperPlayerDef.PlayerRenderType#SURFACE_VIEW}
     *
     * 播放器渲染 view 类型，可参考 {@link SuperPlayerDef.PlayerRenderType}
     * {@link SuperPlayerDef.PlayerRenderType#SURFACE_VIEW} 支持 drm 播放。
     * {@link SuperPlayerDef.PlayerRenderType#CLOUD_VIEW} 不支持 drm 播放。
     * 默认为 {@link SuperPlayerDef.PlayerRenderType#SURFACE_VIEW}
     */
    @SuperPlayerRenderTypeParams
    public int renderViewType = SuperPlayerDef.PlayerRenderType.CLOUD_VIEW;

    /**
     * The maximum number of buffers for the player.
     * This setting is deprecated, not recommended, and the setting will have no effect.
     * Recommended to use {@link SuperPlayerGlobalConfig#maxCacheSizeMB}
     *
     * 播放器最大缓存个数。
     * 此设置已经废弃， 不推荐使用，设置将会无效。
     * 推荐使用 {@link SuperPlayerGlobalConfig#maxCacheSizeMB}
     */
    @Deprecated
    public int maxCacheItem = 5;

    /**
     * Set the maximum cache size of the playback engine, unit: MB, default 500MB.
     * After setting, the files in the Cache directory will be automatically cleaned up according to the set value.
     *
     * 设置播放引擎的最大缓存大小，单位：MB，默认 500MB。
     * 设置后会根据设定值自动清理 Cache 目录的文件。
     */
    public int maxCacheSizeMB = 500;

    /**
     * Whether to enable the floating window (default enabled true )
     *
     * 是否启用悬浮窗 （ 默认开启 true ）
     */
    public boolean enableFloatWindow = true;

    /**
     * Whether to enable fast forward and rewind gestures
     *
     * 是否开启快进快退手势
     */
    public boolean enableFingerTapFastPlay = true;

    /**
     * Whether to enable hardware acceleration (enabled by default)
     *
     * 是否开启硬件加速 （ 默认开启硬件加速 ）
     */
    public boolean enableHWAcceleration = true;

    /**
     * Time shift domain
     *
     * 时移域名
     */
    public String playShiftDomain = "liteavapp.timeshift.qcloud.com";

    /**
     * Floating window position (default in the upper left corner, initialize a floating window with
     * a width of 810 and a height of 540)
     *
     * 悬浮窗位置 （ 默认在左上角，初始化一个宽为 810，高为 540的悬浮窗口 ）
     */
    public TXRect floatViewRect = new TXRect(0, 0, 810, 540);

    /**
     * Whether to mute
     *
     * 是否静音
     */
    public boolean mute = false;

    /**
     * Whether to enable log, consistent with the original logic, enabled by default
     *
     * 是否开启log,与原有逻辑保持一直，默认开启
     */
    public boolean enableLog = true;

    /**
     * Whether to enable mirror
     *
     * 是否开启镜面
     */
    public boolean mirror = false;

    /**
     * Request header
     *
     * 请求header
     */
    public Map<String,String> headers = new HashMap<>();

    public long preferResolution = 720 * 1280;

    public static TXSubtitleRenderModel createVodDefaultSubtitleRenderModel() {
        TXSubtitleRenderModel model = new TXSubtitleRenderModel();
        model.canvasWidth = 1920;  // The width of the subtitle rendering canvas
        model.canvasHeight = 1080;  // The height of the subtitle rendering canvas
        model.fontColor = 0xFFFFFFFF; // Set the subtitle font color, default is white
        model.isBondFontStyle = false;  // Set whether the subtitle font is bold
        return model;
    }

    /**
     * Playback rate
     *
     * 播放速率
     */
    public float playRate = 1.0F;

    /**
     * Whether to enable picture-in-picture support
     *
     * 是否开启支持画中画功能
     */
    public boolean enablePIP = true;

    public static final class TXRect {
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
