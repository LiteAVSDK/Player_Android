package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.tencent.liteav.demo.superplayer.R;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;

import java.util.HashMap;
import java.util.Map;

public class VodSubtitlesSettingView extends RelativeLayout implements View.OnClickListener {

    private Context mContext;

    private RelativeLayout  mRootView;

    private Spinner mSpinnerFontColor;

    private Spinner mSpinnerFontSize;

    private Spinner mSpinnerOutlineWidth;

    private Spinner mSpinnerOutLineColor;

    private OnClickBackButtonListener mListener;

    private ImageView mBackButton;

    private Button    mReset;

    private Button    mDone;

    private int[] colorArray = {R.color.white,R.color.black,R.color.red,
            R.color.blue,R.color.green,
            R.color.yellow,R.color.megenta,R.color.cyan};

    private float[] outLineWidthArray = {0.5f,0.75f,1.00f,1.25f,1.50f,1.75f,2.00f,3.00f,4.00f};


    public VodSubtitlesSettingView(Context context) {
        super(context);
        init(context);
    }

    public VodSubtitlesSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public VodSubtitlesSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRootView = (RelativeLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.superplayer_vod_subtitle_setting_view, this, false);
        addView(mRootView);
        mBackButton = mRootView.findViewById(R.id.subtitle_setting_back);
        mBackButton.setOnClickListener(this);
        mDone = mRootView.findViewById(R.id.subtitle_setting_done);
        mDone.setOnClickListener(this);

        mReset = mRootView.findViewById(R.id.subtitle_setting_reset);
        mReset.setOnClickListener(this);

        mSpinnerFontColor = mRootView.findViewById(R.id.sp_font_color);
        ArrayAdapter fontColorAdapter = new ArrayAdapter<String>(mContext,
                R.layout.superplayer_item_for_custom_spinner, getResources().getStringArray(R.array.font_color));
        fontColorAdapter.setDropDownViewResource(R.layout.superplayer_item_for_drop_down);
        mSpinnerFontColor.setAdapter(fontColorAdapter);

        mSpinnerFontSize = mRootView.findViewById(R.id.sp_font_size);
        ArrayAdapter fontSizeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.superplayer_item_for_custom_spinner, getResources().getStringArray(R.array.font_size));
        fontSizeAdapter.setDropDownViewResource(R.layout.superplayer_item_for_drop_down);
        mSpinnerFontSize.setAdapter(fontSizeAdapter);

        mSpinnerOutLineColor = mRootView.findViewById(R.id.sp_outline_color);
        ArrayAdapter outlineColorAdapter = new ArrayAdapter<String>(mContext,
                R.layout.superplayer_item_for_custom_spinner, getResources().getStringArray(R.array.outline_color));
        outlineColorAdapter.setDropDownViewResource(R.layout.superplayer_item_for_drop_down);
        mSpinnerOutLineColor.setAdapter(outlineColorAdapter);

        mSpinnerOutlineWidth = mRootView.findViewById(R.id.sp_outline_width);
        ArrayAdapter outlineLineAdapter = new ArrayAdapter<String>(mContext,
                R.layout.superplayer_item_for_custom_spinner, getResources().getStringArray(R.array.outline_width));
        outlineLineAdapter.setDropDownViewResource(R.layout.superplayer_item_for_drop_down);
        mSpinnerOutlineWidth.setAdapter(outlineLineAdapter);

        mSpinnerFontColor.setBackgroundColor(getResources().getColor(R.color.superplayer_transparent));
        mSpinnerFontSize.setBackgroundColor(getResources().getColor(R.color.superplayer_transparent));
        mSpinnerOutLineColor.setBackgroundColor(getResources().getColor(R.color.superplayer_transparent));
        mSpinnerOutlineWidth.setBackgroundColor(getResources().getColor(R.color.superplayer_transparent));

        reset();

    }

    public interface OnClickBackButtonListener {

        void onClickBackButton();

        void onCLickDoneButton(Map map);
    }

    public void setOnClickBackButtonListener(OnClickBackButtonListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.subtitle_setting_back) {
            mListener.onClickBackButton();
        } else if (id == R.id.subtitle_setting_done) {
            Map<String,Object> extMap = new HashMap<>();
            extMap.put("PARAM_SUBTITLE_RENDER_MODEL",createVodSubtitleRenderModel());
            mListener.onCLickDoneButton(extMap);
        } else if (id == R.id.subtitle_setting_reset) {
            reset();
        }
    }

    public void reset() {
        mSpinnerFontColor.setSelection(0,true);
        mSpinnerFontSize.setSelection(0,true);
        mSpinnerOutLineColor.setSelection(0,true);
        mSpinnerOutlineWidth.setSelection(2,true);
    }

    public TXSubtitleRenderModel createVodSubtitleRenderModel() {
        TXSubtitleRenderModel model = new TXSubtitleRenderModel();
        model.fontColor = colorArray[mSpinnerFontColor.getSelectedItemPosition()];
        model.isBondFontStyle = mSpinnerFontColor.getSelectedItemPosition() == 1 ? true : false;
        model.outlineWidth = outLineWidthArray[mSpinnerOutlineWidth.getSelectedItemPosition()];
        model.outlineColor = colorArray[mSpinnerOutLineColor.getSelectedItemPosition()];
        return model;
    }
}
