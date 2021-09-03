package com.omni.omninavi.api;

import android.content.Context;

import com.omni.omninavi.manager.NetworkManager;
import com.omni.omninavi.model.BeaconSetBatteryResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wiliiamwang on 03/01/2018.
 */

public class BeaconApi {

    private final int TIMEOUT = 25000;

    private static BeaconApi mBeaconApi;

    public static BeaconApi getInstance() {
        if (mBeaconApi == null) {
            mBeaconApi = new BeaconApi();
        }
        return mBeaconApi;
    }

    public void setBeaconBatteryLevel(Context context, String beaconMac, String voltage, NetworkManager.NetworkManagerListener<BeaconSetBatteryResponse> listener) {

        String url = NetworkManager.DOMAIN_NAME + "api/set_beacon";
        Map<String, String> params = new HashMap<>();
        params.put("beacon_mac", beaconMac); /** Required */
        params.put("voltage", voltage); /** Required */

        NetworkManager.getInstance().addPostStringRequest(context, url, params, BeaconSetBatteryResponse.class, TIMEOUT, listener);
    }
}
