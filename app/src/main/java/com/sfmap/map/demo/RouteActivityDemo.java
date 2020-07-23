package com.sfmap.map.demo;

import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.sfmap.api.maps.CameraUpdateFactory;
import com.sfmap.api.maps.MapView;
import com.sfmap.api.maps.model.BitmapDescriptorFactory;
import com.sfmap.api.maps.model.LatLng;
import com.sfmap.api.maps.model.LatLngBounds;
import com.sfmap.api.maps.model.MarkerOptions;
import com.sfmap.api.maps.model.PolylineOptions;
import com.sfmap.api.navi.Navi;
import com.sfmap.api.navi.model.NaviLatLng;
import com.sfmap.api.navi.model.NaviPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sfmap.map.demo.MainActivity.CARROUTE_INDEX_0;
import static com.sfmap.map.demo.MainActivity.ROUTE_TYPE_CAR;

public class RouteActivityDemo extends NaviBaseActivity{
    private RecyclerView recyclerView;
    private MapView mapView;
    private Navi mNavi;
    //算路终点坐标
    protected NaviLatLng mEndLatlng = new NaviLatLng(34.71806, 113.741719);
    //算路起点坐标
    protected NaviLatLng mStartLatlng = new NaviLatLng(34.718029, 113.743371);
    private int planMode = 9;
    private int routeType = 1;
    protected TruckInfo mTruckInfo;
    private LocationManager mLocationManager;
    //存储算路起点的列表
    protected final List<NaviLatLng> startPoints = new ArrayList<>();
    //存储算路终点的列表
    protected final List<NaviLatLng> endPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        initView(savedInstanceState);

        String startLatlng = getIntent().getStringExtra("startLatlng");
        String endLatlng = getIntent().getStringExtra("endLatlng");
        String truckInfo = getIntent().getStringExtra("truckInfo");
        routeType = getIntent().getIntExtra("routeType",ROUTE_TYPE_CAR);
        planMode = getIntent().getIntExtra("planMode",CARROUTE_INDEX_0);

