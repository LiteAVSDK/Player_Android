package com.tencent.liteav.demo.common.custom.structs;

import android.opengl.EGLContext;

public class TextureFrame {

    public EGLContext eglContext;

    public int textureId;

    public int width;

    public int height;

    public long timestampMs;

    @Override
    public String toString() {
        return "TextureFrame{" + "textureId=" + textureId + ", width=" + width + ", height=" + height + '}';
    }
}
