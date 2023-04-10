package com.tencent.liteav.demo.superplayer.helper;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * 为了解决多次打开画中画的时候，启动画中画被认为是后台启动，导致无法启动的问题。
 * 该问题出现于android 12版本上，目前只在MIUI的android 12版本上发现该问题。
 */
public class Android12BridgeService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Android12BridgeServiceBinder();
    }

    class Android12BridgeServiceBinder extends Binder {
        public Android12BridgeService getService() {
            return Android12BridgeService.this;
        }
    }
}
