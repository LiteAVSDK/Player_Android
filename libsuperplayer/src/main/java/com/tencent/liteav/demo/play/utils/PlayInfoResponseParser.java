package com.tencent.liteav.demo.play.utils;

import com.tencent.liteav.demo.play.bean.TCPlayImageSpriteInfo;
import com.tencent.liteav.demo.play.bean.TCPlayInfoStream;
import com.tencent.liteav.demo.play.bean.TCPlayKeyFrameDescInfo;
import com.tencent.liteav.demo.play.bean.TCVideoClassification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by annidy on 2017/12/13.
 */

public class PlayInfoResponseParser {
    protected JSONObject response;

    public PlayInfoResponseParser(JSONObject response) {
        this.response = response;
    }

    /**
     * 获取服务器下发的播放地址
     *
     * @return 播放地址
     */
    public String playUrl() {
        if (getMasterPlayList() != null) {
            return getMasterPlayList().url;
        }
        if (getStreamList().size() != 0) {
            return getStreamList().get(0).url;
        }
        if (getSource() != null) {
            return getSource().url;
        }
        return null;
    }


    /**
     * 获取封面图片
     *
     * @return 图片url
     */
    public String coverUrl() {
        try {
            JSONObject coverInfo = response.getJSONObject("coverInfo");
            if (coverInfo != null) {
                return coverInfo.getString("coverUrl");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TCPlayInfoStream> getStreamList() {
        ArrayList<TCPlayInfoStream> streamList = new ArrayList<>();
        try {
            JSONObject videoInfo = response.getJSONObject("videoInfo");
            if (!videoInfo.has("transcodeList"))
                return streamList;
            JSONArray transcodeList = response.getJSONObject("videoInfo").getJSONArray("transcodeList");
            if (transcodeList != null) {
                for (int i = 0; i < transcodeList.length(); i++) {
                    JSONObject transcode = transcodeList.getJSONObject(i);

                    TCPlayInfoStream stream = new TCPlayInfoStream();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return streamList;
    }

    public TCPlayInfoStream getSource() {
        try {
            JSONObject sourceVideo = response.getJSONObject("videoInfo").getJSONObject("sourceVideo");

            TCPlayInfoStream stream = new TCPlayInfoStream();
            stream.url = sourceVideo.getString("url");
            stream.duration = sourceVideo.getInt("duration");
            stream.width = sourceVideo.getInt("width");
            stream.height = sourceVideo.getInt("height");
            stream.size = sourceVideo.getInt("size");
            stream.bitrate = sourceVideo.getInt("bitrate");

            return stream;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TCPlayImageSpriteInfo getImageSpriteInfo() {
        try {
            JSONObject obj = response.getJSONObject("imageSpriteInfo");
            if (obj!=null) {
              JSONArray imageSpriteList =   obj.getJSONArray("imageSpriteList");
              if (imageSpriteList!=null) {
                  JSONObject spriteJSONObject = imageSpriteList.getJSONObject(imageSpriteList.length() -1); //获取最后一个来解析
                  TCPlayImageSpriteInfo info = new TCPlayImageSpriteInfo();
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
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            // ignore
        }
        return null;
    }


    public List<TCPlayKeyFrameDescInfo> getKeyFrameDescInfos() {
        try {
            JSONObject obj = response.getJSONObject("keyFrameDescInfo");
            if (obj!=null) {
                JSONArray jsonArr = obj.getJSONArray("keyFrameDescList");
                if (jsonArr!=null) {
                    List<TCPlayKeyFrameDescInfo> infos = new ArrayList<>();

                    for (int i = 0; i < jsonArr.length(); i++) {
                       String content = jsonArr.getJSONObject(i).getString("content");
                       long time = jsonArr.getJSONObject(i).getLong("timeOffset");
                       float timeS = (float) (time / 1000.0);//转换为秒
                       TCPlayKeyFrameDescInfo info = new TCPlayKeyFrameDescInfo();
                        try {
                            info.content = URLDecoder.decode(content,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info.content = "";
                        }
                        info.time = timeS;
                        infos.add(info);
                    }
                    return infos;
                }
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            // ignore
        }
        return null;
    }

    public TCPlayInfoStream getMasterPlayList() {
        try {
            JSONObject videoInfo = response.getJSONObject("videoInfo");
            if (!videoInfo.has("masterPlayList"))
                return null;

            JSONObject masterPlayList = response.getJSONObject("videoInfo").getJSONObject("masterPlayList");

            TCPlayInfoStream stream = new TCPlayInfoStream();
            stream.url = masterPlayList.getString("url");

            return stream;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视频名称
     *
     * @return
     */
    public String name() {
        try {
            JSONObject basicInfo = response.getJSONObject("videoInfo").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视频描述
     *
     * @return
     */
    public String description() {
        try {
            JSONObject basicInfo = response.getJSONObject("videoInfo").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("description");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取默认播放清晰度
     *
     * @return
     */
    public String getDefaultVideoClassification() {
        try {
            return response.getJSONObject("playerInfo").getString("defaultVideoClassification");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取transcode类型视频清晰度匹配列表
     *
     * @return
     */
    public List<TCVideoClassification> getVideoClassificationList() {
        ArrayList<TCVideoClassification> arrayList = new ArrayList<>();
        try {
            JSONArray videoClassificationArray = response.getJSONObject("playerInfo").getJSONArray("videoClassification");
            if (videoClassificationArray != null) {
                for (int i = 0; i < videoClassificationArray.length(); i++) {
                    JSONObject object = videoClassificationArray.getJSONObject(i);

                    TCVideoClassification classification = new TCVideoClassification();
                    classification.setId(object.getString("id"));
                    classification.setName(object.getString("name"));

                    ArrayList definitionList = new ArrayList();
                    JSONArray array = object.getJSONArray("definitionList");
                    if (array != null) {
                        for (int j = 0; j < array.length(); j++) {
                            int definiaton = array.getInt(j);
                            definitionList.add(definiaton);
                        }
                    }
                    classification.setDefinitionList(definitionList);
                    arrayList.add(classification);
                }
            }
            return arrayList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedHashMap<String, TCPlayInfoStream> getTranscodePlayList() {
        List<TCVideoClassification> classificationList = getVideoClassificationList();
        List<TCPlayInfoStream> transcodeList = getStreamList();
        if (transcodeList != null) {
            for (int i = 0; i < transcodeList.size(); i++) {
                TCPlayInfoStream stream = transcodeList.get(i);

                // 寻找匹配的清晰度
                if (classificationList != null) {
                    for (int j = 0; j < classificationList.size(); j++) {
                        TCVideoClassification classification = classificationList.get(j);
                        ArrayList<Integer> definitionList = classification.getDefinitionList();
                        if (definitionList.contains(stream.definition)) {
                            stream.id = classification.getId();
                            stream.name = classification.getName();
                        }
                    }
                }
            }
        }
        //清晰度去重
        LinkedHashMap<String, TCPlayInfoStream> idList = new LinkedHashMap<>();
        for (int i = 0; i < transcodeList.size(); i++) {
            TCPlayInfoStream stream = transcodeList.get(i);
            if (!idList.containsKey(stream.id)) {
                idList.put(stream.id, stream);
            } else {
                TCPlayInfoStream copy = idList.get(stream.id);
                if (copy.getUrl().endsWith("mp4")) {  // 列表中url是mp4，则进行下一步
                    continue;
                }
                if (stream.getUrl().endsWith("mp4")) { // 新判断的url是mp4，则替换列表中
                    idList.remove(copy);
                    idList.put(stream.id, stream);
                }
            }
        }
        //按清晰度排序
        return idList;
    }
}
