package com.tencent.liteav.demo.play.superplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.common.utils.TCUtils;
import com.tencent.liteav.demo.play.SuperPlayerModel;

import java.util.ArrayList;

/**
 * Created by liyuejiao on 2018/7/3.
 */

public class TCVodPlayerListAdapter extends RecyclerView.Adapter<TCVodPlayerListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<SuperPlayerModel> mSuperPlayerModelList;
    private OnItemClickLitener mOnItemClickLitener;

    public TCVodPlayerListAdapter(Context context) {
        mContext = context;
        mSuperPlayerModelList = new ArrayList<SuperPlayerModel>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_new_vod, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final SuperPlayerModel superPlayerModel = mSuperPlayerModelList.get(position);
        Glide.with(mContext).load(superPlayerModel.placeholderImage).into(holder.thumb);
        if (superPlayerModel.duration > 0) {
            holder.duration.setText(TCUtils.formattedTime(superPlayerModel.duration));
        } else {
            holder.duration.setText("");
        }
        if (superPlayerModel.title != null) {
            holder.title.setText(superPlayerModel.title);
        }
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null) {
                    mOnItemClickLitener.onItemClick(position, superPlayerModel);
                }
            }
        });
        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null) {
                    mOnItemClickLitener.onItemClick(position, superPlayerModel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSuperPlayerModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView duration;
        private TextView title;
        private ImageView thumb;

        public ViewHolder(final View itemView) {
            super(itemView);
            thumb = (ImageView) itemView.findViewById(R.id.imageView);
            title = (TextView) itemView.findViewById(R.id.textview);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
        }
    }

    /**
     * 添加一个SuperPlayerModel
     *
     * @param superPlayerModel
     */
    public void addSuperPlayerModel(SuperPlayerModel superPlayerModel) {
        notifyItemInserted(mSuperPlayerModelList.size());
        mSuperPlayerModelList.add(superPlayerModel);
    }

    public void setOnItemClickLitener(OnItemClickLitener listener) {
        mOnItemClickLitener = listener;
    }

    public void clear() {
        mSuperPlayerModelList.clear();
    }

    public interface OnItemClickLitener {
        void onItemClick(int position, SuperPlayerModel superPlayerModel);
    }

}
