package com.tencent.liteav.demo.common.utils;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class IntentUtils {
    private static final String TAG = "IntentUtils";

    /**
     * 确保存在相应的 activity 来处理 intent，以免发生 activity 找不到的异常。
     */
    public static void safeStartActivity(Context context, Intent intent) {
        if (intent == null || context == null) {
            Log.e(TAG, "intent or activity is null");
            return;
        }
        if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            Log.w(TAG, "No activity match : " + intent.toString());
            return;
        }
        try {
            if (context instanceof Application) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("TAG", "ActivityNotFoundException : " + intent.toString());
        }
    }
}
