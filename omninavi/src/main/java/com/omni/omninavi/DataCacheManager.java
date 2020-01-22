package com.omni.omninavi;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;
import com.omni.omninavi.manager.IndoorInfoManager;
import com.omni.omninavi.manager.NetworkManager;
import com.omni.omninavi.model.OGBuilding;
import com.omni.omninavi.model.OGFloor;
import com.omni.omninavi.model.OGFloors;
import com.omni.omninavi.tool.DialogTools;
import com.omni.omninavi.view.OmniClusterItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiliiamwang on 24/07/2017.
 */

public class DataCacheManager {

    public static final String TAIPEI_CITY_GOV_BUILDING_ID = "2";

    interface GetBuildingFloorsListener {
        void onFinished(OGFloors floors);
    }

    interface GetBuildingFloorListener {
        void onFinished(OGFloor floor);
    }

    interface CheckUserInTaipeiCityHallCallback {
        void onFinished(boolean isInTaipeiCityHall);
    }

    private static DataCacheManager mDataCacheManager;

    public static final String USER_OUTDOOR = "outdoor";

    private OGBuilding[] mOGBuildings;

    // key : buildingId, value : buildingFloors
    private Map<String, OGFloors> mBuildingFloorsMap;
    private List<OGFloor> mAllBuildingFloorList;
    // key : floorPlanId, value : buildingId
    private Map<String, String> mFloorPlanIdBuildingIdMap;
    // key : buildingId, value : (the Map that key : floorPlanId, value : marker on this floor list)
    private Map<String, Map<String, List<Marker>>> mBuildingFloorsMarkersMap;
    // key : floorNumber, value : the polyline of this floor
    private Map<String, Polyline> mFloorPolylineMap;
    private List<LatLng> mRoutePointList;
    // key : floorNumber, value : route point list on this floor
    private Map<String, List<LatLng>> mFloorRoutePointsMap;
    // key : floorNumber, value : arrow markers on this floor
    private Map<String, List<Marker>> mFloorArrowMarkersMap;
    private OmniFloor mCurrentShowFloor;
    private OmniFloor mUserCurrentFloor;
    private String mUserCurrentFloorLevel;
    private String mUserCurrentFloorPlanId;
    // key : buildingId, value : clusters in this building
    private Map<String, List<OmniClusterItem>> mBuildingClusterItemsMap;
    // key : POI id, value : cluster item
    private Map<String, OmniClusterItem> mPOIClusterItemMap;

    private OmniFloor mGuideCurrentShowFloor;

    private Gson mGson;

    public static DataCacheManager getInstance() {
        if (mDataCacheManager == null) {
            mDataCacheManager = new DataCacheManager();
        }
        return mDataCacheManager;
    }

    private Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    //    public AllBuildings getAllBuildings(Activity activity) {
//        String allBuildingsStr = PreferencesTools.getInstance().getProperty(activity, PreferencesTools.KEY_ALL_BUILDINGS);
//        if (allBuildingsStr == null) {
//            return null;
//        } else {
//            return getGson().fromJson(allBuildingsStr, AllBuildings.class);
//        }
//    }
//
    public void setAllBuildings(Activity activity, OGBuilding[] ogBuildings) {
        mOGBuildings = ogBuildings;
    }

    public void setBuildingFloors(Activity activity, String buildingId, OGFloors floors) {
        if (mBuildingFloorsMap == null) {
            mBuildingFloorsMap = new HashMap<>();
        }
        if (!mBuildingFloorsMap.containsKey(buildingId)) {
//            mIsNewBuildingAdded = true;
            mBuildingFloorsMap.put(buildingId, floors);

            if (mFloorPlanIdBuildingIdMap == null) {
                mFloorPlanIdBuildingIdMap = new HashMap<>();
            }
            for (OGFloor floor : floors.getData()) {
                mFloorPlanIdBuildingIdMap.put(floor.getFloorPlanId(), buildingId);
            }
        }

//        String floorsStr = PreferencesTools.getInstance().getProperty(activity, PreferencesTools.KEY_FLOORS);
//        Map<String, BuildingFloors> buildingFloorsMap;
//        if (floorsStr == null) {
//            buildingFloorsMap = new HashMap<>();
//        } else {
//            Type type = new TypeToken<HashMap<String, BuildingFloors>>() {
//            }.getType();
//            buildingFloorsMap = getGson().fromJson(floorsStr, type);
//        }
//        /** Don't save floor and poi data. 23/10/2017 17:52
//        if ((!buildingFloorsMap.containsKey(buildingId)) || buildingFloorsMap.get(buildingId) == null) {*/
//        buildingFloorsMap.put(buildingId, floors);
//
//        PreferencesTools.getInstance().saveProperty(activity, PreferencesTools.KEY_FLOORS, getGson().toJson(buildingFloorsMap));
////        }
    }

//    public Map<String, OGFloors> getAllBuildingFloorsMap(Activity activity) {
//        String floorsStr = PreferencesTools.getInstance().getProperty(activity, PreferencesTools.KEY_FLOORS);
//        if (floorsStr == null) {
//            return null;
//        } else {
//            Type type = new TypeToken<HashMap<String, BuildingFloors>>() {
//            }.getType();
//            Map<String, BuildingFloors> buildingFloorsMap = getGson().fromJson(floorsStr, type);
//            return buildingFloorsMap;
//        }
//    }

