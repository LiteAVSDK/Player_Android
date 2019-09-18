package com.tencent.liteav.demo.play.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R;

import java.util.ArrayList;

/**
 * Created by yuejiaoli on 2018/7/4.
 * 清晰度弹框
 */

public class TCVodQualityView extends RelativeLayout {
    private Context mContext;
    private Callback mCallback;
    private ListView mListView;
    private QualityAdapter mAdapter;
    private ArrayList<TCVideoQulity> mList;
    private int mClickPos = -1;

    public TCVodQualityView(Context context) {
        super(context);
        init(context);
    }

    public TCVodQualityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TCVodQualityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mList = new ArrayList<TCVideoQulity>();
        LayoutInflater.from(mContext).inflate(R.layout.player_quality_popup_view, this);
        mListView = (ListView) findViewById(R.id.lv_quality);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallback != null) {
                    if (mList != null && mList.size() > 0) {
                        TCVideoQulity quality = mList.get(position);
                        if (quality != null) {
                            mCallback.onQualitySelect(quality);
                        }
                    }
                }
                mClickPos = position;
                mAdapter.notifyDataSetChanged();
            }
        });
        mAdapter = new QualityAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setVideoQualityList(ArrayList<TCVideoQulity> list) {
        mList.clear();
        mList.addAll(list);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置默认选中的清晰度
     *
     * @param position
     */
    public void setDefaultSelectedQuality(int position) {
        if (position < 0) position = 0;
        mClickPos = position;
        mAdapter.notifyDataSetChanged();
    }

    class QualityAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new QualityItemView(mContext);
            }
            QualityItemView itemView = (QualityItemView) convertView;
            itemView.setSelected(false);
            TCVideoQulity quality = mList.get(position);
            itemView.setQualityName(quality.title);
            if (mClickPos == position) {
                itemView.setSelected(true);
            }
            return itemView;
        }
    }

    class QualityItemView extends RelativeLayout {

        private TextView mTvQuality;

        public QualityItemView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        public QualityItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public QualityItemView(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.player_quality_item_view, this);
            mTvQuality = (TextView) findViewById(R.id.tv_quality);
        }

        public void setQualityName(String qualityName) {
            mTvQuality.setText(qualityName);
        }

        public void setSelected(boolean isChecked) {
            mTvQuality.setSelected(isChecked);
        }
    }

    public interface Callback {
        void onQualitySelect(TCVideoQulity quality);
    }
}
