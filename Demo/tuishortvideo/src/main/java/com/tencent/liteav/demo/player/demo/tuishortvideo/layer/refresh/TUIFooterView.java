package com.tencent.liteav.demo.player.demo.tuishortvideo.layer.refresh;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;

public class TUIFooterView extends RelativeLayout implements TUIRefreshLayout.OnFooterStateListener {

    private ImageView ivLoading;
    private TextView tvState;
    private ProgressBar mProgressBar;

    private boolean hasMore = true;

    private String footerPulling;
    private String footerRelease;
    private String footerLoading;
    private String footerLoadingFinish;
    private String footerLoadingFailure;
    private String footerNothing;

    public TUIFooterView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.footer_view_layout, this, false);
        this.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        initView(layout);

        Resources resources = context.getResources();
        footerPulling = resources.getString(R.string.tuiplayer_footer_pulling);
        footerRelease = resources.getString(R.string.tuiplayer_footer_release);
        footerLoading = resources.getString(R.string.tuiplayer_footer_loading);
        footerLoadingFinish = resources.getString(R.string.tuiplayer_footer_loading_finish);
        footerLoadingFailure = resources.getString(R.string.tuiplayer_footer_loading_failure);
        footerNothing = resources.getString(R.string.tuiplayer_footer_nothing);
    }

    private void initView(View view) {
        ivLoading = (ImageView) view.findViewById(R.id.iv_loading);
        tvState = (TextView) view.findViewById(R.id.tv_state);
        mProgressBar = view.findViewById(R.id.pb_loading);
    }

    @Override
    public void onScrollChange(View tail, int scrollOffset, int scrollRatio) {
        if (hasMore) {
            if (scrollRatio < 100) {
                tvState.setText(footerPulling);
                mProgressBar.setVisibility(GONE);
                ivLoading.setImageResource(R.drawable.tuiplayer_ic_down_arrow);
                ivLoading.setRotation(180);
            } else {
                mProgressBar.setVisibility(GONE);
                tvState.setText(footerRelease);
                ivLoading.setImageResource(R.drawable.tuiplayer_ic_down_arrow);
                ivLoading.setRotation(0);
            }
        }
    }

    @Override
    public void onRefresh(View footerView) {
        if (hasMore) {
            tvState.setText(footerLoading);
            ivLoading.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onRetract(View footerView, boolean isSuccess) {
        if (hasMore) {
            tvState.setText(isSuccess ? footerLoadingFinish : footerLoadingFailure);
            mProgressBar.setVisibility(GONE);
            ivLoading.setVisibility(GONE);
        }
    }

    @Override
    public void onHasMore(View tail, boolean hasMore) {
        this.hasMore = hasMore;
        if (!hasMore) {
            tvState.setText(footerNothing);
            mProgressBar.setVisibility(GONE);
            ivLoading.setVisibility(GONE);
        }
    }
}
