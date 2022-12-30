package com.tencent.liteav.demo.player.demo.feed.feedlistview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.liteav.demo.player.demo.feed.FeedPlayerManager;


public class FeedListScrollListener extends RecyclerView.OnScrollListener {

    private FeedPlayerManager feedPlayerManager = null;

    public FeedListScrollListener(FeedPlayerManager feedPlayerManager) {
        this.feedPlayerManager = feedPlayerManager;
    }

    /**
     * 当刷新或者第一次添加数据的时候调用此方法
     *
     * @param recyclerView
     * @param newState
     */
    public void firstPlayItem(@NonNull final RecyclerView recyclerView, int newState) {
        if (feedPlayerManager != null) {
            feedPlayerManager.reset();
        }
        onScrollStateChanged(recyclerView, newState);
    }


    @Override
    public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) { //滑动停止
            onScrollIdle(recyclerView);
        }
    }


    private void onScrollIdle(RecyclerView recyclerView) {
        if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
            findFocusItem(linearLayoutManager, firstPosition, lastPosition, recyclerView.getHeight());
        }
    }

    /**
     * 查找第一个videoView完全展示的view
     *
     * @param firstPosition
     * @param lastPosition
     */
    private void findFocusItem(LinearLayoutManager linearLayoutManager, int firstPosition, int lastPosition, int recycleViewHeight) {
        int focusStartItemPosition = -1;   //第一个完全可见播放器的item的游标
        if (firstPosition == lastPosition) {  //表示页面中只有一个item可见
            focusStartItemPosition = firstPosition;
        } else if (lastPosition - firstPosition == 1) { //表示页面中有两个item 可见
            View firstView = linearLayoutManager.findViewByPosition(firstPosition);
            View lastView = linearLayoutManager.findViewByPosition(lastPosition);
            if (firstView != null && firstView.getTop() >= -((FeedListItemView) firstView).getItemTopLayoutHeight()) {
                focusStartItemPosition = firstPosition;
            } else if (lastView != null && ((FeedListItemView) lastView).getPlayerDisY() <= recycleViewHeight) {
                focusStartItemPosition = lastPosition;
            }
        } else {        //表示页面中至少有三个item可见
            View firstView = linearLayoutManager.findViewByPosition(firstPosition);
            if (firstView == null) {
                return;
            }
            if (firstView.getTop() >= -((FeedListItemView) firstView).getItemTopLayoutHeight()) {  //表示第一个item的播放器完全可见
                focusStartItemPosition = firstPosition;
            } else {
                focusStartItemPosition = firstPosition + 1;
            }
        }
        if (checkFocusItem(linearLayoutManager, focusStartItemPosition, lastPosition, recycleViewHeight)) {
            return;
        }
        onItemFocus(linearLayoutManager, focusStartItemPosition);
    }


    /**
     * 此方法返回TRUE 表示在可见的范围内找到了，上次正在播放的item，并且上次播放的这个item的播放器也是完全可见的，所以这个时候就不必播放新的item
     *
     * @param linearLayoutManager
     * @param focusStartItemPosition
     * @param lastPosition
     * @param recycleViewHeight
     * @return
     */
    private boolean checkFocusItem(LinearLayoutManager linearLayoutManager, int focusStartItemPosition, int lastPosition, int recycleViewHeight) {
        //focusStartItemPosition为-1表示没有找到符合条件的item，这个时候直接停止上次的item
        if (focusStartItemPosition == -1) {
            feedPlayerManager.onPause();
            return true;
        }
        for (int index = focusStartItemPosition; index <= lastPosition; index++) {
            //修改后的
            //如果正在播放的item的游标和当前的相等，且这个item不是最后一个游标，如果是最后一个可见的item，则需判断最后一个item的播放器是否也完全可见
            if (index != feedPlayerManager.getLastPosition()) {
                continue;
            }
            if (!feedPlayerManager.isPlaying()) {
                continue;
            }
            FeedListItemView itemView = (FeedListItemView) linearLayoutManager.findViewByPosition(index);
            if ((index != lastPosition || itemView.getPlayerDisY() <= recycleViewHeight)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 当滑动结束的时候调用次方法，如果返回-1表示屏幕中没有一个符合条件的的item
     *
     * @param position 符合条件的itemView的位置
     */
    private void onItemFocus(LinearLayoutManager linearLayoutManager, int position) {
        //找到的item与上次播放的item是一个，并且上次的正在播放
        if (feedPlayerManager.getLastPosition() == position && feedPlayerManager.isPlaying()) {
            return;
        }
        View view = linearLayoutManager.findViewByPosition(position);
        if (view instanceof FeedListItemView) {
            feedPlayerManager.setPlayingFeedPlayerView(((FeedListItemView) view).getFeedPlayerView(), position);
            ((FeedListItemView) view).resume();
        }
    }


}
