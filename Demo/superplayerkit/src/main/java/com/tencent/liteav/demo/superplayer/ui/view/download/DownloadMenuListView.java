package com.tencent.liteav.demo.superplayer.ui.view.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.download.VideoDonwloadListener;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadCenter;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadModel;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.utils.VideoQualityUtils;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * download menu from fullscreen layer
 */
public class DownloadMenuListView extends RelativeLayout
        implements DownloadMenuListAdapter.OnCacheItemClickListener,
        DownloadQualityListAdapter.OnQualityItemClickListener,
        View.OnClickListener {

    private static final String TAG = DownloadMenuListView.class.getSimpleName();

    private static final long ANIMATE_TIME = 500;

    // 当前清晰度
    private int mCurrentQualityId;

    private RecyclerView     mRvDownloadListView;
    private RecyclerView     mRvQualityListView;
    private ConstraintLayout mCurrentQualityContainer;
    private TextView         mCurrentQualityView;
    private ImageView        mCurrentQualityIconView;
    private Button           mBtShowCache;
    private String           mUserName;
    private RelativeLayout   mCacheMenuRoot;
    private ConstraintLayout mCacheMenuContainer;

    private boolean   isShowing = false;
    private Animation mEnterAnimation;
    private Animation mExitAnimation;

    private DownloadMenuListAdapter    mMenuListAdapter;
    private List<SuperPlayerModel>     mSuperPlayerModelList = new ArrayList<>();
    private List<VideoQuality>         mVideoQualityList     = new ArrayList<>();
    private DownloadQualityListAdapter mDownloadQualityListAdapter;


    public DownloadMenuListView(Context context) {
        this(context, null);
    }

    public DownloadMenuListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadMenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.superplayer_vod_download_list_menu, this);

        mRvDownloadListView = findViewById(R.id.superplayer_rv_cache_list);
        mCurrentQualityContainer = findViewById(R.id.superplayer_cl_current_quality_container);
        mBtShowCache = findViewById(R.id.superplayer_bt_show_cache_list);
        mCacheMenuRoot = findViewById(R.id.superplayer_cache_menu_root);
        mCacheMenuContainer = findViewById(R.id.superplayer_rl_cache_menu_container);
        mRvQualityListView = findViewById(R.id.superplayer_rv_quality_list);
        mCurrentQualityView = findViewById(R.id.superplayer_tv_current_quality);
        mCurrentQualityIconView = findViewById(R.id.superplayer_iv_current_quality);

        mCacheMenuRoot.setOnClickListener(this);
        mCurrentQualityContainer.setOnClickListener(this);

        mRvDownloadListView.setAdapter(mMenuListAdapter = new DownloadMenuListAdapter(mSuperPlayerModelList));
        mMenuListAdapter.setOnItemClickListener(this);

        mRvQualityListView.setAdapter(mDownloadQualityListAdapter = new DownloadQualityListAdapter(mVideoQualityList));
        mDownloadQualityListAdapter.setOnItemClickListener(this);

        File sdcardDir = getContext().getExternalFilesDir(null);
        VideoDownloadCenter.getInstance().setDownloadDirPath(sdcardDir.getAbsolutePath());
    }

    /**
     * init menu data
     * @param superPlayerModelList video list
     * @param qualityList quality list
     * @param currentQuality current quality
     * @param userName offline video stuck
     */
    @SuppressLint("NotifyDataSetChanged")
    public void initDownloadData(List<SuperPlayerModel> superPlayerModelList,
                                 List<VideoQuality> qualityList,
                                 VideoQuality currentQuality,
                                 String userName) {
        mUserName = userName;

        if (null == qualityList) {
            qualityList = new ArrayList<>();
        }
        if (null == superPlayerModelList) {
            superPlayerModelList = new ArrayList<>();
        }

        mCurrentQualityContainer.setVisibility(VISIBLE);
        if (null != currentQuality) {
            setCurrentQualityId(currentQuality);
            mCurrentQualityView.setText(VideoQualityUtils.transformToQualityName(currentQuality.title));
            if (qualityList.isEmpty()) {
                qualityList.add(currentQuality);
            }
        } else if (!qualityList.isEmpty()) {
            setCurrentQualityId(qualityList.get(0));
            mCurrentQualityView.setText(VideoQualityUtils.transformToQualityName(qualityList.get(0).title));
        } else {
            setCurrentQualityId(null);
            mCurrentQualityContainer.setVisibility(GONE);
            mRvQualityListView.setVisibility(GONE);
        }

        mVideoQualityList.clear();
        mVideoQualityList.addAll(qualityList);
        mDownloadQualityListAdapter.setCurrentSelectQuality(currentQuality);
        mDownloadQualityListAdapter.notifyDataSetChanged();

        mSuperPlayerModelList.clear();
        mSuperPlayerModelList.addAll(superPlayerModelList);
        mMenuListAdapter.updateQuality(mCurrentQualityId);
        mMenuListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyRefreshCacheState() {
        mMenuListAdapter.clearMediaInfoCache();
        mMenuListAdapter.notifyDataSetChanged();
    }

    public void setCurrentPlayVideo(SuperPlayerModel superPlayerModel) {
        if (null != mMenuListAdapter) {
            mMenuListAdapter.setCurrentPlayVideo(superPlayerModel);
        }
    }

    private void setCurrentQualityId(VideoQuality videoQuality) {
        if (null != videoQuality) {
            mCurrentQualityId = VideoQualityUtils.getCacheVideoQualityIndex(videoQuality);
        } else {
            mCurrentQualityId = 0;
        }
    }

    @Override
    public void onItemClick(final SuperPlayerModel superPlayerModel, final int position) {
        VideoDownloadCenter.getInstance().getDownloadMediaInfo(superPlayerModel, mCurrentQualityId
                , new VideoDownloadCenter.OnMediaInfoFetchListener() {
                    @Override
                    public void onReady(TXVodDownloadMediaInfo mediaInfo) {
                        if (null == mediaInfo) {
                            // 检查视频是否有该清晰度
                            if (!checkVideoQuality(superPlayerModel, mCurrentQualityId)) {
                                Toast.makeText(getContext(), R.string.superplayer_download_quality_invalid,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            // 拿不到下载信息则开始启动下载
                            VideoDownloadModel videoDownloadModel = new VideoDownloadModel();
                            videoDownloadModel.setQualityId(mCurrentQualityId);
                            videoDownloadModel.setPlayerModel(superPlayerModel);
                            videoDownloadModel.setUserName(mUserName);

                            TXVodDownloadMediaInfo downloadMediaInfo = VideoDownloadCenter.getInstance()
                                    .startDownload(videoDownloadModel);
                            VideoDownloadCenter.getInstance().registerDownloadListener(downloadMediaInfo,
                                    new VideoDownloadListener(position, superPlayerModel, mCurrentQualityId));
                        }
                    }
                });
    }

    @Override
    public void onItemClick(VideoQuality videoQuality, int position) {
        mCurrentQualityId = VideoQualityUtils.getCacheVideoQualityIndex(videoQuality);
        mCurrentQualityView.setText(VideoQualityUtils.transformToQualityName(videoQuality.title));
        mDownloadQualityListAdapter.setCurrentSelectQuality(videoQuality);
        mDownloadQualityListAdapter.notifyDataSetChanged();

        mMenuListAdapter.updateQuality(mCurrentQualityId);
        mMenuListAdapter.notifyDataSetChanged();
        foldQuality();
    }

    private boolean checkVideoQuality(SuperPlayerModel superPlayerModel, int cacheQualityId) {
        List<VideoQuality> videoQualityList = superPlayerModel.videoQualityList;
        if (null != videoQualityList && !videoQualityList.isEmpty()) {
            for (VideoQuality videoQuality : videoQualityList) {
                int videoQualityId = VideoQualityUtils.getCacheVideoQualityIndex(videoQuality);
                if (videoQualityId == cacheQualityId) {
                    return true;
                }
            }
        } else {
            // videoQualityList为空，交给下载去判断，这里返回true
            return true;
        }
        return false;
    }

    public void setOnCacheListClick(OnClickListener clickListener) {
        if (null != mBtShowCache) {
            mBtShowCache.setOnClickListener(clickListener);
        }
    }

    private Animation getEnterAnimation() {
        if (null == mEnterAnimation) {
            mEnterAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0);
            mEnterAnimation.setDuration(ANIMATE_TIME);
        }
        return mEnterAnimation;
    }

    private Animation getExitAnimation() {
        if (null == mExitAnimation) {
            mExitAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0);
            mExitAnimation.setDuration(ANIMATE_TIME);
            mExitAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                    mRvQualityListView.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return mExitAnimation;
    }

    public void show() {
        if (!isShowing) {
            isShowing = true;
            setVisibility(VISIBLE);
            mCacheMenuContainer.startAnimation(getEnterAnimation());
        }
    }

    public void dismiss() {
        if (isShowing) {
            isShowing = false;
            mCacheMenuContainer.startAnimation(getExitAnimation());
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.superplayer_cache_menu_root) {
            dismiss();
        } else if (v.getId() == R.id.superplayer_cl_current_quality_container) {
            if (mRvQualityListView.getVisibility() == VISIBLE) {
                foldQuality();
            } else {
                unfoldQuality();
            }
        }
    }

    private void foldQuality() {
        mCurrentQualityIconView.setRotation(0);
        mRvQualityListView.setVisibility(GONE);
    }

    private void unfoldQuality() {
        mCurrentQualityIconView.setRotation(180);
        mRvQualityListView.setVisibility(VISIBLE);
    }

    class VideoDownloadListener implements VideoDonwloadListener {

        private final int              mPosition;
        private final SuperPlayerModel mSuperPlayerModel;
        private final int              mQualityId;

        VideoDownloadListener(int position, SuperPlayerModel superPlayerModel, int qualityId) {
            this.mPosition = position;
            this.mSuperPlayerModel = superPlayerModel;
            this.mQualityId = qualityId;
        }

        @Override
        public void onDownloadEvent(int event, TXVodDownloadMediaInfo mediaInfo) {
            if (event >= TXVodDownloadMediaInfo.STATE_START) {
                String msg = String.format(getResources().getString(R.string.superplayer_start_download)
                        , mSuperPlayerModel.title);
                mMenuListAdapter.updateItemMediaCache(mPosition, mSuperPlayerModel,
                        mQualityId, mediaInfo);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                VideoDownloadCenter.getInstance().unRegisterDownloadListener(this);
            }
        }

        @Override
        public void onDownloadError(TXVodDownloadMediaInfo mediaInfo, int errorCode, String errorMsg) {
            String msg = String.format(getResources().getString(R.string.superplayer_download_failed)
                    , mSuperPlayerModel.title);
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            VideoDownloadCenter.getInstance().unRegisterDownloadListener(this);
        }
    }
}
