package com.tencent.liteav.demo.common.custom.customcapture;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGLContext;
import android.opengl.GLES20;

import com.tencent.liteav.demo.common.custom.customcapture.exceptions.ProcessException;
import com.tencent.liteav.demo.common.custom.customcapture.pipeline.ProvidedStage;
import com.tencent.liteav.demo.common.custom.customcapture.pipeline.Provider;
import com.tencent.liteav.demo.common.custom.opengl.GPUImageFilter;
import com.tencent.liteav.demo.common.custom.opengl.GPUImageFilterGroup;
import com.tencent.liteav.demo.common.custom.opengl.OesInputFilter;
import com.tencent.liteav.demo.common.custom.opengl.OpenGlUtils;
import com.tencent.liteav.demo.common.custom.opengl.Rotation;
import com.tencent.liteav.demo.common.custom.render.EglCore;
import com.tencent.liteav.demo.common.custom.structs.Frame;
import com.tencent.liteav.demo.common.custom.structs.FrameBuffer;
import com.tencent.liteav.demo.common.custom.structs.TextureFrame;

import java.nio.FloatBuffer;
import java.util.List;


/**
 * 将解码出来的内容绘制到自己的FrameBuffer上，然后作为输出给到下一个节点
 */
@TargetApi(17)
public class VideoDecoderConsumer extends ProvidedStage<TextureFrame> implements OnFrameAvailableListener {
    private static final int STATE_WAIT_INPUT   = 1;
    private static final int STATE_WAIT_TEXTURE = 2;
    private static final int STATE_WAIT_RENDER  = 3;

    private final int                 mWidth;
    private final int                 mHeight;
    private final float[]             mTextureTransform      = new float[16];
    private final FloatBuffer         mGLCubeBuffer;
    private final FloatBuffer         mGLTextureBuffer;
    private       Provider<Frame>     mFrameProvider;
    private       EglCore             mEglCore;
    private       SurfaceTexture      mSurfaceTexture;
    private       int                 mSurfaceTextureId      = OpenGlUtils.NO_TEXTURE;
    private       FrameBuffer         mFrameBuffer;
    private       OesInputFilter      mOesInputFilter;
    private       GPUImageFilterGroup mGpuImageFilterGroup;
    private       boolean             mFrameBufferIsUnusable = false;    // 标识FrameBuffer是否被外部使用，当前不可再写
    private       int                 mState                 = STATE_WAIT_INPUT;
    private       Thread              mWorkThread;

    public VideoDecoderConsumer(int width, int height) {
        mWidth = width;
        mHeight = height;

        mGLCubeBuffer = OpenGlUtils.createNormalCubeVerticesBuffer();
        mGLTextureBuffer = OpenGlUtils.createTextureCoordsBuffer(Rotation.NORMAL, false, false);
    }

    public void setFrameProvider(Provider<Frame> provider) {
        mFrameProvider = provider;
    }

    @Override
    public void setup() {
        mWorkThread = Thread.currentThread();
        // 创建一个EGLCore出来，采用的是离屏的Surface
        mEglCore = new EglCore(mWidth, mHeight);
        mEglCore.makeCurrent();

        // 创建SurfaceTexture，用于给解码器作为输出，该类以texture id作为输入
        mSurfaceTextureId = OpenGlUtils.generateTextureOES();
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        // 创建一个FrameBuffer，作为输出给到外面（外面不能异步使用）
        mFrameBuffer = new FrameBuffer(mWidth, mHeight);
        mFrameBuffer.initialize();

        mGpuImageFilterGroup = new GPUImageFilterGroup();
        mOesInputFilter = new OesInputFilter();
        mGpuImageFilterGroup.addFilter(mOesInputFilter);
        mGpuImageFilterGroup.addFilter(new GPUImageFilter(true));
        mGpuImageFilterGroup.init();
        mGpuImageFilterGroup.onOutputSizeChanged(mWidth, mHeight);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void processFrame() throws ProcessException {
        super.processFrame();
        if (mState == STATE_WAIT_INPUT) {
            Frame frame = mFrameProvider.dequeueOutputBuffer();
            if (frame != null) {
                // 将Frame归还给Decoder之后，会触发Decoder释放buffer并渲染到Decoder的Surface上
                mFrameProvider.enqueueOutputBuffer(frame);
                mState = STATE_WAIT_TEXTURE;
            }
        } else if (mState == STATE_WAIT_RENDER) {
            renderOesToFrameBuffer();
        }
    }

    private void renderOesToFrameBuffer() {
        if (mFrameBufferIsUnusable) {
            return;
        }

        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTextureTransform);
        final long timestamp = mSurfaceTexture.getTimestamp() / 1000000;

        mOesInputFilter.setTexutreTransform(mTextureTransform);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        mGpuImageFilterGroup.draw(mSurfaceTextureId, mFrameBuffer.getFrameBufferId(), mGLCubeBuffer, mGLTextureBuffer);

        // 等待绘制完成
        GLES20.glFinish();

        TextureFrame textureFrame = new TextureFrame();
        textureFrame.eglContext = (EGLContext) mEglCore.getEglContext();
        textureFrame.textureId = mFrameBuffer.getTextureId();
        textureFrame.width = mWidth;
        textureFrame.height = mHeight;
        textureFrame.timestampMs = timestamp;
        synchronized (this) {
            mWaitOutBuffers.add(textureFrame);
        }

        mState = STATE_WAIT_INPUT;
    }

    @Override
    protected void recycleBuffers(List<TextureFrame> canReuseBuffers) {
        mFrameBufferIsUnusable = false;
    }

    @Override
    public void release() {
        mGpuImageFilterGroup.destroy();
        mGpuImageFilterGroup = null;

        mFrameBuffer.uninitialize();
        mFrameBuffer = null;

        OpenGlUtils.deleteTexture(mSurfaceTextureId);
        mSurfaceTextureId = OpenGlUtils.NO_TEXTURE;
        mSurfaceTexture.release();
        mSurfaceTexture = null;

        mEglCore.unmakeCurrent();
        mEglCore.destroy();
        mEglCore = null;
        mWorkThread = null;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mState = STATE_WAIT_RENDER;
        Thread thread = mWorkThread;
        // 收到数据的时候，立马中断睡眠，进行处理
        if (thread != null && thread != Thread.currentThread() && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
