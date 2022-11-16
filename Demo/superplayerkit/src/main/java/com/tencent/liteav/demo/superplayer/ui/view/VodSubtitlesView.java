package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseAdapter;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseListView;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseViewHolder;
import com.tencent.rtmp.TXTrackInfo;

import java.util.ArrayList;
import java.util.List;

public class VodSubtitlesView extends BaseListView<VodSubtitlesView.VodSubtitlesAdapter,TXTrackInfo> {

    private OnClickSubtitlesItemListener mListener;

    private OnClickSettingListener mOnClickSettingListener;

    private ImageView imageView;

    public VodSubtitlesView(Context context) {
        super(context);
    }

    public VodSubtitlesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodSubtitlesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        imageView = new ImageView(mContext);
        imageView.setImageDrawable(tintDrawable(mContext.getResources().getDrawable(R.drawable.superplayer_setting),
                ColorStateList.valueOf(Color.WHITE)));
        addView(imageView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        layoutParams.setMargins(0,70,100,0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.height = 50;
        layoutParams.width = 50;
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickSettingListener.onClickSetting();
            }
        });
    }

    @Override
    protected String getTitle() {
        return mContext.getString(R.string.superplayer_subtitle);
    }

    @Override
    protected VodSubtitlesAdapter getAdapter() {
        return new VodSubtitlesAdapter();
    }

    class VodSubtitlesAdapter extends BaseAdapter<VodSoundTrackItemView, TXTrackInfo> {

        public VodSubtitlesAdapter() {
            super();
        }

        @Override
        public void setData(List<TXTrackInfo> txTrackInfos) {
            super.setData(txTrackInfos);
            if (txTrackInfos != null && txTrackInfos.size() != 0) {
                TXTrackInfo info = new TXTrackInfo();
                info.name = mContext.getString(R.string.superplayer_off);
                info.trackIndex = -1;
                mItems.add(0,info);
                mData.add(0,info);
            }
            if (mItems != null && mItems.size() > 0) {
                mListener.onClickSubtitlesItem(mData.get(0));
            }
        }

        @Override
        public BaseViewHolder createViewHolder() {
            return new VodSubtitlesItemHolder(new VodSubtitlesItemView(mContext));
        }

        class VodSubtitlesItemHolder extends BaseViewHolder {

            public VodSubtitlesItemHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void onItemClick(View view) {
                int position = (int) (view.getTag());
                setCurrentPosition(position);
                mListener.onClickSubtitlesItem(mData.get(position));
                notifyDataSetChanged();
            }
        }
    }

    public interface OnClickSubtitlesItemListener {
        void onClickSubtitlesItem(TXTrackInfo clickInfo);
    }

    public void setOnClickSubtitlesItemListener(OnClickSubtitlesItemListener listener) {
        mListener = listener;
    }

    public interface OnClickSettingListener {
        void onClickSetting();
    }

    public void setOnClickSettingListener(OnClickSettingListener listener) {
        mOnClickSettingListener = listener;
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

}
