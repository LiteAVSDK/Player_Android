package com.tencent.liteav.demo.player.expand.model;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.player.expand.model.entity.VideoInfo;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by vinsonswang on 2018/3/26.
 */

public class VideoDataMgr {
    private static final String                   TAG = "VideoDataMgr";
    private static       VideoDataMgr             sInstance;
    private              OkHttpClient             mOkHttpClient;
    private              GetVideoInfoListListener mGetVideoInfoListListener;

    public static VideoDataMgr getInstance() {
        if (sInstance == null) {
            synchronized (VideoDataMgr.class) {
                if (sInstance == null) {
                    sInstance = new VideoDataMgr();
                }
            }
        }
        return sInstance;
    }

    private VideoDataMgr() {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)    // 设置超时时间
                .readTimeout(5, TimeUnit.SECONDS)       // 设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)      // 设置写入超时时间
                .build();
    }

    public void setGetVideoInfoListListener(GetVideoInfoListListener getVideoInfoListListener) {
        mGetVideoInfoListListener = getVideoInfoListListener;
    }

    public void getVideoList() {
        Request request = new Request.Builder()
                .url(SuperPlayerConstants.ADDRESS_VIDEO_LIST + "?page_num=0&page_size=20&query=test" + "&" + getSigParams())
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getVideoList onFailure : " + e.toString());
                notifyGetVideoListFail(SuperPlayerConstants.RetCode.CODE_REQUEST_ERR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String contentStr = response.body().string();
                Log.i(TAG, "getVideoList onResponse : " + contentStr);
                parseVideoList(contentStr);
            }
        });
    }

    private void parseVideoList(String contentStr) {
        if (TextUtils.isEmpty(contentStr)) {
            Log.e(TAG, "parseVideoList err, contentStr is empty");
            return;
        }
        try {
            JSONObject resObject = new JSONObject(contentStr);
            int code = resObject.optInt("code");
            if (code != SuperPlayerConstants.RetCode.CODE_SUCCESS) {
                Log.e(TAG, "parseVideoList fail, code = " + code);
                notifyGetVideoListFail(code);
                return;
            }
            JSONObject dataObj = resObject.optJSONObject("data");
            JSONArray list = dataObj.getJSONArray("list");
            List<VideoInfo> videoInfoList = new ArrayList<>();
            for (int i = 0; i < list.length(); i++) {
                JSONObject videoInfoJSONObject = list.optJSONObject(i);
                VideoInfo videoInfo = new VideoInfo();
                videoInfo.fileId = videoInfoJSONObject.optString("fileId");
                videoInfo.name = videoInfoJSONObject.optString("name");
                videoInfo.size = videoInfoJSONObject.optInt("size");
                videoInfo.duration = videoInfoJSONObject.optInt("duration");
                videoInfo.coverUrl = videoInfoJSONObject.optString("coverUrl");
                videoInfo.createTime = videoInfoJSONObject.optLong("createTime");
                videoInfoList.add(videoInfo);
            }
            notifyGetVideoListSuccess(videoInfoList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifyGetVideoListFail(int errCode) {
        if (mGetVideoInfoListListener != null) {
            mGetVideoInfoListListener.onFail(errCode);
        }
    }

    private void notifyGetVideoListSuccess(List<VideoInfo> videoInfoList) {
        if (mGetVideoInfoListListener != null) {
            mGetVideoInfoListListener.onGetVideoInfoList(videoInfoList);
        }
    }

    /**
     * 访问服务器需要加鉴权
     *
     * @return
     */
    private String getSigParams() {
        long timeStamp = System.currentTimeMillis() / 1000;
        String nonce = "";
        String sig = "";
        nonce = getMD5Encryption(String.valueOf(System.currentTimeMillis()));
        sig = getMD5Encryption(SuperPlayerConstants.VOD_APPID + String.valueOf(timeStamp) + nonce + SuperPlayerConstants.VOD_APPKEY);
        return "timestamp=" + timeStamp + "&" + "nonce=" + nonce + "&" + "sig=" + sig + "&" + "appid=" + SuperPlayerConstants.VOD_APPID;
    }

    public ArrayList<VideoModel> loadVideoInfoList(List<VideoInfo> videoInfoList) {
        ArrayList<VideoModel> list = new ArrayList<VideoModel>();
        if (videoInfoList == null || videoInfoList.size() == 0)
            return null;
        for (VideoInfo videoInfo : videoInfoList) {
            VideoModel model = new VideoModel();
            model.appid = SuperPlayerConstants.VOD_APPID;
            model.fileid = videoInfo.fileId;
            list.add(model);
        }
        return list;
    }

    private static String getMD5Encryption(String originString) {
        String result = "";
        if (originString != null) {
            try {
                // 指定加密的方式为MD5
                MessageDigest md = MessageDigest.getInstance("MD5");
                // 进行加密运算
                byte bytes[] = md.digest(originString.getBytes());
                StringBuilder sb = new StringBuilder(40);
                for (byte b : bytes) {
                    if ((b & 0xff) >> 4 == 0) {
                        sb.append("0").append(Integer.toHexString(b & 0xff));
                    } else {
                        sb.append(Integer.toHexString(b & 0xff));
                    }
                }
                result = sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
