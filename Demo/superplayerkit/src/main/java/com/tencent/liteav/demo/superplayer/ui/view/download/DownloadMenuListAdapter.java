package com.tencent.liteav.demo.superplayer.ui.view.download;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadCenter;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * download menu list adapter
 */
public class DownloadMenuListAdapter extends RecyclerView.Adapter<DownloadMenuListAdapter.CacheMenuListHolder> {

    private List<SuperPlayerModel>                                      mVideoModuleList;
    private int                                                         mCurrentQualityId;
    private SuperPlayerModel                                            mCurrentPlayVideo;
    private OnCacheItemClickListener                                    mCacheItemClickListener;
    // getDownloadMediaInfo 涉及IO操作，刚开始下载的时候拿不到mediaInfo，由于这里对于实时性操作要求不高，
    // 所以做缓存存储mediaInfo,根据时机进行更新
    private Map<Integer, Map<SuperPlayerModel, TXVodDownloadMediaInfo>> mMediaInfo = new HashMap<>();

    public DownloadMenuListAdapter(List<SuperPlayerModel> videoModuleList) {
        this.mVideoModuleList = videoModuleList;
    }

    @NonNull
    @Override
    public CacheMenuListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.superplayer_vod_cache_menu_list_item, parent, false);
        return new CacheMenuListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadMenuListAdapter.CacheMenuListHolder holder, int position) {
        final SuperPlayerModel videoModel = mVideoModuleList.get(position);
        holder.setVideoModel(videoModel);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mCacheItemClickListener) {
                    mCacheItemClickListener.onItemClick(videoModel, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideoModuleList.size();
    }

    public void updateQuality(int qualityId) {
        if (qualityId != mCurrentQualityId) {
            this.mCurrentQualityId = qualityId;
        }
    }

    public void setCurrentPlayVideo(SuperPlayerModel videoModel) {
        if (null != videoModel && videoModel != mCurrentPlayVideo) {
            updateVideoItem(mCurrentPlayVideo);
            updateVideoItem(videoModel);
            this.mCurrentPlayVideo = videoModel;
        }
    }

    private void updateVideoItem(SuperPlayerModel videoModel) {
        if (null != videoModel) {
            int index = mVideoModuleList.indexOf(videoModel);
            if (index > -1) {
                notifyItemChanged(index);
            }
        }
    }

    public void setOnItemClickListener(OnCacheItemClickListener clickListener) {
        this.mCacheItemClickListener = clickListener;
    }

    private void handleDownloadFlag(final ImageView ivIsCache, final SuperPlayerModel superPlayerModel,
                                    final int qualityId) {
        ivIsCache.setTag(superPlayerModel);
        Map<SuperPlayerModel, TXVodDownloadMediaInfo> mediaInfoMap = mMediaInfo.get(qualityId);
        if (null == mediaInfoMap) {
            mMediaInfo.put(qualityId, mediaInfoMap = new HashMap<>());
        }
        TXVodDownloadMediaInfo mediaInfo = mediaInfoMap.get(superPlayerModel);
        if (null == mediaInfo) {
            VideoDownloadCenter.getInstance().getDownloadMediaInfo(superPlayerModel, qualityId
                    , new VideoDownloadCenter.OnMediaInfoFetchListener() {
                        @Override
                        public void onReady(TXVodDownloadMediaInfo mediaInfo) {
                            updateItemMediaCache(superPlayerModel, qualityId, mediaInfo);
                            if (null != ivIsCache.getTag() && ivIsCache.getTag().equals(superPlayerModel)) {
                                if (null != mediaInfo
                             && mediaInfo.getDownloadState() != TXVodDownloadMediaInfo.STATE_ERROR) {
                                    ivIsCache.setVisibility(View.VISIBLE);
                                } else {
                                    ivIsCache.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    });
        } else {
            ivIsCache.setVisibility(View.VISIBLE);
        }
    }

    private void updateItemMediaCache(SuperPlayerModel superPlayerModel, int qualityId,
                                      TXVodDownloadMediaInfo mediaInfo) {
        Map<SuperPlayerModel, TXVodDownloadMediaInfo> mediaInfoMap = mMediaInfo.get(qualityId);
        if (null == mediaInfoMap) {
            mMediaInfo.put(qualityId, mediaInfoMap = new HashMap<>());
        }
        mediaInfoMap.put(superPlayerModel, mediaInfo);
    }


    public void updateItemMediaCache(int pos, SuperPlayerModel superPlayerModel, int qualityId,
                                     TXVodDownloadMediaInfo mediaInfo) {
        updateItemMediaCache(superPlayerModel, qualityId, mediaInfo);
        notifyItemChanged(pos);
    }

    class CacheMenuListHolder extends RecyclerView.ViewHolder {

        TextView  mTvVideoName;
        ImageView mIvIsCache;
        View      rootView;

        public CacheMenuListHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            mTvVideoName = itemView.findViewById(R.id.superplayer_tv_video_name);
            mIvIsCache = itemView.findViewById(R.id.superplayer_iv_is_cache);
        }

        public void setVideoModel(SuperPlayerModel videoModel) {
            String title = getFileNameNoEx(videoModel.title);
            mTvVideoName.setText(title);

            handleDownloadFlag(mIvIsCache, videoModel, mCurrentQualityId);

            Resources resources = itemView.getResources();
            if (videoModel == mCurrentPlayVideo) {
                mTvVideoName.setTextColor(resources.getColor(R.color.superplayer_cache_btn_color));
                itemView.setBackgroundResource(R.drawable.superplayer_blue_bottom_line);
            } else {
                mTvVideoName.setTextColor(resources.getColor(R.color.superplayer_white));
                itemView.setBackgroundResource(R.color.superplayer_transparent);
            }
        }
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

    public void clearMediaInfoCache() {
        mMediaInfo.clear();
    }

    interface OnCacheItemClickListener {
        void onItemClick(SuperPlayerModel superPlayerModel, int position);
    }

}
