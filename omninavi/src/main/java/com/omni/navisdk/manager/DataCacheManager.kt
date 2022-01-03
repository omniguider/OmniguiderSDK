package com.omni.navisdk.manager

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omni.navisdk.NaviSDKActivity
import com.omni.navisdk.module.*
import com.omni.navisdk.network.LocationApi
import com.omni.navisdk.network.NetworkManager.NetworkManagerListener
import com.omni.navisdk.tool.DialogTools
import com.omni.navisdk.tool.NaviSDKText
import com.omni.navisdk.tool.PreferencesTools
import java.util.*

class DataCacheManager {
    private var mGson: Gson? = null
    private var mCurrentShowFloor: OmniFloor? = null
    private var mUserCurrentFloorLevel: String? = null
    private var mUserCurrentFloorPlanId: String? = null
    private var mFloorRoutePointsMap: MutableMap<String, List<LatLng>>? = null
    private var mBuildingClusterItemsMap: MutableMap<String, List<OmniClusterItem<*>>>? = null
    private var mPOIClusterItemMap: MutableMap<String, OmniClusterItem<*>>? = null
    private var mFloorPolylineMap: MutableMap<String, Polyline>? = null
    private var mFloorArrowMarkersMap: MutableMap<String, List<Marker>>? = null
    private var mRoutePointList: MutableList<LatLng>? = null
    private var mZMarkerMap: MutableMap<String?, List<Marker?>>? = null
    private val mZMarkerList: List<Marker>? = null
    private val gson: Gson
        private get() {
            if (mGson == null) {
                mGson = Gson()
            }
            return mGson!!
        }


    private var mContext: Context? = null
    private var pois: List<POI> = ArrayList<POI>()

    fun setPOIs(pois: List<POI>) {
        this.pois = pois
    }

    fun getPOIs(): List<POI>? {
        return pois
    }

    private var mSendUserLocationResponse: SendUserLocationResponse? = null

    fun setmSendUserLocationResponse(data: SendUserLocationResponse?) {
        Log.e("LOG","setmSendUserLocationResponse")
        mSendUserLocationResponse = data
    }

    fun initAllBuildingsData(activity: Activity) {
        LocationApi.instance.getBuildings(
            activity,
            object : NetworkManagerListener<Array<Building>> {
                override fun onSucceed(buildings: Array<Building>?) {
                    setAllBuildings(activity, buildings!!)
                    initBuildingList(activity)
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    Log.e(NaviSDKText.LOG_TAG, "errorMsg$errorMsg")
                    DialogTools.instance.showNoNetworkMessage(activity)
                }
            })
    }

