package com.tencent.liteav.demo.superplayer.model.protocol;

import android.util.Log;

import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayInfoStream;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoClassification;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * V2 video information protocol parsing implementation class
 * <p>
 * Responsible for parsing the JSON data of the V2 video information protocol request response
 *
 * V2视频信息协议解析实现类
 * <p>
 * 负责解析V2视频信息协议请求响应的Json数据
 */
public class PlayInfoParserV2 implements IPlayInfoParser {
    private static final String TAG = "TCPlayInfoParserV2";

    private JSONObject mResponse;

    // Player configuration information
    private String                    mDefaultVideoClassification;
    private List<VideoClassification> mVideoClassificationList;

    private PlayImageSpriteInfo        mImageSpriteInfo;
    private List<PlayKeyFrameDescInfo> mKeyFrameDescInfo;
    // Video information
    private String                     mName;
    private PlayInfoStream             mSourceStream;
    private PlayInfoStream             mMasterPlayList;

    private LinkedHashMap<String, PlayInfoStream> mTranscodePlayList;   // Transcode video information list

    private String             mURL;
    private List<VideoQuality> mVideoQualityList;
    private VideoQuality       mDefaultVideoQuality;

    private String mCoverUrl;   // coverUrl

    public PlayInfoParserV2(JSONObject response) {
        mResponse = response;
        parsePlayInfo();
    }

