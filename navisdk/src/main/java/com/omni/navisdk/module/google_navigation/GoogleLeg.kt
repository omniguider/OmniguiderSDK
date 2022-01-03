package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleLeg : Serializable {

    @SerializedName("distance")
    var distance: GoogleDistance? = null

    @SerializedName("duration")
    var duration: GoogleDistance? = null

    @SerializedName("end_address")
    var endAddress: String? = null

    @SerializedName("end_location")
    var endLocation: OmniLatLng? = null

    @SerializedName("start_address")
    var startAddress: String? = null

    @SerializedName("start_location")
    var startLocation: OmniLatLng? = null

    @SerializedName("steps")
    var steps: Array<GoogleStep>? = null

    @SerializedName("traffic_speed_entry")
    var trafficSpeedEntries: Array<Any>? = null

    @SerializedName("via_waypoint")
    var viaWaypoints: Array<Any>? = null

}