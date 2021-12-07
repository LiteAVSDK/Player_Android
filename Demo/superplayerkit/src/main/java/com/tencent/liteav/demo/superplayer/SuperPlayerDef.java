package com.tencent.liteav.demo.superplayer;

public class SuperPlayerDef {

    public enum PlayerMode {
        WINDOW,     // 窗口模式
        FULLSCREEN, // 全屏模式
        FLOAT       // 悬浮窗模式
    }

    public enum PlayerState {
        INIT,       //初始状态
        PLAYING,    // 播放中
        PAUSE,      // 暂停中
        LOADING,    // 缓冲中
        END         // 结束播放
    }

    public enum PlayerType {
        VOD,        // 点播
        LIVE,       // 直播
        LIVE_SHIFT  // 直播会看
    }

    public enum Orientation {
        LANDSCAPE,  // 横屏
        PORTRAIT    // 竖屏
    }
}
