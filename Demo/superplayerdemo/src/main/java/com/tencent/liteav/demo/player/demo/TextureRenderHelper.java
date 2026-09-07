package com.tencent.liteav.demo.player.demo;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tencent.rtmp.TXVodPlayer;

/**
 * TextureView 渲染辅助类
 *
 * <p>封装 TextureView.SurfaceTextureListener 和视频宽高比适配逻辑。</p>
 * <p>使用方式：</p>
 * <pre>
 *   TextureView textureView = findViewById(R.id.texture_view);
 *   TextureRenderHelper helper = new TextureRenderHelper(textureView);
 *   helper.bindVodPlayer(vodPlayer);
 *   // 在 onPlayEvent PLAY_EVT_CHANGE_RESOLUTION 中调用：
 *   helper.notifyVideoResolution(width, height);
 * </pre>
 */
public class TextureRenderHelper implements TextureView.SurfaceTextureListener {

    private static final String TAG = "TextureRenderHelper";

    private final TextureView mTextureView;
    private TXVodPlayer mVodPlayer;
    private Surface mSurface;
    private int mVideoWidth;
    private int mVideoHeight;

    public TextureRenderHelper(TextureView textureView) {
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);
    }

    /**
     * 绑定 VodPlayer，如果 Surface 已就绪则立即调用 setSurface
     */
    public void bindVodPlayer(TXVodPlayer vodPlayer) {
        mVodPlayer = vodPlayer;
        if (mSurface != null && mVodPlayer != null) {
            mVodPlayer.setSurface(mSurface);
            Log.i(TAG, "bindVodPlayer: setSurface immediately");
        }
    }

    /**
     * 获取当前 Surface（可能为 null）
     */
    public Surface getSurface() {
        return mSurface;
    }

    /**
     * 通知视频分辨率变化，触发 TextureView 宽高比重新计算
     *
     * @param width  视频宽度
     * @param height 视频高度
     */
    public void notifyVideoResolution(int width, int height) {
        if (width > 0 && height > 0 && (mVideoWidth != width || mVideoHeight != height)) {
            mVideoWidth = width;
            mVideoHeight = height;
            Log.i(TAG, "notifyVideoResolution: " + width + "x" + height);
            layoutTextureViewByAspectRatio();
        }
    }

    // ======================== TextureView.SurfaceTextureListener ========================

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: " + width + "x" + height);
        mSurface = new Surface(surfaceTexture);
        if (mVodPlayer != null) {
            mVodPlayer.setSurface(mSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged: " + width + "x" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        if (mVodPlayer != null) {
            mVodPlayer.setSurface(null);
        }
        mSurface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // no-op
    }

    // ======================== 内部方法 ========================

    /**
     * 根据视频宽高比，在父容器（FrameLayout）中自适应调整 TextureView 大小（Fit 模式）
     */
    private void layoutTextureViewByAspectRatio() {
        ViewGroup parentView = (ViewGroup) mTextureView.getParent();
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

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mTextureView.getLayoutParams();
        lp.width = targetWidth;
        lp.height = targetHeight;
        lp.gravity = Gravity.CENTER;

        Log.i(TAG, "layoutTextureView: parent=" + parentWidth + "x" + parentHeight
                + " video=" + mVideoWidth + "x" + mVideoHeight
                + " -> texture=" + targetWidth + "x" + targetHeight);

        mTextureView.post(new Runnable() {
            @Override
            public void run() {
                mTextureView.setLayoutParams(lp);
            }
        });
    }
}
