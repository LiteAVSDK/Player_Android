package com.tencent.liteav.demo.common.listener;

import android.view.View;

/**
 * 作用：防止快速点击按钮引起的错误
 * <p>
 * 使用：Button.setOnClickListener 时，使用此类作的实现类作为Listener
 */
public abstract class OnSingleClickListener implements View.OnClickListener{
    private static final long MIN_CLICK_DELAY_TIME = 500;
    private              long mLastClickTime       = 0;
    
    @Override
    public void onClick(View v) {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - mLastClickTime) >= MIN_CLICK_DELAY_TIME) {
            mLastClickTime = curClickTime;
            onSingleClick(v);
        }
    }
    
    public abstract void onSingleClick(View v);
}
