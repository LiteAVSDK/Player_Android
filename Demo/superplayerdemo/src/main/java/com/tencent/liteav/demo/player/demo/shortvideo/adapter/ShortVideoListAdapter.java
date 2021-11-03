package com.tencent.liteav.demo.player.demo.shortvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;

import java.util.List;

/**
 * 列表页面的RecyclerView的Adapter
 */
public class ShortVideoListAdapter extends RecyclerView.Adapter<ShortVideoListAdapter.ViewHolder> {
    private static final String TAG = "ShortVideoDemo:ShortVideoListAdapter";
    private Context mContext;
    private List<ShortVideoBean> mPlayerModelList;
    private IOnItemClickListener mIOnItemClickListener;

    public ShortVideoListAdapter(Context context, IOnItemClickListener onItemClickListener, List<ShortVideoBean> shortVideoBeanList) {
        mContext = context;
        mIOnItemClickListener = onItemClickListener;
        mPlayerModelList = shortVideoBeanList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item_short_video_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int pos) {
        final ShortVideoBean videoModel = mPlayerModelList.get(pos);
        Glide.with(mContext).load(videoModel.placeholderImage).
                diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                .placeholder(R.color.superplayer_color_gray)
                .into(holder.mThumb);
        if (videoModel.duration > 0) {
            String tempString = formattedTime(videoModel.duration);
            holder.mDuration.setText(tempString);
        } else {
            holder.mDuration.setText("");
        }
        if (videoModel.title != null) {
            holder.mTitle.setText(videoModel.title);
        }
        holder.mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIOnItemClickListener != null) {
                    mIOnItemClickListener.onItemClick(pos);
                }
            }
        });
        holder.mThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIOnItemClickListener != null) {
                    mIOnItemClickListener.onItemClick(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlayerModelList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mDuration;
        private TextView mTitle;
        private ImageView mThumb;

        public ViewHolder(final View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.iv_short_video);
            mTitle = itemView.findViewById(R.id.tv_short_video);
            mDuration = itemView.findViewById(R.id.tv_short_video_duration);
        }
    }


    public interface IOnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 将 秒转换成 h m s
     *
     * @param second
     * @return
     */
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
