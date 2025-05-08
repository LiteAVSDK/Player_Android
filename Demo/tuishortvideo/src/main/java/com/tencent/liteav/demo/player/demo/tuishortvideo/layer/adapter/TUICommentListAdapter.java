package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.model.ShortVideoComment;

import java.util.ArrayList;
import java.util.List;

public class TUICommentListAdapter extends RecyclerView.Adapter<TUICommentListAdapter.TUICommentListViewHolder> {


    List<ShortVideoComment> mCommentData = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ShortVideoComment> itemList) {
        mCommentData.clear();
        mCommentData.addAll(itemList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TUICommentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tuiplayer_comment_list_item, parent, false);
        return new TUICommentListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TUICommentListViewHolder holder, int position) {
        ShortVideoComment comment = mCommentData.get(holder.getAdapterPosition());
        holder.bindData(comment);
    }

    @Override
    public int getItemCount() {
        return mCommentData.size();
    }

    class TUICommentListViewHolder extends RecyclerView.ViewHolder {

        private final TextView mCommentName;
        private final TextView mCommentContent;

        public TUICommentListViewHolder(@NonNull View itemView) {
            super(itemView);
            mCommentName = itemView.findViewById(R.id.tv_comment_name);
            mCommentContent = itemView.findViewById(R.id.tv_comment_content);
        }

        public void bindData(ShortVideoComment comment) {
            mCommentName.setText(comment.getName());
            mCommentContent.setText(comment.getComment());
        }
    }
}
