package com.tencent.liteav.demo.superplayer.model.protocol;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.EncryptedStreamingInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * V4 video protocol parsing implementation class
 * <p>
 * Responsible for parsing the JSON data of the V4 video information protocol request response.
 *
 * V4视频协议解析实现类
 * <p>
 * 负责解析V4视频信息协议请求响应的Json数据
 */
public class PlayInfoParserV4 implements IPlayInfoParser {

    private static final String TAG = "TCPlayInfoParserV4";

    private JSONObject mResponse;
    private String     mName;
    private String     mURL;
    private String     mToken;

    private String mDescription; // description
    private String mCoverUrl;   // coverUrl
    private int    mDuration;   // duration

    private List<EncryptedStreamingInfo> mEncryptedStreamingInfoList;
    private PlayImageSpriteInfo          mImageSpriteInfo;
    private List<PlayKeyFrameDescInfo>   mKeyFrameDescInfo;
    private List<ResolutionName>         mResolutionNameList;
    private String                       mDRMType;

    public PlayInfoParserV4(JSONObject response) {
        mResponse = response;
        parsePlayInfo();
    }

    private void parseSubStreams(JSONArray substreams) throws JSONException {
        if (substreams != null && substreams.length() > 0) {
            mResolutionNameList = new ArrayList<>();
            for (int i = 0; i < substreams.length(); i++) {
                JSONObject jsonObject = substreams.optJSONObject(i);
                ResolutionName resolutionName = new ResolutionName();
                int width = jsonObject.optInt("width");
                int height = jsonObject.optInt("height");
                resolutionName.width = width;
                resolutionName.height = height;
                resolutionName.name = jsonObject.optString("resolutionName");
                resolutionName.type = jsonObject.optString("type");
                mResolutionNameList.add(resolutionName);
            }
        }
    }

