package com.tencent.liteav.demo.superplayer.ui.view.base;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<VIEW extends BaseItemView, DATA> extends RecyclerView.Adapter<BaseViewHolder> {

    public List<DATA>  mItems;

    public int mCurrentPositionInAdapter = 0;

    public BaseAdapter() {
        mItems = new ArrayList<>();
    }

    public void setData(List<DATA> data) {
        mItems = data;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createViewHolder();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.itemView.setTag(position);
        ((VIEW)holder.mItemView).bindItemData(mItems.get(position),mCurrentPositionInAdapter,position);
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    public abstract BaseViewHolder createViewHolder();
}
