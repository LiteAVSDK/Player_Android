package com.tencent.liteav.demo.superplayer.helper;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.util.Rational;

import androidx.annotation.DrawableRes;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.ArrayList;

public class PictureInPictureHelper {

    private static final String PIP_ACTION_MEDIA_CONTROL = "media_control";
    private static final String PIP_EXTRA_CONTROL_TYPE = "control_type";
    private static final int PIP_CONTROL_TYPE_PLAY = 1;
    private static final int PIP_CONTROL_TYPE_PAUSE = 2;
    private static final int PIP_CONTROL_TYPE_LAST = 3;
    private static final int PIP_CONTROL_TYPE_NEXT = 4;
    private static final int PIP_REQUEST_TYPE_PLAY = 1;
    private static final int PIP_REQUEST_TYPE_PAUSE = 2;
    private static final int PIP_REQUEST_TYPE_LAST = 3;
    private static final int PIP_REQUEST_TYPE_NEXT = 4;
    private static final int PIP_TIME_SHIFT_INTERVAL = 15;
    private BroadcastReceiver mReceiver;
    private PictureInPictureParams.Builder mPictureInPictureParamsBuilder;
    private Context mContext;
    private OnPictureInPictureClickListener mListener;


    public PictureInPictureHelper(Context context) {
        mContext = context;
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !PIP_ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                    return;
                }
                final int controlType = intent.getIntExtra(PIP_EXTRA_CONTROL_TYPE, 0);
                switch (controlType) {
                    case PIP_CONTROL_TYPE_PLAY:
                        updatePictureInPictureActions(R.drawable.superplayer_ic_vod_pause_normal, "",
                                PIP_CONTROL_TYPE_PAUSE, PIP_REQUEST_TYPE_PAUSE);
                        mListener.onClickPIPPlay();
                        break;
                    case PIP_CONTROL_TYPE_PAUSE:
                        updatePictureInPictureActions(R.drawable.superplayer_ic_vod_play_normal, "",
                                PIP_CONTROL_TYPE_PLAY, PIP_REQUEST_TYPE_PLAY);
                        mListener.onClickPIPPause();
                        break;
                    case PIP_CONTROL_TYPE_LAST:
                        updatePictureInPictureActions(R.drawable.superplayer_ic_vod_pause_normal, "",
                                PIP_CONTROL_TYPE_PAUSE, PIP_REQUEST_TYPE_PAUSE);
                        mListener.onClickPIPPlayBackward();
                        break;
                    case PIP_CONTROL_TYPE_NEXT:
                        updatePictureInPictureActions(R.drawable.superplayer_ic_vod_pause_normal, "",
                                PIP_CONTROL_TYPE_PAUSE, PIP_REQUEST_TYPE_PAUSE);
                        mListener.onClickPIPPlayForward();
                        break;
                    default:
                        break;
                }
            }
        };
        ((Activity) mContext).registerReceiver(mReceiver, new IntentFilter(PIP_ACTION_MEDIA_CONTROL));
    }

    public void setListener(OnPictureInPictureClickListener listener) {
        mListener = listener;
    }


    /**
     *
     * 进入画中画模式
     *
     * @param state
     * @param mTXCloudVideoView
     */
    public void enterPictureInPictureMode(SuperPlayerDef.PlayerState state, TXCloudVideoView mTXCloudVideoView) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O && hasPipPermission((Activity) mContext)) {
            if (mPictureInPictureParamsBuilder == null) {
                mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
            }
            int mVideoWith = mTXCloudVideoView.getWidth();
            int mVideoHeight = mTXCloudVideoView.getHeight();
            if (mVideoWith != 0 && mVideoHeight != 0) {
                Rational aspectRatio = new Rational(mVideoWith, mVideoHeight);
                mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
            }
            if (state == SuperPlayerDef.PlayerState.PLAYING) {
                updatePictureInPictureActions(R.drawable.superplayer_ic_vod_pause_normal, "",
                        PIP_CONTROL_TYPE_PAUSE, PIP_REQUEST_TYPE_PAUSE);
            } else {
                updatePictureInPictureActions(R.drawable.superplayer_ic_vod_play_normal, "",
                        PIP_CONTROL_TYPE_PLAY, PIP_REQUEST_TYPE_PLAY);
            }
            ((Activity) mContext).enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }


    /**
     *
     * 更新画中画中的三个按钮
     *
     * @param iconId
     * @param title
     * @param controlType
     * @param requestCode
     */
    public void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final ArrayList<RemoteAction> actions = new ArrayList<>();

            // 上一个
            final PendingIntent intentNext = PendingIntent.getBroadcast(mContext, PIP_REQUEST_TYPE_LAST,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, PIP_CONTROL_TYPE_NEXT), 0);
            actions.add(new RemoteAction(Icon.createWithResource(mContext, R.drawable.superplayer_seek_left),
                    "", "", intentNext));

            // 暂停/播放
            final PendingIntent intentPause = PendingIntent.getBroadcast(mContext, requestCode,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, controlType), 0);
            actions.add(new RemoteAction(Icon.createWithResource(mContext, iconId), title, title, intentPause));

            // 下一个
            final PendingIntent intentLast = PendingIntent.getBroadcast(mContext, PIP_REQUEST_TYPE_NEXT,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, PIP_CONTROL_TYPE_LAST), 0);
            actions.add(new RemoteAction(Icon.createWithResource(mContext, R.drawable.superplayer_seek_right),
                    "", "", intentLast));

            mPictureInPictureParamsBuilder.setActions(actions);

            ((Activity)mContext).setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
        }
    }


    /**
     * 点击对应按钮的回调逻辑
     */
    public interface OnPictureInPictureClickListener {

        /**
         * 点击画中画 播放
         */
        void onClickPIPPlay();

        /**
         * 点击画中画 暂停
         */
        void onClickPIPPause();

        /**
         * 点击画中画 像后播放
         */
        void onClickPIPPlayBackward();

        /**
         * 点击画中画 向前播放
         */
        void onClickPIPPlayForward();
    }

    public static boolean hasPipPermission(Activity activity) {
        AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int permissionResult = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(), activity.getPackageName());
            return permissionResult == AppOpsManager.MODE_ALLOWED && SuperPlayerGlobalConfig.getInstance().enablePIP;
        } else {
            return false;
        }
    }

    public int getTimeShiftInterval() {
        return PIP_TIME_SHIFT_INTERVAL;
    }

    /**
     * 释放
     */
    public void release() {
        ((Activity) mContext).unregisterReceiver(mReceiver);
        mReceiver = null;
    }
}
