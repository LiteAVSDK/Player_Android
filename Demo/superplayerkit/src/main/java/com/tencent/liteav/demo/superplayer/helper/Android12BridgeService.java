package com.tencent.liteav.demo.superplayer.helper;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;


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
