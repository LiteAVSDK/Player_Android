package com.tencent.liteav.demo;

import android.app.ApplicationErrorReport;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.tencent.feedback.anr.ANRReport;
import com.tencent.feedback.eup.CrashReport;
import com.tencent.rtmp.TXLiveBase;

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

        // 蓝盾 RELEASE 版本下，去掉注释，打开 bugly oa 的上报功能; 代码如有改动，记得在脚本做相应修改。
        initBuglyOA();

        TXLiveBase.getInstance().setLicence(instance, licenceUrl, licenseKey);

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

    private void initBuglyOA() {
        Log.d(TAG, "LiteAVDemo initBuglyOA");
        CrashReport.setProductVersion(mAppContext, TXLiveBase.getSDKVersionStr());

        /** 这条语句已经能让你的java端异常被捕获，并且上报了。BuglyOA 在后台通过包名和 App ID 匹配，buglyoa网站上 BundleId 须和包名保持一致。 */
        CrashReport.initCrashReport(mAppContext);
        String tombDirectoryPath = mAppContext.getDir("tomb", Context.MODE_PRIVATE).getAbsolutePath();
        /**
         * 前提是你需要先初始化java端异常上报功能，
         * 这条语句已经能让你的native端异常被捕获，并且上报了。
         *
         * @param context
         * @param tombDirectoryPath（也可以默认设置为空）
         *            tomb文件的存放路径,tomb文件可以理解为详细的堆栈信息，平均每一个异常会产生一个tomb文件，
         *            平均10k
         * @param openNativeLog
         *            打开Native Log功能,true则输出debug级log，false则只有warn及error有log输出。
         */
        CrashReport.initNativeCrashReport(mAppContext, tombDirectoryPath, true);
        // 开启ANR监控,注意ANR的初始化一定要放在native sdk初始化之后，否则不生效
        ANRReport.startANRMonitor(mAppContext);
    }
}