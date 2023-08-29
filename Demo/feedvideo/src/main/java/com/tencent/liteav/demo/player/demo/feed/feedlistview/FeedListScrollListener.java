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
     * Called when refreshing or adding data for the first time
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
     * Find the view where the first videoView is fully displayed
     * 查找第一个videoView完全展示的view
     *
     * @param firstPosition
     * @param lastPosition
     */
    private void findFocusItem(LinearLayoutManager linearLayoutManager, int firstPosition, int lastPosition,
                               int recycleViewHeight) {
        int focusStartItemPosition = -1;   // The cursor of the first fully visible player item
        if (firstPosition == lastPosition) {  // Indicates that only one item is visible on the page.
            focusStartItemPosition = firstPosition;
        } else if (lastPosition - firstPosition == 1) { // Indicates that two items are visible on the page.
            View firstView = linearLayoutManager.findViewByPosition(firstPosition);
            View lastView = linearLayoutManager.findViewByPosition(lastPosition);
            if (firstView != null && firstView.getTop() >= -((FeedListItemView) firstView).getItemTopLayoutHeight()) {
                focusStartItemPosition = firstPosition;
            } else if (lastView != null && ((FeedListItemView) lastView).getPlayerDisY() <= recycleViewHeight) {
                focusStartItemPosition = lastPosition;
            }
        } else {        // Indicates that at least three items are visible on the page.
            View firstView = linearLayoutManager.findViewByPosition(firstPosition);
            if (firstView == null) {
                return;
            }
            if (firstView.getTop() >= -((FeedListItemView) firstView).getItemTopLayoutHeight()) {
                // Indicates that the player of the first item is fully visible.
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
     * This method returns TRUE, indicating that the item that was being played last time was found within the
     * visible range, and the player of the item being played last time was also fully visible. Therefore,
     * there is no need to play a new item at this time.
     * 此方法返回TRUE 表示在可见的范围内找到了，上次正在播放的item，并且上次播放的这个item的播放器也是完全可见的，所以这个时候就不必播放新的item
     *
     * @param linearLayoutManager
     * @param focusStartItemPosition
     * @param lastPosition
     * @param recycleViewHeight
     * @return
     */
    private boolean checkFocusItem(LinearLayoutManager linearLayoutManager, int focusStartItemPosition,
                                   int lastPosition, int recycleViewHeight) {
        // If focusStartItemPosition is -1, it means that no item that meets the conditions has been found,
        // and the item being played last time should be stopped directly.
        if (focusStartItemPosition == -1) {
            feedPlayerManager.onPause();
            return true;
        }
        for (int index = focusStartItemPosition; index <= lastPosition; index++) {
            // Modified
            // If the cursor of the item being played and the current cursor are equal,
            // and this item is not the last cursor, then if it is the last visible item,
            // it is necessary to determine whether the player of the last item is also fully visible.
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
     * Called when scrolling ends. If -1 is returned, it means that no item that meets the conditions
     * is found on the screen.
     * 当滑动结束的时候调用次方法，如果返回-1表示屏幕中没有一个符合条件的的item
     *
     * @param position The position of the itemView that meets the conditions.
     *                 符合条件的itemView的位置
     */
    private void onItemFocus(LinearLayoutManager linearLayoutManager, int position) {
        // If the found item is the same as the last item being played, and the last item is still playing,
        // return TRUE.
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
