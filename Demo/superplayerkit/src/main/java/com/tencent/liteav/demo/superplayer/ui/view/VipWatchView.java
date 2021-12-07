package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;

/**
 * VIP 试看界面View
 * showTip(string tip,int times)  展示tip提示view
 * 参数说明：tip指展示的文本信息   times是指试看的最长时间，单位为秒
 * setCurrentTime(int currentTime)
 */
public class VipWatchView extends RelativeLayout implements View.OnClickListener {

    private LinearLayout              mLayoutTips                = null;
    private TextView                  mTextTips                  = null;   //用于展示  "可试看30s，开通VIP观看完整视频"的提示语信息
    private RelativeLayout            mLayoutVip                 = null;   //用于展示VIP重试的view
    private VipWatchModel             mVipWatchModel             = null;    //
    private VipWatchViewClickListener mVipWatchViewClickListener = null;


    public VipWatchView(Context context) {
        super(context);
        initView();
    }

    public VipWatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VipWatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setVipWatchViewClickListener(VipWatchViewClickListener mVipWatchViewClickListener) {
        this.mVipWatchViewClickListener = mVipWatchViewClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mVipWatchViewClickListener == null) {
            return;
        }
        if (R.id.vip_watch_tip_close == v.getId()) {  //关闭按钮
            mVipWatchViewClickListener.onCloseVipTip();
        } else if (R.id.vip_watch_back_img == v.getId()) {  //返回按钮
            mVipWatchViewClickListener.onClickVipTitleBack();
        } else if (R.id.vip_watch_retry_btn == v.getId()) {  //重试按钮
            mVipWatchViewClickListener.onClickVipRetry();
        } else if (R.id.vip_watch_handle_vip_btn == v.getId()) {
            mVipWatchViewClickListener.onClickVipBtn();
        }
    }

    public interface VipWatchViewClickListener {
        //当点击左上角返回按钮时回调
        void onClickVipTitleBack();

        //当用于点击重看的时候回调
        void onClickVipRetry();

        //当展示VIP view的时候回调
        void onShowVipView();

        //当点击开通VIP按钮的时候回调
        void onClickVipBtn();

        //当点击关闭提示控件时回调此方法
        void onCloseVipTip();
    }

    /**
     * 初始化页面元素
     */
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.superplayer_vod_vipwatch_view, this);
        mLayoutTips = findViewById(R.id.vip_watch_tip_view);
        mTextTips = findViewById(R.id.vip_watch_tip_txt);
        findViewById(R.id.vip_watch_tip_close).setOnClickListener(this);
        findViewById(R.id.vip_watch_back_img).setOnClickListener(this);
        findViewById(R.id.vip_watch_retry_btn).setOnClickListener(this);
        mLayoutVip = findViewById(R.id.vip_view);
        findViewById(R.id.vip_watch_handle_vip_btn).setOnClickListener(this);
    }


    /**
     * 展示tip提示view
     * 可调用多次，以最后一次的数据为准
     */
    public void setVipWatchMode(VipWatchModel vipWatchModel) {
        hideVipView();
        this.mVipWatchModel = vipWatchModel;
        if (mVipWatchModel != null && !TextUtils.isEmpty(mVipWatchModel.getTipStr())) {
            setVisibility(VISIBLE);
            mTextTips.setText(mVipWatchModel.getTipStr());
            mLayoutTips.setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
            mTextTips.setText("");
            mLayoutTips.setVisibility(GONE);
        }
    }

    /**
     * 当视频开始播放的时候，视频播放进度更新时，调用测方法，设置视频当前的播放位置,单位为秒
     *
     * @param currentTime 视频已经播放了多长时间，单位为秒
     */
    public void setCurrentTime(float currentTime) {
        if (canShowVipWatchView(currentTime)) {
            showVipView();
        }
    }

    /**
     * 用于判断是否可以展示VIP view了
     *
     * @param currentTime 当前视频的播放位置（时间节点）
     * @return
     */
    public boolean canShowVipWatchView(float currentTime) {
        return (mVipWatchModel != null && currentTime >= mVipWatchModel.getCanWatchTime() && !isShowing());
    }

    /**
     * 隐藏提示语view
     */
    public void hideTipView() {
        mLayoutTips.setVisibility(View.GONE);
        setVisibility(View.GONE);
    }

    /**
     * 隐藏调VIP view
     */
    public void hideVipView() {
        mLayoutVip.setVisibility(GONE);
        setVisibility(View.GONE);
    }

    /**
     * 判断是否展示了全黑的界面
     *
     * @return true 表示页面展示的是试看VIP界面
     */
    public boolean isShowing() {
        return mLayoutVip.getVisibility() == View.VISIBLE;
    }

    private void showVipView() {
        if (mVipWatchViewClickListener != null) {
            mVipWatchViewClickListener.onShowVipView();
        }
        setVisibility(View.VISIBLE);
        mLayoutTips.setVisibility(GONE);
        mLayoutVip.setVisibility(VISIBLE);
    }


}
