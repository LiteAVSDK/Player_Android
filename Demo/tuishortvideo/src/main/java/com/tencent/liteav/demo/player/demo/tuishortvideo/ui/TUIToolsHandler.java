package com.tencent.liteav.demo.player.demo.tuishortvideo.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.liteav.demo.player.demo.tuishortvideo.R;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.DemoSVGlobalConfig;
import com.tencent.liteav.demo.player.demo.tuishortvideo.common.SVDemoConstants;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.TUILayerBridge;
import com.tencent.liteav.demo.player.demo.tuishortvideo.layer.data.ShortVideoGenerator;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerLiveStrategy;
import com.tencent.qcloud.tuiplayer.core.api.TUIPlayerVodStrategy;
import com.tencent.qcloud.tuiplayer.core.api.common.TUIConstants;
import com.tencent.qcloud.tuiplayer.core.api.model.TUIVideoSource;
import com.tencent.qcloud.tuiplayer.shortvideo.common.TUIVideoConst;
import com.tencent.qcloud.tuiplayer.shortvideo.ui.view.TUIShortVideoView;
import com.tencent.rtmp.TXVodConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TUIToolsHandler implements View.OnClickListener {

    private final int[] resumeDataArray = {
            TUIConstants.TUIResumeMode.NONE,
            TUIConstants.TUIResumeMode.RESUME_LAST,
            TUIConstants.TUIResumeMode.RESUME_PLAYED
    };
    private final int[] loopModeArray = {
            TUIVideoConst.ListPlayMode.MODE_ONE_LOOP,
            TUIVideoConst.ListPlayMode.MODE_LIST_LOOP,
            TUIVideoConst.ListPlayMode.MODE_CUSTOM
    };

    private final int[] renderModeArray = {
            TUIConstants.TUIRenderMode.ADJUST_RESOLUTION,
            TUIConstants.TUIRenderMode.FULL_FILL_SCREEN,
    };

    private final int[] srModeArray = {
            TUIConstants.TUISuperResolution.SUPER_RESOLUTION_NONE,
            TUIConstants.TUISuperResolution.SUPER_RESOLUTION_ASR
    };

    private final float[] anArray = {
            TXVodConstants.AUDIO_NORMALIZATION_OFF,
            TXVodConstants.AUDIO_NORMALIZATION_LOW,
            TXVodConstants.AUDIO_NORMALIZATION_STANDARD,
            TXVodConstants.AUDIO_NORMALIZATION_HIGH,
    };

    private final int[] rotationArray = {
            0,
            90,
            180,
            270
    };

    private View mToolsView;
    private EditText mRangeView;
    private Button mBtnRemove;
    private Button mBtnInsert;
    private Button mBtnReplace;
    private Button mBtnJumpRandom;
    private Button mBtnBackNoPause;
    private AppCompatSpinner mSpinnerResumeMode;
    private AppCompatSpinner mSpinnerLoopMode;
    private AppCompatSpinner mSpinnerRenderMode;
    private AppCompatSpinner mSpinnerSRMode;
    private AppCompatSpinner mSpinnerAN;
    private AppCompatSpinner mSpinnerRotationMode;
    private SwitchCompat mSwitchMirror;
    private SwitchCompat mSwitchMute;
    private TUIShortVideoView mShortView;
    private TUIPlayerVodStrategy mVodStrategy;
    private TUIPlayerLiveStrategy mLiveStrategy;
    private ConstraintLayout mToolsContent;
    private ImageView mIvSettingView;
    private TUILayerBridge mLayerBridge;
    private boolean mIsShow = false;

    public TUIToolsHandler(FrameLayout parent, TUIShortVideoView videoView, TUIPlayerVodStrategy initVodStrategy,
                           TUIPlayerLiveStrategy initLiveStrategy, TUILayerBridge layerBridge) {
        mToolsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tuiplayer_tools, parent,
                false);
        parent.addView(mToolsView, 0);
        mShortView = videoView;
        mVodStrategy = initVodStrategy;
        mLiveStrategy = initLiveStrategy;
        mLayerBridge = layerBridge;
        initView();
    }

    private void initView() {
        mRangeView = mToolsView.findViewById(R.id.et_range);
        mBtnRemove = mToolsView.findViewById(R.id.btn_data_remove);
        mBtnInsert = mToolsView.findViewById(R.id.btn_data_insert);
        mBtnReplace = mToolsView.findViewById(R.id.btn_data_replace);
        mBtnJumpRandom = mToolsView.findViewById(R.id.btn_jump_random);
        mBtnBackNoPause = mToolsView.findViewById(R.id.btn_enter_back_no_pause);
        mSpinnerResumeMode = mToolsView.findViewById(R.id.acsp_resume_mode);
        mSpinnerLoopMode = mToolsView.findViewById(R.id.acsp_loop_mode);
        mSpinnerRenderMode = mToolsView.findViewById(R.id.acsp_render_mode);
        mSpinnerSRMode = mToolsView.findViewById(R.id.acsp_sr_mode);
        mSpinnerAN = mToolsView.findViewById(R.id.acsp_an_mode);
        mSpinnerRotationMode = mToolsView.findViewById(R.id.acsp_render_rotation_mode);
        mSwitchMirror = mToolsView.findViewById(R.id.sc_config_mirror);
        mSwitchMute = mToolsView.findViewById(R.id.sc_config_mute);
        mToolsContent = mToolsView.findViewById(R.id.cl_content_container);
        mIvSettingView = mToolsView.findViewById(R.id.iv_setting_view);

        mSpinnerResumeMode.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_resume_mode_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        mSpinnerLoopMode.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_loop_mode_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        mSpinnerRenderMode.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_render_mode_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        mSpinnerSRMode.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_sr_mode_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        mSpinnerAN.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_an_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        mSpinnerRotationMode.setAdapter(ArrayAdapter.createFromResource(mToolsView.getContext(),
                R.array.tuiplayer_render_rotation_array,
                R.layout.tuiplayer_simple_spinner_dropdown_item));
        setListener();

        int resumeMode = mVodStrategy.getResumeModel();
        mSpinnerResumeMode.setSelection(Arrays.binarySearch(resumeDataArray, resumeMode));
        int renderMode = mVodStrategy.getRenderMode();
        mSpinnerRenderMode.setSelection(Arrays.binarySearch(renderModeArray, renderMode));
        int srMode = mVodStrategy.getSuperResolutionMode();
        mSpinnerSRMode.setSelection(Arrays.binarySearch(srModeArray, srMode));
        float audioNormalization = mVodStrategy.getAudioNormalization();
        mSpinnerAN.setSelection(Arrays.binarySearch(anArray, audioNormalization));
        int renderRotation = DemoSVGlobalConfig.instance().getRotation();
        mSpinnerRotationMode.setSelection(Arrays.binarySearch(rotationArray, renderRotation));
        mSwitchMirror.setChecked(DemoSVGlobalConfig.instance().isMirror());
        mSwitchMute.setChecked(DemoSVGlobalConfig.instance().isMute());
    }

    private void setListener() {
        mSpinnerResumeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int resumeMode = resumeDataArray[position];
                if (resumeMode != mVodStrategy.getResumeModel()) {
                    mVodStrategy = new TUIPlayerVodStrategy.Builder(mVodStrategy)
                            .setResumeMode(resumeMode)
                            .build();
                    mShortView.setVodStrategy(mVodStrategy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinnerLoopMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int loopMode = loopModeArray[position];
                DemoSVGlobalConfig.instance().setPlayMode(loopMode);
                mShortView.setPlayMode(loopMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerRenderMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int renderMode = renderModeArray[position];
                if (renderMode != mVodStrategy.getRenderMode()) {
                    mVodStrategy = new TUIPlayerVodStrategy.Builder(mVodStrategy)
                            .setRenderMode(renderMode)
                            .build();
                    mShortView.setVodStrategy(mVodStrategy);
                }
                if (renderMode != mLiveStrategy.getRenderMode()) {
                    mLiveStrategy = new TUIPlayerLiveStrategy.Builder(mLiveStrategy)
                            .setRenderMode(renderMode)
                            .build();
                    mShortView.setLiveStrategy(mLiveStrategy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinnerSRMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int srMode = srModeArray[position];
                if (srMode != mVodStrategy.getSuperResolutionMode()) {
                    mVodStrategy = new TUIPlayerVodStrategy.Builder(mVodStrategy)
                            .setSuperResolutionMode(srMode)
                            .build();
                    mShortView.setVodStrategy(mVodStrategy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerAN.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float anValue = anArray[position];
                if (anValue != mVodStrategy.getAudioNormalization()) {
                    mVodStrategy = new TUIPlayerVodStrategy.Builder(mVodStrategy)
                            .setAudioNormalization(anValue)
                            .build();
                    mShortView.setVodStrategy(mVodStrategy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerRotationMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int rotation = rotationArray[position];
                if (rotation != DemoSVGlobalConfig.instance().getRotation()) {
                    DemoSVGlobalConfig.instance().setRotation(rotation);
                    mLayerBridge.postExtInfoToCurLayer(
                            SVDemoConstants.obtainIntEvent(SVDemoConstants.LayerEventCode.UPDATE_RENDER_ROTATION
                                    , rotation));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSwitchMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != DemoSVGlobalConfig.instance().isMirror()) {
                    DemoSVGlobalConfig.instance().setIsMirror(isChecked);
                    mLayerBridge.postExtInfoToCurLayer(
                            SVDemoConstants.obtainBooleanEvent(SVDemoConstants.LayerEventCode.UPDATE_MIRROR
                                    , isChecked));
                }
            }
        });
        mSwitchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != DemoSVGlobalConfig.instance().isMute()) {
                    DemoSVGlobalConfig.instance().setIsMute(isChecked);
                    mLayerBridge.postExtInfoToCurLayer(
                            SVDemoConstants.obtainBooleanEvent(SVDemoConstants.LayerEventCode.UPDATE_MUTE
                                    , isChecked));
                }
            }
        });
        mBtnRemove.setOnClickListener(this);
        mBtnInsert.setOnClickListener(this);
        mBtnReplace.setOnClickListener(this);
        mBtnJumpRandom.setOnClickListener(this);
        mBtnBackNoPause.setOnClickListener(this);
        mIvSettingView.setOnClickListener(this);
        final GestureDetector gestureDetector = new GestureDetector(mToolsView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(@NonNull MotionEvent e) {
                        hide();
                        return true;
                    }
                });
        mToolsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isShow()) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public View getToolsView() {
        return mToolsView;
    }

    public void show() {
        mToolsContent.setVisibility(View.VISIBLE);
        mIsShow = true;
    }

    public void hide() {
        mToolsContent.setVisibility(View.GONE);
        mIsShow = false;
    }

    public void toggleVisible() {
        if (mIsShow) {
            hide();
        } else {
            show();
        }
    }

    public boolean isShow() {
        return mIsShow;
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if (viewId == R.id.iv_setting_view) {
            toggleVisible();
        } else if (null != mShortView) {
            if (viewId == R.id.btn_data_remove) {
                if (checkRangeText(v.getContext(), true)) {
                    String rangeStr = mRangeView.getText().toString();
                    if (rangeStr.contains("-")) {
                        String[] rangeArray = rangeStr.split("-");
                        int rangeFrom = Integer.parseInt(rangeArray[0].trim());
                        int rangeTo = Integer.parseInt(rangeArray[1].trim());
                        mShortView.getDataManager().removeRangeData(rangeFrom, rangeTo - rangeFrom);
                    } else if (rangeStr.contains(",")) {
                        String[] rangeArray = rangeStr.split(",");
                        List<Integer> indexArray = new ArrayList<>();
                        for (String s : rangeArray) {
                            indexArray.add(Integer.parseInt(s.trim()));
                        }
                        mShortView.getDataManager().removeDataByIndex(indexArray);
                    } else if (TextUtils.isDigitsOnly(rangeStr)) {
                        mShortView.getDataManager().removeData(Integer.parseInt(rangeStr));
                    }
                }
            } else if (viewId == R.id.btn_data_insert) {
                if (checkRangeText(v.getContext(), false)) {
                    String rangeStr = mRangeView.getText().toString();
                    if (rangeStr.contains("-")) {
                        String[] rangeArray = rangeStr.split("-");
                        int rangeFrom = Integer.parseInt(rangeArray[0].trim());
                        int rangeTo = Integer.parseInt(rangeArray[1].trim());
                        int count = rangeTo - rangeFrom;
                        List<TUIVideoSource> sources = ShortVideoGenerator.generateSources(count);
                        mShortView.getDataManager().addRangeData(sources, rangeFrom);
                    } else if (TextUtils.isDigitsOnly(rangeStr)) {
                        mShortView.getDataManager().addData(ShortVideoGenerator.generateSource(), Integer.parseInt(rangeStr));
                    }
                }
            } else if (viewId == R.id.btn_data_replace) {
                if (checkRangeText(v.getContext(), false)) {
                    String rangeStr = mRangeView.getText().toString();
                    if (rangeStr.contains("-")) {
                        String[] rangeArray = rangeStr.split("-");
                        int rangeFrom = Integer.parseInt(rangeArray[0].trim());
                        int rangeTo = Integer.parseInt(rangeArray[1].trim());
                        int count = rangeTo - rangeFrom;
                        List<TUIVideoSource> sources = ShortVideoGenerator.generateSources(count);
                        mShortView.getDataManager().replaceRangeData(sources, rangeFrom);
                    } else if (TextUtils.isDigitsOnly(rangeStr)) {
                        mShortView.getDataManager().replaceData(ShortVideoGenerator.generateSource(), Integer.parseInt(rangeStr));
                    }
                }
            } else if (viewId == R.id.btn_jump_random) {
                final int count = mShortView.getDataManager().getCurrentDataCount();
                final int curIndex = mShortView.getDataManager().getCurrentIndex();
                final int randomIndex = findNextRandomIndex(count, curIndex);
                if (randomIndex >= 0) {
                    mShortView.startPlayIndex(randomIndex, false);
                }
            } else if (viewId == R.id.btn_enter_back_no_pause) {
                Context context = v.getContext();
                if (context instanceof Activity) {
                    mLayerBridge.setNeedPauseOnce(false);
                    ((Activity) context).moveTaskToBack(true);
                }
            }
        } else {
            Toast.makeText(v.getContext(), "shortVideoView not set!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @return -1 时表示生成随机索引失败
     */
    private int findNextRandomIndex(int count, int noSameIndex) {
        // count = 1时会 StackOverflowError
        if (count <= 1 || noSameIndex < 0 || noSameIndex >= count) {
            return -1;
        }
        final int randomIndex = (int) (count * Math.random());
        if (noSameIndex == randomIndex || count == randomIndex) {
            return findNextRandomIndex(count, noSameIndex);
        }
        return randomIndex;
    }


    private boolean checkRangeText(Context context, boolean isRemove) {
        String rangeStr = mRangeView.getText().toString();
        if (TextUtils.isEmpty(rangeStr)) {
            Toast.makeText(context, "range text is empty!", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!rangeStr.contains(",") && !rangeStr.contains("-") && !TextUtils.isDigitsOnly(rangeStr)) {
            Toast.makeText(context, "range text is invalid!", Toast.LENGTH_LONG).show();
            return false;
        }
        if (rangeStr.contains(",") && rangeStr.contains("-")) {
            Toast.makeText(context, "range text is only can use one mode!", Toast.LENGTH_LONG).show();
            return false;
        }
        if (rangeStr.contains(",") && !isRemove) {
            Toast.makeText(context, "commas separate only support remove ", Toast.LENGTH_LONG).show();
            return false;
        }
        if (rangeStr.contains("-")) {
            String[] rangeArray = rangeStr.split("-");
            if (rangeArray.length != 2) {
                Toast.makeText(context, "range text is invalid!", Toast.LENGTH_LONG).show();
                return false;
            }
            for (int i = 0; i < 2; i++) {
                if (!TextUtils.isDigitsOnly(rangeArray[i].trim())) {
                    Toast.makeText(context, "range text only support digits!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            int rangeFrom = Integer.parseInt(rangeArray[0].trim());
            int rangeTo = Integer.parseInt(rangeArray[1].trim());
            if (rangeTo < rangeFrom) {
                Toast.makeText(context, "rangeTo must be greater than rangeFrom", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (rangeStr.contains(",")) {
            String[] rangeArray = rangeStr.split(",");
            for (String s : rangeArray) {
                if (!TextUtils.isDigitsOnly(s.trim())) {
                    Toast.makeText(context, "range text only support digits!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }
}
