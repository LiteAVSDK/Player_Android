package com.tencent.liteav.demo.player.demo.feed.feedlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.liteav.demo.feedvideo.R;
import com.tencent.liteav.demo.player.demo.feed.player.FeedPlayerView;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;


/**
 * feed流列表页item
 * 包含播放器FeedPlayerView以及展示视频描述信息的View
 */
public class FeedListItemView extends RelativeLayout {

    private FeedPlayerView feedPlayerView;
    private ImageView                headImg;
    private TextView                 titleTxt;
    private TextView                 describeTxt;
    private FeedListItemViewCallBack feedListItemViewCallBack = null;
    private VideoModel               videoModel               = null;   //数据
    private LayoutParams             playerLayoutParams       = null;
    private boolean                  mIsPaused                   = false;


    public FeedListItemView(Context context) {
        super(context);
        initViews();
    }

    public FeedListItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public FeedListItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * 初始化界面
     */
    private void initViews() {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.feedview_list_item_layout, this, false);
        headImg = relativeLayout.findViewById(R.id.feed_list_holder_head_img);
        titleTxt = relativeLayout.findViewById(R.id.feed_list_holder_title_txt);
        describeTxt = relativeLayout.findViewById(R.id.feed_list_holder_describe_txt);
        RelativeLayout describeLayout = relativeLayout.findViewById(R.id.feed_list_item_describe_layout);
        feedPlayerView = relativeLayout.findViewById(R.id.feed_list_item_player);
        playerLayoutParams = (LayoutParams) feedPlayerView.getLayoutParams();
        relativeLayout.removeAllViews();
        addView(describeLayout);
        addView(feedPlayerView, playerLayoutParams);
    }


    /**
     * 给item设置数据,并预加载视频信息
     *
     * @param videoModel
     * @param callBack
     * @param position
     */
    public void bindItemData(VideoModel videoModel, FeedListItemViewCallBack callBack, final int position) {
        this.videoModel = videoModel;
        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
        Glide.with(headImg.getContext()).load(videoModel.placeholderImage).apply(options).into(headImg);
        titleTxt.setText(videoModel.title);
        describeTxt.setText(videoModel.videoDescription);
        feedListItemViewCallBack = callBack;
        feedPlayerView.setFeedPlayerCallBack(new FeedPlayerView.FeedPlayerCallBack() {
            @Override
            public void onStartFullScreenPlay() {
                if (feedListItemViewCallBack != null) {
                    feedListItemViewCallBack.onStartFullScreenPlay(FeedListItemView.this, position);
                }
            }

            @Override
            public void onStopFullScreenPlay() {
                if (feedListItemViewCallBack != null) {
                    feedListItemViewCallBack.onStopFullScreenPlay(FeedListItemView.this);
                }
            }

            @Override
            public void onClickSmallReturnBtn() {

            }

        });
        feedPlayerView.preparePlayVideo(position, videoModel);
    }

    /**
     * 直接播放个视频
     *
     * @param videoModel
     */
    public void play(VideoModel videoModel) {
        if (feedPlayerView != null) {
            feedPlayerView.play(videoModel);
        }
    }

    public void resume() {
        if (mIsPaused) {
            return;
        }
        if (feedPlayerView != null) {
            feedPlayerView.resume();
        }
    }

    public void pause() {
        if (feedPlayerView != null) {
            feedPlayerView.pause();
        }
    }

    public void stop() {
        if (feedPlayerView != null) {
            feedPlayerView.stop();
        }
    }

    /**
     * 还原ItemView
     */
    public void reset() {
        if (feedPlayerView != null) {
            feedPlayerView.reset();
        }
    }

    /**
     * 销毁item时调用
     */
    public void destroy() {
        if (feedPlayerView != null) {
            feedPlayerView.destroy();
        }
    }

    public VideoModel getVideoModel() {
        return videoModel;
    }

    /**
     * 从item中移除播放器
     */
    public void removeFeedPlayFromItem() {
        if (feedPlayerView.getParent() != null) {
            ((ViewGroup)feedPlayerView.getParent()).removeView(feedPlayerView);
        }
    }

    /**
     * 将播放器添加进item
     */
    public void addFeedPlayToItem() {
        ((ViewGroup) feedPlayerView.getParent()).removeView(feedPlayerView);
        feedPlayerView.setTranslationY(0);
        addView(feedPlayerView, playerLayoutParams);
    }

    private final RecyclerView.OnScrollListener onScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && null != feedPlayerView) {
                        feedPlayerView.preLoad();
                    }
                }
            };

    public void registerScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(onScrollListener);
    }

    public void unRegisterScrollListener(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(onScrollListener);
    }


    /**
     * 获取播放器控件
     *
     * @return
     */
    public FeedPlayerView getFeedPlayerView() {
        return feedPlayerView;
    }


    /**
     * 获取播放器底部Y的坐标，相对于父控件的
     *
     * @return
     */
    public int getPlayerDisY() {
        return getTop() + feedPlayerView.getBottom();
    }


    /**
     * 获取item中视频控件之上的view的高度
     * 因为此item的视频控件在item的最顶部，所以返回0
     *
     * @return
     */
    public int getItemTopLayoutHeight() {
        return (int) dp2px(getContext(), 10);
    }


    private float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public long getProgress() {
        return feedPlayerView.getProgress();
    }

    public interface FeedListItemViewCallBack {

        void onItemClick(FeedListItemView itemView, VideoModel videoModel, int position);

        void onStartFullScreenPlay(FeedListItemView itemView, int position);

        void onStopFullScreenPlay(FeedListItemView itemView);
    }

    public void setIsPaused(boolean isPaused) {
        mIsPaused = isPaused;
    }

}
