package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlayerBitrateItem;

import java.util.ArrayList;
import java.util.List;

public class TUIQualityListAdapter extends RecyclerView.Adapter<TUIQualityListAdapter.TUIQualityListViewHolder> {

    private final List<TUIPlayerBitrateItem> mBitrateItemList = new ArrayList<>();

    private int mCurrentSelectQuality = -1;

    private QualitySelectedListener mSelectListener;

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<TUIPlayerBitrateItem> itemList) {
        mBitrateItemList.clear();
        mBitrateItemList.addAll(itemList);
        notifyDataSetChanged();
    }

    public void setQualityIndex(int index) {
        if (mCurrentSelectQuality != index) {
            int oldIndex = mCurrentSelectQuality;
            mCurrentSelectQuality = index;
            if (oldIndex >= 0) {
                notifyItemChanged(oldIndex);
            }
            if (index >= 0) {
                notifyItemChanged(index);
            }
        }
    }

    public int getCurrentQualityIndex() {
        return mCurrentSelectQuality;
    }

    public void setSelectedListener(QualitySelectedListener listener) {
        mSelectListener = listener;
    }

    @NonNull
    @Override
    public TUIQualityListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tuilayer_quality_list_item, parent, false);
        return new TUIQualityListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TUIQualityListViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        TUIPlayerBitrateItem item = mBitrateItemList.get(adapterPosition);
        holder.bindData(item, adapterPosition);
    }

    @Override
    public int getItemCount() {
        return mBitrateItemList.size();
    }

    class TUIQualityListViewHolder extends RecyclerView.ViewHolder {

        private final TextView mQualityText;

        public TUIQualityListViewHolder(@NonNull View itemView) {
            super(itemView);
            mQualityText = itemView.findViewById(R.id.btn_quality);
        }

        protected void bindData(final TUIPlayerBitrateItem item, final int position) {
            String qualityTxt = item.getWidth() + "*" + item.getHeight();
            mQualityText.setText(qualityTxt);
            if (mCurrentSelectQuality >= 0) {
                mQualityText.setSelected(position == mCurrentSelectQuality);
            } else {
                mQualityText.setSelected(false);
            }
            mQualityText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setQualityIndex(position);
                    if (null != mSelectListener) {
                        mSelectListener.onSelected(item, position);
                    }
                }
            });
        }
    }

    public interface QualitySelectedListener {
        void onSelected(TUIPlayerBitrateItem item, int pos);
    }
}
