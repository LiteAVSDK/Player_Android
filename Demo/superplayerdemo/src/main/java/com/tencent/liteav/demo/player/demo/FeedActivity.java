package com.tencent.liteav.demo.player.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.feed.FeedView;
import com.tencent.liteav.demo.player.feed.FeedViewCallBack;
import com.tencent.liteav.demo.player.feed.model.FeedVodListLoader;

import java.util.List;


public class FeedActivity extends Activity {

    private Button            backBtn           = null;
    private FeedView          feedView          = null;
    private TextView          titleTxt          = null;
    private int               page              = 0;
    private FeedVodListLoader feedVodListLoader = null;
    private boolean           isFullScreen      = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_activity_layout);
        this.backBtn = findViewById(R.id.feed_ac_back_btn);
        titleTxt = findViewById(R.id.feed_ac_layout_txt);
        feedView = findViewById(R.id.feed_ac_feed_view);
        feedView.setFeedViewCallBack(new FeedViewCallBack() {
            @Override
            public void onLoadMore() {
                loadMore();
            }

            @Override
            public void onRefresh() {
                loadData(true);
            }

            @Override
            public void onStartDetailPage() {
                titleTxt.setText(getResources().getString(R.string.app_feed_detail_title));
            }

            @Override
            public void onStopDetailPage() {
                titleTxt.setText(getResources().getString(R.string.app_feed_title));
            }

            @Override
            public void onLoadDetailData(VideoModel videoModel) {
                loadDetailData();
            }


            @Override
            public void onStartFullScreenPlay() {
                isFullScreen = true;
                findViewById(R.id.title_layout).setVisibility(View.GONE);
            }

            @Override
            public void onStopFullScreenPlay() {
                isFullScreen = false;
                findViewById(R.id.title_layout).setVisibility(View.VISIBLE);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        feedVodListLoader = new FeedVodListLoader();
        loadData(false);

    }


    private void loadData(final boolean isRefresh) {
        page = 0;
        feedVodListLoader.loadListData(page, new FeedVodListLoader.LoadDataCallBack() {
            @Override
            public void onLoadedData(List<VideoModel> videoModels) {
                if (isDestroyed()) {
                    return;
                }
                feedView.addData(videoModels, true);
                if (isRefresh) {
                    feedView.finishRefresh(true);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (isDestroyed()) {
                    return;
                }
                Toast.makeText(FeedActivity.this, "暂未获取到数据", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 加载更多数据
     */
    private void loadMore() {
        feedVodListLoader.loadListData(page + 1, new FeedVodListLoader.LoadDataCallBack() {
            @Override
            public void onLoadedData(List<VideoModel> videoModels) {
                if (isDestroyed()) {
                    return;
                }
                page++;
                feedView.addData(videoModels, false);
                feedView.finishLoadMore(true, false);
            }

            @Override
            public void onError(int errorCode) {
                if (isDestroyed()) {
                    return;
                }
                feedView.finishLoadMore(false, true);
            }
        });
    }


    private void loadDetailData() {
        feedVodListLoader.loadListData(page, new FeedVodListLoader.LoadDataCallBack() {
            @Override
            public void onLoadedData(List<VideoModel> videoModels) {
                if (isDestroyed()) {
                    return;
                }
                feedView.addDetailListData(videoModels);
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        feedView.onResume();
        //添加屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (isFullScreen) {
            //隐藏虚拟按键，并且全屏
            View decorView = getWindow().getDecorView();
            if (decorView == null) {
                return;
            }
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                decorView.setSystemUiVisibility(View.GONE);
            } else if (Build.VERSION.SDK_INT >= 19) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        feedView.onPause();
        //清楚屏幕常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!feedView.goBack()) {
            super.onBackPressed();
        }

    }
}
