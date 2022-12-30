package com.tencent.liteav.demo.player.expand.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.vodcommon.entity.VideoListModel;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuejiao on 2018/7/3.
 */

public class TCVodPlayerListAdapter extends RecyclerView.Adapter<TCVodPlayerListAdapter.ViewHolder> {
    private Context                   mContext;
    private ArrayList<VideoListModel> mVideoListModelList;
    private OnItemClickListener       mOnItemClickListener;

    public TCVodPlayerListAdapter(Context context) {
        mContext = context;
        mVideoListModelList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.superplayer_item_new_vod, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position >= 0 && position < mVideoListModelList.size()
                && mVideoListModelList.get(position) != null
                && mVideoListModelList.get(position).videoModelList.size() == 1) {
            final VideoModel videoModel = mVideoListModelList.get(position).videoModelList.get(0);
            if (TextUtils.isEmpty(videoModel.placeholderImage)) {
                Glide.with(mContext).load(R.drawable.superplayer_default_cover_thumb).into(holder.thumb);
            } else {
                Glide.with(mContext).load(videoModel.placeholderImage).into(holder.thumb);
            }
            if (videoModel.duration > 0) {
                holder.duration.setText(formattedTime(videoModel.duration));
            } else {
                holder.duration.setText("");
            }
            if (videoModel.title != null) {
                holder.title.setText(videoModel.title);
            }

        } else {
            final VideoListModel videoListModel = mVideoListModelList.get(position);
            Glide.with(mContext).load(videoListModel.icon).into(holder.thumb);
            holder.title.setText(videoListModel.title);
            holder.duration.setText("");
        }
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mVideoListModelList.get(position).videoModelList);
                }
            }
        });
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mVideoListModelList.get(position).videoModelList);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideoListModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView  duration;
        private TextView  title;
        private ImageView thumb;

        public ViewHolder(final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.superplayer_iv);
            title = (TextView) itemView.findViewById(R.id.superplayer_tv);
            duration = (TextView) itemView.findViewById(R.id.superplayer_tv_duration);
        }
    }

    /**
     * 添加一个SuperPlayerModel
     *
     * @param videoListModel
     */
    public void addSuperPlayerModel(VideoListModel videoListModel) {
        notifyItemInserted(mVideoListModelList.size());
        mVideoListModelList.add(videoListModel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void clear() {
        mVideoListModelList.clear();
    }

    public interface OnItemClickListener {

        void onItemClick(List<VideoModel> videoModelArrayList);
    }

    private static String formattedTime(long second) {
        String hs, ms, ss, formatTime;
        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }
        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }
        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        if (h > 0) {
            formatTime = hs + ":" + ms + ":" + ss;
        } else {
            formatTime = ms + ":" + ss;
        }
        return formatTime;
    }
}
