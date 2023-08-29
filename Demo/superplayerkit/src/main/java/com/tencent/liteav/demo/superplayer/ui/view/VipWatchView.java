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
 * VIP preview page view
 *
 * VIP 试看界面View
 */
public class VipWatchView extends RelativeLayout implements View.OnClickListener {

    private LinearLayout              mLayoutTips                = null;
    private TextView                  mTextTips                  = null;
    private RelativeLayout            mLayoutVip                 = null;
    private VipWatchModel             mVipWatchModel             = null;
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
        if (R.id.vip_watch_tip_close == v.getId()) {
            mVipWatchViewClickListener.onCloseVipTip();
        } else if (R.id.vip_watch_back_img == v.getId()) {
            mVipWatchViewClickListener.onClickVipTitleBack();
        } else if (R.id.vip_watch_retry_btn == v.getId()) {
            mVipWatchViewClickListener.onClickVipRetry();
        } else if (R.id.vip_watch_handle_vip_btn == v.getId()) {
            mVipWatchViewClickListener.onClickVipBtn();
        }
    }

    public interface VipWatchViewClickListener {
        // Callback when the upper left corner return button is clicked
        void onClickVipTitleBack();

        // Callback when the replay button is clicked
        void onClickVipRetry();

        // Callback when the VIP view is displayed
        void onShowVipView();

        // Callback when the Become VIP Member button is clicked
        void onClickVipBtn();

        // Callback when the close prompt control is clicked
        void onCloseVipTip();
    }

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
     * Display the tip prompt view.
     * Can be called multiple times, with the final data taking precedence
     *
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
     * When the video starts playing and the video playback progress is updated,
     * call this method to set the current playback position of the video in seconds
     *
     * 当视频开始播放的时候，视频播放进度更新时，调用测方法，设置视频当前的播放位置,单位为秒
     */
    public void setCurrentTime(float currentTime) {
        if (canShowVipWatchView(currentTime)) {
            showVipView();
        }
    }

    /**
     * Used to determine if the VIP view can be displayed
     *
     * 用于判断是否可以展示VIP view了
     */
    public boolean canShowVipWatchView(float currentTime) {
        return (mVipWatchModel != null && currentTime >= mVipWatchModel.getCanWatchTime() && !isShowing());
    }

    /**
     * Hide the prompt view
     *
     * 隐藏提示语view
     */
    public void hideTipView() {
        mLayoutTips.setVisibility(View.GONE);
        setVisibility(View.GONE);
    }

    /**
     * Hide the VIP view.
     *
     * 隐藏调VIP view
     */
    public void hideVipView() {
        mLayoutVip.setVisibility(GONE);
        setVisibility(View.GONE);
    }

    /**
     * Determine if the page displays a completely black screen
     *
     * 判断是否展示了全黑的界面
     *
     * @return true Indicates that the page displays the VIP preview page
     *              表示页面展示的是试看VIP界面
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