    public void getBuildingFloors(Activity activity, GetBuildingFloorsListener listener) {
        getBuildingFloors(activity, TAIPEI_CITY_GOV_BUILDING_ID, listener);
    }

    @Nullable
    public void getBuildingFloors(final Activity activity,
                                  final String buildingId,
                                  final GetBuildingFloorsListener listener) {
        if (mBuildingFloorsMap == null) {
            IndoorInfoManager.create().getFloors(activity, new NetworkManager.NetworkManagerListener<OGFloors>() {
                @Override
                public void onSucceed(OGFloors ogFloors) {
                    DataCacheManager.this.setBuildingFloors(activity, buildingId, ogFloors);
                    listener.onFinished(ogFloors);
                }

                @Override
                public void onFail(VolleyError volleyError, boolean b) {
                    DialogTools.getInstance().showErrorMessage(activity, "API Error", "get building floors error");
                    listener.onFinished(null);
                }
            });
        } else {
            listener.onFinished(mBuildingFloorsMap.get(buildingId));
        }
    }

//    private List<String> mAllFloorPlanIdList = new ArrayList<>();

//    public List<String> getAllFloorPlanIdList() {
//        if (mBuildingFloorsMap != null) {
//            for (String key : mBuildingFloorsMap.keySet()) {
//                OGFloors floors = mBuildingFloorsMap.get(key);
//                for (OGFloor floor : floors.getData()) {
//                    if (!mAllFloorPlanIdList.contains(floor.getId())) {
//                        mAllFloorPlanIdList.add(floor.getId());
//                    }
//                }
//            }
//        }
//
//        return mAllFloorPlanIdList;
//    }

    public void getBuildingFloor(Activity activity,
                                 String buildingId,
                                 final String floorPlanId,
                                 final GetBuildingFloorListener listener) {
        getBuildingFloors(activity, buildingId, new GetBuildingFloorsListener() {
            @Override
            public void onFinished(OGFloors floors) {
                OGFloor floor = null;
                if (floors != null) {
                    for (OGFloor f : floors.getData()) {
                        if (f.getFloorPlanId().equals(floorPlanId)) {
                            floor = f;
                            break;
                        }
                    }
                }

                listener.onFinished(floor);
            }
        });
    }

    public void getBuildingDefaultFloor(Activity activity, final GetBuildingFloorListener listener) {
        getBuildingFloors(activity, new GetBuildingFloorsListener() {
            @Override
            public void onFinished(OGFloors floors) {
                OGFloor defaultFloor = null;
                if (floors != null) {
                    for (OGFloor f : floors.getData()) {
                        if (f.getIsMap()) {
                            defaultFloor = f;
                        }
                    }
                }

                listener.onFinished(defaultFloor);
            }
        });
    }

