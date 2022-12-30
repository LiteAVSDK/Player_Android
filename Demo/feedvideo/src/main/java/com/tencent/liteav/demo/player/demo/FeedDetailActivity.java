package com.tencent.liteav.demo.player.demo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.tencent.liteav.demo.feedvideo.R;
import com.tencent.liteav.demo.player.demo.feed.feeddetailview.FeedDetailView;
import com.tencent.liteav.demo.player.demo.feed.model.FeedVodListLoader;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.io.Serializable;
import java.util.List;

import static com.tencent.liteav.demo.player.demo.FeedActivity.KEY_CURRENT_TIME;

public class FeedDetailActivity  extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private FeedDetailView    feedDetailView = null;
    private FeedVodListLoader feedVodListLoader = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_activity_detail);
        feedVodListLoader = new FeedVodListLoader(this);
        feedDetailView = findViewById(R.id.feed_detail_view);
        Serializable tempValue = getIntent().getExtras().getSerializable(FeedActivity.KEY_VIDEO_MODEL);
        if (tempValue != null && tempValue instanceof VideoModel) {
            VideoModel videoModel = (VideoModel) getIntent().getExtras().getSerializable(FeedActivity.KEY_VIDEO_MODEL);
            int time = (int) getIntent().getLongExtra(KEY_CURRENT_TIME,0);
            feedDetailView.setStartTime(time);
            feedDetailView.play(videoModel);
            feedDetailView.showDetailView(videoModel);
            loadDetailData();
        }
    }

    private void loadDetailData() {
        feedVodListLoader.loadListData(0, new FeedVodListLoader.LoadDataCallBack() {
            @Override
            public void onLoadedData(List<VideoModel> videoModels) {
                if (isDestroyed()) {
                    return;
                }
                feedDetailView.addDetailListData(videoModels);
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        feedDetailView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        feedDetailView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedDetailView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        feedDetailView.onRequestPermissionsResult(requestCode,grantResults);
    }
}
