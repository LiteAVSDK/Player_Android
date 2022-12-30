package com.tencent.liteav.demo.player.demo.shortvideo.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class AbsBaseActivity extends AppCompatActivity {

    protected abstract void initLayout(@Nullable Bundle savedInstanceState);

    protected abstract void initView();

    protected abstract void initData();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout(savedInstanceState);
        initView();
        initData();
    }

}
