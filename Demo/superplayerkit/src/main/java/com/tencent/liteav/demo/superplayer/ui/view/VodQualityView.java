package com.tencent.liteav.demo.superplayer.ui.view;

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

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuejiaoli on 2018/7/4.
 * <p>
 * 视频画质选择弹框
 * <p>
 * 1、设置画质列表{@link #setVideoQualityList(List)}
 * <p>
 * 2、设置默认选中的画质{@link #setDefaultSelectedQuality(int)}
 */

public class VodQualityView extends RelativeLayout {
    private Context            mContext;
    private Callback           mCallback;      // 回调
    private ListView           mListView;      // 画质listView
    private QualityAdapter     mAdapter;       // 画质列表适配器
    private List<VideoQuality> mList;          // 画质列表
    private int                mClickPos = -1; // 当前的画质下表

    public VodQualityView(Context context) {
        super(context);
        init(context);
    }

    public VodQualityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VodQualityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mList = new ArrayList<VideoQuality>();
        LayoutInflater.from(mContext).inflate(R.layout.superplayer_quality_popup_view, this);
        mListView = (ListView) findViewById(R.id.superplayer_lv_quality);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallback != null) {
                    if (mList != null && mList.size() > 0) {
                        VideoQuality quality = mList.get(position);
                        if (quality != null && position != mClickPos) {
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

    /**
     * 设置回调
     *
     * @param callback
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 设置画质列表
     *
     * @param list
     */
    public void setVideoQualityList(List<VideoQuality> list) {
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
            VideoQuality quality = mList.get(position);
            itemView.setQualityName(quality.title);
            if (mClickPos == position) {
                itemView.setSelected(true);
            }
            return itemView;
        }
    }

    /**
     * 画质item view
     */
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
            LayoutInflater.from(context).inflate(R.layout.superplayer_quality_item_view, this);
            mTvQuality = (TextView) findViewById(R.id.superplayer_tv_quality);
        }

        /**
         * 设置画质名称
         *
         * @param qualityName
         */
        public void setQualityName(String qualityName) {
            mTvQuality.setText(qualityName);
        }

        /**
         * 设置画质item是否为选择状态
         *
         * @param isChecked
         */
        public void setSelected(boolean isChecked) {
            mTvQuality.setSelected(isChecked);
        }
    }

    /**
     * 回调
     */
    public interface Callback {
        /**
         * 画质选择回调
         *
         * @param quality
         */
        void onQualitySelect(VideoQuality quality);
    }
}
