package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseItemView;

public class VodResolutionItemView extends BaseItemView<VideoQuality> {

    public VodResolutionItemView(Context context) {
        super(context);
    }

    public VodResolutionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodResolutionItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void bindItemData(VideoQuality videoQuality, int currentPosition, int position) {
        mTextView.setText(videoQuality.title);
        if (currentPosition == position) {
            mTextView.setTextColor(mContext.getResources().getColor(R.color.superplayer_color_tint_red));
            mBackGround.setBackgroundColor(mContext.getResources().getColor(R.color.superplayer_shape_vip_tip_color));
        } else {
            mTextView.setTextColor(mContext.getResources().getColor(R.color.superplayer_white));
            mBackGround.setBackgroundColor(mContext.getResources().getColor(R.color.superplayer_transparent));
        }
    }
}
