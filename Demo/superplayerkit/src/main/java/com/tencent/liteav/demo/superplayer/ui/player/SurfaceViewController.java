package com.tencent.liteav.demo.superplayer.ui.player;

import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayer;

public class SurfaceViewController implements RenderViewController, SurfaceHolder.Callback {

    private static final String TAG = "SurfaceViewController";
    private static final int HIDE_SCALE_FILL_MODE = -9999;

    private final SurfaceView mSurfaceView;
    private TXLivePlayer mLivePlayer;
    private TXVodPlayer mVodPlayer;
    private Surface mSurface;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mRenderMode;

    public SurfaceViewController(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void bindVodPlayer(TXVodPlayer vodPlayer) {
        mVodPlayer = vodPlayer;
        if (null != mLivePlayer) {
            mLivePlayer.setSurface(null);
            mLivePlayer = null;
        }
        connectPlayer();
    }

    @Override
    public void bindLivePlayer(TXLivePlayer livePlayer) {
        mLivePlayer = livePlayer;
        if (null != mVodPlayer) {
            mVodPlayer.setSurface(null);
            mVodPlayer = null;
        }
        connectPlayer();
    }

    @Override
    public void notifyVideoResolution(int width, int height) {
        if (mVideoWidth != width || mVideoHeight != height) {
            mVideoWidth = width;
            mVideoHeight = height;
            layoutTextureRenderMode();
        }
    }

    @Override
    public void updateRenderMode(int renderMode) {
        if (mRenderMode != renderMode) {
            mRenderMode = renderMode;
            layoutTextureRenderMode();
        }
    }

    @Override
    public void handleLayoutChanged(int parentWidth, int parentHeight) {
        layoutTextureRenderMode(parentWidth, parentHeight);
    }

    @Override
    public void clearLastImg() {
        // impl by container view
    }

    /**
     * On certain systems, an issue may occur where the app crashes when switching to another page while in
     * full-screen mode and then returning to that page. This is due to a rendering lock contention bug in
     * the system's handling of DRM-secured surfaces, which causes subsequent player rendering on top of
     * it to crash without generating an error stack trace. The probability of reproduction on this specific
     * device model is low, and the number of affected devices is small. If the business side needs to
     * mitigate this, it is recommended to identify the device model and, upon leaving the page, record
     * the current playback time and stop playback. When returning to the page, set the previous playback
     * time and restart playback. Currently, the only verified device model where this issue reproduces
     * is the Xiaomi MIX4 running HyperOS 1.0.4.
     */
    private void connectPlayer(final Surface surface, final boolean forceCover) {
        if (mSurface != surface || forceCover) {
            if (null != mVodPlayer) {
                mVodPlayer.setSurface(surface);
            } else if (null != mLivePlayer) {
                mLivePlayer.setSurface(surface);
            }
            mSurface = surface;
            Log.i(TAG, "connectPlayer success:" + surface);
        } else {
            Log.e(TAG, "surface is equal, jump set surface");
        }
    }

    private void connectPlayer() {
        connectPlayer(mSurface, true);
    }

    private void layoutTextureRenderMode() {
        final ViewGroup parentView = (ViewGroup) mSurfaceView.getParent();
        if (parentView != null) {
            final int viewWidth = parentView.getWidth();
            final int viewHeight = parentView.getHeight();
            layoutTextureRenderMode(viewWidth, viewHeight);
        }
    }

    private void layoutTextureRenderMode(int viewWidth, int viewHeight) {
        if (viewWidth > 0 && viewHeight > 0 && mVideoWidth > 0 && mVideoHeight > 0) {
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
            final float videoRadio = (float) mVideoWidth / mVideoHeight;
            final float viewRadio = (float) viewWidth / viewHeight;
            if (mRenderMode == TXVodConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                boolean isFixWidth = videoRadio > viewRadio;
                applyLayoutParams(layoutParams, viewWidth, viewHeight, videoRadio,
                        isFixWidth ? FixMode.FIX_WIDTH : FixMode.FIX_HEIGHT);
            } else if (mRenderMode == TXVodConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                boolean isFixWidth = videoRadio <= viewRadio;
                applyLayoutParams(layoutParams, viewWidth, viewHeight, videoRadio,
                        isFixWidth ? FixMode.FIX_WIDTH : FixMode.FIX_HEIGHT);
            } else if (mRenderMode == HIDE_SCALE_FILL_MODE) {
                applyLayoutParams(layoutParams, viewWidth, viewHeight, videoRadio
                        , FixMode.FIX_NONE);
            }
            layoutParams.gravity = Gravity.CENTER;
            mSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    mSurfaceView.setLayoutParams(layoutParams);
                }
            });
        }
    }

    private void applyLayoutParams(ViewGroup.LayoutParams layoutParams, int viewWidth, int viewHeight,
                                   float videoRadio, int fixMode) {
        if (fixMode == FixMode.FIX_WIDTH) {
            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth / videoRadio);
        } else if (fixMode == FixMode.FIX_HEIGHT) {
            layoutParams.width = (int) (viewHeight * videoRadio);
            layoutParams.height = viewHeight;
        } else {
            layoutParams.width = viewWidth;
            layoutParams.height = viewHeight;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated," + hashCode());
        connectPlayer(holder.getSurface(), false);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged," + hashCode());
        connectPlayer(holder.getSurface(), false);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed," + hashCode());
        connectPlayer(null, true);
    }
}
