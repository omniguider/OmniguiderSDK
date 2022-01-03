package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleRoute : Serializable {

    @SerializedName("bounds")
    var bounds: GoogleBounds? = null

    @SerializedName("copyrights")
    var copyRights: String? = null

    @SerializedName("legs")
    var legs: Array<GoogleLeg>? = null

    @SerializedName("overview_polyline")
    var overviewPolyline: GooglePolyline? = null

    @SerializedName("summary")
    var summary: String? = null

    @SerializedName("warnings")
    var warnings: Array<Any>? = null

    @SerializedName("waypoint_order")
    var waypointOrders: Array<Any>? = null

}