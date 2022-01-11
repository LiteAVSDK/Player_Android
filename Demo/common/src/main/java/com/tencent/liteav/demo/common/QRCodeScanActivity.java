package com.tencent.liteav.demo.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.google.zxing.Result;

import java.util.List;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeScanActivity extends Activity implements ZXingScannerView.ResultHandler {
    private static final String TAG                 = "QRCodeScanActivity";
    public static final  String QR_CODE_SCAN_RESULT = "qr_code_scan_result";

    private Context          mAppContext;
    private ZXingScannerView mScannerView;
    private int              mPosition;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mAppContext = this.getApplicationContext();
        ViewGroup view = (ViewGroup) View.inflate(this, R.layout.common_activity_qr_code_scan, null);
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };
        Intent intent = getIntent();
        if (intent != null) {
            mPosition = intent.getIntExtra("position", 0);
            Log.d(TAG, "getIntent position " + mPosition);
        }
        view.addView(mScannerView, 0);
        setContentView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        PermissionUtils.permission(PermissionConstants.CAMERA).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                mScannerView.startCamera();
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                Toast.makeText(mAppContext, getString(R.string.common_toast_camera_permission_failed),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }).request();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.setResultHandler(null);
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String url = rawResult.toString();
        Log.d(TAG, "handleResult url " + url);
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(mAppContext, getString(R.string.common_toast_qr_code_error), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(QR_CODE_SCAN_RESULT, url);
        intent.putExtra("position", mPosition);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.common_ibtn_back) {
            finish();
        }
    }

    private static class CustomViewFinderView extends ViewFinderView {
        public static final String TRADE_MARK_TEXT         = "scan url";
        public static final int    TRADE_MARK_TEXT_SIZE_SP = 40;
        public final        Paint  PAINT                   = new Paint();

        public CustomViewFinderView(Context context) {
            super(context);
            init();
        }

        public CustomViewFinderView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            PAINT.setColor(Color.WHITE);
            PAINT.setAntiAlias(true);
            float textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TRADE_MARK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
            PAINT.setTextSize(textPixelSize);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawTradeMark(canvas);
        }

        private void drawTradeMark(Canvas canvas) {
            Rect framingRect = getFramingRect();
            float tradeMarkTop;
            float tradeMarkLeft;
            if (framingRect != null) {
                tradeMarkTop = framingRect.bottom + PAINT.getTextSize() + 10;
                tradeMarkLeft = framingRect.left;
            } else {
                tradeMarkTop = 10;
                tradeMarkLeft = canvas.getHeight() - PAINT.getTextSize() - 10;
            }
            canvas.drawText(TRADE_MARK_TEXT, tradeMarkLeft, tradeMarkTop, PAINT);
        }
    }
}
