package com.omni.navisdk.module.google_navigation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GoogleDistance : Serializable {
    @SerializedName("text")
    var text: String? = null

    @SerializedName("value")
    var value = 0

}