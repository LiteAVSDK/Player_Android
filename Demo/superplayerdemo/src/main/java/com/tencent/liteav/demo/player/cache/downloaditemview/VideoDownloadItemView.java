package com.tencent.liteav.demo.player.cache.downloaditemview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.cache.adapter.VideoDownloadHelper;
import com.tencent.liteav.demo.vodcommon.entity.SuperVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadListener;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadCenter;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * video download list item view
 */
public class VideoDownloadItemView extends RelativeLayout implements VideoDownloadListener {

    private static final String TAG = "VideoDownloadItemView";

    private TextView  mTvVideoDurationView;
    private TextView  mTvVideoNameView;
    private ImageView mIvVideoCoverView;
    private TextView  mTvVideoCacheProgressView;
    private TextView  mTvVideoCacheStateTextView;
    private View      mVVideoCacheStateIconView;
    private TextView  mTvVideoQualityView;

    private TXVodDownloadMediaInfo mMediaInfo;
    private VideoDownloadHelper    mVideoDownloadHelper;
    private TextView  mVideoSizeTV;

    private VideoModel mVideoModel;

    public VideoDownloadItemView(Context context) {
        this(context, null);
    }

    public VideoDownloadItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDownloadItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.cache_video_list_item_layout, this);
        mTvVideoDurationView = findViewById(R.id.cache_tv_video_duration);
        mTvVideoNameView = findViewById(R.id.cache_tv_video_title);
        mVideoSizeTV = findViewById(R.id.cache_tv_cache_video_size);
        mIvVideoCoverView = findViewById(R.id.cache_iv_cache_video_cover);
        mTvVideoCacheProgressView = findViewById(R.id.cache_tv_cache_progress);
        mTvVideoCacheStateTextView = findViewById(R.id.cache_tv_status_text);
        mVVideoCacheStateIconView = findViewById(R.id.cache_v_status_indicator);
        mTvVideoQualityView = findViewById(R.id.cache_tv_video_quality);
    }

    public void setVideoCacheHelper(VideoDownloadHelper videoDownloadHelper) {
        this.mVideoDownloadHelper = videoDownloadHelper;
    }

    /**
     * Set video download information.
     *
     * 设置视频下载信息
     */
    public void setVideoInfo(TXVodDownloadMediaInfo mediaInfo) {
        this.mMediaInfo = mediaInfo;

        notifyUnRegisterCacheListener();
        notifyRegisterCacheListener();

        final VideoModel videoModel = new VideoModel();
        videoModel.isEnableDownload = true;
        if (null != mediaInfo.getDataSource()) {
            TXVodDownloadDataSource dataSource = mediaInfo.getDataSource();
            videoModel.appid = dataSource.getAppId();
            videoModel.fileid = dataSource.getFileId();
            videoModel.pSign = dataSource.getPSign();

            mTvVideoQualityView.setText(getQualityText(dataSource.getQuality()));
            mVideoSizeTV.setText(byteToMBStr((long)mediaInfo.getDownloadSize()));

            SuperVodListLoader loader = mVideoDownloadHelper.getLoader();
            loader.getVodByFileId(videoModel, new SuperVodListLoader.OnVodInfoLoadListener() {
                @Override
                public void onSuccess(final VideoModel videoModel) {
                    if (TextUtils.isEmpty(videoModel.title)) {
                        videoModel.title = SuperVodListLoader.VideoInfoHolder.getInstance(getContext()).get(videoModel.fileid);
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mVideoDownloadHelper.updateRequestStatus();
                            int duration = videoModel.duration;
                            mTvVideoDurationView.setText(mVideoDownloadHelper.formattedTime(duration));
                            String title = getFileNameNoEx(videoModel.title);
                            mTvVideoNameView.setText(title);
                            if (TextUtils.isEmpty(videoModel.placeholderImage)) {
                                Glide.with(getContext()).load(R.drawable.superplayer_default_cover_thumb)
                                        .into(mIvVideoCoverView);
                            } else {
                                Glide.with(getContext()).load(videoModel.placeholderImage).into(mIvVideoCoverView);
                            }
                        }
                    });
                }

                @Override
                public void onFail(int errCode) {
                    mVideoDownloadHelper.showNoNetWorkTip(getContext());
                }
            });
        } else {
            VideoDownloadCenter.getInstance()
                    .getDownloadMediaInfo(mediaInfo.getUrl(), new VideoDownloadCenter.OnMediaInfoFetchListener() {
                        @Override
                        public void onReady(TXVodDownloadMediaInfo mediaInfo) {
                            int duration = mediaInfo.getDuration() / 1000;
                            mTvVideoDurationView.setText(mVideoDownloadHelper.formattedTime(duration));
                            Glide.with(getContext())
                                    .load(VideoDownloadHelper.DEFAULT_DOWNLOAD_VIDEO_COVER)
                                    .into(mIvVideoCoverView);
                            mTvVideoNameView.setText(getContext()
                                    .getResources().getString(R.string.superplayer_test_video));
                            videoModel.videoURL = mediaInfo.getUrl();
                        }
                    });
        }
        updateDownloadState(mediaInfo);
        this.mVideoModel = videoModel;
    }

    public String byteToMBStr(long byteNumber) {
        double size =  (byteNumber / (1024.0 * 1024.0));
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(size) + getContext().getString(R.string.superplayer_cache_video_size);
    }

    public String getFileNameNoEx(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    private String getQualityText(int quality) {
        int strId = mVideoDownloadHelper.getDownloadQualityText(quality);
        if (strId < 0) {
            return "";
        } else {
            return getContext().getString(strId);
        }
    }

    private void updateDownloadState(TXVodDownloadMediaInfo mediaInfo) {
        int downloadState = mediaInfo.getDownloadState();
        int downloadProgress = (int) (mediaInfo.getProgress() * 100F);
        Log.e(TAG, "downloadState:" + downloadState + ",downloadProgress:" + mediaInfo.getProgress());
        if (downloadState == TXVodDownloadMediaInfo.STATE_FINISH) {
            downloadProgress = 100;
        }
        mTvVideoCacheProgressView.setText(String.format(mVideoDownloadHelper.getProgressFormatter(), downloadProgress));
        mTvVideoCacheStateTextView.setText(mVideoDownloadHelper.getProgressStateTextRes(downloadState));
        mVVideoCacheStateIconView.setBackgroundResource(mVideoDownloadHelper.getProgressStateIconRes(downloadState));
    }

    /**
     * Notify to register real-time monitoring for video downloads.
     *
     * 通知注册视频下载实时监听
     */
    public void notifyRegisterCacheListener() {
        VideoDownloadCenter.getInstance().registerDownloadListener(mMediaInfo, this);
    }

    /**
     * Notify to unregister real-time monitoring for video downloads.
     *
     * 通知解注册视频下载实时监听
     */
    public void notifyUnRegisterCacheListener() {
        VideoDownloadCenter.getInstance().unRegisterDownloadListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        notifyUnRegisterCacheListener();
    }

    @Override
    public void onDownloadEvent(int event, TXVodDownloadMediaInfo mediaInfo) {
        if (mMediaInfo != mediaInfo) {
            return;
        }
        updateDownloadState(mediaInfo);
    }

    @Override
    public void onDownloadError(TXVodDownloadMediaInfo mediaInfo, int errorCode, String errorMsg) {
        if (mMediaInfo != mediaInfo) {
            return;
        }
        updateDownloadState(mediaInfo);
    }

    @Nullable
    public VideoModel getVideoModel() {
        return mVideoModel;
    }
}
