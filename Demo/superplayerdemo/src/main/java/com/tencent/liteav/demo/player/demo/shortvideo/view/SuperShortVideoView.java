package com.tencent.liteav.demo.player.demo.shortvideo.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoPlayAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.bean.ShortVideoBean;
import com.tencent.liteav.demo.player.demo.shortvideo.core.PlayerManager;
import com.tencent.liteav.demo.player.demo.shortvideo.core.TXVodPlayerWrapper;

import java.util.ArrayList;
import java.util.List;

public class SuperShortVideoView extends RelativeLayout {
    private static final String TAG                      = "ShortVideoDemo:SuperShortVideoView";
    private static final int    MAX_PLAYER_COUNT_ON_PASS = 3;

    private View                  mRootView;
    private RecyclerView          mRecyclerView;
    private ShortVideoPlayAdapter mAdapter;
    private List<ShortVideoBean>  mUrlList;
    private LinearLayoutManager   mLayoutManager;
    private PagerSnapHelper       mSnapHelper;
    private int                   mLastPositionInIDLE = -1;
    private TXVideoBaseView       mBaseItemView;
    private Handler               mHandler;
    private Object                mLock                = new Object();

    public SuperShortVideoView(Context context) {
        super(context);
        init(context);
    }

    public SuperShortVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SuperShortVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.super_short_video_view, null);
        addView(mRootView);
        mRecyclerView = mRootView.findViewById(R.id.rv_super_short_video);
        mUrlList = new ArrayList<>();
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        mAdapter = new ShortVideoPlayAdapter(mUrlList);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPosition(0);
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mUrlList.clear();
                mUrlList.addAll((List<ShortVideoBean>) msg.obj);
                mAdapter.notifyDataSetChanged();
            }
        };
        addListener();
    }

    public void setDataSource(final List<ShortVideoBean> dataSource) {
        Log.i(TAG, "[setDataSource]");
        Message message = new Message();
        message.obj = dataSource;
        synchronized (mLock) {
            if (mHandler == null) {
                return;
            }
            mHandler.sendMessage(message);
        }

    }

    private void addListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) || !recyclerView.canScrollVertically(-1)) {
                    onScrollStateChanged(recyclerView, RecyclerView.SCROLL_STATE_IDLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止滚动
                        View view = mSnapHelper.findSnapView(mLayoutManager);
                        int position = recyclerView.getChildAdapterPosition(view);
                        Log.i(TAG, "[SCROLL_STATE_IDLE] mLastPositionInIDLE " + mLastPositionInIDLE + " position " + position);
                        if (mLastPositionInIDLE != position) {
                            onPageSelectedMethod(position);
                            mLastPositionInIDLE = position;
                            Log.i(TAG, "[SCROLL_STATE_IDLE] into [startPlay] ");
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滑动
                        break;
                }
            }
        });
    }

    private void onPageSelectedMethod(int position) {
        View view = mSnapHelper.findSnapView(mLayoutManager);
        mBaseItemView = (TXVideoBaseView) view.findViewById(R.id.baseItemView);
        Log.i(TAG, "onPageSelected " + position);
        List<ShortVideoBean> tempUrlList = initUrlList(position, MAX_PLAYER_COUNT_ON_PASS);
        PlayerManager.getInstance(getContext()).updateManager(tempUrlList);
        TXVodPlayerWrapper txVodPlayerWrapper = PlayerManager.getInstance(getContext()).getPlayer(mUrlList.get(position));
        Log.i(TAG, "txVodPlayerWrapper " + txVodPlayerWrapper + "url-- " + mUrlList.get(position).videoURL);
        Log.i(TAG, "txVodPlayerWrapper " + txVodPlayerWrapper);
        mBaseItemView.setTXVodPlayer(txVodPlayerWrapper);
        mBaseItemView.startPlay();
    }

    /**
     * 初始化向PlayManager传递的urlList
     *
     * @param startIndex 开始的索引
     * @param maxCount   传递的urlList的数目
     * @return
     */
    private List<ShortVideoBean> initUrlList(int startIndex, int maxCount) {

        int i = startIndex - 1;
        if (i + maxCount > mUrlList.size()) {
            i = mUrlList.size() - maxCount;
        }
        if (i < 0) {
            i = 0;
        }
        int addedCount = 0;
        List<ShortVideoBean> cacheList = new ArrayList<>();
        while (i < mUrlList.size() && addedCount < maxCount) {
            cacheList.add(mUrlList.get(i));
            addedCount++;
            i++;
        }
        return cacheList;
    }

    public void pause() {
        if (mBaseItemView != null) {
            mBaseItemView.pausePlayer();
        }
    }

    public void releasePlayer() {
        synchronized (mLock) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mBaseItemView != null) {
            mBaseItemView.stopPlayer();
        }
        PlayerManager.getInstance(getContext()).releasePlayer();
    }

    public void onListPageScrolled() {
        if (mBaseItemView != null) {
            mBaseItemView.pausePlayer();
        }
    }

    public void onItemClick(final int position) {
        mRecyclerView.scrollToPosition(position);
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onItemClick");
                onPageSelectedMethod(position);
            }
        });
    }
}
