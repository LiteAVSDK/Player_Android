package com.tencent.liteav.demo.player.view.dialog;

import static com.tencent.liteav.demo.player.expand.SuperPlayerConstants.LIST_TYPE_LIVE;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.player.R;
import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig;
import com.tencent.liteav.demo.vodcommon.entity.VideoModel;
import com.tencent.rtmp.TXPlayerDrmBuilder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class PlayerAddVideoDialog extends Dialog {

    private final Map<Class<? extends DialogViewController>, DialogViewController> mDialogViewMap = new HashMap<>();
    private View mRootView;
    private final int mCurrentDataType;
    private OnAddVideoListener mOnAddVideoListener;

    public PlayerAddVideoDialog(Context context, int dataType) {
        this(context, 0, dataType);
    }

    public PlayerAddVideoDialog(@NonNull Context context, int themeResId, int dataType) {
        super(context, themeResId);
        mCurrentDataType = dataType;
        initView();
    }

    private void initView() {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.superplayer_dialog_new_vod_player, null);
        setContentView(mRootView);
        setTitle(R.string.superplayer_choose_video_type);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
        getDialogViewController(ViewPreController.class).showView();
    }

    <T extends DialogViewController> T getDialogViewController(Class<T> controllerClass) {
        DialogViewController dialogViewController = mDialogViewMap.get(controllerClass);
        if (null != dialogViewController) {
            return controllerClass.cast(dialogViewController);
        }
        T viewController;
        try {
            Constructor<T> controllerConstructor = controllerClass.getConstructor(PlayerAddVideoDialog.class);
            viewController = controllerConstructor.newInstance(this);
            mDialogViewMap.put(controllerClass, viewController);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return viewController;
    }

    public int getCurrentDataType() {
        return mCurrentDataType;
    }

    void callbackVideo(VideoModel videoModel) {
        if (null != mOnAddVideoListener) {
            mOnAddVideoListener.onAddVideo(videoModel);
        }
        dismiss();
    }

    public void setOnAddVideoListener(OnAddVideoListener listener) {
        mOnAddVideoListener = listener;
    }

    public void removeOnAddVideoListener() {
        mOnAddVideoListener = null;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnAddVideoListener();
    }


    private static class ViewPreController implements DialogViewController {

        private final ViewStub mViewPre;
        private Button mBtnUrl;
        private Button mBtnFileId;
        private Button mBtnCancel;
        private boolean mIsInflate = false;
        private final PlayerAddVideoDialog mContainer;

        public ViewPreController(PlayerAddVideoDialog container) {
            mContainer = container;
            mViewPre = container.mRootView.findViewById(R.id.layoutVideoAddPre);
        }

        @Override
        public void showView() {
            checkStubInflate();
            mViewPre.setVisibility(ViewStub.VISIBLE);
        }

        @Override
        public void hideView() {
            if (mIsInflate) {
                mViewPre.setVisibility(ViewStub.GONE);
            }
        }

        private void checkStubInflate() {
            if (!mIsInflate) {
                mViewPre.inflate();
                mBtnUrl = mContainer.mRootView.findViewById(R.id.btn_url);
                mBtnFileId = mContainer.mRootView.findViewById(R.id.btn_fileId);
                mBtnCancel = mContainer.mRootView.findViewById(R.id.btn_cancel);

                mBtnUrl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideView();
                        mContainer.setTitle(R.string.superplayer_set_play_url);
                        mContainer.getDialogViewController(ViewUrlController.class).showView();
                    }
                });
                mBtnFileId.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideView();
                        mContainer.setTitle(R.string.superplayer_set_appid_fileid);
                        mContainer.getDialogViewController(ViewFileIdController.class).showView();
                    }
                });
                mBtnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContainer.dismiss();
                    }
                });
                if (mContainer.getCurrentDataType() == LIST_TYPE_LIVE) {
                    mBtnFileId.setVisibility(View.GONE);
                }
                mIsInflate = true;
            }
        }
    }


    public static class ViewUrlController implements DialogViewController {

        private static final int URL_INDEX = 0;
        private static final int DRM_URL_INDEX = 1;

        private final ViewStub mViewUrl;
        private boolean mIsInflate = false;
        private int mCurIndex = URL_INDEX;
        private final PlayerAddVideoDialog mContainer;
        // url
        private LinearLayout mUrlView;
        private EditText etUrl;
        // drm url
        private LinearLayout mDrmView;
        private EditText etDrmLicenseUrl;
        private EditText etDrmPlayUrl;
        private EditText etDrmCertUrl;
        // common
        private CheckBox cbCacheSwitch;
        private CheckBox cbDrmSwitch;
        private Button btnConfirm;
        private Button btnCancel;

        public ViewUrlController(PlayerAddVideoDialog container) {
            mContainer = container;
            mViewUrl = container.mRootView.findViewById(R.id.layoutVideoAddUrl);
        }

        @Override
        public void showView() {
            checkStubInflate();
            mViewUrl.setVisibility(View.VISIBLE);
        }

        @Override
        public void hideView() {
            if (mIsInflate) {
                mViewUrl.setVisibility(View.GONE);
            }
        }

        private void checkStubInflate() {
            if (!mIsInflate) {
                mViewUrl.inflate();

                mUrlView = mContainer.mRootView.findViewById(R.id.ll_url_view);
                mDrmView = mContainer.mRootView.findViewById(R.id.ll_drm_url);

                etUrl = mContainer.mRootView.findViewById(R.id.superplayer_et_url);
                etDrmLicenseUrl = mContainer.mRootView.findViewById(R.id.superplayer_et_drm_license_url);
                etDrmPlayUrl = mContainer.mRootView.findViewById(R.id.superplayer_drm_play_url);
                etDrmCertUrl = mContainer.mRootView.findViewById(R.id.superplayer_et_drm_cert_url);

                cbCacheSwitch = mContainer.mRootView.findViewById(R.id.superplayer_cb_url_cache_switch);
                cbDrmSwitch = mContainer.mRootView.findViewById(R.id.superplayer_cb_drm_switch);

                btnConfirm = mContainer.mRootView.findViewById(R.id.btn_url_confirm);
                btnCancel = mContainer.mRootView.findViewById(R.id.btn_url_cancel);

                if (mContainer.getCurrentDataType() == LIST_TYPE_LIVE) {
                    cbDrmSwitch.setVisibility(View.GONE);
                    cbCacheSwitch.setVisibility(View.GONE);
                } else {
                    cbDrmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                switchIndex(DRM_URL_INDEX);
                            } else {
                                switchIndex(URL_INDEX);
                            }
                        }
                    });
                }
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDone();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContainer.dismiss();
                    }
                });
                switchIndex(URL_INDEX);
                mIsInflate = true;
            }
        }

        private void handleDone() {
            VideoModel videoModel = new VideoModel();
            if (mCurIndex == URL_INDEX) {
                String playUrl = etUrl.getText().toString();
                if (TextUtils.isEmpty(playUrl)) {
                    Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_play_url,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                videoModel.videoURL = playUrl;
            } else if (mCurIndex == DRM_URL_INDEX) {
                String licenseUrl = etDrmLicenseUrl.getText().toString();
                String drmPlayUrl = etDrmPlayUrl.getText().toString();
                String certUrl = etDrmCertUrl.getText().toString();
                if (TextUtils.isEmpty(licenseUrl)) {
                    Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_play_url,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(drmPlayUrl)) {
                    Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_play_url,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                TXPlayerDrmBuilder builder = new TXPlayerDrmBuilder();
                builder.setKeyLicenseUrl(licenseUrl);
                builder.setPlayUrl(drmPlayUrl);
                if (!TextUtils.isEmpty(certUrl)) {
                    builder.setDeviceCertificateUrl(certUrl);
                }
                videoModel.drmBuilder = builder;
            }
            if (cbCacheSwitch.isChecked()) {
                videoModel.isEnableDownload = true;
            }
            mContainer.callbackVideo(videoModel);
        }

        public void switchIndex(int index) {
            mCurIndex = index;
            if (index == URL_INDEX) {
                mUrlView.setVisibility(View.VISIBLE);
                mDrmView.setVisibility(View.GONE);
            } else if (index == DRM_URL_INDEX) {
                mUrlView.setVisibility(View.GONE);
                mDrmView.setVisibility(View.VISIBLE);
            }
        }
    }


    public static class ViewFileIdController implements DialogViewController {

        private final ViewStub mViewFileId;
        private boolean mIsInflate = false;
        private final PlayerAddVideoDialog mContainer;

        private EditText mEtAppId;
        private EditText mEtFileId;
        private EditText mEtPSign;
        private CheckBox cbCacheSwitch;
        private CheckBox cbDrmSwitch;
        private Button btnConfirm;
        private Button btnCancel;

        public ViewFileIdController(PlayerAddVideoDialog container) {
            mContainer = container;
            mViewFileId = container.mRootView.findViewById(R.id.layoutVideoAddFileId);
        }

        @Override
        public void showView() {
            checkStubInflate();
            mViewFileId.setVisibility(View.VISIBLE);
        }

        @Override
        public void hideView() {
            if (mIsInflate) {
                mViewFileId.setVisibility(View.GONE);
            }
        }

        private void checkStubInflate() {
            if (!mIsInflate) {
                mViewFileId.inflate();
                mEtAppId = mContainer.mRootView.findViewById(R.id.superplayer_et_appid);
                mEtFileId = mContainer.mRootView.findViewById(R.id.superplayer_et_fileid);
                mEtPSign = mContainer.mRootView.findViewById(R.id.superplayer_et_psign);
                cbCacheSwitch = mContainer.mRootView.findViewById(R.id.superplayer_cb_fileId_cache_switch);
                cbDrmSwitch = mContainer.mRootView.findViewById(R.id.superplayer_cb_fileId_drm_switch);
                btnConfirm = mContainer.mRootView.findViewById(R.id.btn_fileId_confirm);
                btnCancel = mContainer.mRootView.findViewById(R.id.btn_fileId_cancel);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContainer.dismiss();
                    }
                });
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDone();
                    }
                });
                mIsInflate = true;
            }
        }

        private void handleDone() {
            String appIdStr = mEtAppId.getText().toString();
            String fileIdStr = mEtFileId.getText().toString();
            String pSignStr = mEtPSign.getText().toString();
            if (TextUtils.isEmpty(appIdStr)) {
                Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_appid,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(fileIdStr)) {
                Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_fileid,
                        Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Integer.parseInt(appIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(mContainer.getContext(), R.string.superplayer_input_correct_appid, Toast.LENGTH_SHORT).show();
                return;
            }
            VideoModel videoModel = new VideoModel();
            if (cbCacheSwitch.isChecked()) {
                videoModel.isEnableDownload = true;
            }
            if (cbDrmSwitch.isChecked()) {
                SuperPlayerGlobalConfig.getInstance().renderViewType = SuperPlayerDef.PlayerRenderType.SURFACE_VIEW;
            } else {
                SuperPlayerGlobalConfig.getInstance().renderViewType = SuperPlayerDef.PlayerRenderType.CLOUD_VIEW;
            }
            videoModel.appid = Integer.parseInt(appIdStr);
            videoModel.fileid = fileIdStr;
            videoModel.pSign = pSignStr;
            mContainer.callbackVideo(videoModel);
        }
    }

    interface DialogViewController {

        void showView();

        void hideView();
    }

    public interface OnAddVideoListener {

        void onAddVideo(VideoModel videoModel);

    }

}
