package com.tencent.liteav.demo.common.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.common.R;

/**
 * ContentLoadingDialog implements a AlertDialog that waits a minimum time to be
 * dismissed before showing. Once visible, the dialog will be visible for
 * a minimum amount of time to avoid "flashes" in the UI when an event could take
 * a largely variable time to complete (from none, to a user perceivable amount)
 */
public class ContentLoadingDialog extends AlertDialog {
    private static final int MIN_SHOW_TIME = 500; // ms
    private static final int MIN_DELAY     = 50; // ms

    private TextView mMessageTv;

    private long    mStartTime  = -1;
    private boolean mPostedHide = false;
    private boolean mPostedShow = false;
    private boolean mDismissed  = false;

    private Handler mHandler = new Handler();

    private final Runnable mDelayedHide = new Runnable() {

        @Override
        public void run() {
            mPostedHide = false;
            mStartTime = -1;
            dismiss();
        }
    };

    private final Runnable mDelayedShow = new Runnable() {

        @Override
        public void run() {
            mPostedShow = false;
            if (!mDismissed) {
                mStartTime = System.currentTimeMillis();
                show();
            }
        }
    };

    public ContentLoadingDialog(@NonNull Context context) {
        super(context, R.style.LoadingDialogStyle);
        View loadView = LayoutInflater.from(getContext()).inflate(R.layout.common_content_loading_dialog, null);
        setView(loadView);
        mMessageTv = loadView.findViewById(R.id.common_content_loading_tv);
        setCancelable(false);
    }

    /**
     * Show the dialog view after waiting for a minimum delay. If
     * during that time, hide() is called, the view is never made visible.
     */
    public void showDialog(String message) {
        mMessageTv.setText(message);
        // Reset the start time.
        mStartTime = -1;
        mDismissed = false;
        mHandler.removeCallbacks(mDelayedHide);
        mPostedHide = false;
        if (!mPostedShow) {
            mHandler.postDelayed(mDelayedShow, MIN_DELAY);
            mPostedShow = true;
        }
    }

    /**
     * Hide the dialog view if it is visible. The dialog view will not be
     * hidden until it has been shown for at least a minimum show time. If the
     * dialog view was not yet visible, cancels showing the dialog view.
     */
    public void hideDialog() {
        mDismissed = true;
        mHandler.removeCallbacks(mDelayedShow);
        mPostedShow = false;
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME || mStartTime == -1) {
            // The dialog has been shown long enough
            // OR was not shown yet. If it wasn't shown yet,
            // it will just never be shown.
            dismiss();
        } else {
            // The dialog is shown, but not long enough,
            // so put a delayed message in to hide it when its been
            // shown long enough.
            if (!mPostedHide) {
                mHandler.postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
                mPostedHide = true;
            }
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }

    private void removeCallbacks() {
        mHandler.removeCallbacks(mDelayedHide);
        mHandler.removeCallbacks(mDelayedShow);
    }
}