    public void isUserInTaipeiCityHall(Activity activity, final LatLng userLocation, final CheckUserInTaipeiCityHallCallback callback) {
        getBuildingDefaultFloor(activity, new GetBuildingFloorListener() {
            @Override
            public void onFinished(OGFloor floor) {
                boolean isUserInCityHall = false;
                if (floor != null) {
                    LatLngBounds bounds = new LatLngBounds(new LatLng(floor.getBlLatitude(), floor.getBlLongitude()),
                            new LatLng(floor.getTrLatitude(), floor.getTrLongitude()));

                    isUserInCityHall = bounds.contains(userLocation);
                }

                callback.onFinished(isUserInCityHall);
            }
        });
    }

//    @Nullable
//    public BuildingFloor getBuildingFloorByFloorPlanId(Activity activity, String floorPlanId) {
//        return getBuildingFloor(activity, getBuildingIdByFloorPlanId(activity, floorPlanId), floorPlanId);
//    }
//
//    @Nullable
//    public BuildingFloor getBuildingFirstFloorByFloorPlanId(Activity activity, String floorPlanId) {
//        BuildingFloors floors = getBuildingFloors(activity, getBuildingIdByFloorPlanId(activity, floorPlanId));
//        BuildingFloor floor = null;
//        if (floors != null) {
//            floor = floors.getGroundFloor();
//        }
//        return floor;
//    }
//
//    public List<OGFloor> getAllBuildingFloorList() {
//        if (mAllBuildingFloorList == null) {
//            mAllBuildingFloorList = new ArrayList<>();
//        }
//        if (mAllBuildingFloorList.isEmpty()) {
//            if (mBuildingFloorsMap != null) {
//                for (String buildingId : mBuildingFloorsMap.keySet()) {
//                    OGFloors floors = mBuildingFloorsMap.get(buildingId);
//                    mAllBuildingFloorList.addAll(Arrays.asList(floors.getData()));
//                }
//            }
//        }
//        return mAllBuildingFloorList;
//    }

    public String getBuildingIdByFloorPlanId(String floorPlanId) {
        String buildingId = null;

        Map<String, OGFloors> buildingFloorsMap = mBuildingFloorsMap;

        if (buildingFloorsMap != null) {
            for (String blId : buildingFloorsMap.keySet()) {
                if (!TextUtils.isEmpty(buildingId)) {
                    break;
                }
                OGFloors floors = buildingFloorsMap.get(blId);
                for (OGFloor floor : floors.getData()) {
                    if (floor.getFloorPlanId().equals(floorPlanId)) {
                        buildingId = blId;
                        break;
                    }
                }
            }
        }
        return buildingId;
    }

//    public void setBuildingMarkers(String buildingId, List<Marker> list) {
//        if (mBuildingMarkersMap == null) {
//            mBuildingMarkersMap = new HashMap<>();
//        }
//        mBuildingMarkersMap.put(buildingId, list);
//    }
//
//    @Nullable
//    public List<Marker> getMarkerListByBuildingId(String buildingId) {
//        return mBuildingMarkersMap == null ? null : mBuildingMarkersMap.get(buildingId);
//    }

    public void setBuildingClusterItems(String buildingId, List<OmniClusterItem> itemList) {
        if (mBuildingClusterItemsMap == null) {
            mBuildingClusterItemsMap = new HashMap<>();
        }
        mBuildingClusterItemsMap.put(buildingId, itemList);
    }

    @Nullable
    public List<OmniClusterItem> getClusterListByBuildingId(String buildingId) {
        return mBuildingClusterItemsMap == null ? null : mBuildingClusterItemsMap.get(buildingId);
    }

//    @Nullable
//    public List<OmniClusterItem> getCurrentShowClusterList(Activity activity) {
//        String buildingId = getUserCurrentBuildingId(activity);
//        if (buildingId == null) {
//            return null;
//        } else {
//            return getClusterListByBuildingId(buildingId);
//        }
//    }
//
//    @Nullable
//    public List<OmniClusterItem> getCurrentShowExhibitClusterList(Activity activity) {
//        List<OmniClusterItem> list = getCurrentShowClusterList(activity);
//        if (list == null) {
//            return null;
//        } else {
//            List<OmniClusterItem> exhibitClusterList = new ArrayList<>();
//            for (OmniClusterItem item : list) {
//                if (item.getPOI().isExhibitPOI()) {
//                    exhibitClusterList.add(item);
//                }
//            }
//
//            return exhibitClusterList;
//        }
//    }
//
//    @Nullable
//    public List<OmniClusterItem> getCurrentShowUtilityClusterList(Activity activity) {
//        List<OmniClusterItem> list = getCurrentShowClusterList(activity);
//        if (list == null) {
//            return null;
//        } else {
//            List<OmniClusterItem> utilityClusterList = new ArrayList<>();
//            for (OmniClusterItem item : list) {
//                if (item.getPOI().isUtilityPOI()) {
//                    utilityClusterList.add(item);
//                }
//            }
//
//            return utilityClusterList;
//        }
//    }
//
//    @Nullable
//    public List<OmniClusterItem> getCurrentShowEmergencyClusterList(Activity activity) {
//        List<OmniClusterItem> list = getCurrentShowClusterList(activity);
//        if (list == null) {
//            return null;
//        } else {
//            List<OmniClusterItem> emergencyClusterList = new ArrayList<>();
//            for (OmniClusterItem item : list) {
//                if (item.getPOI().isEmergencyPOI()) {
//                    emergencyClusterList.add(item);
//                }
//            }
//
//            return emergencyClusterList;
//        }
//    }

