package com.tencent.liteav.demo.play.controller;

import java.lang.ref.WeakReference;

/**
 * 定时隐藏播放控制控件的线程
 */
class HideViewControllerViewRunnable implements Runnable {

    public WeakReference<IController> mWefController; //播放控件弱引用

    public HideViewControllerViewRunnable(IController controller) {
        mWefController = new WeakReference<>(controller);
    }

    @Override
    public void run() {
        if (mWefController != null && mWefController.get() != null) {
            mWefController.get().hide();
        }
    }
}
