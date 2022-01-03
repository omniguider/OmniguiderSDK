package com.omni.navisdk.network

import android.app.Activity
import android.util.Log
import com.omni.navisdk.module.*
import com.omni.navisdk.module.google_navigation.GoogleNavigationRoute
import com.omni.navisdk.network.NetworkManager.NetworkManagerListener
import com.omni.navisdk.tool.DialogTools
import com.omni.navisdk.tool.NaviSDKText
import retrofit2.Call
import retrofit2.http.*

class LocationApi {
    internal interface LocationService {
        @get:GET("locapi/get_building")
        val buildings: Call<CommonArrayResponse>

        @GET("locapi/get_floor")
        fun getFloors(@Query("b") buildingId: String?): Call<CommonArrayResponse>

        @GET("locapi/get_floor")
        fun getFloors(
            @Query("b") buildingId: String?,
            @Query("keyword") keyword: String?,
            @Query("lat") lat: Double,
            @Query("lng") lng: Double
        ): Call<CommonArrayResponse>

        @GET("locapi/get_navi_route_xy")
        fun getUserLocationToIndoorPRoute(
            @Query("p") poiId: String?,
            @Query("lat") lat: Double,
            @Query("lng") lng: Double,
            @Query("priority") priority: String?,
            @Query("f") userCurrentFloorLevel: String?,
            @Query("platform") platform: String?
        ): Call<CommonArrayResponse>

        @GET("locapi/get_navi_xytopoi")
        fun getUserLocationToExit(
            @Query("b") userCurrentBuildingId: String?,
            @Query("f") userCurrentFloorLevel: String?,
            @Query("a_lat") userLat: Double,
            @Query("a_lng") userLng: Double,
            @Query("b_lat") exitLat: String?,
            @Query("b_lng") exitLng: String?,
            @Query("priority") priority: String?,
            @Query("platform") platform: String?
        ): Call<CommonArrayResponse>

        @GET("locapi/get_navi_nearby_type")
        fun getEmergencyRoute(
            @Query("b") userCurrentBuildingId: String?,
            @Query("f") userCurrentFloorLevel: String?,
            @Query("lat") lat: Double,
            @Query("lng") lng: Double,
            @Query("type") type: String?,
            @Query("platform") platform: String?
        ): Call<CommonArrayResponse>

        @GET("locapi/get_app_category")
        fun getAppCategory(): Call<CommonArrayResponse>

        @GET("locapi/ap_category")
        fun getHotSearch(): Call<CommonArrayResponse>

        @GET("api/get_keyword")
        fun getKeyword(): Call<Array<KeywordData>>

        @FormUrlEncoded
        @POST("api/set_beacon")
        fun setBeaconBatteryLevel(
            @Field("beacon_mac") beacon_mac: String?,
            @Field("voltage") voltage: String?,
            @Field("timestamp") timestamp: String?,
            @Field("mac") mac: String?
        ): Call<SetBeaconBatteryResponse>

        @FormUrlEncoded
        @POST("api/get_theme_guide")
        fun getThemeGuide(
            @Field("timestamp") timestamp: String?,
            @Field("mac") mac: String?
        ): Call<CommonArrayResponse>

        @FormUrlEncoded
        @POST("api/send_user_location")
        fun sendUserLocation(
            @Field("jsondata") userLocationListStr: String?,
            @Field("device_id") deviceId: String?,
            @Field("login_token") loginToken: String?,
            @Field("timestamp") timestamp: String?,
            @Field("mac") mac: String?
        ): Call<SendUserLocationResponse>
    }

    internal interface GoogleApiService {
        @GET("maps/api/directions/json")
        fun getGoogleRoute(
            @Query("origin") origin: String?,
            @Query("destination") destination: String?,
            @Query("sensor") sensor: String?
        ): Call<CommonResponse>
    }

    private val locationService: LocationService
        private get() = NetworkManager.instance.retrofit!!.create(LocationService::class.java)

