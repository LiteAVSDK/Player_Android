package com.tencent.player;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tencent.player.entity.SuperPlayerModel;
import com.tencent.player.entity.SuperPlayerVideoId;
import com.tencent.player.entity.VideoModel;
import com.tencent.player.playwithsurfaceviewdemo.PlayWithSurfaceViewActivity;
import com.tencent.player.voddownloadandplay.R;
import com.tencent.player.voddownloadandplay.VideoDownloadListView;
import com.tencent.player.voddownloadandplay.download.VideoDownloadCenter;
import com.tencent.player.voddownloadandplay.download.VideoDownloadModel;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VodDownLoadActivity extends AppCompatActivity implements VideoDownloadListView.OnVideoPlayListener
                    , VideoDownloadCenter.OnDownloadStartListener {

    private static final String DEFAULT_NAME = "default";

    private static VideoDownloadListView mVideoDownloadListView;

    private List<TXVodDownloadMediaInfo> mDataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_download);
        mDataList = new ArrayList<>();
        File sdcardDir = getExternalFilesDir(null);
        VideoDownloadCenter.getInstance().setOnDownloadStartListener(this);
        VideoDownloadCenter.getInstance().setDownloadDirPath(sdcardDir.getAbsolutePath());
        mVideoDownloadListView = findViewById(R.id.video_download_list_view);
        mVideoDownloadListView.setOnVideoPlayListener(this);
        loadData();
    }


    
    private void loadData() {
        VideoDownloadModel videoDownloadModel = null;
        SuperPlayerModel model = null;
        SuperPlayerVideoId videoId = null;
        videoDownloadModel = new VideoDownloadModel();
        model = new SuperPlayerModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09d5b1bf387702299773851453/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.title = "不只是音视频 1";
        model.url = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09d5b1bf387702299773851453/adp.10.m3u8";
        model.appId = 1500005830;
        videoId = new SuperPlayerVideoId();
        videoId.fileId = "387702299773851453";
        model.videoId = videoId;
        videoDownloadModel.setQualityId(TXVodDownloadDataSource.QUALITY_HD);
        videoDownloadModel.setPlayerModel(model);
        videoDownloadModel.setUserName(DEFAULT_NAME);
        VideoDownloadCenter.getInstance().startDownload(videoDownloadModel);


        model = new SuperPlayerModel();
        videoDownloadModel = new VideoDownloadModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e1943387702299774155981/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.title = "不只是音视频 2";
        model.url = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e1943387702299774155981/adp.10.m3u8";
        model.appId = 1500005830;
        videoId = new SuperPlayerVideoId();
        videoId.fileId = "387702299774155981";
        model.videoId = videoId;
        videoDownloadModel.setQualityId(TXVodDownloadDataSource.QUALITY_HD);
        videoDownloadModel.setPlayerModel(model);
        videoDownloadModel.setUserName(DEFAULT_NAME);
        VideoDownloadCenter.getInstance().startDownload(videoDownloadModel);

        model = new SuperPlayerModel();
        videoDownloadModel = new VideoDownloadModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09b10980387702299773830943/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.title = "不只是音视频 3";
        model.url = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09b10980387702299773830943/adp.10.m3u8";
        model.appId = 1500005830;
        videoId = new SuperPlayerVideoId();
        videoId.fileId = "387702299773830943";
        model.videoId = videoId;
        videoDownloadModel.setQualityId(TXVodDownloadDataSource.QUALITY_HD);
        videoDownloadModel.setPlayerModel(model);
        videoDownloadModel.setUserName(DEFAULT_NAME);
        VideoDownloadCenter.getInstance().startDownload(videoDownloadModel);

        model = new SuperPlayerModel();
        videoDownloadModel = new VideoDownloadModel();
        model.placeholderImage = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09a09220387702299773823860/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.title = "不只是音视频 4";
        model.url = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/09a09220387702299773823860/adp.10.m3u8";
        model.appId = 1500005830;
        videoId = new SuperPlayerVideoId();
        videoId.fileId = "387702299773823860";
        model.videoId = videoId;
        videoDownloadModel.setQualityId(TXVodDownloadDataSource.QUALITY_HD);
        videoDownloadModel.setPlayerModel(model);
        videoDownloadModel.setUserName(DEFAULT_NAME);
        VideoDownloadCenter.getInstance().startDownload(videoDownloadModel);

        model = new SuperPlayerModel();
        videoDownloadModel = new VideoDownloadModel();
        model.coverPictureUrl = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e97dc387702299774156604/coverBySnapshot/coverBySnapshot_10_0.jpg";
        model.title = "不只是音视频 5";
        model.url = "http://1500005830.vod2.myqcloud.com/43843ec0vodtranscq1500005830/467e97dc387702299774156604/adp.10.m3u8";
        model.appId = 1500005830;
        videoId = new SuperPlayerVideoId();
        videoId.fileId = "387702299774156604";
        model.videoId = videoId;
        videoDownloadModel.setQualityId(TXVodDownloadDataSource.QUALITY_HD);
        videoDownloadModel.setPlayerModel(model);
        videoDownloadModel.setUserName(DEFAULT_NAME);
        VideoDownloadCenter.getInstance().startDownload(videoDownloadModel);
    }

    @Override
    public void onVideoPlay(@Nullable VideoModel videoModel, TXVodDownloadMediaInfo mediaInfo) {
        Intent intent = new Intent(this, PlayWithSurfaceViewActivity.class);
        intent.putExtra(PlayWithSurfaceViewActivity.PLAY_URL, mediaInfo.getPlayPath());
        startActivity(intent);
    }


    @Override
    public void onDownloadStart(TXVodDownloadMediaInfo txVodDownloadMediaInfo) {
        mDataList.add(txVodDownloadMediaInfo);
        if (mDataList.size() == 5) {
            mVideoDownloadListView.addCacheVideo(mDataList, true);
        }
    }
}