    /**
     * Parse video information from the JSON data of the video information protocol request response.
     * <p>
     * Parsing process:
     * <p>
     * 1、Parse the player information (playerInfo) field to obtain the video quality list
     * {@link #mVideoClassificationList} and the default video quality {@link #mDefaultVideoClassification}.
     * <p>
     * 2、Parse the sprite information (imageSpriteInfo) field to obtain the sprite information
     * {@link #mImageSpriteInfo}.
     * <p>
     * 3、Parse the keyframe information (keyFrameDescInfo) field to obtain the keyframe
     * information {@link #mKeyFrameDescInfo}.
     * <p>
     * 4、Parse the video information (videoInfo) field to obtain the video name {@link #mName},
     * source video information {@link #mSourceStream}, main video list {@link #mMasterPlayList},
     * and transcoded video list {@link #mTranscodePlayList}.
     * <p>
     * 5、Parse the video playback URL {@link #mURL}, video quality information {@link #mVideoQualityList},
     * and default video quality {@link #mDefaultVideoQuality} from the main video list, transcoded video list,
     * and source video information.
     *
     * 从视频信息协议请求响应的Json数据中解析出视频信息
     * <p>
     * 解析流程：
     * <p>
     * 1、解析播放器信息(playerInfo)字段，获取视频清晰度列表{@link #mVideoClassificationList}以及默认清晰度{@link #mDefaultVideoClassification}
     * <p>
     * 2、解析雪碧图信息(imageSpriteInfo)字段，获取雪碧图信息{@link #mImageSpriteInfo}
     * <p>
     * 3、解析关键帧信息(keyFrameDescInfo)字段，获取关键帧信息{@link #mKeyFrameDescInfo}
     * <p>
     * 4、解析视频信息(videoInfo)字段，获取视频名称{@link #mName}、源视频信息{@link #mSourceStream}、
     * 主视频列表{@link #mMasterPlayList}、转码视频列表{@link #mTranscodePlayList}
     * <p>
     * 5、从主视频列表、转码视频列表、源视频信息中解析出视频播放url{@link #mURL}、画质信息{@link #mVideoQualityList}、
     * 默认画质{@link #mDefaultVideoQuality}
     */
    private void parsePlayInfo() {
        try {
            JSONObject playerInfo = mResponse.optJSONObject("playerInfo");
            if (playerInfo != null) {
                mDefaultVideoClassification = parseDefaultVideoClassification(playerInfo);
                mVideoClassificationList = parseVideoClassificationList(playerInfo);
            }
            JSONObject imageSpriteInfo = mResponse.optJSONObject("imageSpriteInfo");
            if (imageSpriteInfo != null) {
                mImageSpriteInfo = parseImageSpriteInfo(imageSpriteInfo);
            }
            JSONObject keyFrameDescInfo = mResponse.optJSONObject("keyFrameDescInfo");
            if (keyFrameDescInfo != null) {
                mKeyFrameDescInfo = parseKeyFrameDescInfo(keyFrameDescInfo);
            }
            JSONObject videoInfo = mResponse.optJSONObject("videoInfo");
            if (videoInfo != null) {
                mName = parseName(videoInfo);
                mSourceStream = parseSourceStream(videoInfo);
                mMasterPlayList = parseMasterPlayList(videoInfo);
                mTranscodePlayList = parseTranscodePlayList(videoInfo);
            }
            JSONObject coverInfo = mResponse.optJSONObject("coverInfo");
            if (coverInfo != null) {
                mCoverUrl = coverInfo.optString("coverUrl");
            }
            parseVideoInfo();
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * Parse default video quality information
     *
     * 解析默认视频清晰度信息
     */
    private String parseDefaultVideoClassification(JSONObject playerInfo) throws JSONException {
        return playerInfo.getString("defaultVideoClassification");
    }

    /**
     * Parse video quality information
     *
     * 解析视频清晰度信息
     */
    private List<VideoClassification> parseVideoClassificationList(JSONObject playerInfo) throws JSONException {
        List<VideoClassification> arrayList = new ArrayList<>();
        JSONArray videoClassificationArray = playerInfo.getJSONArray("videoClassification");
        if (videoClassificationArray != null) {
            for (int i = 0; i < videoClassificationArray.length(); i++) {
                JSONObject object = videoClassificationArray.optJSONObject(i);

                VideoClassification classification = new VideoClassification();
                classification.setId(object.getString("id"));
                classification.setName(object.getString("name"));

                List<Integer> definitionList = new ArrayList<>();
                JSONArray array = object.getJSONArray("definitionList");
                if (array != null) {
                    for (int j = 0; j < array.length(); j++) {
                        int definition = array.getInt(j);
                        definitionList.add(definition);
                    }
                }
                classification.setDefinitionList(definitionList);
                arrayList.add(classification);
            }
        }
        return arrayList;
    }

    /**
     * Parse sprite information
     *
     * 解析雪碧图信息
     */
    private PlayImageSpriteInfo parseImageSpriteInfo(JSONObject imageSpriteInfo) throws JSONException {
        JSONArray imageSpriteList = imageSpriteInfo.getJSONArray("imageSpriteList");
        if (imageSpriteList != null) {
            // Get the last one to parse.
            JSONObject spriteJSONObject = imageSpriteList.optJSONObject(imageSpriteList.length() - 1);
            PlayImageSpriteInfo info = new PlayImageSpriteInfo();
            info.webVttUrl = spriteJSONObject.getString("webVttUrl");
            JSONArray jsonArray = spriteJSONObject.getJSONArray("imageUrls");
            List<String> imageUrls = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String url = jsonArray.getString(i);
                imageUrls.add(url);
            }
            info.imageUrls = imageUrls;
            return info;
        }
        return null;
    }

    /**
     * Parse keyframe information
     *
     * 解析关键帧打点信息
     */
    private List<PlayKeyFrameDescInfo> parseKeyFrameDescInfo(JSONObject keyFrameDescInfo) throws JSONException {
        JSONArray jsonArr = keyFrameDescInfo.getJSONArray("keyFrameDescList");
        if (jsonArr != null) {
            List<PlayKeyFrameDescInfo> infoList = new ArrayList<>();
            for (int i = 0; i < jsonArr.length(); i++) {
                String content = jsonArr.optJSONObject(i).getString("content");
                long time = jsonArr.optJSONObject(i).getLong("timeOffset");
                float timeS = (float) (time / 1000.0);
                PlayKeyFrameDescInfo info = new PlayKeyFrameDescInfo();
                try {
                    info.content = URLDecoder.decode(content, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    info.content = "";
                }
                info.time = timeS;
                infoList.add(info);
            }
            return infoList;
        }
        return null;
    }

    /**
     * Parse video name
     *
     * 解析视频名称
     */
    private String parseName(JSONObject videoInfo) throws JSONException {
        JSONObject basicInfo = videoInfo.optJSONObject("basicInfo");
        if (basicInfo != null) {
            return basicInfo.getString("name");
        }
        return null;
    }

    /**
     * Parse source video stream information
     *
     * 解析源视频流信息
     */
    private PlayInfoStream parseSourceStream(JSONObject videoInfo) throws JSONException {
        JSONObject sourceVideo = videoInfo.optJSONObject("sourceVideo");
        if (sourceVideo != null) {
            PlayInfoStream stream = new PlayInfoStream();
            stream.url = sourceVideo.getString("url");
            stream.duration = sourceVideo.getInt("duration");
            stream.width = sourceVideo.getInt("width");
            stream.height = sourceVideo.getInt("height");
            stream.size = sourceVideo.getInt("size");
            stream.bitrate = sourceVideo.getInt("bitrate");
            return stream;
        }
        return null;
    }

    /**
     * Parse main playback video stream information
     *
     * 解析主播放视频流信息
     */
    private PlayInfoStream parseMasterPlayList(JSONObject videoInfo) throws JSONException {
        if (!videoInfo.has("masterPlayList")) {
            return null;
        }
        JSONObject masterPlayList = videoInfo.optJSONObject("masterPlayList");
        if (masterPlayList != null) {
            PlayInfoStream stream = new PlayInfoStream();
            stream.url = masterPlayList.getString("url");
            return stream;
        }
        return null;
    }

    /**
     * Parse transcode video stream information
     * <p>
     * The transcoded video stream information {@link #mTranscodePlayList} does not include the quality name,
     * so it needs to be matched with the video quality information {@link #mVideoClassificationList}.
     *
     * 解析转码视频流信息
     * <p>
     * 转码视频流信息{@link #mTranscodePlayList}中不包含清晰度名称，需要与视频清晰度信息{@link #mVideoClassificationList}做匹配
     *
     * @return Transcode video information list key: quality name value: video stream information
     *         转码视频信息列表 key: 清晰度名称 value: 视频流信息
     */
    private LinkedHashMap<String, PlayInfoStream> parseTranscodePlayList(JSONObject videoInfo) throws JSONException {
        List<PlayInfoStream> transcodeList = parseStreamList(videoInfo);
        if (transcodeList == null) {
            return mTranscodePlayList;
        }
        for (int i = 0; i < transcodeList.size(); i++) {
            PlayInfoStream stream = transcodeList.get(i);
            if (mVideoClassificationList != null) {
                for (int j = 0; j < mVideoClassificationList.size(); j++) {
                    VideoClassification classification = mVideoClassificationList.get(j);
                    List<Integer> definitionList = classification.getDefinitionList();
                    if (definitionList.contains(stream.definition)) {
                        stream.id = classification.getId();
                        stream.name = classification.getName();
                    }
                }
            }
        }
        // Remove duplicates by quality
        LinkedHashMap<String, PlayInfoStream> idList = new LinkedHashMap<>();
        for (int i = 0; i < transcodeList.size(); i++) {
            PlayInfoStream stream = transcodeList.get(i);
            if (!idList.containsKey(stream.id)) {
                idList.put(stream.id, stream);
            } else {
                PlayInfoStream copy = idList.get(stream.id);
                if (copy.getUrl().endsWith("mp4")) {
                    continue;
                }
                if (stream.getUrl().endsWith("mp4")) {
                    idList.remove(copy.id);
                    idList.put(stream.id, stream);
                }
            }
        }
        // Sort by quality
        return idList;
    }

    /**
     * Parse transcoded video information
     *
     * 解析转码视频信息
     */
    private List<PlayInfoStream> parseStreamList(JSONObject videoInfo) throws JSONException {
        List<PlayInfoStream> streamList = new ArrayList<>();
        JSONArray transcodeList = videoInfo.optJSONArray("transcodeList");
        if (transcodeList != null) {
            for (int i = 0; i < transcodeList.length(); i++) {
                JSONObject transcode = transcodeList.optJSONObject(i);
                PlayInfoStream stream = new PlayInfoStream();
                stream.url = transcode.getString("url");
                stream.duration = transcode.getInt("duration");
                stream.width = transcode.getInt("width");
                stream.height = transcode.getInt("height");
                stream.size = transcode.getInt("size");
                stream.bitrate = transcode.getInt("bitrate");
                stream.definition = transcode.getInt("definition");
                streamList.add(stream);
            }
        }
        return streamList;
    }

    /**
     * Parse video playback URL, video quality list, and default video quality.
     * <p>
     * The V2 protocol response JSON data may contain multiple video playback information: main playback video
     * information {@link #mMasterPlayList}, transcoded video information {@link #mTranscodePlayList}, source
     * video information {@link #mSourceStream}, and the playback priority decreases in turn
     * <p>
     * Parse the playback information from the highest priority video information
     *
     * 解析视频播放url、画质列表、默认画质
     * <p>
     * V2协议响应Json数据中可能包含多个视频播放信息：主播放视频信息{@link #mMasterPlayList}、转码视频{@link #mTranscodePlayList}、
     * 源视频{@link #mSourceStream}, 播放优先级依次递减
     * <p>
     * 从优先级最高的视频信息中解析出播放信息
     */
    private void parseVideoInfo() {
        // If there is main playback video information, parse the URL that supports multi-bitrate playback from it.
        if (mMasterPlayList != null) {
            mURL = mMasterPlayList.getUrl();
            if (mTranscodePlayList != null && mTranscodePlayList.size() != 0) {
                PlayInfoStream stream = mTranscodePlayList.get(mDefaultVideoClassification);
                mVideoQualityList = VideoQualityUtils.convertToVideoQualityList(mTranscodePlayList);
                mDefaultVideoQuality = VideoQualityUtils.convertToVideoQuality(stream);
            }
            return;
        }
        // If there is no main playback information, parse the stream information of each bitrate
        // from the transcode video information
        if (mTranscodePlayList != null && mTranscodePlayList.size() != 0) {
            PlayInfoStream stream = mTranscodePlayList.get(mDefaultVideoClassification);
            String videoURL = null;
            if (stream != null) {
                videoURL = stream.getUrl();
            } else {
                for (PlayInfoStream stream1 : mTranscodePlayList.values()) {
                    if (stream1 != null && stream1.getUrl() != null) {
                        stream = stream1;
                        videoURL = stream1.getUrl();
                        break;
                    }
                }
            }
            if (videoURL != null) {
                mVideoQualityList = VideoQualityUtils.convertToVideoQualityList(mTranscodePlayList);
                mDefaultVideoQuality = VideoQualityUtils.convertToVideoQuality(stream);
                mURL = videoURL;
                return;
            }
        }
        // If there is no main playback information or transcode information, parse the playback information
        // from the source video information
        if (mSourceStream != null) {
            if (mDefaultVideoClassification != null) {
                mDefaultVideoQuality = VideoQualityUtils.convertToVideoQuality(mSourceStream);
                mVideoQualityList = new ArrayList<>();
                mVideoQualityList.add(mDefaultVideoQuality);
            }
            mURL = mSourceStream.getUrl();
        }
    }

    /**
     * Get the video playback URL
     *
     * 获取视频播放url
     */
    @Override
    public String getURL() {
        return mURL;
    }

    @Override
    public String getEncryptedURL(PlayInfoConstant.EncryptedURLType type) {
        return null;
    }

    @Override
    public String getToken() {
        return null;
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


    public String getCoverUrl() {
        return mCoverUrl;
    }

    public int getDuration() {
        if (null != mSourceStream) {
            return mSourceStream.duration;
        }
        return 0;
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
        return mVideoQualityList;
    }

    /**
     * Get the default video quality information
     *
     * 获取默认画质信息
     */
    @Override
    public VideoQuality getDefaultVideoQuality() {
        return mDefaultVideoQuality;
    }

    /**
     * Get the video quality alias list
     *
     * 获取视频画质别名列表
     */
    @Override
    public List<ResolutionName> getResolutionNameList() {
        return null;
    }

    @Override
    public String getDRMType() {
        return "";
    }
}
