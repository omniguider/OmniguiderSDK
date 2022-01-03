package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleWaypoint : Serializable {
    @SerializedName("geocoder_status")
    var geoCoderStatus: String? = null

    @SerializedName("place_id")
    var placeId: String? = null

    @SerializedName("types")
    var types: Array<String>? = null

}