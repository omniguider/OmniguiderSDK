package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by wiliiamwang on 14/06/2017.
 */
class OmniLatLng : Serializable {
    @SerializedName("lat")
    var lat = 0.0

    @SerializedName("lng")
    var lng = 0.0

}