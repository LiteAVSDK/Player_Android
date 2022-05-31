package com.tencent.liteav.demo.superplayer.ui.view.download;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.List;

/**
 * download quality on download menu list
 */
public class DownloadQualityListAdapter extends RecyclerView.Adapter<DownloadQualityListAdapter.QualityViewHolder> {

    private final List<VideoQuality> mQualities;

    private VideoQuality               mCurrentSelectQuality;
    private OnQualityItemClickListener mItemClickListener;

    public DownloadQualityListAdapter(List<VideoQuality> qualities) {
        this.mQualities = qualities;
    }

    @NonNull
    @Override
    public QualityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.superplayer_vod_cache_quality_list_item, parent, false);
        return new QualityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadQualityListAdapter.QualityViewHolder viewHolder, int position) {
        final VideoQuality videoQuality = mQualities.get(position);
        viewHolder.mQualityName.setText(videoQuality.title);

        Resources resources = viewHolder.getContext().getResources();
        if (null != mCurrentSelectQuality
                && (mCurrentSelectQuality.width == videoQuality.width
                && mCurrentSelectQuality.height == videoQuality.height)) {
            viewHolder.mQualityName.setTextColor(resources.getColor(R.color.superplayer_cache_btn_color));
        } else {
            viewHolder.mQualityName.setTextColor(resources.getColor(R.color.superplayer_white));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemClickListener) {
                    mItemClickListener.onItemClick(videoQuality, viewHolder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mQualities.size();
    }

    public void setCurrentSelectQuality(VideoQuality videoQuality) {
        this.mCurrentSelectQuality = videoQuality;
    }

    public void setOnItemClickListener(OnQualityItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    static class QualityViewHolder extends RecyclerView.ViewHolder {
        TextView mQualityName;

        public QualityViewHolder(@NonNull View itemView) {
            super(itemView);
            mQualityName = itemView.findViewById(R.id.superplayer_tv_quality_name);
        }

        public Context getContext() {
            return itemView.getContext();
        }
    }


    interface OnQualityItemClickListener {
        void onItemClick(VideoQuality videoQuality, int position);
    }
}
