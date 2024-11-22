package com.tencent.liteav.demo.superplayer;

public class SuperPlayerDef {

    public enum PlayerMode {
        WINDOW,
        FULLSCREEN,
        FLOAT
    }

    public enum PlayerState {
        INIT,
        PLAYING,
        PAUSE,
        LOADING,
        END,
        ERROR
    }

    public enum PlayerType {
        VOD,
        LIVE,
        LIVE_SHIFT  // Live replay
    }

    public enum Orientation {
        LANDSCAPE,
        PORTRAIT
    }
}
