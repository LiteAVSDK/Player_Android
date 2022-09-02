package com.tencent.liteav.demo.common.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.tencent.liteav.demo.common.PermissionIntroductionDialog;
import com.tencent.liteav.demo.common.R;
import com.tencent.liteav.demo.common.utils.SharedPreferenceUtils;


public class PermissionManager {
    private static final long FORTY_EIGHT_HOURS = 60 * 1000 * 48 * 60;
    private static final String DIALOG_NAME = "PermissionIntroductionDialog";
    private PermissionIntroductionDialog mDialog;
    private Context mContext;
    private SharedPreferenceUtils sharedPreferenceUtils;
    public static final int REQUEST_CODE_STORAGE = 101;
    public static final int REQUEST_CODE_CAMERA = 909;
    public static final int REQUEST_CODE_AUDIO = 808;
    public static final String SHARED_PREFERENCE_FILE_NAME_PERMISSION = "permission";
    public static final String SHARED_PREFERENCE_KEY_STORAGE = "storage";
    public static final String SHARED_PREFERENCE_KEY_AUDIO = "audio";
    public static final String SHARED_PREFERENCE_KEY_CAMERA = "camera";
    public static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String SHARED_PREFERENCE_FIRST_START = "FIRST_START";
    private OnStoragePermissionGrantedListener onStoragePermissionGrantedListener;
    private OnCameraPermissionGrantedListener onCameraPermissionGrantedListener;

    public enum PermissionType {
        STORAGE,
        AUDIO,
        CAMERA,
    }

    private PermissionType mPermissionType;

    public PermissionManager(Context context, PermissionType type) {
        mContext = context;
        mPermissionType = type;
        sharedPreferenceUtils = new SharedPreferenceUtils(mContext, SHARED_PREFERENCE_FILE_NAME_PERMISSION);
        if (type.equals(PermissionType.STORAGE)) {
            mDialog = new PermissionIntroductionDialog(mContext.getString(R.string.permission_introduction_write_title),
                    mContext.getString(R.string.permission_introduction_write),
                    PermissionIntroductionDialog.DialogPosition.TOP);
        } else if (mPermissionType.equals(PermissionType.AUDIO)) {
            mDialog = new PermissionIntroductionDialog(mContext
                    .getString(R.string.permission_introduction_audio_title),
                    mContext.getString(R.string.permission_introduction_audio),
                    PermissionIntroductionDialog.DialogPosition.TOP);
        } else if (mPermissionType.equals(PermissionType.CAMERA)) {
            mDialog = new PermissionIntroductionDialog(mContext
                    .getString(R.string.permission_introduction_camera_title),
                    mContext.getString(R.string.permission_introduction_camera),
                    PermissionIntroductionDialog.DialogPosition.TOP);
        }
        mDialog.setCancelable(false);
    }

    public void checkoutIfShowPermissionIntroductionDialog() {
        long now = System.currentTimeMillis();
        long oldTime = 0L;
        if (mPermissionType.equals(PermissionType.STORAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                oldTime = (long) sharedPreferenceUtils.getSharedPreference(SHARED_PREFERENCE_KEY_STORAGE, 0L);
                if (mContext.checkSelfPermission(PERMISSION_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if ((now - oldTime) >= FORTY_EIGHT_HOURS) {
                        checkAndRequestPermission(PERMISSION_STORAGE, REQUEST_CODE_STORAGE);
                        FragmentActivity temp = (FragmentActivity) mContext;
                        mDialog.show(temp.getSupportFragmentManager(), DIALOG_NAME);
                    }
                } else {
                    onStoragePermissionGrantedListener.onStoragePermissionGranted();
                }
            } else {
                onStoragePermissionGrantedListener.onStoragePermissionGranted();
            }
        } else if (mPermissionType.equals(PermissionType.AUDIO)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                oldTime = (long) sharedPreferenceUtils.getSharedPreference(SHARED_PREFERENCE_KEY_AUDIO, 0L);
                if (mContext.checkSelfPermission(PERMISSION_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if ((now - oldTime) >= FORTY_EIGHT_HOURS) {
                        checkAndRequestPermission(PERMISSION_AUDIO, REQUEST_CODE_AUDIO);
                        FragmentActivity temp = (FragmentActivity) mContext;
                        mDialog.show(temp.getSupportFragmentManager(), DIALOG_NAME);
                    }
                }
            }
        } else if (mPermissionType.equals(PermissionType.CAMERA)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                oldTime = (long) sharedPreferenceUtils.getSharedPreference(SHARED_PREFERENCE_KEY_CAMERA, 0L);
                if (mContext.checkSelfPermission(PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if ((now - oldTime) >= FORTY_EIGHT_HOURS) {
                        checkAndRequestPermission(PERMISSION_CAMERA, REQUEST_CODE_CAMERA);
                        FragmentActivity temp = (FragmentActivity) mContext;
                        mDialog.show(temp.getSupportFragmentManager(), DIALOG_NAME);
                    }
                } else {
                    onCameraPermissionGrantedListener.onCameraPermissionGranted();
                }
            } else {
                onCameraPermissionGrantedListener.onCameraPermissionGranted();
            }
        }
    }

    public boolean checkAndRequestPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {permission};
            //验证是否许可权限
            for (String str : permissions) {
                if (mContext.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    FragmentActivity tempActivity = (FragmentActivity) mContext;
                    //申请权限
                    tempActivity.requestPermissions(permissions, requestCode);
                    return false;
                }
            }
        } else {
            if (mPermissionType.equals(PermissionType.STORAGE)) {
                onStoragePermissionGrantedListener.onStoragePermissionGranted();
            } else if (mPermissionType.equals(PermissionType.CAMERA)) {
                onCameraPermissionGrantedListener.onCameraPermissionGranted();
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (grantResults != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requestCode == REQUEST_CODE_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onStoragePermissionGrantedListener.onStoragePermissionGranted();
                    } else {
                        long begin = System.currentTimeMillis();
                        sharedPreferenceUtils.put(SHARED_PREFERENCE_KEY_STORAGE, begin);
                    }
                } else if (requestCode == REQUEST_CODE_AUDIO) {
                    if ((grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                        long begin = System.currentTimeMillis();
                        sharedPreferenceUtils.put(SHARED_PREFERENCE_KEY_AUDIO, begin);
                    }
                    mDialog.dismiss();
                } else if (requestCode == REQUEST_CODE_CAMERA) {
                    if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        onCameraPermissionGrantedListener.onCameraPermissionGranted();
                    } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        long begin = System.currentTimeMillis();
                        sharedPreferenceUtils.put(SHARED_PREFERENCE_KEY_CAMERA, begin);
                    }
                }
                mDialog.dismiss();
            }
        }
    }

    public interface OnStoragePermissionGrantedListener {
        void onStoragePermissionGranted();
    }

    public void setOnStoragePermissionGrantedListener(OnStoragePermissionGrantedListener listener) {
        onStoragePermissionGrantedListener = listener;
    }

    public interface OnCameraPermissionGrantedListener {
        void onCameraPermissionGranted();
    }


    public void setOnCameraPermissionGrantedListener(OnCameraPermissionGrantedListener listener) {
        onCameraPermissionGrantedListener = listener;
    }
}
