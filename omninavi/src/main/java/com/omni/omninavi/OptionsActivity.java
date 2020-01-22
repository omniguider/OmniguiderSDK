package com.omni.omninavi;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;
import com.android.volley.VolleyError;
import com.omni.omninavi.api.BeaconApi;
import com.omni.omninavi.manager.NetworkManager;
import com.omni.omninavi.model.BeaconSetBatteryResponse;
import com.omni.omninavi.model.OGFloor;
import com.omni.omninavi.model.OGFloors;
import com.omni.omninavi.model.OGPOI;
import com.omni.omninavi.tool.DialogTools;

import java.util.ArrayList;

/**
 * Created by wiliiamwang on 06/12/2017.
 */

public class OptionsActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    private static boolean IS_TEST_VERSION = false;

    public static boolean isTestVersion() {
        return IS_TEST_VERSION;
    }

    private static final int REQUEST_CODE_PERMISSIONS = 90;

    final int MSG_LE_START_SCAN = 1000;
    final int MSG_LE_STOP_SCAN = 1001;
    final int MSG_GET_DATA = 1002;
    final int MSG_STOP_SCAN = 1003;

    BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LE_START_SCAN:
                    if (mBTAdapter.isEnabled()) {
                        mBTAdapter.startLeScan(OptionsActivity.this);
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_LE_STOP_SCAN, 2000);
                    break;

                case MSG_LE_STOP_SCAN:
                    if (mBTAdapter.isEnabled()) {
                        mBTAdapter.stopLeScan(OptionsActivity.this);
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_LE_START_SCAN, 200);

                    break;
                case MSG_STOP_SCAN:
                    mHandler.removeMessages(MSG_LE_START_SCAN);
                    mHandler.removeMessages(MSG_LE_STOP_SCAN);
                    if (mBTAdapter.isEnabled()) {
                        mBTAdapter.stopLeScan(OptionsActivity.this);
                    }

//                    if(mPDSearch != null && mPDSearch.isShowing())
//                    {
//                        mPDSearch.dismiss();
//                    }

                    break;
                case MSG_GET_DATA:
//                    mListAdapter.notifyDataSetChanged();
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private TextInputLayout poiIdTIL;
    private TextInputEditText poiIdTIET;
    private TextInputEditText poiNameTIET;
    private AppCompatButton startNaviBtn;
    private AppCompatButton openMapBtn;
    private Spinner floorSpinner;
    private Spinner poiSpinner;
    private TextView beaconBatteryLogTV;

    private OGFloor mGoToFloor;
    private String mLastSendBatteryMac;
    private ArrayList<String> mSendBatteryMac;

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        // TODO Auto-generated method stub

        final BatteryPowerData BP = BatteryPowerData.generateBatteryBeacon(scanRecord);

        if (BP != null && BP.BatteryUuid.toUpperCase().startsWith("00112233-4455-6677-8899-AABBCCDDEEFF")) {
            mHandler.obtainMessage(MSG_GET_DATA).sendToTarget();
            Log.e("@W@", "BatteryPower:" + BP.batteryPower +
                    "\naddress : " + device.getAddress() +
                    "\ndevice name : " + device.getName() +
                    "\nrssi : " + rssi);
            if (!device.getAddress().equals(mLastSendBatteryMac) && !mSendBatteryMac.contains(device.getAddress())) {
                mSendBatteryMac.add(device.getAddress());
                BeaconApi.getInstance().setBeaconBatteryLevel(OptionsActivity.this,
                        device.getAddress(),
                        BP.batteryPower + "",
                        new NetworkManager.NetworkManagerListener<BeaconSetBatteryResponse>() {
                            @Override
                            public void onSucceed(BeaconSetBatteryResponse response) {
                                if (response.isSuccess()) {
                                    mLastSendBatteryMac = device.getAddress();
                                    beaconBatteryLogTV.setText("Beacon Mac : " + device.getAddress() +
                                            "\nBattery Level : " + BP.batteryPower);
                                }
                            }

                            @Override
                            public void onFail(VolleyError volleyError, boolean b) {

                            }
                        });
            }
        } else if (BP == null) {
        }

    }

    public void testScan() {
        mHandler.sendEmptyMessage(MSG_LE_START_SCAN);
//        mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN,20000);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);

        mSendBatteryMac = new ArrayList<>();

        Log.e("@W@", "onCreate");

//        mac : 98:07:2D:07:B5:00, minor : 98
        poiIdTIL = (TextInputLayout) findViewById(R.id.activity_options_til_poi_id);
        poiIdTIET = (TextInputEditText) findViewById(R.id.activity_options_tiet_poi_id);
        poiNameTIET = (TextInputEditText) findViewById(R.id.activity_options_tiet_poi_name);

        startNaviBtn = (AppCompatButton) findViewById(R.id.activity_options_start_navi);
        startNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String poiId = poiIdTIET.getText().toString().trim();
                String poiName = poiNameTIET.getText().toString().trim();
                String gotoFloor = mGoToFloor.getNumber();
                String gotoFloorPlanId = mGoToFloor.getFloorPlanId();

                if (TextUtils.isEmpty(poiId)) {
                    poiIdTIL.setError("POI id is required.");
                    return;
                } else {
                    poiIdTIL.setError("");
                }

                OGMapsActivity.navigationTo(OptionsActivity.this,
                        poiId,
                        poiName,
                        "1",
                        gotoFloor,
                        gotoFloorPlanId);
            }
        });

        openMapBtn = (AppCompatButton) findViewById(R.id.activity_options_open_map);
        openMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionsActivity.this, OGMapsActivity.class));
            }
        });

        floorSpinner = (Spinner) findViewById(R.id.activity_options_spinner_floors);
        poiSpinner = (Spinner) findViewById(R.id.activity_options_spinner_pois);

        DataCacheManager.getInstance().getBuildingFloors(this,
                new DataCacheManager.GetBuildingFloorsListener() {
                    @Override
                    public void onFinished(final OGFloors floors) {
                        if (floors != null) {
                            floorSpinner.setAdapter(new OptionsSpinnerAdapter(OptionsActivity.this, floors.getData()));
                            floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mGoToFloor = floors.getData()[position];
                                    onFloorSelected(floors.getData()[position]);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    }
                });

        final Switch s = (Switch) findViewById(R.id.activity_options_switch);
        s.setVisibility(View.GONE);
        s.setChecked(IS_TEST_VERSION);
        s.setText(IS_TEST_VERSION ? "測試版" : "正式版");
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IS_TEST_VERSION = !IS_TEST_VERSION;
                s.setText(IS_TEST_VERSION ? "測試版" : "正式版");
//                if (!IS_TEST_VERSION) {
//                    DataCacheManager.getInstance().setUserCurrentFloorLevel("");
//                    DataCacheManager.getInstance().setUserCurrentFloorPlanId("");
//                }
            }
        });

