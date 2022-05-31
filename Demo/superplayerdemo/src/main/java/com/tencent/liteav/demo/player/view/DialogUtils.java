package com.tencent.liteav.demo.player.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.liteav.demo.player.R;

public class DialogUtils {

    private static final long    MAX_TIP_SHOW_TIME = 1500L;
    private final        Handler mainHandler;

    private static class SingletonInstance {
        private static final DialogUtils instance = new DialogUtils();
    }

    public static DialogUtils getInstance() {
        return SingletonInstance.instance;
    }

    private DialogUtils() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public Dialog showCommonDialog(Context context, String title, String content,
                                   DialogInterface.OnClickListener okClickListener) {
        return showCommonDialog(context, title, content, null, okClickListener);
    }

    /**
     * 显示弹框
     */
    public Dialog showCommonDialog(Context context, String title, String content
            , final DialogInterface.OnClickListener cancelClickListener,
                                   final DialogInterface.OnClickListener okClickListener) {
        final Dialog dialog = new Dialog(context, R.style.superplayer_dialog_common);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogRootView = inflater.inflate(R.layout.superplayer_common_dialog_view, null, false);
        TextView tvTitle = dialogRootView.findViewById(R.id.dialog_tv_title);
        TextView tvContent = dialogRootView.findViewById(R.id.dialog_tv_content);
        final Button btnCancel = dialogRootView.findViewById(R.id.dialog_btn_cancel);
        final Button btnOk = dialogRootView.findViewById(R.id.dialog_btn_confirm);

        tvTitle.setText(title);
        tvContent.setText(content);
        dialog.setContentView(dialogRootView);
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != cancelClickListener) {
                    cancelClickListener.onClick(dialog, 0);
                } else {
                    dialog.dismiss();
                }
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != okClickListener) {
                    okClickListener.onClick(dialog, 0);
                }
            }
        });

        return dialog;
    }

    /**
     * 显示tip提示，会在MAX_TIP_SHOW_TIME自动消失
     */
    public Dialog showTip(Context context, boolean isSuccess, String content) {
        final Dialog dialog = new Dialog(context, R.style.superplayer_tip_common);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogRootView = inflater.inflate(R.layout.superplayer_common_tip_view, null, false);
        ImageView iconView = dialogRootView.findViewById(R.id.tip_iv_icon);
        TextView contentView = dialogRootView.findViewById(R.id.tip_tv_content);

        if (isSuccess) {
            iconView.setImageResource(R.drawable.superplayer_tip_success);
        } else {
            iconView.setImageResource(R.drawable.superplayer_tip_error);
        }
        contentView.setText(content);
        dialog.setContentView(dialogRootView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // MAX_TIP_SHOW_TIME 后消失
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, MAX_TIP_SHOW_TIME);
        return dialog;
    }

}
