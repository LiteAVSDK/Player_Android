package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseAdapter;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseListView;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseViewHolder;

import java.util.List;

public class VodResolutionView extends BaseListView<VodResolutionView.VodResolutionAdapter,VideoQuality> {

    private OnClickResolutionItemListener mListener;

    public VodResolutionView(Context context) {
        super(context);
    }

    public VodResolutionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodResolutionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String getTitle() {
        return mContext.getString(R.string.superplayer_sharpness);
    }

    @Override
    protected VodResolutionAdapter getAdapter() {
        return new VodResolutionAdapter();
    }

    class VodResolutionAdapter extends BaseAdapter<VodResolutionItemView, VideoQuality> {
        @Override
        public void setData(List<VideoQuality> videoQualities) {
            super.setData(videoQualities);
        }

        @Override
        public BaseViewHolder createViewHolder() {
            return new  VodResolutionItemHolder(new VodResolutionItemView(mContext));
        }

        class VodResolutionItemHolder extends BaseViewHolder {

            public VodResolutionItemHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void onItemClick(View view) {
                int position = (int) (view.getTag());
                setCurrentPosition(position);
                mListener.onClickResolutionItem(mData.get(position));
                notifyDataSetChanged();
            }
        }
    }

    public interface OnClickResolutionItemListener {
        void onClickResolutionItem(VideoQuality videoQuality);
    }

    public void setOnClickResolutionItemListener(OnClickResolutionItemListener listener) {
        mListener = listener;
    }

}
