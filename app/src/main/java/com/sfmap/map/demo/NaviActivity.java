package com.sfmap.map.demo;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.sfmap.api.navi.Navi;
import com.sfmap.api.navi.NaviView;

public class NaviActivity extends NaviBaseActivity{
    private String TAG = NaviActivity.class.getSimpleName();
    private NaviView mNaviView;
    private Navi mNavi;
    private SFSpeechSyntesizer sfSpeechSyntesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        sfSpeechSyntesizer = SFSpeechSyntesizer.getInstance(NaviActivity.this);
        initView(savedInstanceState);
        initNaviData();
    }

    private void initView(Bundle savedInstanceState) {
        mNaviView = findViewById(R.id.navi_view);
        mNaviView.setMapNaviViewListener(this);
        mNaviView.onCreate(savedInstanceState);
        mNaviView.getMap().getUiSettings().setCompassEnabled(false);
    }

    private void initNaviData() {
        mNavi = Navi.getInstance(this);
        mNavi.addNaviListener(this);
        mNavi.setSoTimeout(15000);
        mNavi.setEmulatorNaviSpeed(100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mNavi.getNaviPath()!=null){
                    mNavi.startNavi(Navi.GPSNaviMode);
                }
            }
        },2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviView.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy()");
        try{
            mNavi.stopNavi();
            mNaviView.onDestroy();
            sfSpeechSyntesizer.destroy();
        }catch (Exception e){
            Log.d(TAG,"Exception "+e.getMessage());
        }
        super.onDestroy();
        Log.d(TAG,"super.onDestroy()");
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        sfSpeechSyntesizer.startSpeaking(s);
    }

    @Override
    public void onReCalculateRouteForYaw() {
        sfSpeechSyntesizer.startSpeaking("您已偏航,已为您重新规划路线");
    }

    @Override
    public void onArrivedWayPoint(int i) {
        sfSpeechSyntesizer.startSpeaking("到达第" + i + "个途径点");
    }

    /**
     * 导航结束
     */
    @Override
    public void onArriveDestination() {
        sfSpeechSyntesizer.startSpeaking("到达目的地,本次导航结束");
        finish();
    }

    /**
     * 导航页面左下角返回按钮点击后弹出的 "退出导航对话框" 中选择 "确定" 后的回调接口。
     */
    @Override
    public void onNaviCancel() {
        finish();
    }
}
