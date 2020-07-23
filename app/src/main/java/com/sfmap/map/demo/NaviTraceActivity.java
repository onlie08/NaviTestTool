package com.sfmap.map.demo;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.sfmap.api.maps.model.LatLng;
import com.sfmap.api.navi.Navi;
import com.sfmap.api.navi.NaviView;
import com.sfmap.api.navi.model.NaviLatLng;
import com.sfmap.api.navi.model.NaviPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NaviTraceActivity extends NaviBaseActivity{
    private String TAG = NaviTraceActivity.class.getSimpleName();
    private NaviView mNaviView;
    private Navi mNavi;
    private SFSpeechSyntesizer sfSpeechSyntesizer;
    private String traceName;
    private LatLng start;
    private LatLng end;
    //存储算路起点的列表
    protected final List<NaviLatLng> startPoints = new ArrayList<>();
    //存储算路终点的列表
    protected final List<NaviLatLng> endPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        traceName = getIntent().getStringExtra("TraceName");
        Log.d(TAG,traceName);
        sfSpeechSyntesizer = SFSpeechSyntesizer.getInstance(NaviTraceActivity.this);
        initView(savedInstanceState);
        initNaviData();
        reViewTrace();
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
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(mNavi.getNaviPath()!=null){
//                    mNavi.startNavi(Navi.GPSNaviMode);
//                }
//            }
//        },2000);
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


    int count = 0;
    private void reViewTrace(){
        readTrace();
        startCarNavigation();
        Observable.intervalRange(0, locations.size()*5, 1000, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long time) {
                        locations.get(count).setTime(System.currentTimeMillis());
                        Log.i(TAG,locations.get(count).getLongitude()+","+locations.get(count).getLatitude()+","+count);
                        mNavi.setGPSInfo(1, locations.get(count));
                        count++;
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 读取配置文件
     *
     * @return
     */
    public InputStream GetTraceFile() {
        InputStream tmpStream = null;
        String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/02NaviDemo/"+traceName;
        File file = new File(filePath);
        try {
            tmpStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tmpStream;
    }

    List<Location> locations = new ArrayList<>();
    private void readTrace() {
        try {
            InputStream tmpStream = GetTraceFile();
            BufferedReader bufReader = new BufferedReader(
                    new InputStreamReader(tmpStream, "UTF-8"));
            String str;
            while ((str = bufReader.readLine()) != null) {
                if (str != null && str.length() > 0) {
                    if(str.startsWith("#LOCATION")){
                        String[] name = str.split(",");
                        if(name != null && name.length>5){
                            Location location = new Location("gps");
                            location.setLongitude(Double.parseDouble(name[2]));
                            location.setLatitude(Double.parseDouble(name[3]));
                            location.setProvider("gps");
                            location.setSpeed(Float.parseFloat(name[5]));
                            location.setBearing(Float.parseFloat(name[6]));
                            location.setAccuracy(Float.parseFloat(name[7]));
                            locations.add(location);
                        }
                    }else if(str.startsWith("#RQPOS_S")){
                        Log.d(TAG,"str.startsWith(\"#RQPOS_S\")");
                        String[] item = str.split(",");
                        start = new LatLng(Double.parseDouble(item[2]),Double.parseDouble(item[1]));
                        startPoints.add(new NaviLatLng(start.latitude,start.longitude));
                        Log.d(TAG,"start:"+start.toString());
                    }else if(str.startsWith("#RQPOS_E")){
                        Log.d(TAG,"str.startsWith(\"#RQPOS_E\")");
                        String[] item = str.split(",");
                        end = new LatLng(Double.parseDouble(item[2]),Double.parseDouble(item[1]));
                        endPoints.add(new NaviLatLng(end.latitude,end.longitude));
                        Log.d(TAG,"end:"+end.toString());
                    }
                }
            }
            bufReader.close();
            tmpStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCarNavigation() {
        Log.d(TAG,"startCarNavigation");
        // 驾车算路
        mNavi.calculateDriveRoute(
                startPoints,//指定的导航起点。支持多个起点，起点列表的尾点为实际导航起点，其他坐标点为辅助信息，带有方向性，可有效避免算路到马路的另一侧；
                endPoints,//指定的导航终点。支持一个终点。
                new ArrayList<NaviLatLng>(), //途经点，同时支持最多16个途经点的路径规划；
                9, //驾车路径规划的计算策略
                false //是否为本地算路,true 本地算路,false 网络算路
        );
    }

    /**
     * 驾车路径规划成功后的回调函数。
     */
    @Override
    public void onCalculateRouteSuccess() {
        Log.d(TAG,"onCalculateRouteSuccess");
        mNavi.startNavi(Navi.GPSNaviMode);
    }

    /**
     * 多路线算路成功回调。
     * @param routeIds - 路线id数组
     */
    @Override
    public void onCalculateMultipleRoutesSuccess(int[] routeIds) {
        Log.d(TAG,"onCalculateMultipleRoutesSuccess");
        mNavi.startNavi(Navi.GPSNaviMode);
    }
}
