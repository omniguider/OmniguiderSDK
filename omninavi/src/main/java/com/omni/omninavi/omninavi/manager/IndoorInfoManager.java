package com.omni.omninavi.omninavi.manager;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.omni.omninavi.omninavi.model.OGBuilding;
import com.omni.omninavi.omninavi.model.OGFloors;
import com.omni.omninavi.omninavi.model.OGNaviRoute;
import com.omni.omninavi.omninavi.model.OGNaviRoutePOI;
import com.omni.omninavi.omninavi.model.SendUserLocationResponse;
import com.omni.omninavi.omninavi.model.UserCurrentLocation;
import com.omni.omninavi.omninavi.model.google_navigation.GoogleNavigationRoute;
import com.omni.omninavi.omninavi.tool.DialogTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiliiamwang on 31/10/2017.
 */

public class IndoorInfoManager {

    private static IndoorInfoManager sIndoorManager;

    private final int TIMEOUT = 15000;

    public static IndoorInfoManager create() {
        if (sIndoorManager == null) {
            sIndoorManager = new IndoorInfoManager();
        }
        return sIndoorManager;
    }

    private IndoorInfoManager() {

    }

    public void sendUserLocation(Context context,
                                 List<UserCurrentLocation> list,
                                 String deviceId,
                                 NetworkManager.NetworkManagerListener<SendUserLocationResponse> listener) {
//        DialogTools.getInstance().showProgress(context);

        String jsonStr = NetworkManager.getInstance().getGson().toJson(list);

        String url = NetworkManager.DOMAIN_NAME + "api/send_user_location";
        Map<String, String> params = new HashMap<>();
        params.put("device_id", deviceId);
        params.put("jsondata", jsonStr);

//        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, SendUserLocationResponse.class, TIMEOUT, listener);
        NetworkManager.getInstance().addPostStringRequest(context, url, params, SendUserLocationResponse.class, TIMEOUT, listener);
    }

    public void getBuildings(Context context, NetworkManager.NetworkManagerListener<OGBuilding[]> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_building";

        NetworkManager.getInstance().addJsonRequestToCommonObj(context, Request.Method.GET, url, null, OGBuilding[].class, NetworkManager.DEFAULT_TIMEOUT, listener);
    }

    public void getFloors(Context context, NetworkManager.NetworkManagerListener<OGFloors> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_floor";
        Map<String, String> params = new HashMap<>();
        params.put("b", "2");

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGFloors.class, NetworkManager.DEFAULT_TIMEOUT, listener);
    }

    public void getIndoorPToPRoute(Context context, String startPOIId, String endPOIId, NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_navi_route";
        Map<String, String> params = new HashMap<>();
        params.put("a", startPOIId);
        params.put("b", endPOIId);

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGNaviRoute.class, TIMEOUT, listener);
    }

    public void getOutdoorToIndoorPRoute(Context context, String pOIId, double userLat, double userLng, String userCurrentFloor, NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_navi_route_xy";
        Map<String, String> params = new HashMap<>();
        params.put("p", pOIId);
        params.put("lat", String.valueOf(userLat));
        params.put("lng", String.valueOf(userLng));
        params.put("f", userCurrentFloor);

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGNaviRoute.class, TIMEOUT, listener);
    }

    public void getIndoorUserLocationToExit(Context context, String currentBuildingId, String userCurrentFloorNumber,
                                            String userLat, String userLng, String exitLat, String exitLng,
                                            NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_navi_xytopoi";
        Map<String, String> params = new HashMap<>();
        params.put("b", currentBuildingId);
        params.put("f", userCurrentFloorNumber);
        params.put("a_lat", userLat);// 25.xxx
        params.put("a_lng", userLng);// 121.xxx
        params.put("b_lat", exitLat);
        params.put("b_lng", exitLng);

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGNaviRoute.class, TIMEOUT, listener);
    }

    public void getIndoorUserLocationToExitWithFloorPlanId(Context context, String poiId,
                                                           double userLat, double userLng, String floorPlanId,
                                                           NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_navi_routexy_planid";
        Map<String, String> params = new HashMap<>();
        params.put("p", poiId);
        params.put("lat", userLat + "");
        params.put("lng", userLng + "");
        params.put("planid", floorPlanId);

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGNaviRoute.class, TIMEOUT, listener);
    }

    public void getUserIndoorLocationToOutdoorPRoute(final Context context,
                                                     final double userLat, final double userLng,
                                                     double outdoorPOILat, double outdoorPOILng,
                                                     final String currentBuildingId, final String userCurrentFloorNumber,
                                                     final NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output;
        Map<String, String> params = new HashMap<>();
        params.put("origin", userLat + "," + userLng);
        params.put("destination", outdoorPOILat + "," + outdoorPOILng);
        params.put("sensor", "false");

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, GoogleNavigationRoute.class, TIMEOUT, new NetworkManager.NetworkManagerListener<GoogleNavigationRoute>() {
            @Override
            public void onSucceed(GoogleNavigationRoute response) {
                if (response.getStatus().equals(GoogleNavigationRoute.GET_ROUTE_STATUS_OK)) {

                    final List<OGNaviRoutePOI> poiList = response.getNavigationRoute().getNavigationalRoutePOIs();
                    if (poiList != null && !poiList.isEmpty()) {
                        OGNaviRoutePOI poi = poiList.get(0);

                        getIndoorUserLocationToExit(context,
                                currentBuildingId, userCurrentFloorNumber,
                                String.valueOf(userLat), String.valueOf(userLng),
                                String.valueOf(poi.getLatitude()), String.valueOf(poi.getLongitude()),
                                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                                    @Override
                                    public void onSucceed(OGNaviRoute navigationalRoute) {
                                        if (navigationalRoute.getResult().equals("true")) {
                                            navigationalRoute.getNavigationalRoutePOIs().addAll(poiList);
                                        }
                                        listener.onSucceed(navigationalRoute);
                                    }

                                    @Override
                                    public void onFail(VolleyError error, boolean shouldRetry) {
                                        listener.onFail(error, shouldRetry);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFail(VolleyError error, boolean shouldRetry) {

            }
        });
    }

    public void getEmergencyRoute(Context context, String buildingId, double userLat, double userLng, String userCurrentFloor, String type, NetworkManager.NetworkManagerListener<OGNaviRoute> listener) {
        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "locapi/get_navi_nearby_type";
        Map<String, String> params = new HashMap<>();
        params.put("b", buildingId);
        params.put("f", userCurrentFloor);
        params.put("lat", String.valueOf(userLat));
        params.put("lng", String.valueOf(userLng));
        params.put("type", type); // Stair, AED, Entrance/Exit, Hydrant

        NetworkManager.getInstance().addJsonRequest(context, Request.Method.GET, url, params, OGNaviRoute.class, TIMEOUT, listener);
    }

}
