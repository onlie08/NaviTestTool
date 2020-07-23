package com.sfmap.map.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sfmap.api.navi.Navi;
import com.sfmap.api.navi.model.NaviLatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends Activity {
    private static String[] PERMISSIONS_REQUEST = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    Unbinder unbinder;
    @BindView(R.id.edit_plat)
    AutoEditTextView editPlat;
    @BindView(R.id.edit_type)
    AutoEditTextView editType;
    @BindView(R.id.edit_weight)
    AutoEditTextView editWeight;
    @BindView(R.id.edit_height)
    AutoEditTextView editHeight;
    @BindView(R.id.edit_alax)
    AutoEditTextView editAlax;
    @BindView(R.id.btn_go_navi)
    Button btnGoNavi;
    @BindView(R.id.raido_group_type)
    RadioGroup raidoGroupType;
    @BindView(R.id.radio_group_mode)
    RadioGroup radioGroupMode;
    @BindView(R.id.edit_start_lat)
    AutoEditTextView editStartLat;
    @BindView(R.id.edit_start_lon)
    AutoEditTextView editStartLon;
    @BindView(R.id.edit_end_lat)
    AutoEditTextView editEndLat;
    @BindView(R.id.edit_end_lon)
    AutoEditTextView editEndLon;

    private String TAG = "MainActivity";
    // 默认多策略
    public final static int CARROUTE_INDEX_DEFAULT = 9;

    // 推荐道路
    public final static int CARROUTE_INDEX_0 = 0;
    // 避免收费
    public static final int CARROUTE_INDEX_1 = 1;
    // 距离最短
    public final static int CARROUTE_INDEX_2 = 2;
    // 高速优先
    public final static int CARROUTE_INDEX_3 = 3;

    // 驾车导航
    public final static int ROUTE_TYPE_CAR = 1;
    // 货车导航
    public final static int ROUTE_TYPE_TRUCK = 3;

    int planMode = CARROUTE_INDEX_DEFAULT;
    int routeType = ROUTE_TYPE_CAR;
    TruckInfo truckInfo;
    double startlat = 30.453473;
    double startlon = 114.421412;
    double endlat = 30.543324;
    double endlon = 114.317862;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            LogcatFileManager.getInstance().start(Environment
                    .getExternalStorageDirectory().getAbsolutePath() + "/02NaviDemo");
        } catch (Exception e) {
        }
        unbinder = ButterKnife.bind(this);
        requestPermission();
        Navi.getInstance(this);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkPermission(Manifest.permission.READ_PHONE_STATE, Process.myPid(), Process.myUid())
                    != PackageManager.PERMISSION_GRANTED || this.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Process.myPid(), Process.myUid())
                    != PackageManager.PERMISSION_GRANTED || this.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Process.myPid(), Process.myUid())
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS_REQUEST, 1);
            }
        }
    }

    private void getLatlon() {
        if (TextUtils.isEmpty(editStartLat.getText().toString()) || TextUtils.isEmpty(editStartLon.getText().toString())
                || TextUtils.isEmpty(editEndLat.getText().toString()) || TextUtils.isEmpty(editEndLon.getText().toString())) {
            return;
        }
        startlat = Double.parseDouble(editStartLat.getText().toString());
        startlon = Double.parseDouble(editStartLon.getText().toString());
        endlat = Double.parseDouble(editEndLat.getText().toString());
        endlon = Double.parseDouble(editEndLon.getText().toString());
    }


    private int getMode() {
        switch (radioGroupMode.getCheckedRadioButtonId()) {
            case R.id.radio_tuijian:
                planMode = CARROUTE_INDEX_0;
                break;
            case R.id.radio_shoufei:
                planMode = CARROUTE_INDEX_1;
                break;
            case R.id.radio_juli:
                planMode = CARROUTE_INDEX_2;
                break;
            case R.id.radio_gaosu:
                planMode = CARROUTE_INDEX_3;
                break;
        }
        return planMode;
    }

    private int getType() {
        switch (raidoGroupType.getCheckedRadioButtonId()) {
            case R.id.radio_car:
                routeType = ROUTE_TYPE_CAR;
                break;
            case R.id.radio_truck:
                routeType = ROUTE_TYPE_TRUCK;
                break;
        }
        return routeType;
    }

    private TruckInfo getTruckInfo() {
        truckInfo = new TruckInfo();
        truckInfo.setPlate("鄂A2D823");
        truckInfo.setTruckType("4"); //货车类型 1:小车 4:拖挂车 5:微型货车 6:轻型货车 7:中型货车 8:中型货车 9:危险品运输车
        truckInfo.setWeight("4");
        truckInfo.setHeight("3");
        truckInfo.setAxleNum("4");
        if (TextUtils.isEmpty(editPlat.getText().toString()) || TextUtils.isEmpty(editType.getText().toString()) || TextUtils.isEmpty(editWeight.getText().toString())
                || TextUtils.isEmpty(editHeight.getText().toString()) || TextUtils.isEmpty(editAlax.getText().toString())) {
            return truckInfo;
        }
        truckInfo.setPlate(editPlat.getText().toString());
        truckInfo.setTruckType(editType.getText().toString());
        truckInfo.setWeight(editWeight.getText().toString());
        truckInfo.setHeight(editHeight.getText().toString());
        truckInfo.setAxleNum(editAlax.getText().toString());
        return truckInfo;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private void getTraceList() {
        String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/02NaviDemo";
        File rootFile = new File(filePath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        String[] traceList = rootFile.list();
        List<String> list = new ArrayList<>();
        for(String item : traceList){
            if(item.endsWith("NaviLog.txt")){
                list.add(item);
            }
        }
        String[] strs1=list.toArray(new String[list.size()]);
        if (traceList.length > 0) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(MainActivity.this)
                    .setTitle("将导航日志放到手机指定目录（内部存储/02NaviDemo）")
                    .setItems(strs1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "选择了第" + which + "个:" + traceList[which], Toast.LENGTH_SHORT).show();
                        if(traceList[which].endsWith("NaviLog.txt")){
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(),NaviTraceActivity.class);
                            intent.putExtra("TraceName",traceList[which]);
                            startActivity(intent);
                        }
                        }
                    }).create();
            alertDialog.show();
        }


    }

    @OnClick({R.id.btn_go_navi, R.id.btn_go_trace})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_go_navi:
                getLatlon();
                NaviLatLng mStartLatlng = new NaviLatLng(startlat, startlon);
                NaviLatLng mEndLatlng = new NaviLatLng(endlat, endlon);
                String startLatlng = new Gson().toJson(mStartLatlng);
                String endLatlng = new Gson().toJson(mEndLatlng);
                String truckInfo = new Gson().toJson(getTruckInfo());
                Intent intent = new Intent();
                intent.putExtra("startLatlng", startLatlng);
                intent.putExtra("endLatlng", endLatlng);
                intent.putExtra("planMode", getMode());
                intent.putExtra("routeType", getType());
                intent.putExtra("truckInfo", truckInfo);
                Log.d(TAG, "startLatlng: " + startLatlng + "endLatlng: " + endLatlng + "planMode: "
                        + planMode + "routeType: " + routeType + "truckInfo: " + truckInfo);
                intent.setClass(getApplicationContext(), RouteActivityDemo.class);
                startActivity(intent);
                break;
            case R.id.btn_go_trace:
                getTraceList();
                break;
        }
    }

}
