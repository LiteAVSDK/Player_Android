package com.tencent.liteav.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DemoApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static       String TAG          = "DemoApplication";

    //    private RefWatcher mRefWatcher;
    private static DemoApplication instance;
    private final List<Activity> mActivityList = new ArrayList<>();
    private final List<Integer> mAppTaskIds = new ArrayList<>();

    private Context mAppContext;

    @Override
    public void onCreate() {

        super.onCreate();

        mAppContext = this.getApplicationContext();
        instance = this;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        closeAndroidPDialog();
        initActivityLife();
        preRequestShortVideosIfNeed();
    }

    public static DemoApplication getApplication() {
        return instance;
    }

    private void initActivityLife() {
        registerActivityLifecycleCallbacks(this);
    }

    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mActivityList.add(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        final int taskId = activity.getTaskId();
        if (mAppTaskIds.contains(taskId)) {
            mAppTaskIds.remove(Integer.valueOf(taskId));
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        final int taskId = activity.getTaskId();
        if (!mAppTaskIds.contains(taskId)) {
            mAppTaskIds.add(taskId);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        final int taskId = activity.getTaskId();
        mActivityList.remove(activity);
        // Remove useless `taskId`.
        boolean removeFlag = true;
        for (Activity tmpAct : mActivityList) {
            if (null != tmpAct && !tmpAct.isDestroyed() && !tmpAct.isFinishing() && tmpAct.getTaskId() == taskId) {
                removeFlag = false;
                break;
            }
        }
        if (removeFlag) {
            // Boxing of `valueOf`, remove according to the object.
            mAppTaskIds.remove(Integer.valueOf(taskId));
        }
    }

    public boolean isUsActivity(int taskId) {
        return mAppTaskIds.contains(taskId);
    }

    private void preRequestShortVideosIfNeed() {
        try {
            Class<?> shortVideoModelClass = Class.forName("com.tencent.liteav.demo.player.demo.tuishortvideo."
                    + "data.ShortVideoModel");
            Method getInstanceMethod = shortVideoModelClass.getMethod("getInstance", Context.class);
            Method preloadMethod = shortVideoModelClass.getMethod("preloadVideosIfNeed");
            Object shortVideoModelObj = getInstanceMethod.invoke(null, this);
            preloadMethod.invoke(shortVideoModelObj);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "preRequest shortVideo sources failed:", e);
            e.printStackTrace();
        }
    }
}