    public void setPOIClusterItemMap(String poiId, OmniClusterItem item) {
        if (mPOIClusterItemMap == null) {
            mPOIClusterItemMap = new HashMap<>();
        }
        mPOIClusterItemMap.put(poiId, item);
    }

//    @Nullable
//    public OmniClusterItem getClusterItemByPOIId(String poiId) {
//        return mPOIClusterItemMap == null ? null : mPOIClusterItemMap.get(poiId);
//    }

//    public boolean isInSameBuilding(Activity activity, String buildingId, String floorPlanId) {
//        return buildingId.equals(getBuildingIdByFloorPlanId(activity, floorPlanId));
//    }

//    public void setBuildingFloorsMarkerMapMap(String buildingId, String floorPlanId, List<Marker> list) {
//        if (mBuildingFloorsMarkersMap == null) {
//            mBuildingFloorsMarkersMap = new HashMap<>();
//        }
//
//        Map<String, List<Marker>> map;
//        if (mBuildingFloorsMarkersMap.containsKey(buildingId)) {
//            map = mBuildingFloorsMarkersMap.get(buildingId);
//        } else {
//            map = new HashMap<>();
//        }
//
//        map.put(floorPlanId, list);
//        mBuildingFloorsMarkersMap.put(buildingId, map);
//    }
//
//    @Nullable
//    public List<Marker> getMarkerListByFloorPlanId(String floorPlanId) {
//        List<Marker> list = null;
//        if (mBuildingFloorsMarkersMap != null) {
//            for (Map<String, List<Marker>> map : mBuildingFloorsMarkersMap.values()) {
//                if (map.containsKey(floorPlanId)) {
//                    list = map.get(floorPlanId);
//                    break;
//                }
//            }
//        }
//        return list;
//    }
//
//    @Nullable
//    public List<Marker> getSameBuildingMarkListByBuildingId(String buildingId) {
//        List<Marker> list = null;
//        if (mBuildingFloorsMarkerMap != null && mBuildingFloorsMarkerMap.containsKey(buildingId)) {
//            Map<String, List<Marker>> map = mBuildingFloorsMarkerMap.get(buildingId);
//            for (List<Marker> markerList : map.values()) {
//                list.addAll(markerList);
//            }
//        }
//        return list;
//    }
//
//    @Nullable
//    public List<Marker> getSameBuildingMarkListByFloorPlanId(String floorPlanId) {
//        String buildingId = getBuildingIdByFloorPlanId(floorPlanId);
//        return getSameBuildingMarkListByBuildingId(buildingId);
//    }

    @NonNull
    public Map<String, Polyline> getFloorPolylineMap() {
        if (mFloorPolylineMap == null) {
            mFloorPolylineMap = new HashMap<>();
        }
        return mFloorPolylineMap;
    }

    public void removePolylineByFloorNumber(String floorNumber) {
        if (mFloorPolylineMap != null) {
            mFloorPolylineMap.get(floorNumber).remove();
        }
    }

    @Nullable
    public List<LatLng> getCurrentRoutePointList() {
        if (mRoutePointList == null) {
            mRoutePointList = new ArrayList<>();
        }
        return mRoutePointList;
    }

    public void setCurrentRoutePointList(List<LatLng> currentRoutePointList) {
        mRoutePointList = currentRoutePointList;
    }

    public void setFloorRoutePointsMap(String floorNumber, List<LatLng> routePointList) {
        if (mFloorRoutePointsMap == null) {
            mFloorRoutePointsMap = new HashMap<>();
        }
        List<LatLng> list = new ArrayList<>(routePointList);
        mFloorRoutePointsMap.put(floorNumber, list);
    }

    @Nullable
    public List<LatLng> getFloorRoutePointList(String floorNumber) {
        return mFloorRoutePointsMap == null ? null : mFloorRoutePointsMap.get(floorNumber);
    }

    @Nullable
    public Map<String, List<LatLng>> getFloorRoutePointsMap() {
        return mFloorRoutePointsMap;
    }

    @Nullable
    public List<LatLng> getUserCurrentFloorRoutePointList() {
        return mFloorRoutePointsMap == null ? null : mFloorRoutePointsMap.get(mUserCurrentFloorLevel);
    }

    @Nullable
    public List<Marker> getFloorArrowMarkerList(String floorNumber) {
        return mFloorArrowMarkersMap == null ? null : mFloorArrowMarkersMap.get(floorNumber);
    }

