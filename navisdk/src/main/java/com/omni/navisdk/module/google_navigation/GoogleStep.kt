package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleStep : Serializable {

    @SerializedName("distance")
    var distance: GoogleDistance? = null

    @SerializedName("duration")
    var duration: GoogleDistance? = null

    @SerializedName("end_location")
    var endLocation: OmniLatLng? = null

    @SerializedName("html_instructions")
    var htmlInstruction: String? = null

    @SerializedName("polyline")
    var polyline: GooglePolyline? = null

    @SerializedName("start_location")
    var startLocation: OmniLatLng? = null

    @SerializedName("travel_mode")
    var travelMode: String? = null

}