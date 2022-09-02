package com.tencent.liteav.demo.player.feed.model;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.player.expand.model.entity.VideoModel;
import com.tencent.liteav.demo.player.expand.model.utils.SuperVodListLoader;
import com.tencent.liteav.demo.superplayer.SuperPlayerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FeedVodListLoader {

    private final Handler            mHandler = new Handler(Looper.getMainLooper());
    private final SuperVodListLoader mDataLoader;
    private Context                  mContext;

    public FeedVodListLoader(Context context) {
        mContext = context;
        mDataLoader = new SuperVodListLoader(mContext);
    }

    private List<VideoModel> loadDefaultVodList() {
        List<VideoModel> list = new ArrayList<>();
        VideoModel model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774251236";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_achievements_introduction);;
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_achievements_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774544650";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_steady_introduction);
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_steady_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774644824";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_real_introduction);
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_real_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774211080";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_complete_introduction);
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_complete_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774545556";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_business_introduction_introduction);
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_business_introduction_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774574470";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.what_are_numbers_introduction);
        model.videoMoreDescription = mContext.getString(R.string.what_are_numbers_detail);
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774253670";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.simplify_complexity_and_build_big_from_small_introduction);
        model.videoMoreDescription = mContext.getString(R.string.simplify_complexity_and_build_big_from_small_detail);;
        list.add(model);

        model = new VideoModel();
        model.appid = 1500005830;
        model.fileid = "387702299774390972";
        model.playAction = PLAY_ACTION_PRELOAD;
        model.videoDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_introduction);
        model.videoMoreDescription = mContext.getString(R.string.tencent_cloud_audio_and_video_detail);
        list.add(model);

        return list;
    }

    /**
     * 获取数据
     *
     * @param loadDataCallBack
     */
    public void loadListData(int page, final LoadDataCallBack loadDataCallBack) {
        int random = getRandomNumber(1, 5);
        List<VideoModel> videoModelList = page == 0 ? loadDefaultVodList() : loadDefaultVodList().subList(0, random);
        final int size = videoModelList.size();
        mDataLoader.getVodInfoOneByOne(videoModelList, new SuperVodListLoader.OnVodInfoLoadListener() {
            int count = 0;
            List<VideoModel> resultList = new ArrayList<>();

            @Override
            public void onSuccess(VideoModel videoModel) {
                resultCallBack(videoModel);
            }

            @Override
            public void onFail(int errCode) {
                resultCallBack(null);
            }

            private void resultCallBack(final VideoModel videoModel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (videoModel != null) {
                            resultList.add(videoModel);
                        }
                        count++;
                        if (count != size) {
                            return;
                        }
                        if (resultList.size() > 0) {
                            loadDataCallBack.onLoadedData(resultList);
                        } else {
                            loadDataCallBack.onError(-1);
                        }
                    }
                });
            }
        });
    }

    public interface LoadDataCallBack {
        void onLoadedData(List<VideoModel> videoModels);

        void onError(int errorCode);
    }

    /**
     * 将VideoModel 转换为SuperPlayerModel
     *
     * @param videoModel
     * @return
     */
    public static SuperPlayerModel conversionModel(VideoModel videoModel) {
        return videoModel.convertToSuperPlayerModel();
    }

    /**
     * 获取范围内的数据
     *
     * @param min
     * @param max
     * @return
     */
    private static Integer getRandomNumber(int min, int max) {
        Random random = new Random();
        int result = random.nextInt(max) % (max - min + 1) + min;
        return result;
    }
}
