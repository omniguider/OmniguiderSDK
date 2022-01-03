package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Building : Serializable {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("desc")
    val desc: String? = null

    @SerializedName("locid")
    val locId: String? = null

    @SerializedName("enabled")
    val enabled: String? = null

}