package com.tencent.liteav.demo.common;

import com.blankj.utilcode.util.SPUtils;

public class AppRuntime {

    private static final String SP_NAME_CONFIG      = "liteav_config";
    private static final String SP_KEY_CONFIG_DEBUG = "debug";

    private boolean mIsDebug;

    static class Single {
        static AppRuntime INSTANCE = new AppRuntime();
    }

    private AppRuntime() {
        deserializeLocal();
    }

    public static AppRuntime get() {
        return Single.INSTANCE;
    }

    public void setDebug(boolean isDebug) {
        mIsDebug = isDebug;
        serializeLocal();
    }

    public boolean isDebug() {
        return mIsDebug;
    }

    private void serializeLocal() {
        SPUtils.getInstance(SP_NAME_CONFIG).put(SP_KEY_CONFIG_DEBUG, mIsDebug);
    }

    private void deserializeLocal() {
        mIsDebug = SPUtils.getInstance(SP_NAME_CONFIG).getBoolean(SP_KEY_CONFIG_DEBUG, false);
    }
}
