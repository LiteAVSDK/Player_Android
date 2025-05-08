package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.comment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.adapter.TUICommentListAdapter;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.data.ShortVideoGenerator;
import com.tencent.qcloud.tuiplayer.core.api.ui.player.ITUIVodPlayer;
import com.tencent.qcloud.tuiplayer.core.api.ui.view.TUIBaseVideoView;

public class TUICommentDemoMenu extends FrameLayout {

    private static final long ANIM_TIME = 300L;

    private int displayOrgHeight = -1;
    private int displayDstHeight = -1;

    private boolean isShow = false;

    private boolean isAnimating = false;

    private View mCommentContainer;

    public TUICommentDemoMenu(@NonNull Context context) {
        this(context, null);
    }

    public TUICommentDemoMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TUICommentDemoMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mCommentContainer = LayoutInflater.from(getContext())
                .inflate(R.layout.tuiplayer_comment_menu, this);
        RecyclerView commentListView = findViewById(R.id.rv_comment_list);
        TUICommentListAdapter commentListAdapter = new TUICommentListAdapter();
        commentListView.setAdapter(commentListAdapter);
        commentListAdapter.setData(ShortVideoGenerator.generateComments(8));
    }

    private void resetContainerHeight() {
        ViewGroup.LayoutParams layoutParams = mCommentContainer.getLayoutParams();
        layoutParams.height = 0;
        mCommentContainer.setLayoutParams(layoutParams);
    }

    private void startSqueezeVideo(final View displayView) {
        ValueAnimator displayViewSqueezeAnimator = ValueAnimator.ofInt(displayOrgHeight, displayDstHeight);
        displayViewSqueezeAnimator.setDuration(ANIM_TIME); // 设置动画持续时间，单位：毫秒
        displayViewSqueezeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = displayView.getLayoutParams();
                layoutParams.height = animatedValue;
                displayView.setLayoutParams(layoutParams);
            }
        });
        displayViewSqueezeAnimator.start();
    }

    private void startShowComment() {
        ValueAnimator commentShowAnimator = ValueAnimator.ofInt(0, displayDstHeight);
        commentShowAnimator.setDuration(ANIM_TIME); // 设置动画持续时间，单位：毫秒
        commentShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mCommentContainer.getLayoutParams();
                layoutParams.height = animatedValue;
                mCommentContainer.setLayoutParams(layoutParams);
            }
        });
        commentShowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isShow = true;
                isAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isShow = true;
                isAnimating = false;
            }
        });
        commentShowAnimator.start();
    }

    private void startExpandVideo(final View displayView, final ITUIVodPlayer player) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(displayDstHeight, displayOrgHeight);
        valueAnimator.setDuration(ANIM_TIME); // 设置动画持续时间，单位：毫秒
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = displayView.getLayoutParams();
                layoutParams.height = animatedValue;
                displayView.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.start();
    }

    private void startHideComment() {
        ValueAnimator commentHideAnimator = ValueAnimator.ofInt(displayDstHeight, 0);
        commentHideAnimator.setDuration(ANIM_TIME); // 设置动画持续时间，单位：毫秒
        commentHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mCommentContainer.getLayoutParams();
                layoutParams.height = animatedValue;
                mCommentContainer.setLayoutParams(layoutParams);
            }
        });
        commentHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mCommentContainer.setVisibility(View.GONE);
                isShow = false;
                isAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCommentContainer.setVisibility(View.GONE);
                isShow = false;
                isAnimating = false;
            }
        });
        commentHideAnimator.start();
    }

    public void show(TUIBaseVideoView videoView, ITUIVodPlayer player) {
        if (isAnimating) {
            return;
        }
        if (!isShow) {
            if (null != videoView && null != videoView.getDisplayView()) {
                resetContainerHeight();
                mCommentContainer.setVisibility(View.VISIBLE);
                isAnimating = true;
                final View displayView = videoView.getDisplayView();
                if (displayDstHeight <= 0) {
                    displayDstHeight = (int) (videoView.getHeight() * 0.5);
                }
                if (displayOrgHeight <= 0) {
                    displayOrgHeight = displayView.getHeight();
                }
                startSqueezeVideo(displayView);
                startShowComment();
            }
        }
    }

    public void dismiss(TUIBaseVideoView videoView, final ITUIVodPlayer player) {
        if (isAnimating) {
            return;
        }
        if (isShow) {
            if (null != videoView && null != videoView.getDisplayView() && displayOrgHeight > 0) {
                isAnimating = true;
                final View displayView = videoView.getDisplayView();
                startExpandVideo(displayView, player);
                startHideComment();
            }
        }
    }

    public void toggle(TUIBaseVideoView videoView, ITUIVodPlayer player) {
        if (isShow) {
            dismiss(videoView, player);
        } else {
            show(videoView, player);
        }
    }

    public boolean isShow() {
        return isShow;
    }
}
