package com.tencent.liteav.demo.player.demo.tuishortvideo.layer;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerController;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIFileVideoInfo;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseLayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseVideoView;

public class TUICoverLayer extends TUIBaseLayer {

    private String coverUrlFromServer = null;
    RequestOptions imgOptions = new RequestOptions()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(true)
            .format(DecodeFormat.PREFER_RGB_565);

    @Override
    public View createView(ViewGroup parent) {
        ImageView imageView = new ImageView(parent.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    @Override
    public void show() {
        super.show();
        loadCover();
    }

    @Override
    public void onBindData(TUIVideoSource videoSource) {
        show();
    }

    @Override
    public void onControllerUnBind(TUIPlayerController controller) {
        super.onControllerUnBind(controller);
        // show cover when unbind
        show();
    }

    @Override
    public void onRcvFirstIframe() {
        hidden();
    }

    @Override
    protected void unBindLayerManager() {
        super.unBindLayerManager();
    }

    @Override
    public void onRecFileVideoInfo(TUIFileVideoInfo params) {
        if (isShowing()) {
            TUIBaseVideoView videoView = getVideoView();
            if (null != videoView && null != params) {
                String coverUrl = params.getCoverUrl();
                if (!TextUtils.isEmpty(coverUrl)) {
                    ImageView imageView = getView();
                    Glide.with(videoView).load(coverUrl)
                            .centerCrop()
                            .into(imageView);
                    coverUrlFromServer = coverUrl;
                }
            }
        }
    }

    @Override
    public void onViewRecycled(TUIBaseVideoView videoView) {
        super.onViewRecycled(videoView);
        // clear image bitmap when page itemView recycled
        Glide.with(videoView.getContext()).clear(getView());
    }

    private void loadCover() {
        TUIBaseVideoView videoView = getVideoView();
        if (null != videoView) {
            TUIVideoSource videoSource = videoView.getVideoModel();
            if (null != videoSource) {
                ImageView imageView = getView();
                if (!TextUtils.isEmpty(videoSource.coverPictureUrl)) {
                    Glide.with(videoView).load(videoSource.coverPictureUrl)
                            .apply(imgOptions)
                            .into(imageView);
                } else if (!TextUtils.isEmpty(coverUrlFromServer)) {
                    Glide.with(videoView).load(coverUrlFromServer)
                            .apply(imgOptions)
                            .into(imageView);
                }
            }
        }
    }

    @Override
    public String tag() {
        return "CoverLayer";
    }
}
