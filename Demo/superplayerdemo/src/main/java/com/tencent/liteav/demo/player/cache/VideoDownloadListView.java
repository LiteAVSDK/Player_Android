package com.tencent.liteav.demo.player.cache;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.cache.adapter.VideoDownloadListAdapter;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.view.DialogUtils;
import com.tencent.liteav.demo.superplayer.model.download.VideoDownloadCenter;
import com.tencent.rtmp.downloader.TXVodDownloadDataSource;
import com.tencent.rtmp.downloader.TXVodDownloadMediaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * list component for Video that from downloaded
 */
public class VideoDownloadListView extends RelativeLayout
        implements VideoDownloadListAdapter.OnItemClickListener,
        VideoDownloadListAdapter.OnItemLongClickListener {

    private VideoDownloadListAdapter mVideoDownloadListAdapter;
    private RecyclerView             mRvCacheVideoListView;
    private LinearLayout             mCacheEmptyContainer;
    private Dialog                   mDeleteVideoDialog;
    private Dialog                   mTipDialog;

    private List<TXVodDownloadMediaInfo> mMediaInfoList = new ArrayList<>();

    private OnVideoPlayListener mVideoPlayListener;

    public VideoDownloadListView(Context context) {
        this(context, null);
    }

    public VideoDownloadListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDownloadListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.cache_video_listview_layout, this);

        mRvCacheVideoListView = findViewById(R.id.cache_rv_video_list);
        mCacheEmptyContainer = findViewById(R.id.cache_ll_empty_container);

        mRvCacheVideoListView.setAdapter(mVideoDownloadListAdapter = new VideoDownloadListAdapter(mMediaInfoList));

        mVideoDownloadListAdapter.setOnItemClickListener(this);
        mVideoDownloadListAdapter.setOnItemLongClickListener(this);

        // 去掉不好看的动画
        mRvCacheVideoListView.setItemAnimator(null);
        checkIfShowEmptyView();
    }


    public void addCacheVideo(List<TXVodDownloadMediaInfo> mediaInfoList, boolean isNeedClean) {
        if (isNeedClean) {
            mMediaInfoList.clear();
        }
        int oldLength = mMediaInfoList.size();
        mMediaInfoList.addAll(mediaInfoList);

        mVideoDownloadListAdapter.notifyItemRangeInserted(oldLength, mediaInfoList.size());
        checkIfShowEmptyView();
    }

    private void checkIfShowEmptyView() {
        if (mMediaInfoList.isEmpty()) {
            mCacheEmptyContainer.setVisibility(VISIBLE);
            mRvCacheVideoListView.setVisibility(GONE);
        } else {
            mCacheEmptyContainer.setVisibility(GONE);
            mRvCacheVideoListView.setVisibility(VISIBLE);
        }
    }

    public void setOnVideoPlayListener(OnVideoPlayListener videoPlayListener) {
        this.mVideoPlayListener = videoPlayListener;
    }

    private void refreshMediaInfo(final TXVodDownloadMediaInfo orgMediaInfo, final int position) {
        VideoDownloadCenter.OnMediaInfoFetchListener listener
                = new VideoDownloadCenter.OnMediaInfoFetchListener() {
            @Override
            public void onReady(TXVodDownloadMediaInfo mediaInfo) {
                if (mediaInfo != orgMediaInfo) {
                    int index = mMediaInfoList.indexOf(orgMediaInfo);
                    mMediaInfoList.remove(orgMediaInfo);
                    mMediaInfoList.add(index, mediaInfo);
                    mVideoDownloadListAdapter.notifyItemChanged(position);
                }
            }
        };

        if (orgMediaInfo.getDataSource() != null) {
            TXVodDownloadDataSource dataSource = orgMediaInfo.getDataSource();
            VideoDownloadCenter.getInstance()
                    .getDownloadMediaInfo(dataSource.getAppId(), dataSource.getFileId(),
                            dataSource.getQuality(), listener);
        } else {
            VideoDownloadCenter.getInstance()
                    .getDownloadMediaInfo(orgMediaInfo.getUrl(), listener);
        }
    }

    @Override
    public void onClick(VideoModel videoModel, TXVodDownloadMediaInfo mediaInfo, int position) {
        int state = mediaInfo.getDownloadState();
        if (state == TXVodDownloadMediaInfo.STATE_FINISH) {
            if (null != mVideoPlayListener) {
                mVideoPlayListener.onVideoPlay(videoModel, mediaInfo);
            }
        } else if (state == TXVodDownloadMediaInfo.STATE_STOP) {
            VideoDownloadCenter.getInstance().resumeDownload(mediaInfo);
            // resume之后，再次刷新状态
            refreshMediaInfo(mediaInfo, position);
        } else {
            VideoDownloadCenter.getInstance().stopDownload(mediaInfo);
            // stop之后，再次刷新状态
            refreshMediaInfo(mediaInfo, position);
        }
    }

    @Override
    public boolean onLongClick(final TXVodDownloadMediaInfo mediaInfo, final int position) {
        checkDeleteDialogIsNeedDismiss();
        mDeleteVideoDialog = DialogUtils.getInstance().showCommonDialog(getContext()
                , getResources().getString(R.string.superplayer_hint)
                , getResources().getString(R.string.superplayer_delete_video_confirm)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isSuccess = VideoDownloadCenter.getInstance().deleteDownloadMediaInfo(mediaInfo);
                        if (isSuccess) {
                            showTipDialog(true, getResources().getString(R.string.superplayer_delete_success));
                            mMediaInfoList.remove(mediaInfo);
                            mVideoDownloadListAdapter.notifyItemRemoved(position);
                            checkIfShowEmptyView();
                        } else {
                            showTipDialog(false, getResources().getString(R.string.superplayer_delete_failed));
                        }
                        dialog.dismiss();
                    }
                });
        return true;
    }

    private void checkDeleteDialogIsNeedDismiss() {
        if (null != mDeleteVideoDialog && mDeleteVideoDialog.isShowing()) {
            mDeleteVideoDialog.dismiss();
        }
    }

    private void checkTipDialogIsNeedDismiss() {
        if (null != mTipDialog && mTipDialog.isShowing()) {
            mTipDialog.dismiss();
        }
    }

    private void showTipDialog(boolean isSuccess, String content) {
        checkTipDialogIsNeedDismiss();
        mTipDialog = DialogUtils.getInstance().showTip(getContext(), isSuccess, content);
    }

    @Override
    protected void onDetachedFromWindow() {
        checkDeleteDialogIsNeedDismiss();
        checkTipDialogIsNeedDismiss();
        super.onDetachedFromWindow();
    }

    public interface OnVideoPlayListener {
        void onVideoPlay(@Nullable VideoModel videoModel, TXVodDownloadMediaInfo mediaInfo);
    }

}