    private fun initBuildingList(activity: Activity) {
        Log.e(NaviSDKText.LOG_TAG, "initBuildingList")
        val buildings = getAllBuildings(activity)

        for (building in buildings!!) {
            if (building.enabled == "Y") {
                LocationApi.instance.getFloors(
                    activity,
                    building.id,
                    object : NetworkManagerListener<Array<BuildingFloor>> {
                        override fun onSucceed(floors: Array<BuildingFloor>?) {
                            setBuildingFloors(activity, building.id!!, floors!!)
                            NaviSDKActivity.floorNum = floors.size
                            NaviSDKActivity.floorNumber = IntArray(floors.size)
                            for (i in floors.indices) {
                                NaviSDKActivity.floorNumber[i] = floors[i].order!!.toInt()
                            }
                            /**mAlreadyGetBuildingFloorsCount++;
                             * Log.e(LOG_TAG, "mAlreadyGetBuildingFloorsCount : " + mAlreadyGetBuildingFloorsCount);
                             * if (mAlreadyGetBuildingFloorsCount == mEnabledBuildingCount) {
                             * DialogTools.getInstance().dismissProgress(activity);
                             * } */
                        }

                        override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                            DialogTools.instance.showNoNetworkMessage(activity)
                        }
                    })
            }
        }
    }

    fun setAllBuildings(context: Context?, allBuildings: Array<Building>?) {
        Log.e(NaviSDKText.LOG_TAG, "setAllBuildings")
        PreferencesTools.instance.saveProperty(
            context,
            PreferencesTools.KEY_ALL_BUILDINGS,
            allBuildings
        )
    }

    fun getAllBuildings(context: Context?): Array<Building>? {
        Log.e(NaviSDKText.LOG_TAG, "getAllBuildings")
        val allBuildingsStr =
            PreferencesTools.instance.getProperty(context, PreferencesTools.KEY_ALL_BUILDINGS)
        return if (allBuildingsStr == null) {
            null
        } else {
            gson.fromJson(allBuildingsStr, Array<Building>::class.java)
        }
    }

    fun getMainGroundFloorPlanId(context: Context?): BuildingFloor? {
        val buildings = getAllBuildings(context)
        if (buildings != null) {
            Log.e(NaviSDKText.LOG_TAG, "getMainGroundFloorPlanId")
            for (building in buildings) {
                val floors = getFloorsByBuildingId(context, building.id)
                if (floors != null) {
                    for (floor in floors) {
                        if (floor.order == "1") {
                            return floor
                        }
                    }
                }
            }
        }
        return null
    }

    fun getSearchFloorPlanId(context: Context?, selectedPoiId: Int): BuildingFloor? {
        val buildings = getAllBuildings(context)
        if (buildings != null) {
            Log.e(NaviSDKText.LOG_TAG, "getSearchFloorPlanId")
            for (building in buildings) {
                val floors = getFloorsByBuildingId(context, building.id)
                if (floors != null) {
                    for (floor in floors) {
                        for (poi in floor.pois!!) {
                            if (poi.id == selectedPoiId) {
                                return floor
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    fun getFloorPlanIdByNumber(context: Context?, floorNumber: Int): String? {
        val buildings = getAllBuildings(context)
        if (buildings != null) {
            Log.e(NaviSDKText.LOG_TAG, "getFloorPlanIdByNumber" + floorNumber)
            for (building in buildings) {
                val floors = getFloorsByBuildingId(context, building.id)
                if (floors != null) {
                    for (floor in floors) {
                        if (floor.order == floorNumber.toString()) {
                            return floor.floorPlanId
                        }
                    }
                }
            }
        }
        return null
    }

    fun getEntrancePOI(floor: BuildingFloor?): POI? {
        Log.e(NaviSDKText.LOG_TAG, "111")
        return if (floor == null) {
            Log.e(NaviSDKText.LOG_TAG, "222")
            null
        } else {
            for (poi in floor.pois!!) {
                if (poi.type!!.contains("Entrance") && poi.isEntrance == "Y") {
                    Log.e(NaviSDKText.LOG_TAG, "poi.getName" + poi.name)
                    return poi
                }
            }
            null
        }
    }

    fun getMainEntrancePOI(context: Context?): POI? {
        val floor = getMainGroundFloorPlanId(context)
        return floor?.let { getEntrancePOI(it) }
    }

    fun setBuildingFloors(context: Context?, buildingId: String, floors: Array<BuildingFloor>) {
        Log.e(NaviSDKText.LOG_TAG, "setBuildingFloors")
        val floorsStr = PreferencesTools.instance.getProperty(context, PreferencesTools.KEY_FLOORS)
        val buildingFloorsMap: MutableMap<String, Array<BuildingFloor>>
        buildingFloorsMap = if (floorsStr == null) {
            HashMap()
        } else {
            val type = object : TypeToken<HashMap<String?, Array<BuildingFloor?>?>?>() {}.type
            gson.fromJson(floorsStr, type)
        }
        buildingFloorsMap[buildingId] = floors
        PreferencesTools.instance.saveProperty(
            context,
            PreferencesTools.KEY_FLOORS,
            gson.toJson(buildingFloorsMap)
        )
    }

    fun getAllBuildingFloorsMap(activity: Activity?): Map<String, Array<BuildingFloor>>? {
        val floorsStr = PreferencesTools.instance.getProperty(activity, PreferencesTools.KEY_FLOORS)
        return if (floorsStr == null) {
            null
        } else {
            val type = object : TypeToken<HashMap<String?, Array<BuildingFloor?>?>?>() {}.type
            gson.fromJson<Map<String, Array<BuildingFloor>>>(floorsStr, type)
        }
    }

    fun getFloorNumberByPlanId(activity: Activity?, floorPlanId: String): String {
        val allBuildingFloorsMap = getAllBuildingFloorsMap(activity)
        if (allBuildingFloorsMap != null) {
            for (buildingId in allBuildingFloorsMap.keys) {
                val floors = allBuildingFloorsMap[buildingId]!!
                for (floor in floors) {
                    if (floor.floorPlanId == floorPlanId) {
                        return floor.order!!
                    }
                }
            }
        }
        return ""
    }

    fun containsFloor(activity: Activity?, floorPlanId: String): Boolean {
        val allBuildingFloorsMap = getAllBuildingFloorsMap(activity)
        if (allBuildingFloorsMap != null) {
            for (floors in allBuildingFloorsMap.values) {
                for (floor in floors) {
                    if (floor.floorPlanId == floorPlanId) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getBuildingIdByFloorPlanId(activity: Activity?, floorPlanId: String): String? {
        val buildingId: String? = null
        val buildingFloorsMap = getAllBuildingFloorsMap(activity)
        if (buildingFloorsMap != null) {
            for (blId in buildingFloorsMap.keys) {
                if (!TextUtils.isEmpty(buildingId)) {
                    break
                }
                val floors = buildingFloorsMap[blId]!!
                for (floor in floors) {
                    if (floor.floorPlanId == floorPlanId) {
                        return blId
                    }
                }
            }
        }
        return buildingId
    }

    fun getBuildingByFloorPlanId(activity: Activity?, floorPlanId: String): Building? {
        val buildingId = getBuildingIdByFloorPlanId(activity, floorPlanId)
        if (buildingId != null) {
            val buildings = getAllBuildings(activity)
            if (buildings != null) {
                for (building in buildings) {
                    if (building.id == buildingId) {
                        return building
                    }
                }
            }
        }
        return null
    }

    fun getFloorsByBuildingId(activity: Context?, buildingId: String?): Array<BuildingFloor>? {
        val floorsStr = PreferencesTools.instance.getProperty(activity, PreferencesTools.KEY_FLOORS)
        //        Log.e(LOG_TAG, "floorsStr" + floorsStr);
        return if (floorsStr == null) {
            null
        } else {
            val type = object : TypeToken<HashMap<String?, Array<BuildingFloor?>?>?>() {}.type
            val buildingFloorsMap =
                gson.fromJson<Map<String, Array<BuildingFloor>>>(floorsStr, type)
            buildingFloorsMap[buildingId]
        }
    }

    fun getBuildingFloor(
        activity: Activity?,
        buildingId: String?,
        floorPlanId: String
    ): BuildingFloor? {
        val floors = getFloorsByBuildingId(activity, buildingId)
        var floor: BuildingFloor? = null
        if (floors != null) {
            for (f in floors) {
                if (f.floorPlanId == floorPlanId) {
                    floor = f
                    break
                }
            }
        }
        return floor
    }

    var currentShowFloor: OmniFloor?
        get() {
            if (mCurrentShowFloor == null) {
                mCurrentShowFloor = OmniFloor(NaviSDKText.USER_OUTDOOR, NaviSDKText.USER_OUTDOOR)
            }
            return mCurrentShowFloor
        }
        set(floor) {
            mCurrentShowFloor = floor
        }

    fun getUserCurrentBuildingId(activity: Activity?): String? {
        val omniFloor = currentShowFloor
        return if (omniFloor == null) null else getBuildingIdByFloorPlanId(
            activity,
            omniFloor.floorPlanId!!
        )
    }

    fun getUserCurrentFloorLevel(activity: Activity?): String {
        val userCurrentFloorPlanId = userCurrentFloorPlanId!!
        val buildingId = getBuildingIdByFloorPlanId(activity, userCurrentFloorPlanId)
        if (!TextUtils.isEmpty(buildingId)) {
            val floor = getBuildingFloor(activity, buildingId, userCurrentFloorPlanId)
            return floor!!.order!!
        }
        return "1"
    }

    fun setUserCurrentFloorLevel(userCurrentFloorLevel: String?) {
        Log.e(NaviSDKText.LOG_TAG, "setUserCurrentFloorLevel")
        mUserCurrentFloorLevel = userCurrentFloorLevel
    }

    var userCurrentFloorPlanId: String?
        get() {
            if (TextUtils.isEmpty(mUserCurrentFloorPlanId)) {
                mUserCurrentFloorPlanId = NaviSDKText.USER_OUTDOOR
            }
            if (mUserCurrentFloorPlanId != null && mUserCurrentFloorPlanId == "919f0ac4-62e4-48ae-8217-dcb707bbcdc9") {
                mUserCurrentFloorPlanId = NaviSDKText.USER_OUTDOOR
            }
            Log.e(NaviSDKText.LOG_TAG, "getUserCurrentFloorPlanId$mUserCurrentFloorPlanId")
            return mUserCurrentFloorPlanId
        }
        set(planId) {
            mUserCurrentFloorPlanId = planId
        }

    fun setFloorRoutePointsMap(floorNumber: String, routePointList: List<LatLng>?) {
        if (mFloorRoutePointsMap == null) {
            mFloorRoutePointsMap = HashMap()
        }
        val list: List<LatLng> = ArrayList(routePointList!!)
        mFloorRoutePointsMap!![floorNumber] = list
    }

    fun getFloorRoutePointList(floorNumber: String?): List<LatLng>? {
        return if (mFloorRoutePointsMap == null) null else mFloorRoutePointsMap!![floorNumber]
    }

    val floorRoutePointsMap: MutableMap<String, List<LatLng>>?
        get() = mFloorRoutePointsMap

    fun getUserCurrentFloorRoutePointList(activity: Activity?): List<LatLng>? {
        return if (mFloorRoutePointsMap == null) null else mFloorRoutePointsMap!![getUserCurrentFloorLevel(
            activity
        )]
    }

    fun setBuildingClusterItems(buildingId: String, itemList: MutableList<OmniClusterItem<*>>) {
        Log.e(NaviSDKText.LOG_TAG, "setBuildingClusterItems")
        if (mBuildingClusterItemsMap == null) {
            mBuildingClusterItemsMap = HashMap()
        }
        mBuildingClusterItemsMap!![buildingId] = itemList
    }

    fun getClusterListByBuildingId(buildingId: String?): MutableList<OmniClusterItem<*>>? {
        Log.e(NaviSDKText.LOG_TAG, "getClusterListByBuildingId")
        return if (mBuildingClusterItemsMap == null) null else mBuildingClusterItemsMap!![buildingId] as MutableList<OmniClusterItem<*>>?
    }

    fun setPOIClusterItemMap(poiId: String, item: OmniClusterItem<*>) {
        if (mPOIClusterItemMap == null) {
            mPOIClusterItemMap = HashMap()
        }
        mPOIClusterItemMap!![poiId] = item
    }

    fun getClusterItemByPOIId(poiId: String?): OmniClusterItem<*>? {
        return if (mPOIClusterItemMap == null) null else mPOIClusterItemMap!![poiId]
    }

    val floorPolylineMap: MutableMap<String, Polyline>
        get() {
            if (mFloorPolylineMap == null) {
                mFloorPolylineMap = HashMap()
            }
            return mFloorPolylineMap!!
        }

    val currentRoutePointList: List<LatLng>?
        get() {
            if (mRoutePointList == null) {
                mRoutePointList = ArrayList()
            }
            return mRoutePointList
        }

    fun setCurrentRoutePointList(currentRoutePointList: MutableList<LatLng>?) {
        mRoutePointList = currentRoutePointList
    }

    fun clearAllPolyline() {
        Log.e(
            NaviSDKText.LOG_TAG,
            "clearAllPolyline mFloorPolylineMap == null ? " + (mFloorPolylineMap == null)
        )
        if (mFloorPolylineMap != null) {
            for (polyline in mFloorPolylineMap!!.values) {
                polyline.remove()
            }
            mFloorPolylineMap!!.clear()
        }
    }

    fun clearAllArrowMarkers() {
        if (mRoutePointList != null) {
            mRoutePointList!!.clear()
        }
        if (mFloorArrowMarkersMap != null) {
            for (list in mFloorArrowMarkersMap!!.values) {
                for (marker in list) {
                    marker.remove()
                }
            }
            mFloorArrowMarkersMap!!.clear()
        }
        if (mFloorRoutePointsMap != null) {
            mFloorRoutePointsMap!!.clear()
        }
    }

    fun getFloorArrowMarkerList(floorNumber: String?): List<Marker>? {
        return if (mFloorArrowMarkersMap == null) null else mFloorArrowMarkersMap!![floorNumber]
    }

    val currentFloorArrowMarkerList: List<Marker>?
        get() = if (mFloorArrowMarkersMap == null) null else mFloorArrowMarkersMap!![mUserCurrentFloorLevel]

    val floorArrowMarkersMap: Map<String, List<Marker>>
        get() {
            if (mFloorArrowMarkersMap == null) {
                mFloorArrowMarkersMap = HashMap()
            }
            return mFloorArrowMarkersMap!!
        }

    fun setFloorArrowMarkersMap(floorNumber: String, markerList: List<Marker>) {
        if (mFloorArrowMarkersMap == null) {
            mFloorArrowMarkersMap = HashMap()
        }
        mFloorArrowMarkersMap!![floorNumber] = markerList
    }

    fun isInBuilding(activity: Activity?): Boolean {
        val planId = userCurrentFloorPlanId!!
        return if (TextUtils.isEmpty(planId)) {
            false
        } else {
            val buildingId = getBuildingIdByFloorPlanId(activity, planId)
            !TextUtils.isEmpty(buildingId)
        }
    }

    fun isInSameBuilding(activity: Activity?, compareBuildingId: String): Boolean {
        val planId = userCurrentFloorPlanId!!
        return if (TextUtils.isEmpty(planId)) {
            false
        } else {
            val userCurrentBuildingId = getBuildingIdByFloorPlanId(activity, planId)
            !TextUtils.isEmpty(userCurrentBuildingId) && userCurrentBuildingId == compareBuildingId
        }
    }

    fun getZMarkerListByBuildingId(buildingId: String?): MutableList<Marker?>? {
        return if (mZMarkerMap == null) null else mZMarkerMap!![buildingId] as MutableList<Marker?>?
    }

    fun setZMarkerByBuildingId(buildingId: String?, marker: Marker?) {
        var list = getZMarkerListByBuildingId(buildingId)
        if (list == null) {
            list = ArrayList()
        }
        list.add(marker)
        if (mZMarkerMap == null) {
            mZMarkerMap = ArrayMap()
        }
        mZMarkerMap!![buildingId] = list
    }

    fun setZMarkerListByBuildingId(buildingId: String?, list: List<Marker?>) {
        if (mZMarkerMap == null) {
            mZMarkerMap = ArrayMap()
        }
        mZMarkerMap!![buildingId] = list
    }

    fun setSearchRecord(context: Context?, record: String) {
        Log.e(NaviSDKText.LOG_TAG, "setSearchRecord")
        var recordStr =
            PreferencesTools.instance.getPropertyString(context, PreferencesTools.KEY_SEARCH_RECORD)
        recordStr = if (recordStr!!.isEmpty()) {
            record
        } else {
            "$recordStr,$record"
        }
        PreferencesTools.instance.saveProperty(
            context,
            PreferencesTools.KEY_SEARCH_RECORD,
            recordStr
        )
    }

    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getNotificationManager(): NotificationManager? {
        return mNotificationManager
    }

    fun initNotificationManager(context: Context) {
        if (mNotificationManager == null) mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getInstance(context: Context): DataCacheManager? {
        mContext = context
        if (mDataCacheManager == null) {
            mDataCacheManager = DataCacheManager()
        }
        mDataCacheManager!!.initNotificationManager(context)
        return mDataCacheManager
    }

    fun getmSendUserLocationResponse(): SendUserLocationResponse? {
        return mSendUserLocationResponse
    }

    companion object {
        private var mDataCacheManager: DataCacheManager? = null
        private var mNotificationManager: NotificationManager? = null

        @JvmStatic
        val instance: DataCacheManager
            get() {
                if (mDataCacheManager == null) {
                    mDataCacheManager = DataCacheManager()
                }
                return mDataCacheManager!!
            }
    }
}