        if(!TextUtils.isEmpty(startLatlng) && !TextUtils.isEmpty(endLatlng)){
            mStartLatlng = new Gson().fromJson(startLatlng, NaviLatLng.class);
            mEndLatlng = new Gson().fromJson(endLatlng, NaviLatLng.class);
        }
        if(!TextUtils.isEmpty(truckInfo)){
            mTruckInfo = new Gson().fromJson(truckInfo, TruckInfo.class);
        }
        initNaviData();
    }

    private void initView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMap().getUiSettings().setCompassEnabled(false);

        recyclerView = findViewById(R.id.recycler);
    }

    private void initRecyclerView(){
        GridLayoutManager gridLayoutManager;
        if(mNavi.getNaviPaths() == null){
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        }else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), mNavi.getNaviPaths().size());
        }
        gridLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        List<NaviPath> naviPaths = new ArrayList<>();
        if(mNavi.getNaviPaths() == null){
            naviPaths.add(mNavi.getNaviPath());
        }else {
            for (int i =0 ;i<mNavi.getNaviPaths().size();i++){
                naviPaths.add(mNavi.getNaviPaths().get(i));
            }
        }
        PathAdapter pathAdapter = new PathAdapter(naviPaths);
        recyclerView.setAdapter(pathAdapter);
        pathAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(getApplicationContext(),"选择第"+position+"条路线导航",Toast.LENGTH_SHORT).show();
                mNavi.selectRouteId(position);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), NaviActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initNaviData() {
        mNavi = Navi.getInstance(this);
        mNavi.addNaviListener(this);
        //设置模拟导航的行车速度
        mNavi.setEmulatorNaviSpeed(300);
        startPoints.add(mStartLatlng);
        endPoints.add(mEndLatlng);
        mNavi.setSoTimeout(15000);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        switch (routeType){
            case 1:
                startCarNavigation();
                break;
            case 3:
                startTruckNavigation();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            mapView.onDestroy();
        }catch (Exception e){

        }
    }

    private void startCarNavigation() {
        // 驾车算路
        mNavi.calculateDriveRoute(
                startPoints,//指定的导航起点。支持多个起点，起点列表的尾点为实际导航起点，其他坐标点为辅助信息，带有方向性，可有效避免算路到马路的另一侧；
                endPoints,//指定的导航终点。支持一个终点。
                new ArrayList<NaviLatLng>(), //途经点，同时支持最多16个途经点的路径规划；
                planMode, //驾车路径规划的计算策略
                false //是否为本地算路,true 本地算路,false 网络算路
        );
    }

    private void startTruckNavigation() {
        if(null == mTruckInfo){
            Toast.makeText(getApplicationContext(),"请先设置货车信息" ,Toast.LENGTH_LONG).show();
            return;
        }
        String carVehicle = mTruckInfo.getTruckType(); //1:小车 4:拖挂车 5:微型货车 6:轻型货车 7:中型货车 8:中型货车 9:危险品运输车
        String carWeight = mTruckInfo.getWeight();
        String carAxleNumber = mTruckInfo.getAxleNum();
        String carHeight = mTruckInfo.getHeight();
        String carPlate = mTruckInfo.getPlate();

        mNavi.setPlate(carPlate);
        mNavi.setVehicle(Integer.parseInt(carVehicle));
        mNavi.setWeight(Double.parseDouble(carWeight));
        mNavi.setHeight(Double.parseDouble(carHeight));
        mNavi.setAxleNumber(Integer.parseInt(carAxleNumber.substring(0, 1)));
        mNavi.calculateDriveRoute(startPoints, endPoints, new ArrayList<NaviLatLng>(), planMode, false);
    }

    /**
     * 导航创建成功时的回调函数。
     */
    @Override
    public void onInitNaviSuccess() {
//        switch (routeType){
//            case 1:
//                startCarNavigation();
//                break;
//            case 3:
//                startTruckNavigation();
//                break;
//            default:
//                break;
//        }
    }

    /**
     * 驾车路径规划成功后的回调函数。
     */
    @Override
    public void onCalculateRouteSuccess() {
        HashMap<Integer, NaviPath> naviPathHashMap = new HashMap<>();
        naviPathHashMap.put(0,mNavi.getNaviPath());
        drawLines(naviPathHashMap);

    }

    /**
     * 多路线算路成功回调。
     * @param routeIds - 路线id数组
     */
    @Override
    public void onCalculateMultipleRoutesSuccess(int[] routeIds) {
        drawLines(mNavi.getNaviPaths());
    }

    private void drawStartMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(mStartLatlng.getLatitude(),mStartLatlng.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble_start));
        mapView.getMap().addMarker(markerOptions);
    }

    private void drawEndMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(mEndLatlng.getLatitude(),mEndLatlng.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble_end));
        mapView.getMap().addMarker(markerOptions);
    }

    private void drawLines(HashMap<Integer, NaviPath> naviPaths) {
        initRecyclerView();
        drawStartMarker();
        drawEndMarker();
        for (int i =0 ;i<naviPaths.size();i++){
            NaviPath naviPath = naviPaths.get(i);
            drawLine(naviPath.getCoordList());
        }
        setLatLngsCenter(200);
    }

    private void drawLine(List<NaviLatLng> naviLatLngs){
        PolylineOptions polylineOptions = new PolylineOptions();
        for(NaviLatLng naviLatLng : naviLatLngs){
            polylineOptions.add(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
        }
        polylineOptions.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.routetexture_green));
        polylineOptions.width(40);
        mapView.getMap().addPolyline(polylineOptions);
    }

    /**
     * 根据传入的地址明细，计算得出地图的缩放比，确保在初始界面中加载所有传入的地址
     */
    public void setLatLngsCenter(int padding) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds;

        builder.include(new LatLng(mStartLatlng.getLatitude(),mStartLatlng.getLongitude()));//把你所有的坐标点放进去
        builder.include(new LatLng(mEndLatlng.getLatitude(),mEndLatlng.getLongitude()));//把你所有的坐标点放进去

        bounds = builder.build();
        mapView.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
