package com.tencent.liteav.demo.player.demo.shortvideo.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataBeanParser {
    protected JSONObject response;

    public DataBeanParser(JSONObject response) {
        this.response = response;
    }

    /**
     * 获取视频名称
     *
     * @return
     */
    public String name() {
        try {
            JSONObject basicInfo = response.getJSONObject("media").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("name");
            }
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
    public String coverUrl() {
        try {
            JSONObject basicInfo = response.getJSONObject("media").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("coverUrl");
            }
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
    public String url() {
        try {
            JSONObject basicInfo = response.getJSONObject("media").getJSONObject("streamingInfo").getJSONObject("plainOutput");
            if (basicInfo != null) {
                return basicInfo.getString("url");
            }
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
    public int duration() {
        try {
            JSONObject basicInfo = response.getJSONObject("media").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getInt("duration");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<SubStreamsDTO> getSubStreamDTOArray() {
        List<SubStreamsDTO> subStreamsDTOList = new ArrayList<>();
        try {
            JSONArray jsonElements = response.getJSONObject("media").getJSONObject("streamingInfo").getJSONObject("plainOutput").getJSONArray("subStreams");
            for (int i = 0; i < jsonElements.length(); i++) {
                String type = jsonElements.getJSONObject(i).getString("type");
                subStreamsDTOList.add(new SubStreamsDTO(type));
            }
            return subStreamsDTOList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}


