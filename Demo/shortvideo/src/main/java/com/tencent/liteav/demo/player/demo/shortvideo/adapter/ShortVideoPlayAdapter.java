package com.tencent.liteav.demo.player.demo.shortvideo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.liteav.demo.player.demo.shortvideo.view.TXVideoBaseView;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.shortvideoplayerdemo.R;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.List;

public class ShortVideoPlayAdapter extends AbsPlayerRecyclerViewAdapter<VideoModel,
        ShortVideoPlayAdapter.VideoViewHolder> {

    private static final String TAG = "ShortVideoDemo:ShortVideoPlayAdapter";

    private Context  mContext;

    public ShortVideoPlayAdapter(Context context,List<VideoModel> list) {
        super(list);
        mContext = context;
    }

    @Override
    public void onHolder(VideoViewHolder holder, VideoModel bean, int position) {
        if (bean != null && bean.placeholderImage != null) {
            Glide.with(mContext).load(bean.placeholderImage)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                    .into(holder.mImageViewCover);
        }
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(mContext).clear(holder.mImageViewCover);
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
