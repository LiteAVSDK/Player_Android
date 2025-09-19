package com.tencent.liteav.demo.superplayer.ui.player;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class SuperPlayerRenderView extends FrameLayout {

    private static final String TAG = "SuperPlayerRenderView";

    private RenderViewController mRenderViewController;
    private int mCurrentRenderViewType = SuperPlayerDef.PlayerRenderType.CLOUD_VIEW;

    public SuperPlayerRenderView(@NonNull Context context) {
        super(context);
        initView();
    }

    public SuperPlayerRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SuperPlayerRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SuperPlayerRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mCurrentRenderViewType = SuperPlayerGlobalConfig.getInstance().renderViewType;
        applyRenderView();
    }

    private void applyRenderView() {
        final int renderViewType = mCurrentRenderViewType;
        Log.i(TAG, "start apply renderViewType:" + renderViewType + ",obj:" + hashCode());
        if (renderViewType == SuperPlayerDef.PlayerRenderType.CLOUD_VIEW) {
            removeAllViews();
            TXCloudVideoView cloudVideoView = new TXCloudVideoView(getContext());
            addView(cloudVideoView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mRenderViewController = new TXCloudVideoViewController(cloudVideoView);
            mRenderViewController.updateRenderMode(SuperPlayerGlobalConfig.getInstance().renderMode);
        } else if (renderViewType == SuperPlayerDef.PlayerRenderType.SURFACE_VIEW) {
            removeAllViews();
            SurfaceView surfaceView = new SurfaceView(getContext());
            addView(surfaceView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mRenderViewController = new SurfaceViewController(surfaceView);
            mRenderViewController.updateRenderMode(SuperPlayerGlobalConfig.getInstance().renderMode);
        } else {
            throw new IllegalArgumentException("can not recognized renderViewType " + renderViewType
                    + ", please pass a valid renderViewType ,obj:" + hashCode());
        }
    }

    public void refreshRenderView() {
        final int renderViewType = SuperPlayerGlobalConfig.getInstance().renderViewType;
        if (mCurrentRenderViewType != renderViewType) {
            Log.i(TAG, "renderViewType is changed, old:" + mCurrentRenderViewType + ",new:"
                    + renderViewType + ",obj:" + hashCode());
            mCurrentRenderViewType = renderViewType;
            applyRenderView();
        }
    }

    public void bindPlayer(TXVodPlayer vodPlayer) {
        Log.i(TAG, "start bind vodPlayer,obj:" + hashCode());
        mRenderViewController.bindVodPlayer(vodPlayer);
    }

    public void bindPlayer(TXLivePlayer livePlayer) {
        Log.i(TAG, "start bind livePlayer,obj:" + hashCode());
        mRenderViewController.bindLivePlayer(livePlayer);
    }

    public void notifyVideoResolution(int width, int height) {
        Log.i(TAG, "notifyVideoResolution,w:" + width + ",h:" + height + ",obj:" + hashCode());
        mRenderViewController.notifyVideoResolution(width, height);
    }

    public void clearLastImg() {
        if (mRenderViewController instanceof SurfaceViewController) {
            // surfaceView need reAdd to clear img
            Log.i(TAG, "target surfaceView clear img ,obj:" + hashCode());
            applyRenderView();
        } else {
            mRenderViewController.clearLastImg();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRenderViewController.handleLayoutChanged(w, h);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
