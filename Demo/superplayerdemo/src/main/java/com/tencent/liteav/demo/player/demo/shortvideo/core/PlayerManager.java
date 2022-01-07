package com.tencent.liteav.demo.player.demo.shortvideo.core;

import android.content.Context;
import android.util.Log;

import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static final String TAG = "ShortVideoDemo:PlayerManager";
    private final static int sMaxPlayerSize = 10;
    private Map<ShortVideoBean, TXVodPlayerWrapper> mUrlPlayerMap;

    private ShortVideoBean mLastPlayedVideoBean;
    private Context mContext;
    private volatile static PlayerManager mInstance;


    private PlayerManager(Context context) {
        mContext = context.getApplicationContext();
        mUrlPlayerMap = new HashMap<>();
    }

    public static PlayerManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PlayerManager.class) {
                if (mInstance == null) {
                    mInstance = new PlayerManager(context);
                }
            }
        }
        return mInstance;
    }

    public void updateManager(List<ShortVideoBean> shortVideoBeanList) {
        if (shortVideoBeanList == null || shortVideoBeanList.isEmpty()) {
            return;
        }
        if (shortVideoBeanList.size() > sMaxPlayerSize) {
            throw new IllegalArgumentException("shortVideoBeanList is larger than sMaxPlayerSize");
        }

        List<ShortVideoBean> lastBeanList = playedShortVideoBean();
        Log.i(TAG, " [updateManager]" + ",urlList = " + shortVideoBeanList.toString() + ",lastBeanList = " + lastBeanList.toString());

        //找到 lastUrlList中不包含urlList的 lastUrlList为上次传进来的urlList urlList为这次传进来的urlList
        List<ShortVideoBean> exprList = findDiffBeanList(shortVideoBeanList, lastBeanList);
        //找到 urlList中不包含lastUrlList的
        List<ShortVideoBean> newList = findDiffBeanList(lastBeanList, shortVideoBeanList);
        if (exprList != null) {
            for (int i = 0; i < exprList.size(); i++) {
                Log.i(TAG, "[updateManager] exprUrl " + exprList.get(i).videoURL);
            }
        }
        if (newList != null) {
            for (int i = 0; i < newList.size(); i++) {
                Log.i(TAG, "[updateManager] newUrl " + newList.get(i).videoURL);
            }
        }
        if (newList.size() > 0) {
            for (int i = 0; i < newList.size(); i++) {
                TXVodPlayerWrapper tempPlayer = null;
                if (exprList.size() > 0) {
                    tempPlayer = mUrlPlayerMap.remove(exprList.remove(0));
                }
                if (tempPlayer == null) {
                    tempPlayer = new TXVodPlayerWrapper(mContext);
                }

                tempPlayer.preStartPlay(newList.get(i));

                mUrlPlayerMap.put(newList.get(i), tempPlayer);
            }
        }

        if (exprList.size() > 0) {
            for (int i = 0; i < exprList.size(); i++) {
                TXVodPlayerWrapper exprPlayer = mUrlPlayerMap.get(exprList.get(i));
                mUrlPlayerMap.remove(exprList.get(i));
                exprPlayer.stopPlay();
                exprPlayer = null;
            }
        }

        if (shortVideoBeanList.contains(mLastPlayedVideoBean)) {
            Log.i(TAG, " [updateManager]" + ",mLastPlayedBean = " + mLastPlayedVideoBean.videoURL);
            if (mUrlPlayerMap.get(mLastPlayedVideoBean) != null) {
                mUrlPlayerMap.get(mLastPlayedVideoBean).preStartPlay(mLastPlayedVideoBean);
            }
        }
    }

    public TXVodPlayerWrapper getPlayer(ShortVideoBean bean) {
        mLastPlayedVideoBean = bean;
        return mUrlPlayerMap.get(bean);
    }


    private List<ShortVideoBean> findDiffBeanList(List<ShortVideoBean> playUrlList, List<ShortVideoBean> lastPlayUrlList) {
        List<ShortVideoBean> exprList = new ArrayList<>();
        for (int i = 0; i < lastPlayUrlList.size(); i++) {
            if (!playUrlList.contains(lastPlayUrlList.get(i))) {
                exprList.add(lastPlayUrlList.get(i));
            }
        }
        return exprList;
    }

    public void releasePlayer() {
        for (TXVodPlayerWrapper txVodPlayerWrapper : mUrlPlayerMap.values()) {
            txVodPlayerWrapper.stopPlay();
        }
        mUrlPlayerMap.clear();
        mInstance = null;
    }

    private List<ShortVideoBean> playedShortVideoBean() {
        List<ShortVideoBean> urlList = new ArrayList<>();
        for (ShortVideoBean bean : mUrlPlayerMap.keySet()) {
            urlList.add(bean);
        }
        return urlList;
    }
}
