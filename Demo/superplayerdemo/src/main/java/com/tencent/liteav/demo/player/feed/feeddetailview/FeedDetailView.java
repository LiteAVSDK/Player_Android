package com.tencent.liteav.demo.player.feed.feeddetailview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.feed.FeedView;
import com.tencent.liteav.demo.player.feed.anim.FeedAnim;
import com.tencent.liteav.demo.player.feed.player.FeedPlayerView;

import java.util.List;

/**
 * feed流详情页面
 */
public class FeedDetailView extends FrameLayout implements FeedDetailListClickListener {

    private RecyclerView                      recyclerView           = null;
    private FeedDetailAdapter                 feedDetailAdapter      = null;
    private ImageView                         headImg                = null;
    private TextView                          titleTxt               = null;
    private TextView                          descriptionTxt         = null;
    private TextView                          detailDescriptionTxt   = null;
    private boolean                           isChangeVideo          = false;   //用于在详情页面是否播放了底部列表的视频，TRUE表示播放了
    private FeedPlayerView                    feedPlayerView         = null;
    private FeedDetailViewCallBack            feedDetailViewCallBack = null;
    private int                               playerViewHeight       = 0;   //PlayerView 的在窗口模式时的高度
    private FeedPlayerView.FeedPlayerCallBack feedPlayerCallBack     = null;   //用于存放之前给FeedPlayerView设置的callBack对象
    private RelativeLayout                    detailLayout           = null;  //详情页面用于展示视频介绍和视频列表的布局

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
        detailLayout = findViewById(R.id.feed_detail_layout);
        headImg = findViewById(R.id.feed_detail_layout_head_img);
        titleTxt = findViewById(R.id.feed_detail_describe_layout_title_txt);
        descriptionTxt = findViewById(R.id.feed_detail_layout_describe_txt);
        detailDescriptionTxt = findViewById(R.id.feed_detail_layout_detail_describe_txt);
        recyclerView = findViewById(R.id.feed_detail_layout_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedDetailAdapter = new FeedDetailAdapter(this);
        recyclerView.setAdapter(feedDetailAdapter);
    }

    public void setFeedDetailViewCallBack(FeedDetailViewCallBack feedDetailViewCallBack) {
        this.feedDetailViewCallBack = feedDetailViewCallBack;
    }

    /**
     * 设置视频的描述信息
     *
     * @param videoModel
     */
    private void setVideoDescription(VideoModel videoModel) {
        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
        Glide.with(headImg.getContext()).load(videoModel.placeholderImage).apply(options).into(headImg);
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
     * @param parentView
     */
    public void showDetailView(FeedView parentView, final VideoModel videoModel, FeedPlayerView playerView, int distanceY) {
        feedPlayerView = playerView;
        isChangeVideo = false;   //还原为默认值
        parentView.addView(this, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //保存之前的callback
        feedPlayerCallBack = playerView.getFeedPlayerCallBack();
        //根据在列表页面视频比例，计算出在详情页面播放器的高度
        int phoneWidth = playerView.getContext().getResources().getDisplayMetrics().widthPixels;
        playerViewHeight = playerView.getHeight() * phoneWidth / playerView.getWidth();
        LayoutParams layoutParams = (LayoutParams) detailLayout.getLayoutParams();
        layoutParams.topMargin = playerViewHeight;
        detailLayout.setLayoutParams(layoutParams);
        //设置新的callback回调
        feedPlayerView.setFeedPlayerCallBack(new FeedPlayerView.FeedPlayerCallBack() {
            @Override
            public void onStartFullScreenPlay() {
                if (feedDetailViewCallBack != null) {
                    feedDetailViewCallBack.onStartDetailFullScreenPlay(feedPlayerView.getPosition());
                }
                feedPlayerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onStopFullScreenPlay() {
                if (feedDetailViewCallBack != null) {
                    feedDetailViewCallBack.onStopDetailFullScreenPlay();
                }
                feedPlayerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerViewHeight));
            }

            @Override
            public void onClickSmallReturnBtn() {
                //小窗口的时候，左上角的返回事件
                if (feedDetailViewCallBack != null) {
                    feedDetailViewCallBack.onClickSmallReturnBtn();
                }
            }
        });
        //将播放器添加进view
        addView(playerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerViewHeight));
        //启动上移动画
        FeedAnim.moveAnim(playerView, distanceY, 0, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVideoDescription(videoModel);
                if (feedDetailViewCallBack != null) {
                    feedDetailViewCallBack.onLoadDetailData(videoModel);
                }
            }
        });
    }


    /**
     * 移除详情页面
     * 1. 启动视频下移动画，动画结束时回调onMoveDownwardEnd()方法，在onMoveDownwardEnd方法中将feedPlayerView移除
     * 并调用回调接口feedDetailViewCallBack.onRemoveDetailView方法，告知
     */
    public void removeFeedDetailView(int itemTop) {
        //先清理数据
        destroy();
        feedPlayerView.setFeedPlayerCallBack(feedPlayerCallBack);
        FeedAnim.moveAnim(feedPlayerView, 0, itemTop, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (feedDetailViewCallBack != null) {
                    feedDetailViewCallBack.onRemoveDetailView(feedPlayerView, isChangeVideo);
                }
                //将feedDetail页面从View树中移除
                ((ViewGroup) getParent()).removeView(FeedDetailView.this);
            }
        });
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
            feedPlayerView.play(entity);
        }
        setVideoDescription(entity);
    }

    /**
     * 对页面数据进行清除
     */
    public void destroy() {
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
    }

    /**
     * 当此页面从父控件移除的时候调用，
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }
}
