package com.tencent.liteav.demo.player.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.tencent.liteav.demo.player.R;
import com.tencent.rtmp.TXBitrateItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BitrateView extends FrameLayout {

    public ArrayList<Button> mButtons;

    public BitrateView(Context context) {
        super(context);
        init(null, 0);
    }

    public BitrateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BitrateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes


        View view = inflate(getContext(), R.layout.superplayer_sample_bitrate_view, null);
        addView(view);

        mButtons = new ArrayList<>();
        mButtons.add((Button)findViewById(R.id.superplayer_bv_button1));
        mButtons.add((Button)findViewById(R.id.superplayer_bv_button2));
        mButtons.add((Button)findViewById(R.id.superplayer_bv_button3));
        mButtons.add((Button)findViewById(R.id.superplayer_bv_button4));
        for (int i = 0; i < mButtons.size(); i++) {
            mButtons.get(i).setVisibility(GONE);
        }
    }

    private int mSelectedIndex;
    private int mShows;
    private ArrayList<TXBitrateItem> mSource;

    public void setDataSource(ArrayList<TXBitrateItem> source) {
        if (source == null)
            return;
        mShows = Math.min(mButtons.size(), source.size());
        if (mShows <= 1)
            return;
        mSource = new ArrayList<>(source);
        Collections.sort(mSource, new Comparator<TXBitrateItem>() {
            @Override
            public int compare(TXBitrateItem t1, TXBitrateItem t2) {
                return t2.bitrate - t1.bitrate;
            }
        });
        // hidden button
        int i, j;
        for (i = 0; i < mButtons.size()-mShows; i++) {
            mButtons.get(i).setVisibility(GONE);
        }
        // assign index
        for (j = 0; j < mShows; j++, i++) {
            TXBitrateItem item = mSource.get(j);
            Button btn = mButtons.get(i);
            btn.setTag(item);
            btn.setVisibility(VISIBLE);
            if (item.index == mSelectedIndex) {
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setTextColor(Color.LTGRAY);
            }
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickEvent(view);
                }
            });
        }
    }

    public interface OnSelectBitrateListener {
        void onBitrateIndex(int index);
    }

    private OnSelectBitrateListener mListener;
    public void onClickEvent(View view) {
        for (int j = mButtons.size()-mShows; j < mButtons.size(); j++) {
            Button btn = mButtons.get(j);
            if (view == btn) {
                btn.setTextColor(Color.WHITE);
                TXBitrateItem item = (TXBitrateItem) btn.getTag();
                mSelectedIndex = item.index;
            } else {
                btn.setTextColor(Color.LTGRAY);
            }
        }
        if (mListener != null) {
            mListener.onBitrateIndex(mSelectedIndex);
        }
    }

    public void setListener(OnSelectBitrateListener listener) {
        mListener = listener;
    }
    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
    }
}
