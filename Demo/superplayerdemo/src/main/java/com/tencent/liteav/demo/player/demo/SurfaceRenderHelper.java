package com.tencent.liteav.demo.player.demo;

import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tencent.rtmp.TXVodPlayer;

/**
 * SurfaceView 渲染辅助类
 *
 * <p>封装 SurfaceHolder.Callback 和视频宽高比适配逻辑。</p>
 * <p>使用方式：</p>
 * <pre>
 *   SurfaceView surfaceView = findViewById(R.id.surface_view);
 *   SurfaceRenderHelper helper = new SurfaceRenderHelper(surfaceView);
 *   helper.bindVodPlayer(vodPlayer);
 *   // 在 onPlayEvent PLAY_EVT_CHANGE_RESOLUTION 中调用：
 *   helper.notifyVideoResolution(width, height);
 * </pre>
 */
public class SurfaceRenderHelper implements SurfaceHolder.Callback {

    private static final String TAG = "SurfaceRenderHelper";

    private final SurfaceView mSurfaceView;
    private TXVodPlayer mVodPlayer;
    private Surface mSurface;
    private int mVideoWidth;
    private int mVideoHeight;

    public SurfaceRenderHelper(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().addCallback(this);
    }

    /**
     * 绑定 VodPlayer，如果 Surface 已就绪则立即调用 setSurface
     */
    public void bindVodPlayer(TXVodPlayer vodPlayer) {
        mVodPlayer = vodPlayer;
        connectPlayer(mSurface, true);
    }

    /**
     * 获取当前 Surface（可能为 null）
     */
    public Surface getSurface() {
        return mSurface;
    }

    /**
     * 通知视频分辨率变化，触发 SurfaceView 宽高比重新计算
     *
     * @param width  视频宽度
     * @param height 视频高度
     */
    public void notifyVideoResolution(int width, int height) {
        if (width > 0 && height > 0 && (mVideoWidth != width || mVideoHeight != height)) {
            mVideoWidth = width;
            mVideoHeight = height;
            Log.i(TAG, "notifyVideoResolution: " + width + "x" + height);
            layoutSurfaceViewByAspectRatio();
        }
    }

    // ======================== SurfaceHolder.Callback ========================

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        connectPlayer(holder.getSurface(), false);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: " + width + "x" + height);
        connectPlayer(holder.getSurface(), false);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        connectPlayer(null, true);
    }

    // ======================== 内部方法 ========================

    /**
     * 将 Surface 设置给 VodPlayer
     */
    private void connectPlayer(Surface surface, boolean forceCover) {
        if (mSurface != surface || forceCover) {
            if (mVodPlayer != null) {
                mVodPlayer.setSurface(surface);
            }
            mSurface = surface;
            Log.i(TAG, "connectPlayer surface=" + surface);
        }
    }

    /**
     * 根据视频宽高比，在父容器（FrameLayout）中自适应调整 SurfaceView 大小（Fit 模式）
     */
    private void layoutSurfaceViewByAspectRatio() {
        ViewGroup parentView = (ViewGroup) mSurfaceView.getParent();
        if (parentView == null) return;

        int parentWidth = parentView.getWidth();
        int parentHeight = parentView.getHeight();
        if (parentWidth <= 0 || parentHeight <= 0 || mVideoWidth <= 0 || mVideoHeight <= 0) {
            return;
        }

        float videoRatio = (float) mVideoWidth / mVideoHeight;
        float parentRatio = (float) parentWidth / parentHeight;

        int targetWidth;
        int targetHeight;
        if (videoRatio > parentRatio) {
            // 视频更宽，以父容器宽度为准，高度按比例缩放
            targetWidth = parentWidth;
            targetHeight = (int) (parentWidth / videoRatio);
        } else {
            // 视频更高，以父容器高度为准，宽度按比例缩放
            targetWidth = (int) (parentHeight * videoRatio);
            targetHeight = parentHeight;
        }

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = targetWidth;
        lp.height = targetHeight;
        lp.gravity = Gravity.CENTER;

        Log.i(TAG, "layoutSurfaceView: parent=" + parentWidth + "x" + parentHeight
                + " video=" + mVideoWidth + "x" + mVideoHeight
                + " -> surface=" + targetWidth + "x" + targetHeight);

        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                mSurfaceView.setLayoutParams(lp);
            }
        });
    }
}
