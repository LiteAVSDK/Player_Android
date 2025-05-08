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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TUIHeaderView extends RelativeLayout implements TUIRefreshLayout.OnHeaderStateListener {
    private ImageView ivLoading;
    private TextView tvState;
    private TextView tvRefreshTime;
    private ProgressBar mProgressBar;

    private DateFormat mLastUpdateFormat;

    private String headerPulling;
    private String headerRefreshing;
    private String headerRelease;
    private String headerRefreshFinish;
    private String headerRefreshFailure;
    private String headerUpdate;

    public TUIHeaderView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.header_view_layout, this, false);
        this.addView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        initView(layout);

        Resources resources = context.getResources();
        headerPulling = resources.getString(R.string.tuiplayer_header_pulling);
        headerRefreshing = resources.getString(R.string.tuiplayer_header_refreshing);
        headerRelease = resources.getString(R.string.tuiplayer_header_release);
        headerRefreshFinish = resources.getString(R.string.tuiplayer_header_refresh_finish);
        headerRefreshFailure = resources.getString(R.string.tuiplayer_header_refresh_failure);
        headerUpdate = resources.getString(R.string.tuiplayer_header_update);

        mLastUpdateFormat = new SimpleDateFormat(headerUpdate, Locale.ROOT);

        tvRefreshTime.setText(mLastUpdateFormat.format(new Date()));
    }

    private void initView(View view) {
        ivLoading = (ImageView) view.findViewById(R.id.iv_loading);
        tvState = (TextView) view.findViewById(R.id.tv_state);
        tvRefreshTime = (TextView) view.findViewById(R.id.tv_refresh_time);
        mProgressBar = view.findViewById(R.id.pb_loading);
    }

    public void setRefreshTime(Date date) {
        tvRefreshTime.setText(mLastUpdateFormat.format(date));
    }

    @Override
    public void onScrollChange(View head, int scrollOffset, int scrollRatio) {
        if (scrollRatio < 100) {
            tvState.setText(headerPulling);
            ivLoading.setImageResource(R.drawable.tuiplayer_ic_down_arrow);
            ivLoading.setRotation(0);
        } else {
            tvState.setText(headerRelease);
            ivLoading.setImageResource(R.drawable.tuiplayer_ic_down_arrow);
            ivLoading.setRotation(180);
        }
    }

    @Override
    public void onRefresh(View headerView) {
        tvState.setText(headerRefreshing);
        ivLoading.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    @Override
    public void onRetract(View headerView, boolean isSuccess) {
        if (isSuccess){
            tvState.setText(headerRefreshFinish);
            tvRefreshTime.setText(mLastUpdateFormat.format(new Date()));
        } else {
            tvState.setText(headerRefreshFailure);
        }
        mProgressBar.setVisibility(GONE);
    }
}
