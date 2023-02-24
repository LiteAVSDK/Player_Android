package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.demo.player.demo.shortvideo.base.AbsBaseFragment;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.shortvideoplayerdemo.R;


import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ShortVideoPlayFragment extends AbsBaseFragment implements View.OnClickListener {
    private static final String TAG = "ShortVideoDemo:ShortVideoPlayFragment";
    private static final String SHARE_PREFERENCE_NAME = "tx_short_video_player_guide_setting";
    private static final String KEY_GUIDE_ONE = "is_guide_one_finish";
    private static final String KEY_GUIDE_TWO = "is_guide_two_finish";
    private static final String KEY_GUIDE_THREE = "is_guide_three_finish";
    private static final String KEY_GUIDE_FOUR = "is_guide_four_finish";

    private RelativeLayout mMaskOne;
    private RelativeLayout mMaskTwo;
    private RelativeLayout mMaskFour;
    private TextView mMaskOneIKnow;
    private TextView mMaskTwoIKnow;
    private TextView mMaskFourIKnow;
    private ImageButton mBack;
    private SuperShortVideoView mSuperShortVideoView;

    @Override
    protected int getLayoutResId() {
        return R.layout.player_fragment_short_video_play;
    }


    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        mMaskOne = getActivity().findViewById(R.id.rl_mask_one);
        mMaskTwo = getActivity().findViewById(R.id.rl_mask_two);
        mMaskFour = getActivity().findViewById(R.id.rl_mask_four);
        mSuperShortVideoView = getActivity().findViewById(R.id.super_short_video_view_play_fragment);
        mMaskOneIKnow = getActivity().findViewById(R.id.tv_mask_one_i_know);
        mMaskOneIKnow.setOnClickListener(this);
        mMaskTwoIKnow = getActivity().findViewById(R.id.tv_mask_two_i_know);
        mMaskTwoIKnow.setOnClickListener(this);
        mMaskFourIKnow = getActivity().findViewById(R.id.tv_mask_four_i_know);
        mMaskFourIKnow.setOnClickListener(this);
        mBack = getActivity().findViewById(R.id.ib_back_play);
        mBack.setOnClickListener(this);
        initMask();
    }

    private void initMask() {
        boolean isFinishOne = getBoolean(KEY_GUIDE_ONE);
        boolean isFinishTwo = getBoolean(KEY_GUIDE_TWO);
        boolean isFinishFour = getBoolean(KEY_GUIDE_FOUR);
        if (!isFinishOne) {
            mMaskOne.setVisibility(View.VISIBLE);
            mMaskTwo.setVisibility(View.GONE);
            mMaskFour.setVisibility(View.GONE);
        } else if (!isFinishTwo) {
            mMaskOne.setVisibility(View.GONE);
            mMaskTwo.setVisibility(View.VISIBLE);
            mMaskFour.setVisibility(View.GONE);
        } else if (!isFinishFour) {
            mMaskOne.setVisibility(View.GONE);
            mMaskTwo.setVisibility(View.GONE);
            mMaskFour.setVisibility(View.VISIBLE);
        } else {
            mMaskOne.setVisibility(View.GONE);
            mMaskTwo.setVisibility(View.GONE);
            mMaskFour.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_back_play) {
            getActivity().finish();
        } else if (v.getId() == R.id.tv_mask_one_i_know) {
            mMaskOne.setVisibility(GONE);
            mMaskTwo.setVisibility(VISIBLE);
            mMaskFour.setVisibility(GONE);
            putBoolean(KEY_GUIDE_ONE, true);
            putBoolean(KEY_GUIDE_TWO, false);
            putBoolean(KEY_GUIDE_THREE, false);
            putBoolean(KEY_GUIDE_FOUR, false);
        } else if (v.getId() == R.id.tv_mask_two_i_know) {
            mMaskOne.setVisibility(GONE);
            mMaskTwo.setVisibility(GONE);
            mMaskFour.setVisibility(VISIBLE);
            putBoolean(KEY_GUIDE_ONE, true);
            putBoolean(KEY_GUIDE_TWO, true);
            putBoolean(KEY_GUIDE_THREE, false);
            putBoolean(KEY_GUIDE_FOUR, false);
        } else if (v.getId() == R.id.tv_mask_four_i_know) {
            mMaskOne.setVisibility(GONE);
            mMaskTwo.setVisibility(GONE);
            mMaskFour.setVisibility(GONE);
            putBoolean(KEY_GUIDE_ONE, true);
            putBoolean(KEY_GUIDE_TWO, true);
            putBoolean(KEY_GUIDE_THREE, true);
            putBoolean(KEY_GUIDE_FOUR, true);
        } else {
            Log.i(TAG, "onClick in other case");
        }
    }

    @Override
    protected void initData() {

    }


    @Override
    public void onStop() {
        super.onStop();
        mSuperShortVideoView.pause();
    }


    @Override
    public void onDestroyView() {
        mSuperShortVideoView.onDestroy();
        super.onDestroyView();
        mSuperShortVideoView.releasePlayer();
    }

    private void putBoolean(String key, boolean value) {
        getContext().getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    private boolean getBoolean(String key) {
        return getContext().getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    public void onLoaded(List<VideoModel> shortVideoBeanList) {
        if (mSuperShortVideoView != null) {
            mSuperShortVideoView.setDataSource(shortVideoBeanList);
        }
    }
}
