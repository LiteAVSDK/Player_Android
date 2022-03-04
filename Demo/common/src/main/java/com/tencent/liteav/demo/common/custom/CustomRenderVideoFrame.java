package com.tencent.liteav.demo.common.custom;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView.ScaleType;

import com.tencent.liteav.demo.common.custom.opengl.GPUImageFilter;
import com.tencent.liteav.demo.common.custom.opengl.GpuImageI420Filter;
import com.tencent.liteav.demo.common.custom.opengl.OpenGlUtils;
import com.tencent.liteav.demo.common.custom.opengl.Rotation;
import com.tencent.liteav.demo.common.custom.render.EglCore;
import com.tencent.liteav.demo.common.custom.structs.Size;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

@TargetApi(17)
public class CustomRenderVideoFrame implements Handler.Callback {

    public static final String TAG = "CustomRenderVideoFrame";

    private static final int MSG_RENDER_FRAME = 2;
    private static final int MSG_RENDER_TEXTURE = 3;
    private static final int MSG_DESTROY = -1;

    public static final int RENDER_TYPE_TEXTURE = 0;
    public static final int RENDER_TYPE_I420 = 1;

    private final HandlerThread mGLThread;
    private final GLHandler mGLHandler;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private TextureView mRenderView;
    private int mRenderType = RENDER_TYPE_TEXTURE;
    private Object mGLContext;
    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private Size mSurfaceSize = new Size();
    private Size mLastInputSize = new Size();
    private Size mLastOutputSize = new Size();
    private GPUImageFilter mNormalFilter;
    private GpuImageI420Filter mYUVFilter;


