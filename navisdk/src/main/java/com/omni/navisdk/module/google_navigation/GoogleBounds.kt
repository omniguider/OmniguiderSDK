package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleBounds : Serializable {
    @SerializedName("northeast")
    var northeast: OmniLatLng? = null

    @SerializedName("southwest")
    var southwest: OmniLatLng? = null

}