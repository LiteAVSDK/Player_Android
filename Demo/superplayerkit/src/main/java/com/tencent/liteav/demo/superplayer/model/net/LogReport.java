package com.tencent.liteav.demo.superplayer.model.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liyuejiao on 2018/7/19.
 * <p>
 * 数据上报模块
 */
public class LogReport {

    private static final String TAG                          = "TCLogReport";
    private              String mAppName;
    private              String mPackageName;
    public static final  String ELK_ACTION_CHANGE_RESOLUTION = "change_resolution";    //ELK上报事件
    public static final  String ELK_ACTION_TIMESHIFT         = "timeshift";
    public static final  String ELK_ACTION_FLOATMOE          = "floatmode";
    public static final  String ELK_ACTION_LIVE_TIME         = "superlive";
    public static final  String ELK_ACTION_VOD_TIME          = "supervod";
    public static final  String ELK_ACTION_CHANGE_SPEED      = "change_speed";
    public static final  String ELK_ACTION_MIRROR            = "mirror";
    public static final  String ELK_ACTION_SOFT_DECODE       = "soft_decode";
    public static final  String ELK_ACTION_HW_DECODE         = "hw_decode";
    public static final  String ELK_ACTION_IMAGE_SPRITE      = "image_sprite";
    public static final  String ELK_ACTION_PLAYER_POINT      = "player_point";

    private LogReport() {
    }

    private static class Holder {
        private static LogReport instance = new LogReport();
    }

    public static LogReport getInstance() {
        return Holder.instance;
    }

    public void uploadLogs(String action, long usedtime, int fileid) {
        String reqUrl = "https://ilivelog.qcloud.com";
        String body = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);
            jsonObject.put("fileid", fileid);
            jsonObject.put("type", "log");
            jsonObject.put("bussiness", "superplayer");
            jsonObject.put("usedtime", usedtime);
            jsonObject.put("platform", "android");
            if (mAppName != null) {
                jsonObject.put("appname", mAppName);
            }
            if (mPackageName != null) {
                jsonObject.put("appidentifier", mPackageName);
            }
            body = jsonObject.toString();
            Log.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpURLClient.getInstance().postJson(reqUrl, body, new HttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError() {

            }
        });
    }

    public void setAppName(Context context) {
        if (context == null) {
            return;
        }
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        mAppName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void setPackageName(Context context) {
        if (context == null) {
            return;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            // 当前版本的包名
            mPackageName = info.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