    private val googleApiService: GoogleApiService
        private get() = NetworkManager.instance.googleApiRetrofit!!.create(GoogleApiService::class.java)

    fun getBuildings(activity: Activity, listener: NetworkManagerListener<Array<Building>>) {
        Log.e(NaviSDKText.LOG_TAG, "getBuildings")
        DialogTools.instance.showProgress(activity)
        val call = locationService.buildings
        NetworkManager.instance.addPostRequestToCommonArrayObj(
            activity,
            call,
            Array<Building>::class.java,
            listener
        )
    }

    fun getFloors(
        activity: Activity,
        buildingId: String?,
        listener: NetworkManagerListener<Array<BuildingFloor>>
    ) {
        Log.e(NaviSDKText.LOG_TAG, "getFloors")
        DialogTools.instance.showProgress(activity)
        val call = locationService.getFloors(buildingId)
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<BuildingFloor>::class.java,
            listener
        )
    }

    fun doSearch(
        activity: Activity, buildingId: String, keyword: String,
        lat: Double, lng: Double,
        listener: NetworkManagerListener<Array<BuildingFloor>>
    ) {
        Log.e(NaviSDKText.LOG_TAG, "doSearch")
        DialogTools.instance.showProgress(activity)
        val call = locationService.getFloors(buildingId, keyword, lat, lng)
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<BuildingFloor>::class.java,
            listener
        )
    }

    fun getUserIndoorLocationToOutdoorPRoute(
        activity: Activity,
        currentBuildingId: String, userCurrentFloorNumber: String,
        userLat: Double, userLng: Double,
        outdoorPOILat: Double, outdoorPOILng: Double,
        priority: String,
        listener: NetworkManagerListener<Array<NavigationRoutePOI>>
    ) {
        DialogTools.instance.showProgress(activity)
        val call = googleApiService.getGoogleRoute(
            "$userLat,$userLng",
            "$outdoorPOILat,$outdoorPOILng",
            "false"
        )
        NetworkManager.instance.addGetRequestToCommonObj(activity,
            call,
            GoogleNavigationRoute::class.java,
            object : NetworkManagerListener<GoogleNavigationRoute> {
                override fun onSucceed(response: GoogleNavigationRoute?) {
                    if (response?.status == GoogleNavigationRoute.GET_ROUTE_STATUS_OK) {
                        val poiList = response.navigationRoute
                        if (poiList.isNotEmpty()) {
                            val poi = poiList[0]
                            getUserLocationToExit(
                                activity,
                                currentBuildingId,
                                userCurrentFloorNumber,
                                userLat,
                                userLng,
                                poi.latitude,
                                poi.longitude!!,
                                priority,
                                listener
                            )
                        }
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {}
            })
    }

    fun getUserLocationToIndoorPRoute(
        activity: Activity, poiId: String, lat: Double, lng: Double, userCurrentFloorLevel: String,
        priority: String,
        listener: NetworkManagerListener<Array<NavigationRoutePOI>>
    ) {
        DialogTools.instance.showProgress(activity)
        val call = locationService.getUserLocationToIndoorPRoute(
            poiId,
            lat,
            lng,
            priority,
            userCurrentFloorLevel,
            platform
        )
        Log.e(NaviSDKText.LOG_TAG, "call" + call.request().url().toString())
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<NavigationRoutePOI>::class.java,
            listener
        )
    }

    fun getUserLocationToExit(
        activity: Activity, userCurrentBuildingId: String, userCurrentFloorLevel: String,
        userLat: Double, userLng: Double, exitLat: String?, exitLng: String,
        priority: String,
        listener: NetworkManagerListener<Array<NavigationRoutePOI>>
    ) {
        DialogTools.instance.showProgress(activity)
        val call = locationService.getUserLocationToExit(
            userCurrentBuildingId, userCurrentFloorLevel,
            userLat, userLng, exitLat, exitLng, priority, platform
        )
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<NavigationRoutePOI>::class.java,
            listener
        )
    }

    fun getEmergencyRoute(
        activity: Activity, userCurrentBuildingId: String?, userCurrentFloorLevel: String,
        userLat: Double, userLng: Double, type: String,
        listener: NetworkManagerListener<Array<NavigationRoutePOI>>
    ) {
        DialogTools.instance.showProgress(activity)
        val call = locationService.getEmergencyRoute(
            userCurrentBuildingId,
            userCurrentFloorLevel,
            userLat,
            userLng,
            type,
            platform
        )
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<NavigationRoutePOI>::class.java,
            listener
        )
    }

    fun getAppCategory(
        activity: Activity,
        listener: NetworkManagerListener<Array<PoiCategory>>
    ) {
        val call = locationService.getAppCategory()
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<PoiCategory>::class.java,
            listener
        )
    }

    fun getHotSearch(
        activity: Activity,
        listener: NetworkManagerListener<Array<HotSearchData>>
    ) {
        val call = locationService.getHotSearch()
        NetworkManager.instance.addGetRequestToCommonArrayObj(
            activity,
            call,
            Array<HotSearchData>::class.java,
            listener
        )
    }

    fun getKeyword(
        activity: Activity,
        listener: NetworkManagerListener<Array<KeywordData>?>
    ) {
        val call = locationService.getKeyword()
        NetworkManager.instance.addGetRequest(
            activity,
            call,
            Array<KeywordData>::class.java,
            listener
        )
    }

    fun setBeaconBatteryLevel(
        activity: Activity, beaconMac: String?, voltage: String?,
        listener: NetworkManagerListener<SetBeaconBatteryResponse?>
    ) {

        val currentTimestamp = System.currentTimeMillis() / 1000L
        val mac: String? = NetworkManager.instance.getMacStr(currentTimestamp.toDouble())
        val call: Call<SetBeaconBatteryResponse> = locationService.setBeaconBatteryLevel(
            beaconMac, voltage, currentTimestamp.toString() + "", mac
        )
        NetworkManager.instance.addPostRequest(
            activity,
            call,
            SetBeaconBatteryResponse::class.java,
            listener
        )
    }

    fun getThemeGuide(activity: Activity, listener: NetworkManagerListener<Array<OfficialEvent>>) {

        val currentTimestamp = System.currentTimeMillis() / 1000L
        val mac: String? = NetworkManager.instance.getMacStr(currentTimestamp.toDouble())
        val call: Call<CommonArrayResponse> =
            locationService.getThemeGuide(currentTimestamp.toString() + "", mac)

        Log.e("LOG","currentTimestamp"+currentTimestamp)
        Log.e("LOG","mac"+mac)
        NetworkManager.instance.addPostRequestToCommonArrayObj(
            activity,
            call,
            Array<OfficialEvent>::class.java,
            listener
        )
    }

    fun sendUserLocation(
        activity: Activity,
        list: List<UserCurrentLocation>,
        loginToken: String,
        listener: NetworkManagerListener<SendUserLocationResponse?>
    ) {
        val currentTimestamp = System.currentTimeMillis() / 1000L
        val mac: String? = NetworkManager.instance.getMacStr(currentTimestamp.toDouble())
        val jsonStr: String = NetworkManager.instance.gson.toJson(list)
        val call: Call<SendUserLocationResponse> = locationService.sendUserLocation(
            jsonStr,
            NetworkManager.instance.getDeviceId(activity),
            loginToken, currentTimestamp.toString() + "",
            mac
        )

        Log.e("LOG","jsonStr"+jsonStr)
        Log.e("LOG","call"+call)
        NetworkManager.instance.addPostRequest(
            activity, false, call,
            SendUserLocationResponse::class.java, listener
        )
    }

    companion object {
        private var sLOCATION_API: LocationApi? = null
        private const val platform = "android"
        val instance: LocationApi
            get() {
                if (sLOCATION_API == null) {
                    sLOCATION_API = LocationApi()
                }
                return sLOCATION_API!!
            }
    }
}