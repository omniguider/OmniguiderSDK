package com.omni.navisdk.module

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class NavigationRoutePOI : Serializable {
    @SerializedName("buid")
    private val bUID: String? = null

    @SerializedName("floor_number")
    var floorNumber: String? = null

    @SerializedName("lat")
    var latitude: String? = null

    @SerializedName("lon")
    var longitude: String? = null

    @SerializedName("pois_type")
    var poisType: String? = null

    @SerializedName("puid")
    private val pUID: String? = null

    @SerializedName("id")
    val iD: String? = null

    @SerializedName("store_id")
    val storeID: String? = null

    @SerializedName("selected")
    val selected: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("desc")
    val desc: String? = null

    @SerializedName("logo")
    val logo: String? = null

    fun getbUID(): String? {
        return bUID
    }

    fun getpUID(): String? {
        return pUID
    }

    var turnHint: String? = null
    var turnImg = 0

    val location: LatLng
        get() = LatLng(java.lang.Double.valueOf(latitude!!), java.lang.Double.valueOf(longitude!!))

    override fun toString(): String {
        return "NavigationRoutePOI{" +
                "bUID='" + bUID + '\'' +
                ", floorNumber='" + floorNumber + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", poisType='" + poisType + '\'' +
                ", pUID='" + pUID + '\'' +
                '}'
    }
}