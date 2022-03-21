package com.tencent.liteav.demo.common.custom.customcapture;

import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import com.tencent.liteav.demo.common.custom.customcapture.decoder.Decoder;
import com.tencent.liteav.demo.common.custom.customcapture.exceptions.ProcessException;
import com.tencent.liteav.demo.common.custom.customcapture.exceptions.SetupException;
import com.tencent.liteav.demo.common.custom.customcapture.extractor.Extractor;
import com.tencent.liteav.demo.common.custom.customcapture.extractor.ExtractorAdvancer;
import com.tencent.liteav.demo.common.custom.customcapture.extractor.RangeExtractorAdvancer;
import com.tencent.liteav.demo.common.custom.customcapture.utils.MediaUtils;
import com.tencent.liteav.demo.common.custom.structs.Size;
import com.tencent.liteav.demo.common.custom.structs.TextureFrame;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class VideoFrameReader extends BaseReader {
    private static final String TAG = "VideoFrameReader";

    public interface VideoFrameReadListener {
        void onFrameAvailable(TextureFrame frame);
    }

    private final String                 mVideoPath;
    private final long                   mLoopDurationMs;
    private       VideoFrameReadListener mListener;
    private       Decoder                mVideoDecoder;
    private       VideoDecoderConsumer   mDecoderConsumer;
    private       long                   mStartTimeMs = -1;

    public VideoFrameReader(String videoPath, long durationMs, CountDownLatch countDownLatch) {
        super(countDownLatch);
        mVideoPath = videoPath;
        mLoopDurationMs = durationMs;
    }

    public void setListener(VideoFrameReadListener listener) {
        mListener = listener;
    }

    @Override
    protected void setup() throws SetupException {
        Size size = retriveVideoSize();
        mDecoderConsumer = new VideoDecoderConsumer(size.width, size.height);
        mDecoderConsumer.setup();
        SurfaceTexture surfaceTexture = mDecoderConsumer.getSurfaceTexture();

        ExtractorAdvancer advancer = new RangeExtractorAdvancer(MILLISECONDS.toMicros(mLoopDurationMs));
        Extractor extractor = new Extractor(true, mVideoPath, advancer);
        mVideoDecoder = new Decoder(extractor, surfaceTexture);
        mVideoDecoder.setLooping(true);
        mVideoDecoder.setup();

        mDecoderConsumer.setFrameProvider(mVideoDecoder);
    }

    @Override
    protected void processFrame() throws ProcessException {
        if (mStartTimeMs == -1) {
            mStartTimeMs = SystemClock.elapsedRealtime();
        }

        mVideoDecoder.processFrame();
        mDecoderConsumer.processFrame();

        TextureFrame textureFrame = mDecoderConsumer.dequeueOutputBuffer();
        if (textureFrame == null) {
            return;
        }

        // 检查当前帧与预期发送的时间差多久，睡眠这段时间，然后再发送
        long sleepTime;
        while ((sleepTime = textureFrame.timestampMs - (SystemClock.elapsedRealtime() - mStartTimeMs)) > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        VideoFrameReadListener listener = mListener;
        if (listener != null) {
            listener.onFrameAvailable(textureFrame);
        }
        mDecoderConsumer.enqueueOutputBuffer(textureFrame);
    }

    private Size retriveVideoSize() throws SetupException {
        MediaFormat mediaFormat = MediaUtils.retriveMediaFormat(mVideoPath, true);
        Size size = new Size();
        size.width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        size.height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        if (mediaFormat.containsKey(MediaUtils.KEY_ROTATION)) {
            int rotation = mediaFormat.getInteger(MediaUtils.KEY_ROTATION);
            if (rotation == 90 || rotation == 270) {
                size.swap();
            }
        }
        return size;
    }

    @Override
    protected void release() {
        if (mVideoDecoder != null) {
            mVideoDecoder.release();
            mVideoDecoder = null;
        }

        if (mDecoderConsumer != null) {
            mDecoderConsumer.release();
            mDecoderConsumer = null;
        }
        Log.i(TAG, "released");
    }
}
