package com.tencent.liteav.demo.play.superplayer;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtils {


    public static final SharedPreferences newInstance(Context context, String name) {
        SharedPreferences s = context.getSharedPreferences(name, 0);
        return s;
    }

    public static final void putBoolean(SharedPreferences s, String key, boolean value) {
        s.edit().putBoolean(key, value).commit();
    }


    public static final boolean getBoolean(SharedPreferences s, String key) {
        return s.getBoolean(key,false);
    }
}
