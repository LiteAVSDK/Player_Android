package com.tencent.liteav.demo;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveBaseListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DemoApplication extends MultiDexApplication {
    private static       String TAG          = "DemoApplication";

    //    private RefWatcher mRefWatcher;
    private static DemoApplication instance;

    // 如何获取License? 请参考官网指引 https://cloud.tencent.com/document/product/454/34750
    String licenceUrl = "请替换成您的licenseUrl";
    String licenseKey = "请替换成您的licenseKey";

    private Context mAppContext;
    @Override
    public void onCreate() {

        super.onCreate();

        mAppContext = this.getApplicationContext();
        instance = this;


        TXLiveBase.getInstance().setLicence(instance, licenceUrl, licenseKey);
        TXLiveBase.setListener(new TXLiveBaseListener() {
            @Override
            public void onUpdateNetworkTime(int errCode, String errMsg) {
                if (errCode != 0) {
                    TXLiveBase.updateNetworkTime();
                }
            }
        });
        TXLiveBase.updateNetworkTime();

        // 短视频licence设置
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        closeAndroidPDialog();
    }

    public static DemoApplication getApplication() {
        return instance;
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
}