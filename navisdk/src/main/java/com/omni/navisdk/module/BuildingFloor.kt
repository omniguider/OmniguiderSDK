package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

class BuildingFloor : Serializable {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("number")
    val number: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("desc")
    val desc: String? = null

    @SerializedName("order")
    val order: String? = null

    @SerializedName("lat")
    val latitude = 0.0

    @SerializedName("lng")
    val longitude = 0.0

    @SerializedName("bl_lat")
    val blLatitude = 0.0

    @SerializedName("bl_lng")
    val blLongitude = 0.0

    @SerializedName("tr_lat")
    val trLatitude = 0.0

    @SerializedName("tr_lng")
    val trLongitude = 0.0

    @SerializedName("is_map")
    val isMap: String? = null

    @SerializedName("plan_id")
    val floorPlanId: String? = null

    @SerializedName("pois")
    val pois: Array<POI>? = null

    val zPois: List<POI>
        get() {
            val list: MutableList<POI> = ArrayList()
            val allPOIs = pois
            for (poi in allPOIs!!) {
                if (poi.type!!.contains("Z") || poi.type == "map_text") {
                    list.add(poi)
                }
            }
            return list
        }

    override fun toString(): String {
        return "BuildingFloor{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", order='" + order + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", blLatitude=" + blLatitude +
                ", blLongitude=" + blLongitude +
                ", trLatitude=" + trLatitude +
                ", trLongitude=" + trLongitude +
                ", isMap='" + isMap + '\'' +
                ", floorPlanId='" + floorPlanId + '\'' +
                ", pois=" + Arrays.toString(pois) +
                '}'
    }
}