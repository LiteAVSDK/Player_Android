package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseItemView;
import com.tencent.rtmp.TXTrackInfo;

public class VodSoundTrackItemView extends BaseItemView<TXTrackInfo> {



    public VodSoundTrackItemView(Context context) {
        super(context);
    }

    public VodSoundTrackItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodSoundTrackItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void bindItemData(TXTrackInfo txTrackInfo, int currentPosition, int position) {
        if (txTrackInfo.trackIndex != -1) {
            mTextView.setText(mContext.getString(R.string.superplayer_audio_track)
                    + " " + String.valueOf(txTrackInfo.trackIndex));
        } else {
            mTextView.setText(txTrackInfo.name);
        }
        if (currentPosition == position) {
            mTextView.setTextColor(mContext.getResources().getColor(R.color.superplayer_color_tint_red));
            mBackGround.setBackgroundColor(mContext.getResources().getColor(R.color.superplayer_shape_vip_tip_color));
        } else {
            mTextView.setTextColor(mContext.getResources().getColor(R.color.superplayer_white));
            mBackGround.setBackgroundColor(mContext.getResources().getColor(R.color.superplayer_transparent));
        }
    }
}