    @Nullable
    public List<Marker> getCurrentFloorArrowMarkerList() {
        return mFloorArrowMarkersMap == null ? null : mFloorArrowMarkersMap.get(mUserCurrentFloorLevel);
    }

    public Map<String, List<Marker>> getFloorArrowMarkersMap() {
        if (mFloorArrowMarkersMap == null) {
            mFloorArrowMarkersMap = new HashMap<>();
        }
        return mFloorArrowMarkersMap;
    }

    public void setFloorArrowMarkersMap(String floorNumber, List<Marker> markerList) {
        if (mFloorArrowMarkersMap == null) {
            mFloorArrowMarkersMap = new HashMap<>();
        }
        mFloorArrowMarkersMap.put(floorNumber, markerList);
    }

    public void clearAllPolyline() {
        if (mFloorPolylineMap != null) {
            for (Polyline polyline : mFloorPolylineMap.values()) {
                polyline.remove();
            }
            mFloorPolylineMap.clear();
        }
    }

    public void clearAllArrowMarkers() {
        if (mRoutePointList != null) {
            mRoutePointList.clear();
        }
        if (mFloorArrowMarkersMap != null) {
            for (List<Marker> list : mFloorArrowMarkersMap.values()) {
                for (Marker marker : list) {
                    marker.remove();
                }
            }
            mFloorArrowMarkersMap.clear();
        }
        if (mFloorRoutePointsMap != null) {
            mFloorRoutePointsMap.clear();
        }
    }

    public void setCurrentShowFloor(OmniFloor floor) {
        mCurrentShowFloor = floor;
    }

    @NonNull
    public OmniFloor getCurrentShowFloor() {
        if (mCurrentShowFloor == null) {
            mCurrentShowFloor = new OmniFloor(USER_OUTDOOR, USER_OUTDOOR);
        }
        return mCurrentShowFloor;
    }

    public void setUserCurrentFloor(OmniFloor floor) {
        mUserCurrentFloor = floor;
    }

    @NonNull
    public OmniFloor getUserCurrentFloor() {
        if (mUserCurrentFloor == null) {
            mUserCurrentFloor = new OmniFloor(USER_OUTDOOR, USER_OUTDOOR);
        }
        return mUserCurrentFloor;
    }

    @NonNull
    public String getUserCurrentFloorLevel() {
        return (mUserCurrentFloorLevel == null) ? "1" : mUserCurrentFloorLevel;
    }

    public void setUserCurrentFloorLevel(String userCurrentFloorLevel) {
        mUserCurrentFloorLevel = userCurrentFloorLevel;
    }

    public void setUserCurrentFloorPlanId(String planId) {
        mUserCurrentFloorPlanId = planId;
    }

    @NonNull
    public String getUserCurrentFloorPlanId() {
        if (TextUtils.isEmpty(mUserCurrentFloorPlanId)) {
            mUserCurrentFloorPlanId = USER_OUTDOOR;
        }
        return mUserCurrentFloorPlanId;
    }

    public String getUserCurrentBuildingId() {
        OmniFloor omniFloor = getCurrentShowFloor();
        return omniFloor == null ? null :
                getBuildingIdByFloorPlanId(omniFloor.getFloorPlanId());
    }

    private boolean mIsLocatedNaviFragmentCreated = false;

    public boolean isLocatedNaviFragmentCreated() {
        return mIsLocatedNaviFragmentCreated;
    }

    public void setLocatedNaviFragmentCreated(boolean mIsLocatedNaviFragmentCreated) {
        this.mIsLocatedNaviFragmentCreated = mIsLocatedNaviFragmentCreated;
    }

//    public NMPNews[] getNMPNewsArr() {
//        return mNMPNewsArr;
//    }
//
//    public void setNMPNewsArr(NMPNews[] nMPNewsArr) {
//        mNMPNewsArr = nMPNewsArr;
//    }
//
//    @Nullable
//    public BuildingFloors getNMPAllFloors(Activity activity) {
//        return getBuildingFloors(activity, "6");
//    }


    /**
     * Guide part
     */
    public OmniFloor getGuideCurrentShowFloor() {
//        if (mGuideCurrentShowFloor == null) {
//            mGuideCurrentShowFloor = new OmniFloor("1", PrehistoryText.PREHISTORY_1F_FLOOR_PLAN_ID);
//        }

        return mGuideCurrentShowFloor;
    }

    public void setGuideCurrentShowFloor(OmniFloor guideCurrentShowFloor) {
        this.mGuideCurrentShowFloor = guideCurrentShowFloor;
    }
}
