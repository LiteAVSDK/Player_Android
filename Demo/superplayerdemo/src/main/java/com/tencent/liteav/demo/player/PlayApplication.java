package com.tencent.liteav.demo.player;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.Method;

public class PlayApplication {
    @SuppressLint("StaticFieldLeak")
    private static Application application;

    public static Application get() {
        if (application == null) {
            synchronized (PlayApplication.class) {
                if (application == null) {
                    new PlayApplication();
                }
            }
        }
        return application;
    }

    private PlayApplication() {
        Object activityThread;
        try {
            Class acThreadClass = Class.forName("android.app.ActivityThread");
            if (acThreadClass == null) {
                return;
            }
            Method acThreadMethod = acThreadClass.getMethod("currentActivityThread");
            if (acThreadMethod == null) {
                return;
            }
            acThreadMethod.setAccessible(true);
            activityThread = acThreadMethod.invoke(null);
            Method applicationMethod = activityThread.getClass().getMethod("getApplication");
            if (applicationMethod == null) {
                return;
            }
            Object app = applicationMethod.invoke(activityThread);
            application = (Application) app;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
