package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.custom.img;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.model.DemoImgSource;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIPlaySource;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUICustomLayer;

public class PicDisplayLayer extends TUICustomLayer {

    private ImageView mDisplayImgView;

    private final RequestOptions imgOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .dontTransform()
            .skipMemoryCache(false)
            .fitCenter()
            .format(DecodeFormat.PREFER_RGB_565);

    @Override
    public View createView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tuiplayer_img_display_layer, parent, false);
        mDisplayImgView = view.findViewById(R.id.iv_img_display);
        return view;
    }

    @Override
    public void onBindData(TUIPlaySource videoSource) {
        super.onBindData(videoSource);
        if (videoSource.getExtViewType() == SVDemoConstants.CustomSourceType.SINGLE_IMG_TYPE) {
            DemoImgSource source = (DemoImgSource) videoSource;
            Glide.with(mDisplayImgView).load(source.getImgUrl())
                    .apply(imgOptions)
                    .into(mDisplayImgView);
        }
    }

    @Override
    public String tag() {
        return "PicDisplayLayer";
    }
}
