package com.tencent.liteav.demo.player.demo.shortvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class AbsPlayerRecyclerViewAdapter<T, K extends AbsViewHolder> extends RecyclerView.Adapter<K> {

    private List<T> mList;
    public Context mContext;

    public AbsPlayerRecyclerViewAdapter(List<T> list) {
        this.mList = list;
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        K holder = onCreateHolder(parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        onHolder(holder, mList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    public abstract void onHolder(K holder, T bean, int position);

    public abstract K onCreateHolder(ViewGroup parent);


    public View getViewByRes(int res, ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(res, parent, false);
    }
}
