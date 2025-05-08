package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.vod;

import android.graphics.drawable.ColorDrawable;
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
import com.tencent.qcloud.tuiplayer.core.api.common.TUIConstants;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIFileVideoInfo;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseVideoView;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIVodLayer;

public class TUICoverLayer extends TUIVodLayer {

    private String coverUrlFromServer = null;
    private final RequestOptions imgOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .dontTransform()
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
        // show cover when unbind
        show();
        coverUrlFromServer = null;
    }

    /**
     * video rec first frame, you can use {@link #onRcvFirstIframe()} to hide cover.
     * but there may be a slightly delay in rendering the first frame of the first video to the texture sometimes,
     * in this condition, if you encounter a problem after the cover hidden but the first frame hasn't been
     * rendered yet, you can use this method to prevent it.
     */
    @Override
    public void onFirstFrameRendered() {
//        hidden();
    }


    @Override
    public void onRcvFirstIframe() {
        hidden();
    }

    @Override
    public void onRecFileVideoInfo(TUIFileVideoInfo params) {
        if (isShowing()) {
            TUIBaseVideoView videoView = getVideoView();
            if (null != videoView && null != params) {
                String coverUrl = params.getCoverUrl();
                if (!TextUtils.isEmpty(coverUrl)) {
                    coverUrlFromServer = coverUrl;
                    loadCover();
                }
            }
        }
    }

    @Override
    public void onViewRecycled(TUIBaseVideoView videoView) {
        super.onViewRecycled(videoView);
        // viewHolder is target recycle

    }

    private void loadCover() {
        TUIBaseVideoView videoView = getVideoView();
        if (null != videoView) {
            TUIVideoSource videoSource = (TUIVideoSource) videoView.getVideoModel();
            if (null != videoSource) {
                ImageView imageView = getView();
                final int renderMode = getRenderMode();
                RequestOptions requestOptions = imgOptions.clone();
                if (renderMode == TUIConstants.TUIRenderMode.FULL_FILL_SCREEN) {
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    imageView.setLayoutParams(params);
                    imageView.setAdjustViewBounds(false);
                    requestOptions = requestOptions.centerCrop();
                } else if (renderMode == TUIConstants.TUIRenderMode.ADJUST_RESOLUTION) {
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    imageView.setLayoutParams(params);
                    imageView.setAdjustViewBounds(true);
                    requestOptions = requestOptions.fitCenter();
                }
                if (!TextUtils.isEmpty(videoSource.getCoverPictureUrl())) {
                    Glide.with(videoView).load(videoSource.getCoverPictureUrl())
                            .apply(requestOptions)
                            .into(imageView);
                } else if (!TextUtils.isEmpty(coverUrlFromServer)) {
                    Glide.with(videoView).load(coverUrlFromServer)
                            .apply(requestOptions)
                            .into(imageView);
                } else {
                    Glide.with(videoView).load(new ColorDrawable(0x00000000))
                            .apply(requestOptions)
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
