package com.tencent.liteav.demo.player.server;

import android.text.TextUtils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.player.common.utils.TCConstants;
import com.tencent.liteav.demo.player.common.utils.TCUtils;
import com.tencent.liteav.demo.player.superplayer.VideoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by vinsonswang on 2018/3/26.
 */

public class VideoDataMgr {
    private final String TAG = "VideoDataMgr";
    private static VideoDataMgr instance;
    private OkHttpClient okHttpClient;
    private PublishSigListener mPublishSigListener;
    private GetVideoInfoListListener mGetVideoInfoListListener;
    private ReportVideoInfoListener mReportVideoInfoListener;

    public static VideoDataMgr getInstance(){
        if(instance == null){
            instance = new VideoDataMgr();
        }
        return instance;
    }

    private VideoDataMgr(){
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)    // 设置超时时间
                .readTimeout(5, TimeUnit.SECONDS)       // 设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)      // 设置写入超时时间
                .build();
    }

    public void setPublishSigListener(PublishSigListener listener){
        mPublishSigListener = listener;
    }

    public void getPublishSig() {
        String sigParams = getSigParams();
        Request request = new Request.Builder()
                .url(Const.ADDRESS_SIG + "?" + sigParams)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "getPublishSig onFailure : " + e.toString());
                notifyGetPublishSigFail(Const.RetCode.CODE_REQUEST_ERR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // response.body().string()调用后，会把流关闭，因此只能调用一次
                String contentStr = response.body().string();
                TXCLog.i(TAG, "getPublishSig onResponse : " + contentStr);
                parseSigRes(contentStr);
            }
        });
    }

    private void parseSigRes(String sigRes) {
        if(TextUtils.isEmpty(sigRes)){
            notifyGetPublishSigFail(Const.RetCode.CODE_PARSE_ERR);
            TXCLog.e(TAG, "parseSigRes err, sigRes is empty!");
            return;
        }
        try {
            JSONObject resJson = new JSONObject(sigRes);
            int code = resJson.optInt("code");
            if(code != Const.RetCode.CODE_SUCCESS){
                TXCLog.e(TAG, "parseSigRes fail, code = " + code);
                notifyGetPublishSigFail(code);
                return;
            }
            JSONObject dataObj = resJson.getJSONObject("data");
            String signature = dataObj.optString("signature");
            if(TextUtils.isEmpty(signature)){
                TXCLog.e(TAG, "parseSigRes, after parse signature is empty!");
                notifyGetPublishSigFail(Const.RetCode.CODE_PARSE_ERR);
                return;
            }
            notifyGetPublishSigSuccess(signature);
        } catch (JSONException e) {
            e.printStackTrace();
            notifyGetPublishSigFail(Const.RetCode.CODE_PARSE_ERR);
        }
    }

    private void notifyGetPublishSigFail(int errCode){
        if(mPublishSigListener != null){
            mPublishSigListener.onFail(errCode);
        }
    }

    private void notifyGetPublishSigSuccess(String signatureStr){
        if(mPublishSigListener != null){
            mPublishSigListener.onSuccess(signatureStr);
        }
    }

    public void setReportVideoInfoListener(ReportVideoInfoListener reportVideoInfoListener){
        mReportVideoInfoListener = reportVideoInfoListener;
    }

    public void reportVideoInfo(String fileId, String authorName){
        FormBody formBody = new FormBody.Builder().
                add("author", authorName)
                .build();
        Request request = new Request.Builder()
                .url(Const.ADDRESS_VIDEO_REPORT + fileId + "?" + getSigParams())
                .put(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "reportVideoInfo onFailure : " + e.toString());
                notifyReportVideoInfoFail(Const.RetCode.CODE_REQUEST_ERR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String contentStr = response.body().string();
                TXCLog.i(TAG, "reportVideoInfo onResponse : " + contentStr);
                parseReportVideoResult(contentStr);
            }
        });
    }

    private void parseReportVideoResult(String contentStr) {
        if(TextUtils.isEmpty(contentStr)){
            TXCLog.e(TAG, "parseReportVideoResult err, contentStr is empty!");
            notifyReportVideoInfoFail(Const.RetCode.CODE_PARSE_ERR);
            return;
        }
        JSONObject resJson = null;
        try {
            resJson = new JSONObject(contentStr);
            int code = resJson.optInt("code");
            if(code != Const.RetCode.CODE_SUCCESS){
                TXCLog.e(TAG, "parseReportVideoResult fail, code = " + code);
                notifyGetPublishSigFail(code);
                return;
            }
            notifyReportVideoInfoSuc();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifyReportVideoInfoFail(int codeRequestErr) {
        if(mReportVideoInfoListener != null){
            mReportVideoInfoListener.onFail(codeRequestErr);
        }
    }

    private void notifyReportVideoInfoSuc(){
        if(mReportVideoInfoListener != null){
            mReportVideoInfoListener.onSuccess();
        }
    }

    public void setGetVideoInfoListListener(GetVideoInfoListListener getVideoInfoListListener){
        mGetVideoInfoListListener = getVideoInfoListListener;
    }

    public void getVideoList() {
        Request request = new Request.Builder()
                .url(Const.ADDRESS_VIDEO_LIST + "?page_num=0&page_size=20&query=test" + "&" + getSigParams())
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "getVideoList onFailure : " + e.toString());
                notifyGetVideoListFail(Const.RetCode.CODE_REQUEST_ERR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String contentStr = response.body().string();
                TXCLog.i(TAG, "getVideoList onResponse : " + contentStr);
                parseVideoList(contentStr);
            }
        });
    }

    private void parseVideoList(String contentStr) {
        if(TextUtils.isEmpty(contentStr)){

            TXCLog.e(TAG, "parseVideoList err, contentStr is empty");
            return;
        }
        try {
            JSONObject resObject = new JSONObject(contentStr);
            int code = resObject.optInt("code");
            if(code != Const.RetCode.CODE_SUCCESS){
                TXCLog.e(TAG, "parseVideoList fail, code = " + code);
                notifyGetVideoListFail(code);
                return;
            }
            JSONObject dataObj = resObject.getJSONObject("data");
            JSONArray list = dataObj.getJSONArray("list");
            List<VideoInfo> videoInfoList = new ArrayList<>();
            for(int i = 0; i < list.length(); i++){
                JSONObject videoInfoJSONObject = list.getJSONObject(i);
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

    private void notifyGetVideoListFail(int errCode){
        if(mGetVideoInfoListListener != null){
            mGetVideoInfoListListener.onFail(errCode);
        }
    }

    private void notifyGetVideoListSuccess(List<VideoInfo> videoInfoList){
        if(mGetVideoInfoListListener != null){
            mGetVideoInfoListListener.onGetVideoInfoList(videoInfoList);
        }
    }

    public void getVideoInfo() {
        Request request = new Request.Builder()
                .url(Const.ADDRESS_VIDEO_INFO + "")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                TXCLog.e(TAG, "getVideoInfo onFailure : " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                TXCLog.i(TAG, "getVideoInfo onResponse : " + response.body().string());
//                parseVideoInfo();
            }
        });
    }

    /**
     * 访问服务器需要加鉴权
     *
     * @return
     */
    private String getSigParams() {
        long timeStamp = System.currentTimeMillis()/1000;
        String nonce = "";
        String sig = "";
        try {
            nonce = TCUtils.getMD5Encryption(String.valueOf(System.currentTimeMillis()));
            sig = TCUtils.getMD5Encryption(TCConstants.VOD_APPID + String.valueOf(timeStamp) + nonce + TCConstants.VOD_APPKEY);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sigParams = "timestamp=" + timeStamp
                + "&" + "nonce=" + nonce
                + "&" + "sig=" + sig
                + "&" + "appid=" + TCConstants.VOD_APPID;
        return sigParams;
    }

    public ArrayList<VideoModel> loadVideoInfoList(List<VideoInfo> videoInfoList) {
        ArrayList<VideoModel> list = new ArrayList<VideoModel>();
        if (videoInfoList == null || videoInfoList.size() == 0)
            return null;
        for (VideoInfo videoInfo : videoInfoList) {
            VideoModel model = new VideoModel();
            model.appid = TCConstants.VOD_APPID;
            model.fileid = videoInfo.fileId;
            list.add(model);
        }
        return list;
    }
}
