package com.tencent.liteav.demo.superplayer.model;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.liteav.demo.superplayer.SubtitleSourceModel;
import com.tencent.liteav.demo.superplayer.SuperPlayerCode;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.ResolutionName;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.protocol.IPlayInfoProtocol;
import com.tencent.liteav.demo.superplayer.model.protocol.IPlayInfoRequestCallback;
import com.tencent.liteav.demo.superplayer.model.protocol.PlayInfoParams;
import com.tencent.liteav.demo.superplayer.model.protocol.PlayInfoProtocolV2;
import com.tencent.liteav.demo.superplayer.model.protocol.PlayInfoProtocolV4;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXPlayInfoParams;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXTrackInfo;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.rtmp.ui.TXSubtitleView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuperPlayerImpl implements SuperPlayer, ITXVodPlayListener, ITXLivePlayListener {

    private static final String TAG                   = "SuperPlayerImpl";
    private static final int    SUPERPLAYER_MODE      = 1;
    private static final int    SUPPORT_MAJOR_VERSION = 8;
    private static final int    SUPPORT_MINOR_VERSION = 5;
    private static final int    PLAY_BACK_WARD_SEEK_INTERVAL_SECOND = 5;
    private static final int    PLAY_FOR_WARD_SPEED_RATE = 3;
    private static final int    PLAY_BACK_WARD_PLAY_INTERVAL_MILLI_SECOND = 1000;

    private Context                    mContext;
    private TXCloudVideoView           mVideoView;        // Tencent Cloud video playback view
    private IPlayInfoProtocol          mCurrentProtocol; // Current video information protocol class
    private TXVodPlayer                mVodPlayer;
    private TXVodPlayConfig            mVodPlayConfig;
    private TXLivePlayer               mLivePlayer;
    private TXLivePlayConfig           mLivePlayConfig;
    private ISuperPlayerListener       mSuperPlayerListener;
    private SuperPlayerModel           mCurrentModel;
    private SuperPlayerObserver        mObserver;
    private VideoQuality               mVideoQuality;
    private SuperPlayerDef.PlayerType  mCurrentPlayType     = SuperPlayerDef.PlayerType.VOD;
    private SuperPlayerDef.PlayerMode  mCurrentPlayMode     = SuperPlayerDef.PlayerMode.WINDOW;
    private SuperPlayerDef.PlayerState mCurrentPlayState    = SuperPlayerDef.PlayerState.INIT;
    private String                     mCurrentPlayVideoURL;
    // Record the playback time when switching to hardware decoding
    private int                        mSeekPos;
    private float                      mStartPos;
    private long                       mMaxLiveProgressTime;      // Maximum viewing time for live streaming
    private boolean                    mIsAutoPlay          = true;
    private boolean                    mIsPlayWithFileId;      // Whether it is Tencent Cloud fileId playback
    // Flag before receiving the first keyframe after switching to hardware decoding
    private boolean                    mChangeHWAcceleration;
    private String                     mFileId;
    private int                        mAppId;
    private int                        mPlayAction;
    private boolean                    isPrepared           = false;
    private boolean                    isNeedResume         = false;
    private boolean                    mNeedToPause         = false;
    private int                        mCurrentIndex        = -1;
    private TXTrackInfo                mSelectedSoundTrackInfo;
    private TXTrackInfo                mSelectedSubtitleTrackInfo;
    private boolean                    mIsContinuePlayBackSeek = false;
    private int                        mCurrentPosition     = 0;
    private Handler                    mHandler;
    private float                      mCurrentSpeedRate    = 1;

    public SuperPlayerImpl(Context context, TXCloudVideoView videoView) {
        initialize(context, videoView);
    }

    /**
     * Live player event callback
     *
     * 直播播放器事件回调
     */
    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "TXLivePlayer onPlayEvent event: " + event + ", "
                    + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            Log.d(TAG, playEventLog);
        }
        switch (event) {
            case TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED:
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT:
            case TXLiveConstants.PLAY_EVT_PLAY_END:
                if (mCurrentPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
                    updatePlayerType(SuperPlayerDef.PlayerType.LIVE);
                    onError(SuperPlayerCode.LIVE_SHIFT_FAIL, "time shift failed, return to live");
                    updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                } else {
                    stop();
                    updatePlayerState(SuperPlayerDef.PlayerState.END);
                    if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                        onError(SuperPlayerCode.NET_ERROR, "poor network, click to retry");
                    } else {
                        onError(SuperPlayerCode.LIVE_PLAY_END, param.getString(TXLiveConstants.EVT_DESCRIPTION));
                    }
                }
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                updatePlayerState(SuperPlayerDef.PlayerState.LOADING);
                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME:
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                mObserver.onRcvFirstIframe();
                break;
            case TXLiveConstants.PLAY_EVT_STREAM_SWITCH_SUCC:
                updateStreamEndStatus(true, SuperPlayerDef.PlayerType.LIVE, mVideoQuality);
                break;
            case TXLiveConstants.PLAY_ERR_STREAM_SWITCH_FAIL:
                updateStreamEndStatus(false, SuperPlayerDef.PlayerType.LIVE, mVideoQuality);
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
                mMaxLiveProgressTime = progress > mMaxLiveProgressTime ? progress : mMaxLiveProgressTime;
                updatePlayProgress(progress / 1000, mMaxLiveProgressTime / 1000,0);
                break;
            default:
                break;
        }
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onLivePlayEvent(event, param);
        }
    }

    /**
     * Live player network status callback
     *
     * 直播播放器网络状态回调
     */
    @Override
    public void onNetStatus(Bundle bundle) {
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onLiveNetStatus(bundle);
        }
    }

    /**
     * On-demand player network status callback
     *
     * 点播播放器网络状态回调
     */
    @Override
    public void onNetStatus(TXVodPlayer player, Bundle bundle) {
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onVodNetStatus(player, bundle);
        }
    }

    @Override
    public void setNeedToPause(boolean value) {
        mNeedToPause = value;
    }

    @Override
    public void playBackward(int position) {
        mIsContinuePlayBackSeek = true;
        mCurrentPosition = position;
        playBackwardSeek();
    }

    @Override
    public void playForward() {
        mVodPlayer.setRate(PLAY_FOR_WARD_SPEED_RATE);
    }

    @Override
    public void revertSpeedRate() {
        mIsContinuePlayBackSeek = false;
        mVodPlayer.setRate(mCurrentSpeedRate);
    }

    private void playBackwardSeek() {
        mCurrentPosition = mCurrentPosition - PLAY_BACK_WARD_SEEK_INTERVAL_SECOND;
        if (mCurrentPosition < 0) {
            mCurrentPosition = 0;
        }
        mVodPlayer.seek(mCurrentPosition);
    }

    /**
     * On-demand player event callback
     *
     * 点播播放器事件回调
     */
    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            String playEventLog = "TXVodPlayer onPlayEvent event: " + event + ", " + param.getString(TXLiveConstants.EVT_DESCRIPTION);
            Log.d(TAG, playEventLog);
        }
        switch (event) {
            case TXVodConstants.VOD_PLAY_EVT_GET_PLAYINFO_SUCC:
                mCurrentPlayVideoURL = param.getString(TXVodConstants.EVT_PLAY_URL);
                PlayImageSpriteInfo playImageSpriteInfo = new PlayImageSpriteInfo();
                playImageSpriteInfo.imageUrls = param.getStringArrayList(TXVodConstants.EVT_IMAGESPRIT_IMAGEURL_LIST);
                playImageSpriteInfo.webVttUrl = param.getString(TXVodConstants.EVT_IMAGESPRIT_WEBVTTURL);
                ArrayList<String> keyFrameContentList =
                        param.getStringArrayList(TXVodConstants.EVT_KEY_FRAME_CONTENT_LIST);
                float[] keyFrameTimeArray = param.getFloatArray(TXVodConstants.EVT_KEY_FRAME_TIME_LIST);
                List<PlayKeyFrameDescInfo> keyFrameDescInfoList = null;
                if (keyFrameContentList != null && keyFrameTimeArray != null
                        && keyFrameContentList.size() == keyFrameTimeArray.length) {
                    keyFrameDescInfoList = new ArrayList<>();
                    for (int i = 0; i < keyFrameContentList.size(); i++) {
                        PlayKeyFrameDescInfo frameDescInfo = new PlayKeyFrameDescInfo();
                        frameDescInfo.content = keyFrameContentList.get(i);
                        frameDescInfo.time = keyFrameTimeArray[i];
                        keyFrameDescInfoList.add(frameDescInfo);
                    }
                }
                updateVideoImageSpriteAndKeyFrame(playImageSpriteInfo,keyFrameDescInfoList);

                String waterMarkText = param.getString(TXVodConstants.EVT_KEY_WATER_MARK_TEXT);
                long videoDuration = param.getInt(TXVodConstants.EVT_PLAY_DURATION);
                onRcvWaterMark(waterMarkText, videoDuration);
                break;
            case TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED:
                onVodPlayPrepared();
                getAudioTrackInfo();
                getSubTitleTrackInfo();
                chooseLastTrackInfo();
                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME:
                Log.i(TAG, "PLAY_EVT_RCV_FIRST_I_FRAME");
                if (mNeedToPause) {
                    return;
                }
                // After switching the software and hardware decoders, re-seek the position
                if (mChangeHWAcceleration) {
                    Log.i(TAG, "seek pos:" + mSeekPos);
                    seek(mSeekPos);
                    mChangeHWAcceleration = false;
                }
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                mObserver.onRcvFirstIframe();
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_END:
                Log.i(TAG, "PLAY_EVT_PLAY_END");
                updatePlayerState(SuperPlayerDef.PlayerState.END);
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
                int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
                int playable = param.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS);
                if (duration != 0) {
                    updatePlayProgress(progress / 1000, duration / 1000, playable / 1000);
                }
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                Log.i(TAG, "PLAY_EVT_PLAY_LOADING");
                updatePlayerState(SuperPlayerDef.PlayerState.LOADING);
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                if (mNeedToPause) {
                    pause();
                    return;
                }
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                break;
            case TXVodConstants.VOD_PLAY_EVT_SELECT_TRACK_COMPLETE:
                int trackIndex = param.getInt(TXVodConstants.EVT_KEY_SELECT_TRACK_INDEX);
                int errorCode = param.getInt(TXVodConstants.EVT_KEY_SELECT_TRACK_ERROR_CODE);
                Log.d(TAG, "receive VOD_PLAY_EVT_SELECT_TRACK_COMPLETE, trackIndex="
                        + trackIndex + " ,errorCode=" + errorCode);
                break;
            case TXVodConstants.VOD_PLAY_EVT_SEEK_COMPLETE:
                if (mIsContinuePlayBackSeek) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playBackwardSeek();
                        }
                    }, PLAY_BACK_WARD_PLAY_INTERVAL_MILLI_SECOND);
                }
                break;
            default:
                break;
        }
        if (event < 0) {
            // Failed to play on-demand file
            mVodPlayer.stopPlay(true);
            updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
            onError(SuperPlayerCode.VOD_PLAY_FAIL, param.getString(TXLiveConstants.EVT_DESCRIPTION));
        }
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onVodPlayEvent(player, event, param);
        }
    }

    private void getAudioTrackInfo() {
        List<TXTrackInfo> soundTrackInfo = mVodPlayer.getAudioTrackInfo();
        mObserver.onRcvTrackInformation(soundTrackInfo);
    }

    private void getSubTitleTrackInfo() {
        List<TXTrackInfo> subtitleTrackInfo = mVodPlayer.getSubtitleTrackInfo();
        List<TXTrackInfo> finalSubtitleTrackInfo = new ArrayList<>();
        for (TXTrackInfo trackInfo : subtitleTrackInfo) {
            if (!trackInfo.isInternal) {
                finalSubtitleTrackInfo.add(trackInfo);
            }
        }
        mObserver.onRcvSubTitleTrackInformation(finalSubtitleTrackInfo);
    }

    private void onVodPlayPrepared() {
        Log.i(TAG, "PLAY_EVT_VOD_PLAY_PREPARED");
        isPrepared = true;
        List<TXBitrateItem> bitrateItems = mVodPlayer.getSupportedBitrates();
        int bitrateItemSize = bitrateItems != null ? bitrateItems.size() : 0;
        if (bitrateItemSize > 0) {
            // MasterPlaylist multi-bitrate, sorted by bitrate from low to high
            Collections.sort(bitrateItems);
            List<VideoQuality> videoQualities = new ArrayList<>();
            for (int i = 0; i < bitrateItemSize; i++) {
                TXBitrateItem bitrateItem = bitrateItems.get(i);
                VideoQuality quality = VideoQualityUtils.convertToVideoQuality(mContext, bitrateItem);
                videoQualities.add(quality);
            }
            int bitrateIndex = mVodPlayer.getBitrateIndex();
            VideoQuality defaultQuality = null;
            for (VideoQuality quality : videoQualities) {
                if (quality.index == bitrateIndex) {
                    defaultQuality = quality;
                }
            }
            updateVideoQualityList(videoQualities, defaultQuality);
        }
        if (mNeedToPause) {
            pauseVod();
            return;
        }
        if (isNeedResume) {
            mVodPlayer.resume();
        }
    }

    private void initialize(Context context, TXCloudVideoView videoView) {
        mContext = context;
        mVideoView = videoView;
        mHandler = new Handler();
        initLivePlayer(mContext);
        initVodPlayer(mContext);
    }

    private void initVodPlayer(Context context) {
        mVodPlayer = new TXVodPlayer(context);
        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
        mVodPlayConfig = new TXVodPlayConfig();

        if (TXPlayerGlobalSetting.getCacheFolderPath() == null
                || TXPlayerGlobalSetting.getCacheFolderPath().equals("")) {
            File sdcardDir = context.getExternalFilesDir(null);
            TXPlayerGlobalSetting.setCacheFolderPath(sdcardDir.getPath() + "/txcache");
        }
        mVodPlayConfig.setPreferredResolution(720 * 1280);
        TXPlayerGlobalSetting.setMaxCacheSize(config.maxCacheSizeMB);
        mVodPlayConfig.setHeaders(config.headers);
        mVodPlayer.setConfig(mVodPlayConfig);
        mVodPlayer.setRenderMode(config.renderMode);
        mVodPlayer.setVodListener(this);
        mVodPlayer.enableHardwareDecode(config.enableHWAcceleration);
        mVodPlayer.setSubtitleStyle(config.txSubtitleRenderModel);
        mVodPlayer.setRate(config.playRate);
        mVodPlayer.setMute(config.mute);
        mVodPlayer.setMirror(config.mirror);
    }

    private void initLivePlayer(Context context) {
        mLivePlayer = new TXLivePlayer(context);
        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
        mLivePlayConfig = new TXLivePlayConfig();
        mLivePlayConfig.setHeaders(config.headers);
        mLivePlayer.setConfig(mLivePlayConfig);
        mLivePlayer.setRenderMode(config.renderMode);
        mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mLivePlayer.setPlayListener(this);
        mLivePlayer.enableHardwareDecode(config.enableHWAcceleration);
        mLivePlayer.setMute(config.mute);
    }

    /**
     * Play video
     *
     * 播放视频
     */
    public void playWithModel(SuperPlayerModel model) {
        reset();
        updateVideoImageSpriteAndKeyFrame(null, null);
        mCurrentProtocol = null;
        if (!TextUtils.isEmpty(model.url)
                || (model.multiURLs != null
                 && !model.multiURLs.isEmpty())) {
            playWithUrl(model);
            sendRequestToUpdateVideoImageSpriteAndKeyFrame(model);
        } else if (model.videoId != null) {
            playWithFileId(model);
        } else if (model.videoIdV2 != null) {
            // Play according to fileId
            PlayInfoParams params = new PlayInfoParams();
            params.appId = model.appId;
            params.fileId = model.videoIdV2.fileId;
            params.videoIdV2 = model.videoIdV2;
            mCurrentProtocol = new PlayInfoProtocolV2(params);
            mFileId = params.fileId;
            mAppId = params.appId;
            sendRequest(model);
        }
        addSubtitle();
    }

    @Deprecated
    private void sendRequest(final SuperPlayerModel model) {
        mCurrentProtocol.sendRequest(new IPlayInfoRequestCallback() {
            @Override
            public void onSuccess(IPlayInfoProtocol protocol, PlayInfoParams param) {
                if (mCurrentModel != model) {
                    return;
                }
                Log.i(TAG, "onSuccess: protocol params = " + param.toString());
                IPlayInfoProtocol tmpProtocol = mCurrentProtocol;
                if (tmpProtocol == null) {
                    return;
                }
                mVodPlayer.setPlayerView(mVideoView);
                playModeVideo(tmpProtocol);
                updatePlayerType(SuperPlayerDef.PlayerType.VOD);
                updatePlayProgress(0, model.duration,0);
                updateVideoImageSpriteAndKeyFrame(tmpProtocol.getImageSpriteInfo(), tmpProtocol.getKeyFrameDescInfo());
            }

            @Override
            public void onError(int errCode, String message) {
                Log.i(TAG, "onFail: errorCode = " + errCode + " message = " + message);
                SuperPlayerImpl.this.onError(SuperPlayerCode.VOD_REQUEST_FILE_ID_FAIL,
                        "failed to play video file code = " + errCode + " msg = " + message);
            }
        });
    }

    private void playWithUrl(SuperPlayerModel model) {
        mAppId = model.appId;
        if (model.videoId != null) {
            mFileId = model.videoId.fileId;
        }
        String videoURL = null;
        List<VideoQuality> videoQualities = model.videoQualityList;
        VideoQuality defaultVideoQuality = null;
        if (model.multiURLs != null && !model.multiURLs.isEmpty()) {    // Play multiple bitrate URL
            int i = 0;
            for (SuperPlayerModel.SuperPlayerURL superPlayerURL : model.multiURLs) {
                if (i == model.playDefaultIndex) {
                    videoURL = superPlayerURL.url;
                }
            }
            if (videoQualities != null && model.playDefaultIndex < videoQualities.size()) {
                defaultVideoQuality = videoQualities.get(model.playDefaultIndex);
            }
        } else if (!TextUtils.isEmpty(model.url)) { // Play in traditional URL mode
            videoURL = model.url;
        }
        if (TextUtils.isEmpty(videoURL)) {
            onError(SuperPlayerCode.PLAY_URL_EMPTY, "failed to play video, the playback link is empty");
            return;
        }
        // Live player: normal RTMP stream playback and webrtc
        if (isRTMPPlay(videoURL) || isWebrtcPlay(videoURL)) {
            mLivePlayer.setPlayerView(mVideoView);
            playLiveURL(videoURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
        } else if (isFLVPlay(videoURL)) {
            // Live player: live FLV stream playback
            mLivePlayer.setPlayerView(mVideoView);
            playTimeShiftLiveURL(model.appId, videoURL);
            if (model.multiURLs != null && !model.multiURLs.isEmpty()) {
                startMultiStreamLiveURL(videoURL);
            }
        } else {
            // On-demand player: play on-demand files
            mVodPlayer.setPlayerView(mVideoView);
            playVodURL(videoURL);
        }
        boolean isLivePlay = (isRTMPPlay(videoURL) || isFLVPlay(videoURL) || isWebrtcPlay(videoURL));
        updatePlayerType(isLivePlay ? SuperPlayerDef.PlayerType.LIVE : SuperPlayerDef.PlayerType.VOD);
        updatePlayProgress(0, model.duration,0);
        updateVideoQualityList(videoQualities, defaultVideoQuality);
    }

    /**
     * Play v4 protocol video
     *
     * 播放v4协议视频
     */
    private void playWithFileId(SuperPlayerModel model) {
        if (model == null || model.videoId == null) {
            return;
        }
        if (mVodPlayer != null) {
            mVodPlayer.setPlayerView(mVideoView);
            mVodPlayer.setStartTime(mStartPos);
            mVodPlayer.setAutoPlay(mIsAutoPlay);
            if (mPlayAction == PLAY_ACTION_AUTO_PLAY || mPlayAction == PLAY_ACTION_MANUAL_PLAY) {
                mVodPlayer.setAutoPlay(true);
            } else if (mPlayAction == PLAY_ACTION_PRELOAD) {
                mVodPlayer.setAutoPlay(false);
                mPlayAction = PLAY_ACTION_AUTO_PLAY;
            }
            mVodPlayer.setVodListener(this);
            TXPlayInfoParams params = new TXPlayInfoParams(model.appId, model.videoId.fileId, model.videoId.pSign);
            mVodPlayer.startVodPlay(params);
        }
        mIsPlayWithFileId = true;
        updatePlayerType(SuperPlayerDef.PlayerType.VOD);
        updatePlayProgress(0, model.duration,0);
    }

    /**
     * Play v2 protocol video
     *
     * 播放v2协议视频
     */
    @Deprecated
    private void playModeVideo(IPlayInfoProtocol protocol) {
        playVodURL(protocol.getUrl());
        List<VideoQuality> videoQualities = protocol.getVideoQualityList();
        List<ResolutionName> resolutionNames = protocol.getResolutionNameList();
        String videoUrl = protocol.getUrl();
        VideoQuality defaultVideoQuality = protocol.getDefaultVideoQuality();
        updateVideoQualityList(videoQualities, defaultVideoQuality);
    }

    /**
     * Play non-v2 and v4 protocol video
     *
     * 播放非v2和v4协议视频
     */
    private void playModeVideo(SuperPlayerModel model) {
        if (model.multiURLs != null && !model.multiURLs.isEmpty()) {
            for (int i = 0; i < model.multiURLs.size(); i++) {
                if (i == model.playDefaultIndex) {
                    playVodURL(model.multiURLs.get(i).url);
                }
            }
        } else if (!TextUtils.isEmpty(model.url)) {
            playVodURL(model.url);
        } else if (model.videoId != null) {
            playWithFileId(model);
        }
        addSubtitle();
    }

    /**
     * Play live URL
     *
     * 播放直播URL
     */
    private void playLiveURL(String url, int playType) {
        mCurrentPlayVideoURL = url;
        if (mLivePlayer != null) {
            mLivePlayer.setPlayListener(this);
            mLivePlayer.setPlayerView(mVideoView);
            int result = mLivePlayer.startLivePlay(url, playType);
            if (result != 0) {
                Log.e(TAG, "playLiveURL videoURL:" + url + ",result:" + result);
            } else {
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
            }
        }
    }

    /**
     * Play on-demand URL
     *
     * 播放点播url
     */
    private void playVodURL(String url) {
        if (url == null || "".equals(url)) {
            return;
        }
        mCurrentPlayVideoURL = url;
        if (mVodPlayer != null) {
            mVodPlayer.setStartTime(mStartPos);
            mVodPlayer.setAutoPlay(mIsAutoPlay);
            if (mPlayAction == PLAY_ACTION_AUTO_PLAY || mPlayAction == PLAY_ACTION_MANUAL_PLAY) {
                mVodPlayer.setAutoPlay(true);
            } else if (mPlayAction == PLAY_ACTION_PRELOAD) {
                mVodPlayer.setAutoPlay(false);
                mPlayAction = PLAY_ACTION_AUTO_PLAY;
            }
            mVodPlayer.setVodListener(this);
            String drmType = "plain";
            if (mCurrentProtocol != null) {
                Log.d(TAG, "TOKEN: " + mCurrentProtocol.getToken());
                mVodPlayer.setToken(mCurrentProtocol.getToken());
                String type = mCurrentProtocol.getDRMType();
                if (type != null && !type.isEmpty()) {
                    drmType = type;
                }
            } else {
                mVodPlayer.setToken(null);
            }
            if (mCurrentIndex != -1) {
                mVodPlayer.setBitrateIndex(mCurrentIndex);
            }
            if (url.startsWith("http") && !TextUtils.isEmpty(mFileId) && mAppId != 0 
                && isVersionSupportAppendUrl() && canAppendCustomQuery(url)) {
                Uri uri = Uri.parse(url);
                String query = uri.getQuery();
                if (query == null || query.isEmpty()) {
                    query = "";
                } else {
                    query = query + "&";
                    if (query.contains("spfileid") || query.contains("spdrmtype") || query.contains("spappid")) {
                        Log.e(TAG, "url contains superplay key. " + query);
                    }
                }
                query += "spfileid=" + mFileId + "&spdrmtype=" + drmType + "&spappid=" + mAppId;
                Uri newUri = uri.buildUpon().query(query).build();
                Log.i(TAG, "playVodURL: newurl = " + Uri.decode(newUri.toString()) + " ;url= " + url);
                mVodPlayer.startVodPlay(Uri.decode(newUri.toString()));
            } else {
                mVodPlayer.startVodPlay(url);
            }
        }
        mIsPlayWithFileId = false;
    }

    private boolean canAppendCustomQuery(String url) {
        if (url.contains("spfileid") || url.contains("spdrmtype") || url.contains("spappid")) {
            return false;
        } else {
            return true;
        }
    }

    private void sendRequestToUpdateVideoImageSpriteAndKeyFrame(SuperPlayerModel model) {
        PlayInfoParams params = new PlayInfoParams();
        params.appId = mCurrentModel.appId;
        if (model.videoId != null) {
            params.fileId = mCurrentModel.videoId.fileId;
            params.videoId = mCurrentModel.videoId;
            mCurrentProtocol = new PlayInfoProtocolV4(params);
            mCurrentProtocol.sendRequest(new IPlayInfoRequestCallback() {
                @Override
                public void onSuccess(IPlayInfoProtocol protocol, PlayInfoParams param) {
                    IPlayInfoProtocol tmpProtocol = mCurrentProtocol;
                    if (tmpProtocol == null) {
                        return;
                    }
                    updateVideoImageSpriteAndKeyFrame(tmpProtocol.getImageSpriteInfo(),
                            tmpProtocol.getKeyFrameDescInfo());
                }

                @Override
                public void onError(int errCode, String message) {

                }
            });
        }
    }

    private boolean isVersionSupportAppendUrl() {
        String strVersion = TXLiveBase.getSDKVersionStr();
        String[] strVers = strVersion.split("\\.");
        if (strVers.length <= 1) {
            return false;
        }
        int majorVer = 0;
        int minorVer = 0;
        try {
            majorVer = Integer.parseInt(strVers[0]);
            minorVer = Integer.parseInt(strVers[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parse version failed.", e);
            majorVer = 0;
            minorVer = 0;
        }
        Log.i(TAG, strVersion + " , " + majorVer + " , " + minorVer);
        return majorVer > SUPPORT_MAJOR_VERSION || (majorVer == SUPPORT_MAJOR_VERSION && minorVer >= SUPPORT_MINOR_VERSION);
    }

    /**
     * Play time-shifted live URL
     *
     * 播放时移直播url
     */
    private void playTimeShiftLiveURL(int appId, String url) {
        final String bizid = url.substring(url.indexOf("//") + 2, url.indexOf("."));
        final String domian = SuperPlayerGlobalConfig.getInstance().playShiftDomain;
        final String streamid = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        Log.i(TAG, "bizid:" + bizid + ",streamid:" + streamid + ",appid:" + appId);
        playLiveURL(url, TXLivePlayer.PLAY_TYPE_LIVE_FLV);
        int bizidNum = -1;
        try {
            bizidNum = Integer.parseInt(bizid);
        } catch (NumberFormatException e) {
            Log.e(TAG, "playTimeShiftLiveURL: bizidNum error = " + bizid);
        }
    }

    /**
     * Configure multi-bitrate URLs
     *
     * 配置多码流url
     */
    private void startMultiStreamLiveURL(String url) {
        mLivePlayConfig.setAutoAdjustCacheTime(false);
        mLivePlayConfig.setMaxAutoAdjustCacheTime(5);
        mLivePlayConfig.setMinAutoAdjustCacheTime(5);
        mLivePlayer.setConfig(mLivePlayConfig);
        if (mObserver != null) {
            mObserver.onPlayTimeShiftLive(mLivePlayer, url);
        }
    }

    /**
     * Update playback progress
     *
     * 更新播放进度
     *
     * @param current  Current playback progress (seconds)
     *                 当前播放进度(秒)
     * @param duration Total duration (seconds)
     *                 总时长(秒)
     */
    private void updatePlayProgress(long current, long duration, long playable) {
        if (mObserver != null) {
            mObserver.onPlayProgress(current, duration, playable);
        }
    }

    /**
     * Update playback type
     *
     * 更新播放类型
     */
    private void updatePlayerType(SuperPlayerDef.PlayerType playType) {
        if (playType != mCurrentPlayType) {
            mCurrentPlayType = playType;
        }
        if (mObserver != null) {
            mObserver.onPlayerTypeChange(playType);
        }
    }

    /**
     * Update playback status
     *
     * 更新播放状态
     */
    private void updatePlayerState(SuperPlayerDef.PlayerState playState) {
        mCurrentPlayState = playState;
        if (mObserver == null) {
            return;
        }
        switch (playState) {
            case INIT:
                mObserver.onPlayPrepare();
                break;
            case PLAYING:
                mObserver.onPlayBegin(getPlayName());
                break;
            case PAUSE:
                mObserver.onPlayPause();
                break;
            case LOADING:
                mObserver.onPlayLoading();
                break;
            case END:
                mObserver.onPlayStop();
                break;
        }
    }

    private void updateStreamStartStatus(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
        if (mObserver != null) {
            mObserver.onSwitchStreamStart(success, playerType, quality);
        }
    }

    private void updateStreamEndStatus(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
        if (mObserver != null) {
            mObserver.onSwitchStreamEnd(success, playerType, quality);
        }
    }

    private void updateVideoQualityList(List<VideoQuality> videoQualities, VideoQuality defaultVideoQuality) {
        if (mObserver != null) {
            mObserver.onVideoQualityListChange(videoQualities, defaultVideoQuality);
        }
    }

    private void updateVideoImageSpriteAndKeyFrame(PlayImageSpriteInfo info, List<PlayKeyFrameDescInfo> list) {
        if (mObserver != null) {
            mObserver.onVideoImageSpriteAndKeyFrameChanged(info, list);
        }
    }

    private void onError(int code, String message) {
        if (mObserver != null) {
            mObserver.onError(code, message);
        }
    }

    private void onRcvWaterMark(String text, long duration) {
        if (mObserver != null) {
            mObserver.onRcvWaterMark(text, duration);
        }
    }

    private String getPlayName() {
        String title = "";
        if (mCurrentModel != null && !TextUtils.isEmpty(mCurrentModel.title)) {
            title = mCurrentModel.title;
        } else if (mCurrentProtocol != null && !TextUtils.isEmpty(mCurrentProtocol.getName())) {
            title = mCurrentProtocol.getName();
        }
        return title;
    }

    /**
     * Whether it is RTMP protocol
     *
     * 是否是RTMP协议
     */
    private boolean isRTMPPlay(String videoURL) {
        return !TextUtils.isEmpty(videoURL) && videoURL.startsWith("rtmp");
    }

    private boolean isWebrtcPlay(String videoURL) {
        return !TextUtils.isEmpty(videoURL) && videoURL.startsWith("webrtc");
    }

    /**
     * Whether it is HTTP-FLV protocol
     *
     * 是否是HTTP-FLV协议
     */
    private boolean isFLVPlay(String videoURL) {
        return (!TextUtils.isEmpty(videoURL) && videoURL.startsWith("http://")
                || videoURL.startsWith("https://")) && videoURL.contains(".flv");
    }

    @Override
    public void play(SuperPlayerModel model) {
        mPlayAction = model.playAction;
        mCurrentModel = model;
        playWithModel(model);
    }

    @Override
    public void reStart() {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.LIVE || mCurrentPlayType == SuperPlayerDef.PlayerType.LIVE_SHIFT) {
            if (isRTMPPlay(mCurrentPlayVideoURL)) {
                playLiveURL(mCurrentPlayVideoURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
            } else if (isFLVPlay(mCurrentPlayVideoURL)) {
                playTimeShiftLiveURL(mCurrentModel.appId, mCurrentPlayVideoURL);
                if (mCurrentModel.multiURLs != null && !mCurrentModel.multiURLs.isEmpty()) {
                    startMultiStreamLiveURL(mCurrentPlayVideoURL);
                }
            }
        } else {
            if (mCurrentPlayVideoURL != null) {
                playVodURL(mCurrentPlayVideoURL);
            } else {
                playWithModel(mCurrentModel);
            }
            addSubtitle();
        }
    }

    private void chooseLastTrackInfo() {
        if (mSelectedSoundTrackInfo != null) {
            onClickSoundTrackItem(mSelectedSoundTrackInfo);
        }
        if (mSelectedSubtitleTrackInfo != null) {
            onClickSubTitleItem(mSelectedSubtitleTrackInfo);
        }
    }

    private void addSubtitle() {
        if (mCurrentModel.subtitleSourceModelList != null) {
            for (SubtitleSourceModel subtitleSourceModel : mCurrentModel.subtitleSourceModelList) {
                mVodPlayer.addSubtitleSource(subtitleSourceModel.url,
                        subtitleSourceModel.name, subtitleSourceModel.mimeType);
            }
        }
    }

    private String obtGetString(int resourceId) {
        if (null != mContext) {
            mContext.getString(resourceId);
        }
        return "";
    }

    @Override
    public void pause() {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.pause();
        } else {
            mLivePlayer.pause();
        }
        updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
    }

    @Override
    public void pauseVod() {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.pause();
        }
        updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
    }

    @Override
    public void resume() {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            isNeedResume = true;
            if (isPrepared) {
                mVodPlayer.resume();
            }
        } else {
            mLivePlayer.resume();
        }
        updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
    }

    @Override
    public void resumeLive() {
        updatePlayerType(SuperPlayerDef.PlayerType.LIVE);
    }

    @Override
    public void stop() {
        mCurrentIndex = -1;
        resetPlayer();
        updatePlayerState(SuperPlayerDef.PlayerState.END);
    }

    @Override
    public void reset() {
        mCurrentIndex = -1;
        resetPlayer();
        updatePlayerState(SuperPlayerDef.PlayerState.INIT);
    }

    private void resetPlayer() {
        isPrepared = false;
        isNeedResume = false;
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(false);
        }
        if (mLivePlayer != null) {
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(true);
            mVideoView.removeVideoView();
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * Restore mirror settings
     * Restore playback speed
     *
     * 对镜像设置进行还原处理
     * 对播放速度进行还原处理
     */
    @Override
    public void revertSettings() {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.setMirror(false);
            mVodPlayer.setRate(1.0f);
        }
    }


    @Override
    public void switchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
        if (mCurrentPlayMode == playerMode) {
            return;
        }
        mCurrentPlayMode = playerMode;
    }

    @Override
    public void enableHardwareDecode(boolean enable) {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.enableHardwareDecode(enable);
            if (mCurrentPlayState != SuperPlayerDef.PlayerState.END) {
                mChangeHWAcceleration = true;
                mSeekPos = (int) mVodPlayer.getCurrentPlaybackTime();
                Log.i(TAG, "save pos:" + mSeekPos);
                resetPlayer();
                // When the protocol is empty, it means that the current played video is non-v2 and v4 video
                if (mCurrentProtocol == null) {
                    playModeVideo(mCurrentModel);
                } else {
                    playModeVideo(mCurrentProtocol);
                }
            }
        } else {
            mLivePlayer.enableHardwareDecode(enable);
            playWithModel(mCurrentModel);
        }
    }

    @Override
    public void setPlayerView(TXCloudVideoView videoView) {
        mVideoView = videoView;
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.setPlayerView(videoView);
        } else {
            mLivePlayer.setPlayerView(videoView);
        }
    }

    @Override
    public void seek(int position) {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            if (mVodPlayer != null) {
                mVodPlayer.seek(position);
                if (!mVodPlayer.isPlaying()) {
                    mVodPlayer.resume();
                }
            }
        } else {
            updatePlayerType(SuperPlayerDef.PlayerType.LIVE_SHIFT);
        }
        if (mObserver != null) {
            mObserver.onSeek(position);
        }
    }

    @Override
    public void snapshot(TXLivePlayer.ITXSnapshotListener listener) {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.snapshot(listener);
        } else if (mCurrentPlayType == SuperPlayerDef.PlayerType.LIVE) {
            mLivePlayer.snapshot(listener);
        } else {
            listener.onSnapshot(null);
        }
    }

    @Override
    public void setRate(float speedLevel) {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.setRate(speedLevel);
            mCurrentSpeedRate = speedLevel;
            mIsContinuePlayBackSeek = false;
        }

    }

    @Override
    public void setMirror(boolean isMirror) {
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            mVodPlayer.setMirror(isMirror);
        }
    }

    @Override
    public void switchStream(VideoQuality quality) {
        mVideoQuality = quality;
        if (mCurrentPlayType == SuperPlayerDef.PlayerType.VOD) {
            if (mVodPlayer != null) {
                if (quality.url != null) { // br!=0;index=-1;url!=null   //br=0;index!=-1;url!=null
                    // Manual seek is required for non-multi-bitrate m3u8 sub-streams
                    if (mCurrentPlayState != SuperPlayerDef.PlayerState.END) {
                        float currentTime = mVodPlayer.getCurrentPlaybackTime();
                        mVodPlayer.stopPlay(true);
                        Log.i(TAG, "onQualitySelect quality.url:" + quality.url);
                        mVodPlayer.setStartTime(currentTime);
                        mVodPlayer.startVodPlay(quality.url);
                    }
                } else { //br!=0;index!=-1;url=null
                    Log.i(TAG, "setBitrateIndex quality.index:" + quality.index);
                    // Automatic seamless seek for multi-bitrate m3u8 sub-strea
                    mVodPlayer.setBitrateIndex(quality.index);
                    mCurrentIndex = quality.index;
                }
                updateStreamStartStatus(true, SuperPlayerDef.PlayerType.VOD, quality);
            }
        } else {
            boolean success = false;
            if (mLivePlayer != null && !TextUtils.isEmpty(quality.url)) {
                int result = mLivePlayer.switchStream(quality.url);
                success = result >= 0;
            }
            updateStreamStartStatus(success, SuperPlayerDef.PlayerType.LIVE, quality);
        }
    }

    @Override
    public String getPlayURL() {
        return mCurrentPlayVideoURL;
    }

    @Override
    public SuperPlayerDef.PlayerMode getPlayerMode() {
        return mCurrentPlayMode;
    }

    @Override
    public SuperPlayerDef.PlayerState getPlayerState() {
        return mCurrentPlayState;
    }

    @Override
    public SuperPlayerDef.PlayerType getPlayerType() {
        return mCurrentPlayType;
    }

    @Override
    public void setObserver(SuperPlayerObserver observer) {
        mObserver = observer;
    }

    @Override
    public void setSuperPlayerListener(ISuperPlayerListener superPlayerListener) {
        mSuperPlayerListener = superPlayerListener;
    }

    @Override
    public void setLoop(boolean isLoop) {
        mVodPlayer.setLoop(isLoop);
    }

    @Override
    public void setStartTime(float startPos) {
        this.mStartPos = startPos;
        mVodPlayer.setStartTime(startPos);
    }

    @Override
    public void setAutoPlay(boolean isAutoPlay) {
        this.mIsAutoPlay = isAutoPlay;
        mVodPlayer.setAutoPlay(isAutoPlay);
    }

    @Override
    public void onClickSoundTrackItem(TXTrackInfo clickInfo) {
        List<TXTrackInfo> soundTrackInfoList = mVodPlayer.getAudioTrackInfo();
        mVodPlayer.setMute(clickInfo.trackIndex == -1);
        if (clickInfo.trackIndex == -1) {
            for (TXTrackInfo trackInfo : soundTrackInfoList) {
                mVodPlayer.deselectTrack(trackInfo.trackIndex);
            }
        } else {
            for (TXTrackInfo trackInfo : soundTrackInfoList) {
                if (trackInfo.trackIndex == clickInfo.trackIndex) {
                    mVodPlayer.selectTrack(trackInfo.trackIndex);
                    mSelectedSoundTrackInfo = trackInfo;
                } else {
                    mVodPlayer.deselectTrack(trackInfo.trackIndex);
                }
            }
        }
    }

    @Override
    public void onClickSubTitleItem(TXTrackInfo clickInfo) {
        List<TXTrackInfo> subtitleTrackInfoList = mVodPlayer.getSubtitleTrackInfo();
        if (clickInfo.trackIndex == -1) {
            for (TXTrackInfo trackInfo : subtitleTrackInfoList) {
                mVodPlayer.deselectTrack(trackInfo.trackIndex);
            }
        } else {
            for (TXTrackInfo trackInfo : subtitleTrackInfoList) {
                if (trackInfo.trackIndex == clickInfo.trackIndex) {
                    mVodPlayer.selectTrack(trackInfo.trackIndex);
                    mSelectedSubtitleTrackInfo = trackInfo;
                } else {
                    mVodPlayer.deselectTrack(trackInfo.trackIndex);
                }
            }
        }
    }

    @Override
    public void setSubTitleView(TXSubtitleView subTitleView) {
        mVodPlayer.setSubtitleView(subTitleView);
    }

    @Override
    public void onSubtitleSettingDone(TXSubtitleRenderModel model) {
        mVodPlayer.setSubtitleStyle(model);
    }

}
