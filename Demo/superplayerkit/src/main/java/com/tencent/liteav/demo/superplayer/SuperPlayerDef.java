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

    public interface PlayerRenderType {
        /**
         * child view is textureView,not support drm play
         */
        int CLOUD_VIEW = 0;
        /**
         * domain view is SurfaceView, support drm play
         */
        int SURFACE_VIEW = 1;
    }
}
