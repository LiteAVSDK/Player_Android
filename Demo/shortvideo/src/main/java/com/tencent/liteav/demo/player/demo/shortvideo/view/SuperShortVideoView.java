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

import com.tencent.liteav.demo.player.demo.shortvideo.adapter.ShortVideoPlayAdapter;
import com.tencent.liteav.demo.player.demo.shortvideo.core.PlayerManager;
import com.tencent.liteav.demo.player.demo.shortvideo.core.TXVodPlayerWrapper;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.liteav.shortvideoplayerdemo.R;

import java.util.ArrayList;
import java.util.List;

public class SuperShortVideoView extends RelativeLayout {
    private static final String TAG                      = "ShortVideoDemo:SuperShortVideoView";
    private static final int    MAX_PLAYER_COUNT_ON_PASS = 3;

    private View                  mRootView;
    private RecyclerView          mRecyclerView;
    private ShortVideoPlayAdapter mAdapter;
    private List<VideoModel>      mUrlList;
    private LinearLayoutManager   mLayoutManager;
    private PagerSnapHelper       mSnapHelper;
    private int                   mLastPositionInIDLE = -1;
    private TXVideoBaseView       mBaseItemView;
    private Handler               mHandler;
    private Object                mLock                = new Object();
    private Context               mContext;
    private boolean               mIsOnDestroy;

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
        mContext = context;
        mRootView = LayoutInflater.from(context).inflate(R.layout.super_short_video_view, null);
        addView(mRootView);
        mRecyclerView = mRootView.findViewById(R.id.rv_super_short_video);
        mUrlList = new ArrayList<>();
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        mAdapter = new ShortVideoPlayAdapter(mContext,mUrlList);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(6);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPosition(0);
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mUrlList.clear();
                mUrlList.addAll((List<VideoModel>) msg.obj);
                mAdapter.notifyDataSetChanged();
            }
        };
        addListener();
    }

    public void setDataSource(final List<VideoModel> dataSource) {
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
                        onPageSelectedMethod(position);
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
        if (mLastPositionInIDLE != position) {
            View view = mSnapHelper.findSnapView(mLayoutManager);
            if (view == null) {
                return;
            }
            mBaseItemView = (TXVideoBaseView) view.findViewById(R.id.baseItemView);
            Log.i(TAG, "onPageSelected " + position);
            List<VideoModel> tempUrlList = initUrlList(position, MAX_PLAYER_COUNT_ON_PASS);
            PlayerManager.getInstance(getContext()).updateManager(tempUrlList);
            TXVodPlayerWrapper txVodPlayerWrapper = PlayerManager.getInstance(getContext())
                    .getPlayer(mUrlList.get(position));
            Log.i(TAG, "txVodPlayerWrapper " + txVodPlayerWrapper + "url-- " + mUrlList.get(position).videoURL);
            Log.i(TAG, "txVodPlayerWrapper " + txVodPlayerWrapper);
            mBaseItemView.setTXVodPlayer(txVodPlayerWrapper);
            mLastPositionInIDLE = position;
        }
        if (mBaseItemView != null && !mIsOnDestroy) {
            mBaseItemView.startPlay();
        }
    }

    /**
     * 初始化向PlayManager传递的urlList
     *
     * @param startIndex 开始的索引
     * @param maxCount   传递的urlList的数目
     * @return
     */
    private List<VideoModel> initUrlList(int startIndex, int maxCount) {

        int i = startIndex - 1;
        if (i + maxCount > mUrlList.size()) {
            i = mUrlList.size() - maxCount;
        }
        if (i < 0) {
            i = 0;
        }
        int addedCount = 0;
        List<VideoModel> cacheList = new ArrayList<>();
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

    public void onDestroy() {
        mIsOnDestroy = true;
    }
}
