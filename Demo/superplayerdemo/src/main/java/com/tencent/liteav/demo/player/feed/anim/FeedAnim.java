package com.tencent.liteav.demo.player.feed.anim;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class FeedAnim {


    public static void moveAnim(View view, int start, int end, AnimatorListenerAdapter listenerAdapter) {
        if (start == end) {
            if (listenerAdapter != null) {
                listenerAdapter.onAnimationEnd(null);
            }
            return;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", start, end);//文字标签Y轴平移
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new LinearInterpolator());
        if (listenerAdapter != null) {
            objectAnimator.addListener(listenerAdapter);
        }
        objectAnimator.start();
    }


}
