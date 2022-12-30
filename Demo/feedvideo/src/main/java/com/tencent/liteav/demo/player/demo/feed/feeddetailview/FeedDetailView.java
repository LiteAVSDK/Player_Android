package com.tencent.liteav.demo.player.demo.feed.feeddetailview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import com.tencent.liteav.demo.feedvideo.R;
import com.tencent.liteav.demo.player.demo.feed.player.FeedPlayerView;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;

import java.util.List;

/**
 * feed流详情页面
 */
public class FeedDetailView extends FrameLayout implements FeedDetailListClickListener {

    private RecyclerView                      recyclerView           = null;
    private FeedDetailAdapter feedDetailAdapter      = null;
    private ImageView                         headImg                = null;
    private TextView                          titleTxt               = null;
    private TextView                          descriptionTxt         = null;
    private TextView                          detailDescriptionTxt   = null;
    private boolean                           isChangeVideo          = false;   //用于在详情页面是否播放了底部列表的视频，TRUE表示播放了
    private FeedPlayerView                    feedPlayerView         = null;
    private int                               playerViewHeight       = 0;   //PlayerView 的在窗口模式时的高度
    private FeedPlayerView.FeedPlayerCallBack feedPlayerCallBack     = null;   //用于存放之前给FeedPlayerView设置的callBack对象
    private RelativeLayout                    detailLayout           = null;  //详情页面用于展示视频介绍和视频列表的布局
    private boolean                           isDestroy              = false;
    private RelativeLayout                    titleLayout            = null;
    private Button                            backButton             = null;

    public FeedDetailView(Context context) {
        super(context);
        initViews();
    }

    public FeedDetailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FeedDetailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        setBackgroundResource(R.color.feed_page_bg);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        LayoutInflater.from(getContext()).inflate(R.layout.feedview_detailview_layout, this, true);
        feedPlayerView = findViewById(R.id.feed_player_view_detail);
        detailLayout = findViewById(R.id.feed_detail_layout);
        headImg = findViewById(R.id.feed_detail_layout_head_img);
        titleLayout = findViewById(R.id.title_layout);
        titleTxt = findViewById(R.id.feed_detail_describe_layout_title_txt);
        descriptionTxt = findViewById(R.id.feed_detail_layout_describe_txt);
        detailDescriptionTxt = findViewById(R.id.feed_detail_layout_detail_describe_txt);
        recyclerView = findViewById(R.id.feed_detail_layout_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        backButton = findViewById(R.id.feed_ac_back_btn);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });
        feedDetailAdapter = new FeedDetailAdapter(this);
        recyclerView.setAdapter(feedDetailAdapter);
    }

    /**
     * 设置视频的描述信息
     *
     * @param videoModel
     */
    private void setVideoDescription(VideoModel videoModel) {
        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
        if (!isDestroy) {
            Glide.with(headImg.getContext()).load(videoModel.placeholderImage).apply(options).into(headImg);
        }
        titleTxt.setText(videoModel.title);
        descriptionTxt.setText(videoModel.videoDescription);
        detailDescriptionTxt.setText(videoModel.videoMoreDescription);
    }

    /**
     * 设置底部列表的数据
     *
     * @param videoModels
     */
    public void addDetailListData(List<VideoModel> videoModels) {
        feedDetailAdapter.setFeedEntityList(videoModels);
    }


    /**
     * 添加详情页面到feedview中
     *
     */
    public void showDetailView(final VideoModel videoModel) {
        feedPlayerView.post(new Runnable() {
            @Override
            public void run() {
                int phoneWidth = feedPlayerView.getContext().getResources().getDisplayMetrics().widthPixels;
                playerViewHeight = feedPlayerView.getHeight() * phoneWidth / feedPlayerView.getWidth();
            }
        });
        //设置新的callback回调
        feedPlayerView.setFeedPlayerCallBack(new FeedPlayerView.FeedPlayerCallBack() {
            @Override
            public void onStartFullScreenPlay() {
                feedPlayerView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                titleLayout.setVisibility(GONE);
                setBackgroundResource(R.color.black);
            }

            @Override
            public void onStopFullScreenPlay() {
                feedPlayerView.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                                .MATCH_PARENT, playerViewHeight));
                titleLayout.setVisibility(VISIBLE);
                setBackgroundResource(R.color.feed_page_bg);
            }

            @Override
            public void onClickSmallReturnBtn() {

            }
        });
        setVideoDescription(videoModel);
    }



    /**
     * 当点击详情页面底部视频列表时触发此事件，此时可在此处进行视频播放
     *
     * @param entity
     * @param position
     */
    @Override
    public void onItemClickListener(VideoModel entity, int position) {
        isChangeVideo = true;
        if (feedPlayerView != null) {
            feedPlayerView.setStartTime(0);
            feedPlayerView.play(entity);
        }
        setVideoDescription(entity);
    }

    /**
     * 对页面数据进行清除
     */
    public void destroy() {
        isDestroy = true;
        //清理描述信息
        headImg.setImageResource(0);
        titleTxt.setText("");
        descriptionTxt.setText("");
        detailDescriptionTxt.setText("");
        //清理掉列表数据
        if (recyclerView != null && recyclerView.getChildCount() > 0) {
            recyclerView.removeAllViews();
            feedDetailAdapter.setFeedEntityList(null);
        }
        feedPlayerView.destroy();
    }

    /**
     * 当此页面从父控件移除的时候调用，
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (feedPlayerView != null) {
            feedPlayerView.onRequestPermissionsResult(requestCode,grantResults);
        }
    }

    public void setStartTime(int progress) {
        feedPlayerView.setStartTime(progress);
    }

    public void play(VideoModel videoModel) {
        feedPlayerView.play(videoModel);
    }

    public void pause() {
        feedPlayerView.pause();
    }

    public void resume() {
        feedPlayerView.resume();
    }
}
