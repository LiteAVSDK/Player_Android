package com.tencent.liteav.demo.player.feed.feedlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.feed.FeedPlayerManager;

import java.util.ArrayList;
import java.util.List;


public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.FeedListItemHolder> {

    private List<VideoModel>                          videoModels              = null;
    private FeedListItemView.FeedListItemViewCallBack feedListItemViewCallBack = null;
    private FeedPlayerManager                         feedPlayerManager        = null;
    private List<FeedListItemView>                    listItemViews            = new ArrayList<>();
    private   int                                     listItemHeight           = 0;   //列表item 高度

    public FeedListAdapter(Context context, FeedListItemView.FeedListItemViewCallBack itemCallBack, FeedPlayerManager feedPlayerManager) {
        this.feedListItemViewCallBack = itemCallBack;
        this.feedPlayerManager = feedPlayerManager;
        int videoViewWidth = (int) (context.getResources().getDisplayMetrics().widthPixels - dp2px(context, 20));
        int videoViewHeight = videoViewWidth * 9 / 16;
        listItemHeight = (int) (videoViewHeight + dp2px(context, 20 + 55));
    }

    public int getListItemHeight() {
        return listItemHeight;
    }

    /**
     * 添加数据
     *
     * @param videoModels 数据列表
     * @param isCleanData TRUE 表示清理之前的数据
     */
    public void addVideoData(List<VideoModel> videoModels, boolean isCleanData) {
        if (isCleanData) {
            if (this.videoModels != null) {
                this.videoModels.clear();
            }
            this.videoModels = videoModels;
            notifyDataSetChanged();
        } else {
            if (videoModels != null && videoModels.size() > 0) {
                if (this.videoModels == null) {
                    this.videoModels = new ArrayList<>();
                }
                int size = this.videoModels.size();
                this.videoModels.addAll(videoModels);
                notifyItemRangeInserted(size, getItemCount() - size);
            }
        }
    }


    @NonNull
    @Override
    public FeedListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FeedListItemView feedListItemView = new FeedListItemView(parent.getContext());
        feedListItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listItemHeight));
        listItemViews.add(feedListItemView);
        return new FeedListItemHolder(feedListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedListItemHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setTag(position);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull final FeedListItemHolder holder) {
        super.onViewAttachedToWindow(holder);
        final int position = (int) holder.itemView.getTag();
        holder.feedListItemView.bindItemData(videoModels.get(position), feedListItemViewCallBack, position);
        holder.feedListItemView.getFeedPlayerView().setFeedPlayerManager(feedPlayerManager);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull FeedListItemHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.feedListItemView.stop();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        for (FeedListItemView itemView : listItemViews) {
            itemView.destroy();
        }
        listItemViews.clear();
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
    }


    @Override
    public int getItemCount() {
        return this.videoModels != null ? this.videoModels.size() : 0;
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
            feedListItemViewCallBack.onItemClick(feedListItemView, videoModels.get(position), position);
        }
    }
}
