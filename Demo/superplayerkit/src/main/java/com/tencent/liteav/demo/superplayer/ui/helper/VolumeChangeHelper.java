package com.tencent.liteav.demo.superplayer.ui.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class VolumeChangeHelper {

    private Context mContext;

    private static AudioManager audioManager;

    private static final String VOLUME_CHANGE_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";

    private static VolumeChangeListener mListener;

    private VolumeBroadCastReceiver mVolumeBroadCastReceiver;

    public VolumeChangeHelper(Context context) {
        mContext = context;
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void registerVolumeChangeListener(VolumeChangeListener listener) {
        mListener = listener;
        mVolumeBroadCastReceiver = new VolumeBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGE_ACTION);
        if (mVolumeBroadCastReceiver != null) {
            mContext.registerReceiver(mVolumeBroadCastReceiver,filter);
        }
    }

    public void unRegisterVolumeChangeListener() {
        if (mVolumeBroadCastReceiver != null) {
            mContext.unregisterReceiver(mVolumeBroadCastReceiver);
            mVolumeBroadCastReceiver = null;
            mListener = null;
        }
    }

    static class VolumeBroadCastReceiver extends BroadcastReceiver {

       @Override
       public void onReceive(Context context, Intent intent) {
           if (intent.getAction() == VOLUME_CHANGE_ACTION
                   && intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE,-1)
                   == AudioManager.STREAM_MUSIC) {
               int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
               final VolumeChangeListener listener = mListener;
               if (listener != null && currentVolume > 0) {
                   listener.onVolumeChange(currentVolume);
               }
           }
       }
   }

   public interface VolumeChangeListener {
        void onVolumeChange(int volume);
   }
}