//        findViewById(R.id.activity_options_emergency).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(OptionsActivity.this, OGEmergencyActivity.class));
//            }
//        });
        beaconBatteryLogTV = (TextView) findViewById(R.id.activity_options_battery_log);

        checkLocationService();
    }

    private void checkLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("位置服務尚未開啟，請設定");
            dialog.setPositiveButton("open settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    DialogTools.getInstance().showErrorMessage(OptionsActivity.this,
                            getString(R.string.error_dialog_title_text_normal),
                            "沒有開啟位置服務，無法顯示正確位置");
                }
            });
            dialog.show();
        }
    }

    private void ensurePermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSIONS);

        } else {
            testScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0) {

                boolean shouldRegetPermission = false;

                for (int result : grantResults) {
                    if (result != 0) {
                        shouldRegetPermission = true;
                        break;
                    }
                }

                if (shouldRegetPermission) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                                    Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSIONS);
                } else {
                    testScan();
                }

            }
        }
    }

    private void onFloorSelected(final OGFloor floor) {
        if (floor != null && floor.getPois() != null) {
            poiSpinner.setAdapter(new OptionsSpinnerAdapter(OptionsActivity.this, floor.getPois()));
            poiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    OGPOI poi = floor.getPois()[position];
                    poiIdTIET.setText(poi.getId());
                    poiNameTIET.setText(poi.getName());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(MSG_LE_START_SCAN);
        mHandler.removeMessages(MSG_LE_STOP_SCAN);
        mHandler.sendEmptyMessage(MSG_STOP_SCAN);
        if (mBTAdapter.isEnabled()) {
            mBTAdapter.stopLeScan(this);
        }
        Log.e("@W@", "onDestroy");
        super.onDestroy();
    }

    class OptionsSpinnerAdapter<T> extends BaseAdapter {

        private T[] mList;
        private Context mContext;

        public OptionsSpinnerAdapter(Context context, T[] dataList) {
            mList = dataList;
            mContext = context;
        }

        public void updateData(T[] dataList) {
            mList = dataList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.length;
        }

        @Override
        public Object getItem(int position) {
            return mList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.options_spinner_item_view, null, false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.options_spinner_item_view_tv_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Object object = getItem(position);

            if (object instanceof OGFloor) {
                OGFloor floor = (OGFloor) object;
                viewHolder.titleTextView.setText(floor.getDesc());
            } else if (object instanceof OGPOI) {
                OGPOI poi = (OGPOI) object;
                viewHolder.titleTextView.setText(poi.getName() + ", " + poi.getId());
            }

            return convertView;
        }

        class ViewHolder {
            TextView titleTextView;
        }
    }
}
