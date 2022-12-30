package com.tencent.liteav.demo.player.demo.feed.feedlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.feed.FeedPlayerManager;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.util.ArrayList;
import java.util.List;


public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.FeedListItemHolder> {

    private       List<VideoModel>                          mVideoModels              = null;
    private       FeedListItemView.FeedListItemViewCallBack mFeedListItemViewCallBack = null;
    private FeedPlayerManager mFeedPlayerManager        = null;
    private       int                                       mListItemHeight           = 0;   //列表item 高度
    private       RecyclerView                              mRecyclerView;
    private final FeedViewManager mFeedViewManager;

    public FeedListAdapter(Context context, FeedListItemView.FeedListItemViewCallBack itemCallBack, FeedPlayerManager feedPlayerManager) {
        this.mFeedListItemViewCallBack = itemCallBack;
        this.mFeedPlayerManager = feedPlayerManager;
        int videoViewWidth = (int) (context.getResources().getDisplayMetrics().widthPixels - dp2px(context, 20));
        int videoViewHeight = videoViewWidth * 9 / 16;
        mListItemHeight = (int) (videoViewHeight + dp2px(context, 20 + 55));
        // recyclerView目前最多会创建11个，这里选择预缓存12个
        mFeedViewManager = new FeedViewManager(context, mListItemHeight, 12);
    }

    public int getListItemHeight() {
        return mListItemHeight;
    }

    /**
     * 添加数据
     *
     * @param videoModels 数据列表
     * @param isCleanData TRUE 表示清理之前的数据
     */
    public void addVideoData(List<VideoModel> videoModels, boolean isCleanData) {
        if (isCleanData) {
            if (this.mVideoModels != null) {
                this.mVideoModels.clear();
            }
            this.mVideoModels = videoModels;
            notifyDataSetChanged();
        } else {
            if (videoModels != null && videoModels.size() > 0) {
                if (this.mVideoModels == null) {
                    this.mVideoModels = new ArrayList<>();
                }
                int size = this.mVideoModels.size();
                this.mVideoModels.addAll(videoModels);
                notifyItemRangeInserted(size, getItemCount() - size);
            }
        }
    }


    @NonNull
    @Override
    public FeedListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedListItemHolder(mFeedViewManager.fetchFeedListItemView());
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedListItemHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setTag(position);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull final FeedListItemHolder holder) {
        super.onViewAttachedToWindow(holder);
        final int position = (int) holder.itemView.getTag();
        holder.feedListItemView.bindItemData(mVideoModels.get(position), mFeedListItemViewCallBack, position);
        holder.feedListItemView.getFeedPlayerView().setFeedPlayerManager(mFeedPlayerManager);
        if (null != this.mRecyclerView) {
            holder.feedListItemView.registerScrollListener(this.mRecyclerView);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull FeedListItemHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.feedListItemView.stop();
        if (null != this.mRecyclerView) {
            holder.feedListItemView.unRegisterScrollListener(this.mRecyclerView);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mFeedViewManager.release();
    }

    /**
     * 此处可以对itemView进行还原处理，
     *
     * @param holder
     */
    @Override
    public void onViewRecycled(@NonNull FeedListItemHolder holder) {
        super.onViewRecycled(holder);
        holder.feedListItemView.reset();
        if (null != this.mRecyclerView) {
            holder.feedListItemView.unRegisterScrollListener(this.mRecyclerView);
        }
    }


    @Override
    public int getItemCount() {
        return this.mVideoModels != null ? this.mVideoModels.size() : 0;
    }

    private float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    class FeedListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public FeedListItemView feedListItemView;

        public FeedListItemHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView instanceof FeedListItemView) {
                feedListItemView = (FeedListItemView) itemView;
                feedListItemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = (int) feedListItemView.getTag();
            mFeedListItemViewCallBack.onItemClick(feedListItemView, mVideoModels.get(position), position);
        }
    }
}
