package com.tencent.liteav.demo.player.demo.feed.feeddetailview;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.feedvideo.R;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.util.List;


/**
 * 详情页面recycleview 的adapter
 */
public class FeedDetailAdapter extends RecyclerView.Adapter<FeedDetailAdapter.FeedDetailItemHolder> {


    private List<VideoModel>            videoModels   = null;
    private FeedDetailListClickListener clickListener = null;

    public FeedDetailAdapter(FeedDetailListClickListener feedDetailListClickListener) {
        clickListener = feedDetailListClickListener;
    }

    public void setFeedEntityList(List<VideoModel> videoModels) {
        this.videoModels = videoModels;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public FeedDetailItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedview_detailview_item_layout, parent, false);
        return new FeedDetailItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedDetailItemHolder holder, @SuppressLint("RecyclerView") final int position) {
        VideoModel videoModel = videoModels.get(position);
        Glide.with(holder.itemView.getContext()).load(videoModel.placeholderImage).into(holder.headImg);
        holder.titleTxt.setText(videoModel.title);
        holder.describeTxt.setText(videoModel.videoDescription);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClickListener(videoModels.get(position), position);
                }
            }
        });
        holder.durationTxt.setText(formattedTime(videoModel.duration));
    }

    @Override
    public int getItemCount() {
        return videoModels != null ? videoModels.size() : 0;
    }

    private String formattedTime(long second) {
        String formatTime;
        long h = second / 3600;
        long m = (second % 3600) / 60;
        long s = (second % 3600) % 60;
        if (h == 0) {
            formatTime = String.format("%02d:%02d", m, s);
        } else {
            formatTime = String.format("%02d:%02d:%02d", h, m, s);
        }
        return formatTime;
    }


    static class FeedDetailItemHolder extends RecyclerView.ViewHolder {

        private ImageView headImg     = null;
        private TextView  titleTxt    = null;
        private TextView  describeTxt = null;
        private TextView  durationTxt = null;

        public FeedDetailItemHolder(@NonNull View itemView) {
            super(itemView);
            headImg = itemView.findViewById(R.id.feed_detail_holder_img);
            titleTxt = itemView.findViewById(R.id.feed_detail_holder_title_txt);
            describeTxt = itemView.findViewById(R.id.feed_detail_holder_describe_txt);
            durationTxt = itemView.findViewById(R.id.feed_detail_holder_video_duration_tv);
        }
    }


}