    public CustomRenderVideoFrame() {
        mGLCubeBuffer = ByteBuffer.allocateDirect(OpenGlUtils.CUBE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLCubeBuffer.put(OpenGlUtils.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(OpenGlUtils.TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLTextureBuffer.put(OpenGlUtils.TEXTURE).position(0);

        mGLThread = new HandlerThread(TAG);
        mGLThread.start();
        mGLHandler = new GLHandler(mGLThread.getLooper(), this);
        Log.i(TAG, "CustomRenderVideoFrame");
    }

    /**
     * start
     *
     * @param videoView
     */
    public void start(TextureView videoView) {
        if (videoView == null) {
            Log.w(TAG, "start error when render view is null");
            return;
        }
        if (mRenderView != null) {
            return;
        }
        Log.i(TAG, "start render");

        // 设置TextureView的SurfaceTexture生命周期回调，用于管理GLThread的创建和销毁
        mRenderView = videoView;
        mSurfaceTexture = mRenderView.getSurfaceTexture();
        mSurfaceSize = new Size(videoView.getWidth(), videoView.getHeight());
        mRenderView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 保存surfaceTexture，用于创建OpenGL线程
                mSurfaceTexture = surface;
                mSurfaceSize = new Size(width, height);
                Log.i(TAG, String.format("onSurfaceTextureAvailable width: %d, height: %d", width, height));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mSurfaceSize = new Size(width, height);
                Log.i(TAG, String.format("onSurfaceTextureSizeChanged width: %d, height: %d", width, height));
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // surface释放了，需要停止渲染
                mSurfaceTexture = null;
                // 等待Runnable执行完，再返回，否则GL线程会使用一个无效的SurfaceTexture
                mGLHandler.runAndWaitDone(new Runnable() {
                    @Override
                    public void run() {
                        uninitGlComponent();
                    }
                });
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
    }

    public void stop() {
        Log.i(TAG, "stop: ");
        if (mRenderView != null) {
            mRenderView.setSurfaceTextureListener(null);
        }
        mGLHandler.obtainMessage(MSG_DESTROY).sendToTarget();
    }

    /**
     * 当视频帧准备送入编码器时，SDK会触发该回调，将视频帧抛出来，这里主要处理TRTC_VIDEO_BUFFER_TYPE_TEXTURE
     */
    public void onRenderVideoFrame(LiveVideoFrame frame) {
        if (frame.textureId < 0) {
            // 等待frame.texture的纹理绘制完成
            GLES20.glFinish();
        }
        mGLHandler.obtainMessage(MSG_RENDER_FRAME, frame).sendToTarget();
    }

    public void onRenderVideoFrame(LiteAVTexture texture) {
        if (texture != null) {
            // 等待frame.texture的纹理绘制完成
            GLES20.glFinish();
        }
        mGLHandler.obtainMessage(MSG_RENDER_TEXTURE, texture).sendToTarget();
    }


    private void initGlComponent(Object eglContext) {
        if (mSurfaceTexture == null) {
            return;
        }

        // 创建的时候，增加判断，防止这边创建的时候，传入的EGLContext已经被销毁了。
        try {
            if (eglContext instanceof javax.microedition.khronos.egl.EGLContext) {
                mEglCore = new EglCore((javax.microedition.khronos.egl.EGLContext) eglContext,
                        new Surface(mSurfaceTexture));
            } else {
                mEglCore = new EglCore((android.opengl.EGLContext) eglContext, new Surface(mSurfaceTexture));
            }
        } catch (Exception e) {
            Log.e(TAG, "create EglCore failed.", e);
            return;
        }

        mEglCore.makeCurrent();
        if (mRenderType == RENDER_TYPE_TEXTURE) {
            mNormalFilter = new GPUImageFilter();
            mNormalFilter.init();
        } else if (mRenderType == RENDER_TYPE_I420) {
            mYUVFilter = new GpuImageI420Filter();
            mYUVFilter.init();
        }
    }

    private void renderInternal(LiveVideoFrame frame) {
        if (RENDER_TYPE_I420 != frame.renderType && RENDER_TYPE_TEXTURE != frame.renderType) {
            Log.w(TAG, "error video frame type, type -> " + frame.renderType);
            return;
        }
        mRenderType = frame.renderType;
        if (frame.glContext != mGLContext) {
            uninitGlComponent();
        }
        if (mLastInputSize.width != frame.width || mLastInputSize.height != frame.height) {
            // 宽高发生变化
            uninitGlComponent();
        }
        if (mEglCore == null && mSurfaceTexture != null) {
            if (frame.textureId >= 0) {
                mGLContext = frame.glContext;
            }
            initGlComponent(mGLContext);
        }
        if (mEglCore == null) {
            return;
        }
        if (mLastInputSize.width != frame.width || mLastInputSize.height != frame.height
                || mLastOutputSize.width != mSurfaceSize.width || mLastOutputSize.height != mSurfaceSize.height) {
            Pair<float[], float[]> cubeAndTextureBuffer = OpenGlUtils.calcCubeAndTextureBuffer(ScaleType.CENTER,
                    Rotation.ROTATION_180, true, frame.width, frame.height, mSurfaceSize.width, mSurfaceSize.height);
            mGLCubeBuffer.clear();
            mGLCubeBuffer.put(cubeAndTextureBuffer.first);
            mGLTextureBuffer.clear();
            mGLTextureBuffer.put(cubeAndTextureBuffer.second);

            mLastInputSize = new Size(frame.width, frame.height);
            mLastOutputSize = new Size(mSurfaceSize.width, mSurfaceSize.height);
        }
        swapBuffer(frame);
    }

    private void renderInternal(LiteAVTexture texture) {
        if (texture != null && texture.eglContext != mGLContext) {
            uninitGlComponent();
        }

        if (mLastInputSize.width != texture.width || mLastInputSize.height != texture.height) {
            // 宽高发生变化
            uninitGlComponent();
        }

        if (mEglCore == null && mSurfaceTexture != null) {
            Object eglContext = null;
            if (texture != null) {
                eglContext = texture.eglContext;
                mGLContext = eglContext;
            }
            initGlComponent(eglContext);
        }

        if (mLastInputSize.width != texture.width || mLastInputSize.height != texture.height
                || mLastOutputSize.width != mSurfaceSize.width || mLastOutputSize.height != mSurfaceSize.height) {
            Pair<float[], float[]> cubeAndTextureBuffer = OpenGlUtils.calcCubeAndTextureBuffer(ScaleType.CENTER,
                    Rotation.ROTATION_180, true, texture.width, texture.height, mSurfaceSize.width,
                    mSurfaceSize.height);
            mGLCubeBuffer.clear();
            mGLCubeBuffer.put(cubeAndTextureBuffer.first);
            mGLTextureBuffer.clear();
            mGLTextureBuffer.put(cubeAndTextureBuffer.second);

            mLastInputSize = new Size(texture.width, texture.height);
            mLastOutputSize = new Size(mSurfaceSize.width, mSurfaceSize.height);
        }
        if (mEglCore == null) {
            return;
        }

        mEglCore.makeCurrent();
        GLES20.glViewport(0, 0, mSurfaceSize.width, mSurfaceSize.height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        mNormalFilter.onDraw(texture.textureId, mGLCubeBuffer, mGLTextureBuffer);

        mEglCore.swapBuffer();
    }

    private void swapBuffer(LiveVideoFrame frame) {
        mEglCore.makeCurrent();
        GLES20.glViewport(0, 0, mSurfaceSize.width, mSurfaceSize.height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if (mRenderType == RENDER_TYPE_TEXTURE) {
            mNormalFilter.onDraw(frame.textureId, mGLCubeBuffer, mGLTextureBuffer);
        } else {
            mYUVFilter.loadYuvDataToTexture(frame.data, frame.width, frame.height);
            mYUVFilter.onDraw(OpenGlUtils.NO_TEXTURE, mGLCubeBuffer, mGLTextureBuffer);
        }
        mEglCore.swapBuffer();
    }

    private void uninitGlComponent() {
        if (mNormalFilter != null) {
            mNormalFilter.destroy();
            mNormalFilter = null;
        }
        if (mYUVFilter != null) {
            mYUVFilter.destroy();
            mYUVFilter = null;
        }
        if (mEglCore != null) {
            mEglCore.unmakeCurrent();
            mEglCore.destroy();
            mEglCore = null;
        }
    }

    private void destroyInternal() {
        uninitGlComponent();

        if (Build.VERSION.SDK_INT >= 18) {
            mGLHandler.getLooper().quitSafely();
        } else {
            mGLHandler.getLooper().quit();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RENDER_FRAME:
                renderInternal((LiveVideoFrame) msg.obj);
                break;
            case MSG_RENDER_TEXTURE:
                renderInternal((LiteAVTexture) msg.obj);
                break;
            case MSG_DESTROY:
                destroyInternal();
                break;
            default:
                break;
        }
        return false;
    }

    public static class GLHandler extends Handler {

        public GLHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        public void runAndWaitDone(final Runnable runnable) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class LiteAVTexture {
        public int textureId;
        public int width;
        public int height;
        public Object eglContext;
    }

    public static final class LiveVideoFrame {
        public int renderType;
        public int textureId;
        public Object glContext;
        public byte[] data;
        public ByteBuffer buffer;
        public int width;
        public int height;
        public int rotation;

        public LiveVideoFrame() {
            renderType = -1;
        }
    }
}
