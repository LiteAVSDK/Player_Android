package com.tencent.liteav.demo;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class CustomAudioFileReader  implements Runnable {
    private static final String TAG = CustomAudioFileReader.class.getSimpleName();
    private static CustomAudioFileReader instance = null;

    private int mSampleRate = 48000;
    private int mChannels = 1;
    private int mBits = 16;

    private Context mContext;
    private byte[] mCaptureBuffer = null;
    private int mCaptureBufferReadLen = 0;
    private int mFrameLen = 0;
    private WeakReference<TXICustomAudioFileReadListener> mWeakRefListener;
    private Thread mFileReadThread = null;
    private volatile boolean mIsRunning = false;

    public interface TXICustomAudioFileReadListener {
        void onAudioCapturePcm(byte[] data, int sampleRate, int channels, long timestampMs);
    }

    //单例录制类
    public static CustomAudioFileReader getInstance() {
        if (instance == null) {
            synchronized (CustomAudioCapturor.class) {
                if (instance == null) {
                    instance = new CustomAudioFileReader();
                }
            }
        }
        return instance;
    }

    private CustomAudioFileReader() {}


    //启动线程
    public void start(int sampleRate, int channels, int frameLen, Context context) {
        stop();

        mContext = context;
        mSampleRate = sampleRate;
        mChannels = channels;
        mFrameLen = frameLen;
        mIsRunning = true;
        mFileReadThread = new Thread(this, "CustomAudioFileReadThread");
        mFileReadThread.start();
    }

    //停止线程
    public void stop() {
        mIsRunning = false;
        if (mFileReadThread != null && mFileReadThread.isAlive() && Thread.currentThread().getId() != mFileReadThread.getId()) {
            try {
                mFileReadThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mFileReadThread = null;
    }

    //file read
    private void init() {
        try {
            InputStream is = mContext.getAssets().open("CustomAudio48000_1.pcm");
            int length = is.available();
            mCaptureBuffer = new byte[length];
            is.read(mCaptureBuffer);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //释放AudioRecord对象
    private void uninit() {
        mCaptureBuffer = null;
    }

    public void setCustomAudioFileReadListener(TXICustomAudioFileReadListener listener) {
        if (listener == null) {
            mWeakRefListener = null;
        } else {
            mWeakRefListener = new WeakReference<TXICustomAudioFileReadListener>(listener);
        }
    }

    private void onAudioCapturePcm(byte[] data, long timestampMs) {
        TXICustomAudioFileReadListener listener = null;
        synchronized (this) {
            if (null != mWeakRefListener) {
                listener = mWeakRefListener.get();
            }
        }
        if (null != listener) {
            listener.onAudioCapturePcm(data, mSampleRate, mChannels, timestampMs);
        }
    }

    @Override
    public void run() {
        if (!mIsRunning) {
            return;
        }

        init();

        while (mIsRunning && !Thread.interrupted() &&  mCaptureBuffer != null) {
            byte[] pcmData = new byte[mFrameLen];
            System.arraycopy(mCaptureBuffer, mCaptureBufferReadLen, pcmData, 0, mFrameLen);
            onAudioCapturePcm(pcmData, System.currentTimeMillis());
            mCaptureBufferReadLen += mFrameLen;
            if (mCaptureBufferReadLen+mFrameLen > mCaptureBuffer.length) mCaptureBufferReadLen = 0;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mCaptureBufferReadLen = 0;

        uninit();
    }
}