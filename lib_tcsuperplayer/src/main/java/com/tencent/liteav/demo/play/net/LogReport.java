package com.tencent.liteav.demo.play.net;

import com.tencent.liteav.basic.log.TXCLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liyuejiao on 2018/7/19.
 *
 * 数据上报模块
 */
public class LogReport {

    private static final String TAG = "LogReport";
    private String appName;
    private String packageName;

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
            if (appName != null) {
                jsonObject.put("appname", appName);
            }
            if (packageName != null) {
                jsonObject.put("appidentifier", packageName);
            }
            body = jsonObject.toString();
            TXCLog.d(TAG, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TCHttpURLClient.getInstance().postJson(reqUrl, body, new TCHttpURLClient.OnHttpCallback() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError() {

            }
        });
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
