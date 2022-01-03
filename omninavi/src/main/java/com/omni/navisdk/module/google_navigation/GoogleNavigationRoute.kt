package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import com.omni.navisdk.module.NavigationRoutePOI
import java.io.Serializable
import java.util.*

class GoogleNavigationRoute : Serializable {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("geocoded_waypoints")
    var waypoints: Array<GoogleWaypoint>? = null

    @SerializedName("routes")
    private var routes: Array<GoogleRoute>? = null

    fun setRoutes(routes: Array<GoogleRoute>) {
        this.routes = routes
    }

    val navigationRoute: List<NavigationRoutePOI>
        get() {
            val navigationalRoutePOIList: MutableList<NavigationRoutePOI> = ArrayList()
            for (route in routes!!) {
                for (leg in route.legs!!) {
                    for (step in leg.steps!!) {
                        val polyPointList = step.polyline!!.decodedPolyPoints
                        for (latLng in polyPointList!!) {
                            val poi = NavigationRoutePOI()
                            poi.floorNumber = "1"
                            poi.poisType = "normal_road"
                            poi.latitude = latLng!!.latitude.toString()
                            poi.longitude = latLng.longitude.toString()
                            navigationalRoutePOIList.add(poi)
                        }
                    }
                }
            }
            return navigationalRoutePOIList
        }

    companion object {
        @JvmField
        var GET_ROUTE_STATUS_OK = "OK"
    }
}