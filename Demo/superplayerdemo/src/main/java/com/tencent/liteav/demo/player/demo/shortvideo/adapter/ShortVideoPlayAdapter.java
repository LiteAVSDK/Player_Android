package com.tencent.liteav.demo.player.demo.shortvideo.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.liteav.demo.player.demo.shortvideo.view.TXVideoBaseView;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.List;

public class ShortVideoPlayAdapter extends AbsPlayerRecyclerViewAdapter<ShortVideoBean, ShortVideoPlayAdapter.VideoViewHolder> {

    private static final String TAG = "ShortVideoDemo:ShortVideoPlayAdapter";

    public ShortVideoPlayAdapter(List<ShortVideoBean> list) {
        super(list);
    }

    @Override
    public void onHolder(VideoViewHolder holder, ShortVideoBean bean, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        if (bean != null && bean.placeholderImage != null) {
            Glide.with(mContext).load(bean.placeholderImage).
                    diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                    .into(holder.mImageViewCover);
        }
    }


    @Override
    public VideoViewHolder onCreateHolder(ViewGroup viewGroup) {
        return new VideoViewHolder(getViewByRes(R.layout.player_item_short_video_play, viewGroup));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ShortVideoPlayAdapter.VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.i(TAG,"onViewDetachedFromWindow");
        TXVideoBaseView videoView = (TXVideoBaseView) holder.mRootView.findViewById(R.id.baseItemView);
        videoView.stopForPlaying();
    }

    public class VideoViewHolder extends AbsViewHolder {
        public View mRootView;
        public ImageView mImageViewCover;
        public TXCloudVideoView mVideoView;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.mRootView = rootView;
            this.mImageViewCover = rootView.findViewById(R.id.iv_cover);
            this.mVideoView = rootView.findViewById(R.id.tcv_video_view);
        }
    }
}
