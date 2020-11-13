package com.tencent.liteav.demo.player.expand.webdata.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.tencent.liteav.demo.player.expand.webdata.WebDataInfo;
import com.tencent.liteav.demo.player.expand.webdata.WebDataParser;

public class WebDataReceiver extends BroadcastReceiver {

    private static final String TAG = "WebDataReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: intent -> " + intent);
        Uri data = intent.getData();
        if (data == null) {
            return;
        }
        String target = data.getQueryParameter(WebDataInfo.EXTRA_TARGET);
        if (WebDataInfo.TARGET_SUPERPLAYER.equals(target)) {
            WebDataParser.get().build(data).start(context);
        }
    }
}
