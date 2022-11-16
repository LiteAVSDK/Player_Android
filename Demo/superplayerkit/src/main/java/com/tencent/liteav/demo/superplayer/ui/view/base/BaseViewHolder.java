package com.tencent.liteav.demo.superplayer.ui.view.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public View mItemView;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        mItemView = itemView;
        mItemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemClick(view);
    }

    protected abstract void onItemClick(View view);
}

