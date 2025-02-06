package com.tencent.liteav.demo.superplayer.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.util.Rational;

import androidx.annotation.DrawableRes;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.ArrayList;

public class PictureInPictureHelper implements ServiceConnection {

    private static final String PIP_ACTION_MEDIA_CONTROL = "media_control";
    private static final String PIP_EXTRA_CONTROL_TYPE = "control_type";
    public static final int PIP_CONTROL_TYPE_PLAY = 1;
    public static final int PIP_CONTROL_TYPE_PAUSE = 2;
    private static final int PIP_CONTROL_TYPE_LAST = 3;
    private static final int PIP_CONTROL_TYPE_NEXT = 4;
    public static final int PIP_REQUEST_TYPE_PLAY = 1;
    public static final int PIP_REQUEST_TYPE_PAUSE = 2;
    private static final int PIP_REQUEST_TYPE_LAST = 3;
    private static final int PIP_REQUEST_TYPE_NEXT = 4;
    private static final int PIP_TIME_SHIFT_INTERVAL = 15;
    private BroadcastReceiver mReceiver;
    private PictureInPictureParams.Builder mPictureInPictureParamsBuilder;
    private Context mContext;
    private OnPictureInPictureClickListener mListener;
    private boolean mIsBindService = false;
    private boolean isInPipMode;


    @SuppressLint({"WrongConstant", "UnspecifiedRegisterReceiverFlag"})
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.registerReceiver(mReceiver, new IntentFilter(PIP_ACTION_MEDIA_CONTROL), 0x04);
        } else {
            mContext.registerReceiver(mReceiver, new IntentFilter(PIP_ACTION_MEDIA_CONTROL));
        }
    }

    public void setListener(OnPictureInPictureClickListener listener) {
        mListener = listener;
    }


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

    private void bindAndroid12BugServiceIfNeed() {
        if (Build.VERSION.SDK_INT >= 31 && !mIsBindService) {
            mIsBindService = true;
            Intent serviceIntent = new Intent(mContext, Android12BridgeService.class);
            mContext.startService(serviceIntent);
            mContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     *
     * Update the three buttons in the picture-in-picture
     *
     * @param iconId
     * @param title
     * @param controlType
     * @param requestCode
     */
    public void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        if (mPictureInPictureParamsBuilder == null) {
            return;
        }
        final Activity activity = (Activity)mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && null != activity
                && !activity.isDestroyed() && !activity.isFinishing()) {

            final ArrayList<RemoteAction> actions = new ArrayList<>();
            int defaultFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0;
            final PendingIntent intentNext = PendingIntent.getBroadcast(mContext, PIP_REQUEST_TYPE_LAST,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, PIP_CONTROL_TYPE_NEXT), defaultFlag);
            actions.add(new RemoteAction(Icon.createWithResource(activity, R.drawable.superplayer_seek_left),
                    "", "", intentNext));

            final PendingIntent intentPause = PendingIntent.getBroadcast(mContext, requestCode,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, controlType),  defaultFlag);
            actions.add(new RemoteAction(Icon.createWithResource(activity, iconId), title, title, intentPause));

            final PendingIntent intentLast = PendingIntent.getBroadcast(activity, PIP_REQUEST_TYPE_NEXT,
                    new Intent(PIP_ACTION_MEDIA_CONTROL).putExtra(PIP_EXTRA_CONTROL_TYPE, PIP_CONTROL_TYPE_LAST), defaultFlag);
            actions.add(new RemoteAction(Icon.createWithResource(activity, R.drawable.superplayer_seek_right),
                    "", "", intentLast));

            mPictureInPictureParamsBuilder.setActions(actions);

            activity.setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
            bindAndroid12BugServiceIfNeed();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // do nothing
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // do nothing
    }


    /**
     * The callback logic when clicking on the corresponding button.
     */
    public interface OnPictureInPictureClickListener {

        void onClickPIPPlay();

        void onClickPIPPause();

        void onClickPIPPlayBackward();

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

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        isInPipMode = isInPictureInPictureMode;
    }

    public boolean isInPipMode() {
        return isInPipMode;
    }

    public void release() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (Build.VERSION.SDK_INT >= 31 && mIsBindService) {
            mIsBindService = false;
            mContext.unbindService(this);
        }
    }
}
