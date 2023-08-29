package com.tencent.liteav.demo.superplayer.model.net;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * The SuperPlayer module needs a built-in HTTP request module because it involves querying video information.
 * To avoid introducing additional network request libraries, the native Java HTTPURLConnection is used here.
 * We recommend that you modify the network module and use the network request library in your project,
 * such as OkHTTP, Volley, etc.
 *
 * <p>
 * 超级播放器模块由于涉及查询视频信息，所以需要有一个内置的HTTP请求模块
 * <p>
 * 为了不引入额外的网络请求库，这里使用原生的Java HTTPURLConnection实现
 * <p>
 * 推荐您修改网络模块，使用您项目中的网络请求库，如okHTTP、Volley等
 */
public class HttpURLClient {

    private static class Holder {
        static final HttpURLClient INSTANCE = new HttpURLClient();
    }

    public static HttpURLClient getInstance() {
        return Holder.INSTANCE;
    }

    public void get(final String urlStr, final OnHttpCallback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(urlStr);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.connect();
                    InputStream in = connection.getInputStream();
                    if (in == null) {
                        if (callback != null)
                            callback.onError();
                        return;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    if (callback != null)
                        callback.onSuccess(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onError();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Post JSON data request.
     *
     * post json数据请求
     */
    public void postJson(final String urlStr, final String json, final OnHttpCallback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.connect();

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(json.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    InputStream in = connection.getInputStream();
                    if (in == null) {
                        if (callback != null)
                            callback.onError();
                        return;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    if (callback != null)
                        callback.onSuccess(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onError();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public interface OnHttpCallback {
        void onSuccess(String result);

        void onError();
    }
}
