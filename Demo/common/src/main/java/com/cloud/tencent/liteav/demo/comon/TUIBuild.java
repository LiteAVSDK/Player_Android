package com.cloud.tencent.liteav.demo.comon;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public final class TUIBuild {

    private static final String TAG = "TUIBuild";

    private static String sModel              = ""; //Build.MODEL;
    private static String sBrand              = ""; //Build.BRAND;
    private static String sManufacturer       = ""; //Build.MANUFACTURER;
    private static String sHardware           = ""; //Build.HARDWARE;
    private static String sReleaseVersion     = ""; //Build.VERSION.RELEASE;
    private static String sBoard              = ""; //Build.BOARD;
    private static String sVersionIncremental = ""; //Build.VERSION.INCREMENTAL
    private static int    sSdkInt             = 0;  //Build.VERSION.SDK_INT;

    public static void setModel(final String model) {
        synchronized (TUIBuild.class) {
            sModel = model;
        }
    }

    public static String getModel() {
        if (TextUtils.isEmpty(sModel)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sModel)) {
                    sModel = Build.MODEL;
                    Log.i(TAG, "get MODEL by Build.MODEL :" + sModel);
                }
            }
        }
        return sModel;
    }

    public static void setBrand(final String brand) {
        synchronized (TUIBuild.class) {
            sBrand = brand;
        }
    }

    public static String getBrand() {
        if (TextUtils.isEmpty(sBrand)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sBrand)) {
                    sBrand = Build.BRAND;
                    Log.i(TAG, "get BRAND by Build.BRAND :" + sBrand);
                }
            }
        }

        return sBrand;
    }

    public static void setManufacturer(final String manufacturer) {
        synchronized (TUIBuild.class) {
            sManufacturer = manufacturer;
        }
    }

    public static String getManufacturer() {
        if (TextUtils.isEmpty(sManufacturer)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sManufacturer)) {
                    sManufacturer = Build.MANUFACTURER;
                    Log.i(TAG, "get MANUFACTURER by Build.MANUFACTURER :" + sManufacturer);
                }
            }
        }

        return sManufacturer;
    }

    public static void setHardware(final String hardware) {
        synchronized (TUIBuild.class) {
            sHardware = hardware;
        }
    }

    public static String getHardware() {
        if (TextUtils.isEmpty(sHardware)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sHardware)) {
                    sHardware = Build.HARDWARE;
                    Log.i(TAG, "get HARDWARE by Build.HARDWARE :" + sHardware);
                }
            }
        }

        return sHardware;
    }

    public static void setReleaseVersion(final String version) {
        synchronized (TUIBuild.class) {
            sReleaseVersion = version;
        }
    }

    public static String getReleaseVersion() {
        if (TextUtils.isEmpty(sReleaseVersion)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sReleaseVersion)) {
                    sReleaseVersion = Build.VERSION.RELEASE;
                    Log.i(TAG, "get VERSION by Build.VERSION.RELEASE :" + sReleaseVersion);
                }
            }
        }

        return sReleaseVersion;
    }

    public static void setSdkInt(final int versionInt) {
        synchronized (TUIBuild.class) {
            sSdkInt = versionInt;
        }
    }

    public static int getSdkInt() {
        if (sSdkInt == 0) {
            synchronized (TUIBuild.class) {
                if (sSdkInt == 0) {
                    sSdkInt = Build.VERSION.SDK_INT;
                    Log.i(TAG, "get VERSION_INT by Build.VERSION.SDK_INT :" + sSdkInt);
                }
            }
        }

        return sSdkInt;
    }

    public static void setVersionIncremental(final String versionIncremental) {
        synchronized (TUIBuild.class) {
            sVersionIncremental = versionIncremental;
        }
    }

    public static String getVersionIncremental() {
        if (TextUtils.isEmpty(sVersionIncremental)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sVersionIncremental)) {
                    sVersionIncremental = Build.VERSION.INCREMENTAL;
                    Log.i(TAG, "get VERSION_INCREMENTAL by Build.VERSION.INCREMENTAL :" + sVersionIncremental);
                }
            }
        }
        return sVersionIncremental;
    }

    public static void setBoard(final String board) {
        synchronized (TUIBuild.class) {
            sBoard = board;
        }
    }

    public static String getBoard() {
        if (TextUtils.isEmpty(sBoard)) {
            synchronized (TUIBuild.class) {
                if (TextUtils.isEmpty(sBoard)) {
                    sBoard = Build.BOARD;
                    Log.i(TAG, "get BOARD by Build.BOARD :" + sBoard);
                }
            }
        }

        return sBoard;
    }
}
