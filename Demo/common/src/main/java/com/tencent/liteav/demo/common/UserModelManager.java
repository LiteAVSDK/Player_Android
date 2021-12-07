package com.tencent.liteav.demo.common;

import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserModelManager {
    private static final String TAG            = "UserModelManager";
    private static final String PER_DATA       = "per_profile_manager";
    private static final String PER_USER_MODEL = "per_user_model";
    private static final String PER_USER_DATE  = "per_user_publish_video_date";

    private static UserModelManager sInstance;
    private        UserModel        mUserModel;
    private        String           mUserPubishVideoDate;

    public static UserModelManager getInstance() {
        if (sInstance == null) {
            synchronized (UserModelManager.class) {
                if (sInstance == null) {
                    sInstance = new UserModelManager();
                }
            }
        }
        return sInstance;
    }

    public synchronized UserModel getUserModel() {
        // 保证每次都能获取到最新的UserModel
        loadUserModel();
        return mUserModel == null ? new UserModel() : mUserModel;
    }

    public synchronized void setUserModel(UserModel model) {
        mUserModel = model;
        try {
            SPUtils.getInstance(PER_DATA).put(PER_USER_MODEL, GsonUtils.toJson(mUserModel));
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    private void loadUserModel() {
        try {
            String json = SPUtils.getInstance(PER_DATA).getString(PER_USER_MODEL);
            mUserModel = GsonUtils.fromJson(json, UserModel.class);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    private String getUserPublishVideoDate() {
        if (mUserPubishVideoDate == null) {
            mUserPubishVideoDate = SPUtils.getInstance(PER_DATA).getString(PER_USER_DATE, "");
        }
        return mUserPubishVideoDate;
    }

    private void setUserPublishVideoDate(String date) {
        mUserPubishVideoDate = date;
        try {
            SPUtils.getInstance(PER_DATA).put(PER_USER_DATE, mUserPubishVideoDate);
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    // 首次TRTC打开摄像头提示"Demo特别配置了无限期云端存储"
    public boolean needShowSecurityTips() {
        String profileDate = getUserPublishVideoDate();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        String day = formatter.format(date);
        if (!day.equals(profileDate)) {
            setUserPublishVideoDate(day);
            return true;
        }
        return false;
    }
}