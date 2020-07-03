package com.tencent.liteav.demo.player.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharePreferenceUtils {

    public static SharedPreferences newInstance(Context context, String name) {
        return context.getSharedPreferences(name, 0);
    }

    public static void putBoolean(SharedPreferences s, String key, boolean value) {
        s.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(SharedPreferences s, String key) {
        return s.getBoolean(key,false);
    }
}
