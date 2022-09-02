package com.tencent.liteav.demo.player.cache.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.cache.downloaditemview.VideoDownloadItemView;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.List;

public class VideoDownloadListAdapter extends RecyclerView.Adapter<VideoDownloadListAdapter.VideoDownloadViewHolder> {

    private final List<TXVodDownloadMediaInfo> mMediaInfoList;

    private OnItemClickListener     mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private VideoDownloadHelper     mVideoDownloadHelper;

    public VideoDownloadListAdapter(List<TXVodDownloadMediaInfo> mediaInfoList) {
        this.mMediaInfoList = mediaInfoList;
    }

    @NonNull
    @Override
    public VideoDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (null == mVideoDownloadHelper) {
            mVideoDownloadHelper =
                    new VideoDownloadHelper(parent.getContext()
                            .getString(R.string.superplayer_cache_progress),parent.getContext());
        }
        VideoDownloadItemView videoDownloadItemView = new VideoDownloadItemView(parent.getContext());
        videoDownloadItemView.setVideoCacheHelper(mVideoDownloadHelper);
        videoDownloadItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new VideoDownloadViewHolder(videoDownloadItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoDownloadViewHolder holder, int position) {
        final TXVodDownloadMediaInfo mediaInfo = mMediaInfoList.get(position);
        holder.mCacheItemView.setVideoInfo(mediaInfo);
        holder.mCacheItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    VideoModel videoModel = ((VideoDownloadItemView) v).getVideoModel();
                    mOnItemClickListener.onClick(videoModel, mediaInfo, holder.getAdapterPosition());
                }
            }
        });
        holder.mCacheItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mOnItemLongClickListener) {
                    return mOnItemLongClickListener.onLongClick(mediaInfo, holder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoDownloadViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.mCacheItemView.notifyRegisterCacheListener();
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull VideoDownloadViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.mCacheItemView.notifyUnRegisterCacheListener();
    }

    @Override
    public int getItemCount() {
        return mMediaInfoList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    static class VideoDownloadViewHolder extends RecyclerView.ViewHolder {

        VideoDownloadItemView mCacheItemView;

        public VideoDownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView instanceof VideoDownloadItemView) {
                mCacheItemView = (VideoDownloadItemView) itemView;
            }
        }
    }

    public interface OnItemClickListener {
        void onClick(@Nullable VideoModel videoModel, TXVodDownloadMediaInfo mediaInfo, int position);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(TXVodDownloadMediaInfo mediaInfo, int position);
    }
}
