package com.tencent.liteav.demo.common;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;


public class PermissionIntroductionDialog extends DialogFragment {

    private PositiveClickListener mPositiveButtonClickListener;
    private Button mButtonNegative;
    private Button mButtonPositive;
    private Dialog mDialog;
    private TextView mTvMessage;
    private TextView mTvTitle;
    private String mTitle;
    private String mContent;
    private DialogPosition mPosition;
    private ConstraintLayout mCLButton;
    private View mView;

    public PermissionIntroductionDialog(String title, String content, DialogPosition position) {
        mTitle = title;
        mContent = content;
        mPosition = position;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mDialog = new Dialog(getActivity(), R.style.DialogFragment);
        mDialog.setContentView(R.layout.ugckit_fragment_dialog_permission_introduction);
        mButtonPositive = (Button) mDialog.findViewById(R.id.btn_positive);
        mButtonNegative = (Button) mDialog.findViewById(R.id.btn_negative);

        mTvTitle = (TextView) mDialog.findViewById(R.id.tv_title);
        mTvMessage = (TextView) mDialog.findViewById(R.id.tv_message);
        mCLButton = (ConstraintLayout) mDialog.findViewById(R.id.cl_button_panel);
        mView = (View) mDialog.findViewById(R.id.view);
        mTvMessage.setText(mContent);
        mTvTitle.setText(mTitle);
        mDialog.setCancelable(false);
        mView.setVisibility(mPosition == DialogPosition.TOP ? View.GONE : View.VISIBLE);
        mCLButton.setVisibility(mPosition == DialogPosition.TOP ? View.GONE : View.VISIBLE);
        initButtonPositive();
        initButtonNegtive();

        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = mPosition == DialogPosition.TOP ? Gravity.TOP : Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        return mDialog;
    }


    private void initButtonPositive() {
        mButtonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositiveButtonClickListener != null) {
                    mPositiveButtonClickListener.onClickPositive();
                    mDialog.dismiss();
                }
            }
        });
    }

    private void initButtonNegtive() {
        mButtonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    public void setPositiveClickListener(PositiveClickListener listener) {
        mPositiveButtonClickListener = listener;
    }

    public interface PositiveClickListener {
        void onClickPositive();
    }

    public enum DialogPosition {
        TOP,
        BOTTOM
    }
}
