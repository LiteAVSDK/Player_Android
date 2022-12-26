package com.tencent.liteav.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.tencent.player.VodDownLoadActivity;
import com.tencent.player.baseplayer.VodPlayerActivity;
import com.tencent.player.common.Constants;

import com.tencent.player.playwithsurfaceviewdemo.PlayWithSurfaceViewActivity;
import com.tencent.player.playwithtextureviewdemo.PlayWithTextureViewActivity;
import com.tencent.player.setpreferredresolutiondemo.SetPreferredResolutionDemoActivity;
import com.tencent.player.vodpreloaddemo.VodPreloadDemoActivity;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveBaseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private   List<CustomAdapter.ItemData> mDatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLicense();
        initData();
        initUIView();
    }

    private void initUIView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CustomAdapter(this, mDatList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {
        mDatList = new ArrayList<>();
        mDatList.add(new CustomAdapter.ItemData("Base Vod Player","the useage of Vod Player", VodPlayerActivity.class));
        mDatList.add(new CustomAdapter.ItemData(getString(R.string.title_play_with_surface_view), getString(R.string.sub_title_play_with_surface_view), PlayWithSurfaceViewActivity.class));
        mDatList.add(new CustomAdapter.ItemData(getString(R.string.title_play_with_texture_view), getString(R.string.sub_title_play_with_texture_view), PlayWithTextureViewActivity.class));
        mDatList.add(new CustomAdapter.ItemData(getString(R.string.title_vod_preload_demo), getString(R.string.sub_title_vod_preload_demo), VodPreloadDemoActivity.class));
        mDatList.add(new CustomAdapter.ItemData(getString(R.string.set_preferred_resolution),getString(R.string.sub_set_preferred_resolution), SetPreferredResolutionDemoActivity.class));
        mDatList.add(new CustomAdapter.ItemData(getString(R.string.title_vod_download_and_play),getString(R.string.sub_title_vod_download_and_play),VodDownLoadActivity.class));
    }

    /**
     * see instruction page: https://cloud.tencent.com/document/product/881/20217#.E6.AD.A5.E9.AA.A42.EF.BC.9A.E9.85.8D.E7.BD.AE-license-.E6.8E.88.E6.9D.83
     */
    private void setLicense() {
        String licenceURL = ""; // 获取到的 licence url
        String licenceKey = ""; // 获取到的 licence key
        TXLiveBase.getInstance().setLicence(this, licenceURL, licenceKey);
        TXLiveBase.setListener(new TXLiveBaseListener() {
            @Override
            public void onLicenceLoaded(int result, String reason) {
                Log.i(Constants.TAG, "onLicenceLoaded: result:" + result + ", reason:" + reason);
            }
        });
    }
}