    /**
     * Parse video information from the JSON data of the video information protocol request response.
     *
     * 从视频信息协议请求响应的Json数据中解析出视频信息
     */
    private void parsePlayInfo() {
        try {
            JSONObject media = mResponse.optJSONObject("media");
            if (media != null) {
                // Parse video name
                JSONObject basicInfo = media.optJSONObject("basicInfo");
                if (basicInfo != null) {
                    mName = basicInfo.optString("name");
                    mDescription = basicInfo.optString("description");
                    mCoverUrl = basicInfo.optString("coverUrl");
                    mDuration = basicInfo.optInt("duration");
                }
                String audioVideoType = media.optString("audioVideoType");
                // Multi-bitrate video information
                if (TextUtils.equals(audioVideoType, "AdaptiveDynamicStream")) {
                    // Parse video playback URL
                    JSONObject streamingInfo = media.optJSONObject("streamingInfo");
                    if (streamingInfo != null) {
                        JSONObject plainoutObj = streamingInfo.optJSONObject("plainOutput"); // Unencrypted output
                        if (plainoutObj != null) {
                            // Parse the video URL directly if it is unencrypted
                            mURL = plainoutObj.optString("url");
                            parseSubStreams(plainoutObj.optJSONArray("subStreams"));
                        }
                        JSONArray drmoutputobj = streamingInfo.optJSONArray("drmOutput"); // Encrypted output
                        if (drmoutputobj != null && drmoutputobj.length() > 0) {
                            mEncryptedStreamingInfoList = new ArrayList<>();
                            for (int i = 0; i < drmoutputobj.length(); i++) {
                                JSONObject jsonObject = drmoutputobj.optJSONObject(i);
                                String drmType = jsonObject.optString("type");
                                String url = jsonObject.optString("url");
                                EncryptedStreamingInfo info = new EncryptedStreamingInfo();
                                info.drmType = drmType;
                                info.url = url;
                                mDRMType = drmType;
                                mEncryptedStreamingInfoList.add(info);
                                parseSubStreams(jsonObject.optJSONArray("subStreams"));
                            }
                        }
                        mToken = streamingInfo.optString("drmToken");
                    }
                } else if (TextUtils.equals(audioVideoType, "Transcode")) { // Transcode video information
                    JSONObject transCodeInfo = media.optJSONObject("transcodeInfo");
                    if (transCodeInfo != null) {
                        mURL = transCodeInfo.optString("url");
                    }
                } else if (TextUtils.equals(audioVideoType, "Original")) { // Original video information
                    JSONObject originalInfo = media.optJSONObject("originalInfo");
                    if (originalInfo != null) {
                        mURL = originalInfo.optString("url");
                    }
                }

                // Parse the sprite information
                JSONObject imageSpriteInfo = media.optJSONObject("imageSpriteInfo");
                if (imageSpriteInfo != null) {
                    mImageSpriteInfo = new PlayImageSpriteInfo();
                    mImageSpriteInfo.webVttUrl = imageSpriteInfo.getString("webVttUrl");
                    JSONArray jsonArray = imageSpriteInfo.optJSONArray("imageUrls");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        List<String> imageUrls = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String url = jsonArray.getString(i);
                            imageUrls.add(url);
                        }
                        mImageSpriteInfo.imageUrls = imageUrls;
                    }
                }
                parseKeyFrameDescList(media);
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void parseKeyFrameDescList(JSONObject media) {
        JSONObject keyFrameDescInfo = media.optJSONObject("keyFrameDescInfo");
        if (keyFrameDescInfo != null) {
            mKeyFrameDescInfo = new ArrayList<>();
            JSONArray keyFrameDescList = keyFrameDescInfo.optJSONArray("keyFrameDescList");
            if (keyFrameDescList != null && keyFrameDescList.length() > 0) {
                for (int i = 0; i < keyFrameDescList.length(); i++) {
                    JSONObject jsonObject = null;
                    jsonObject = keyFrameDescList.optJSONObject(i);
                    PlayKeyFrameDescInfo info = new PlayKeyFrameDescInfo();
                    if (jsonObject != null) {
                        info.time = jsonObject.optLong("timeOffset");
                        info.content = jsonObject.optString("content");
                        mKeyFrameDescInfo.add(info);
                    }
                }
            }
        }
    }

    /**
     * Get the video playback URL
     *
     * 获取视频播放url
     */
    @Override
    public String getURL() {
        String url = mURL;
        if (!TextUtils.isEmpty(mToken)) {
            url = getEncryptedURL(PlayInfoConstant.EncryptedURLType.SIMPLEAES);
        }
        return url;
    }

    @Override
    public String getEncryptedURL(PlayInfoConstant.EncryptedURLType type) {
        for (EncryptedStreamingInfo info : mEncryptedStreamingInfoList) {
            if (info.drmType != null && info.drmType.equalsIgnoreCase(type.getValue())) {
                return info.url;
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return TextUtils.isEmpty(mToken) ? null : mToken;
    }

    /**
     * Get the video name
     *
     * 获取视频名称
     */
    @Override
    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public int getDuration() {
        return mDuration;
    }

    /**
     * Get the sprite information
     *
     * 获取雪碧图信息
     */
    @Override
    public PlayImageSpriteInfo getImageSpriteInfo() {
        return mImageSpriteInfo;
    }

    /**
     * Get the keyframe information
     *
     * 获取关键帧信息
     */
    @Override
    public List<PlayKeyFrameDescInfo> getKeyFrameDescInfo() {
        return mKeyFrameDescInfo;
    }

    /**
     * Get the video quality information
     *
     * 获取画质信息
     */
    @Override
    public List<VideoQuality> getVideoQualityList() {
        return null;
    }

    /**
     * Get the default video quality information
     *
     * 获取默认画质信息
     */
    @Override
    public VideoQuality getDefaultVideoQuality() {
        return null;
    }

    /**
     * Get the video quality alias list
     *
     * 获取视频画质别名列表
     */
    @Override
    public List<ResolutionName> getResolutionNameList() {
        return mResolutionNameList;
    }

    @Override
    public String getDRMType() {
        return mDRMType;
    }
}
