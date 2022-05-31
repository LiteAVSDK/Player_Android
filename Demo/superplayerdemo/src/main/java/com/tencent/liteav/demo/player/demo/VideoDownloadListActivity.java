package com.tencent.liteav.demo.player.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.tencent.liteav.demo.common.utils.IntentUtils;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.cache.VideoDownloadListView;
import com.tencent.liteav.demo.player.expand.model.SuperPlayerConstants;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadCenter;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.List;

public class VideoDownloadListActivity extends Activity
        implements VideoDownloadListView.OnVideoPlayListener
        , View.OnClickListener {

    private static final int REQUEST_CODE_QR_SCAN = 100;

    private VideoDownloadListView mVclCacheListView;
    private ImageView             mIvBack;
    private ImageButton           mImageLink;
    private ImageView             mBtnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_cache_list);

        mVclCacheListView = findViewById(R.id.cache_vclv_video_list);
        mIvBack = findViewById(R.id.superplayer_iv_back);
        mImageLink = findViewById(R.id.superplayer_ib_webrtc_link_button);
        mBtnScan = findViewById(R.id.superplayer_btn_scan);

        mVclCacheListView.setOnVideoPlayListener(this);
        mIvBack.setOnClickListener(this);
        mImageLink.setOnClickListener(this);
        mBtnScan.setOnClickListener(this);

        updateCacheVideo();
    }

    private void updateCacheVideo() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final List<TXVodDownloadMediaInfo> mediaInfoList = VideoDownloadCenter.getInstance().getDownloadList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVclCacheListView.addCacheVideo(mediaInfoList, false);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onVideoPlay(VideoModel videoModel, TXVodDownloadMediaInfo mediaInfo) {
        Intent intent = new Intent(this, SuperPlayerActivity.class);

        intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FROM,
                SuperPlayerConstants.SuperPlayerIntent.FROM_CACHE);

        if (null != mediaInfo.getDataSource()) {
            TXVodDownloadDataSource dataSource = mediaInfo.getDataSource();
            intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_APP_ID, dataSource.getAppId());
            intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FILE_ID, dataSource.getFileId());
            intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_P_SIGN, dataSource.getPSign());
        }
        intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_URL, mediaInfo.getPlayPath());

        if (null != videoModel) {
            intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_TITLE, videoModel.title);
            intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_COVER_IMG, videoModel.placeholderImage);
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.superplayer_iv_back) {
            finish();
        } else if (id == R.id.superplayer_ib_webrtc_link_button) {
            showCloudLink();
        } else if (id == R.id.superplayer_btn_scan) {
            scanQRCode();
        }
    }

    private void showCloudLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/18872"));
        IntentUtils.safeStartActivity(this, intent);
    }

    /**
     * 扫描二维码
     */
    private void scanQRCode() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || TextUtils.isEmpty(data.getStringExtra("result"))) {
            return;
        }
        String result = data.getStringExtra("result");
        if (REQUEST_CODE_QR_SCAN == requestCode) {
            // 二维码播放视频
            Intent intent = new Intent(this, SuperPlayerActivity.class);
            //noinspection ConstantConditions
            if (result.contains("protocol=v4vodplay")) { // 优先解析包含v4协议字段的特殊食品
                Uri uri = Uri.parse(result);
                String appId = uri.getQueryParameter("appId");
                String fileId = uri.getQueryParameter("fileId");
                String psign = uri.getQueryParameter("psign");

                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FROM
                        , SuperPlayerConstants.SuperPlayerIntent.FROM_SUPERPLAYER);
                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_APP_ID, appId);
                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FILE_ID, fileId);
                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_P_SIGN, psign);
            } else {
                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_FROM
                        , SuperPlayerConstants.SuperPlayerIntent.FROM_URL);
                intent.putExtra(SuperPlayerConstants.SuperPlayerIntent.KEY_URL, result);
            }
            startActivity(intent);
            finish();
        }
    }
}