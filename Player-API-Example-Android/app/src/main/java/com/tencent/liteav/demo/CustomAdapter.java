package com.tencent.liteav.demo;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private List<ItemData> mDataList;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTvMainTitle;
        private final TextView mTvSubTitle;

        public ViewHolder(View v) {
            super(v);
            mTvMainTitle = (TextView) v.findViewById(R.id.tv_main_title);
            mTvSubTitle = (TextView) v.findViewById(R.id.tv_sub_title);
        }

        public TextView getMainTitleTv() {
            return mTvMainTitle;
        }

        public TextView getSubTitleTv() {
            return mTvSubTitle;
        }
    }


    public CustomAdapter(Context context, List<ItemData> dataList) {
        mContext = context;
        mDataList = dataList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_main_row_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ItemData currItem = mDataList.get(position);
        viewHolder.getMainTitleTv().setText(currItem.mMainTitle);
        viewHolder.getSubTitleTv().setText(currItem.mSubTitle);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, currItem.mDestClass);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    public static class ItemData {
        public String mMainTitle;
        public String mSubTitle;
        public Class mDestClass;

        public ItemData(String mainTitle, String subTitle, Class destClass) {
            this.mMainTitle = mainTitle;
            this.mSubTitle = subTitle;
            this.mDestClass = destClass;
        }
    }
}
