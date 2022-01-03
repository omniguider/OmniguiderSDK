package com.omni.navisdk.module

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

    class OmniClusterItem<T>(val pOI: POI) : ClusterItem {
    private val mPosition: LatLng
    val title: String?
    val snippet: String?
    var iconRes = -1
    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        return if (obj !is OmniClusterItem<*>) {
            false
        } else super.equals(obj)
    }

    init {
        mPosition = LatLng(pOI.latitude, pOI.longitude)
        title = pOI.name
        snippet = pOI.desc
        iconRes = pOI.getPOIIconRes(false)
    }
}