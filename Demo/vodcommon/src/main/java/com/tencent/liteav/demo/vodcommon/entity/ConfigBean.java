package com.tencent.liteav.demo.vodcommon.entity;

import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodConstants;

public class ConfigBean {

    private boolean mIsEnableSelfAdaption;
    private boolean mIsEnableHardWareDecode;
    private int mLogLevel;
    private boolean mIsUseDash;
    private boolean mEnableAccurateSeek;
    private boolean mSmoothSwitchBitrate;
    private boolean mAutoRotate;
    private int mConnectRetryInterval;
    private boolean mEnableRenderProcess;
    private int mProgressInterval;
    private int mConnectRetryCount;
    private int mTimeout;
    private String mCacheFolderPath;
    private int mMaxCacheItems;
    private long mPreferredResolution;
    private int mMediaType;
    private int mMaxPreloadSize;
    private int mMaxBufferSize;
    private int mBitRateIndex;
    private static volatile ConfigBean mInstance;

    private ConfigBean() {
        reset();
    }

    public static ConfigBean getInstance() {
        if (mInstance == null) {
            synchronized (ConfigBean.class) {
                if (mInstance == null) {
                    mInstance = new ConfigBean();
                }
            }
        }
        return mInstance;
    }

    public void reset() {
        setEnableAccurateSeek(true);
        setSmoothSwitchBitrate(true);
        setAutoRotate(true);
        setEnableSelfAdaption(true);
        setEnableRenderProcess(true);
        setConnectRetryCount(3);
        setConnectRetryInterval(3);
        setTimeout(10);
        setProgressInterval(500);
        setCacheFolderPath(null);
        setMaxCacheItems(200);
        setMaxPreloadSize(50);
        setMaxBufferSize(50);
        setPreferredResolution(720 * 1280);
        setMediaType(TXVodConstants.MEDIA_TYPE_AUTO);
        setEnableHardWareDecode(true);
        setLogLevel(TXLiveConstants.LOG_LEVEL_VERBOSE);
    }

    public boolean isEnableSelfAdaption() {
        return mIsEnableSelfAdaption;
    }

    public void setEnableSelfAdaption(boolean isEnableSelfAdaption) {
        this.mIsEnableSelfAdaption = isEnableSelfAdaption;
    }

    public boolean isEnableHardWareDecode() {
        return mIsEnableHardWareDecode;
    }

    public void setEnableHardWareDecode(boolean enableHardWareDecode) {
        this.mIsEnableHardWareDecode = enableHardWareDecode;
    }

    public int getLogLevel() {
        return mLogLevel;
    }

    public void setLogLevel(int logLevel) {
        this.mLogLevel = logLevel;
    }

    public boolean isIsUseDash() {
        return mIsUseDash;
    }

    public void setsIsUseDash(boolean isUseDash) {
        this.mIsUseDash = isUseDash;
    }

    public boolean isEnableAccurateSeek() {
        return mEnableAccurateSeek;
    }

    public void setEnableAccurateSeek(boolean enableAccurateSeek) {
        this.mEnableAccurateSeek = enableAccurateSeek;
    }

    public boolean isSmoothSwitchBitrate() {
        return mSmoothSwitchBitrate;
    }

    public void setSmoothSwitchBitrate(boolean smoothSwitchBitrate) {
        this.mSmoothSwitchBitrate = smoothSwitchBitrate;
    }

    public boolean isAutoRotate() {
        return mAutoRotate;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.mAutoRotate = autoRotate;
    }

    public boolean isEnableRenderProcess() {
        return mEnableRenderProcess;
    }

    public void setEnableRenderProcess(boolean enableRenderProcess) {
        this.mEnableRenderProcess = enableRenderProcess;
    }

    public int getProgressInterval() {
        return mProgressInterval;
    }

    public void setProgressInterval(int progressInterval) {
        this.mProgressInterval = progressInterval;
    }

    public int getConnectRetryCount() {
        return mConnectRetryCount;
    }

    public void setConnectRetryCount(int connectRetryCount) {
        this.mConnectRetryCount = connectRetryCount;
    }

    public int getTimeout() {
        return mTimeout;
    }

    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    public String getCacheFolderPath() {
        return mCacheFolderPath;
    }

    public void setCacheFolderPath(String cacheFolderPath) {
        this.mCacheFolderPath = cacheFolderPath;
    }

    public int getMaxCacheItems() {
        return mMaxCacheItems;
    }

    public void setMaxCacheItems(int maxCacheItems) {
        this.mMaxCacheItems = maxCacheItems;
    }

    public long getPreferredResolution() {
        return mPreferredResolution;
    }

    public void setPreferredResolution(long preferredResolution) {
        this.mPreferredResolution = preferredResolution;
    }

    public int getMediaType() {
        return mMediaType;
    }

    public void setMediaType(int mediaType) {
        this.mMediaType = mediaType;
    }

    public int getConnectRetryInterval() {
        return mConnectRetryInterval;
    }

    public void setConnectRetryInterval(int connectRetryInterval) {
        this.mConnectRetryInterval = connectRetryInterval;
    }

    public int getMaxPreloadSize() {
        return mMaxPreloadSize;
    }

    public void setMaxPreloadSize(int maxPreloadSize) {
        this.mMaxPreloadSize = maxPreloadSize;
    }

    public int getMaxBufferSize() {
        return mMaxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.mMaxBufferSize = maxBufferSize;
    }
}