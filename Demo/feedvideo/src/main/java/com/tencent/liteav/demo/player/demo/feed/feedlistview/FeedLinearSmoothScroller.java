package com.tencent.liteav.demo.player.demo.feed.feedlistview;


import android.content.Context;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class FeedLinearSmoothScroller extends LinearSmoothScroller {
    private Context context    = null;
    private int     itemHeight = 0;

    public FeedLinearSmoothScroller(Context context, int height) {
        super(context);
        this.context = context;
        itemHeight = height;
    }


    @Override
    protected int getVerticalSnapPreference() {
        return LinearSmoothScroller.SNAP_TO_START;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        switch (snapPreference) {
            case SNAP_TO_START:
                int phoneHeight = context.getResources().getDisplayMetrics().heightPixels;
                int height = (int) (phoneHeight - dp2px(context, 50) - getStatusBarHeightCompat(context));
                return boxStart - viewStart + (height - itemHeight) / 2;
            case SNAP_TO_END:
                return boxEnd - viewEnd;
            case SNAP_TO_ANY:
                final int dtStart = boxStart - viewStart;
                if (dtStart > 0) {
                    return dtStart;
                }
                final int dtEnd = boxEnd - viewEnd;
                if (dtEnd < 0) {
                    return dtEnd;
                }
                break;
            default:
                throw new IllegalArgumentException("snap preference should be one of the"
                        + " constants defined in SmoothScroller, starting with SNAP_");
        }
        return 0;
    }

    private float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private int getStatusBarHeightCompat(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }
        if (result <= 0) {
            result = (int) dp2px(context, 25);
        }
        return result;
    }

}
