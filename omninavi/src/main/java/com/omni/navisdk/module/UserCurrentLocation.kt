package com.omni.navisdk.module

import java.io.Serializable

class UserCurrentLocation : Serializable {
    var lat: String? = null
    var lng: String? = null
    var plan_id: String? = null
    var date: String? = null

    override fun toString(): String {
        return "UserCurrentLocation : { Date : $date,\nplan_id : $plan_id,\nlat : $lat,\nlng : $lng }"
    }

    class Builder {
        private val mUserCurrentLocation: UserCurrentLocation
        fun setLat(lat: String?): Builder {
            mUserCurrentLocation.lat = lat
            return this
        }

        fun setLng(lng: String?): Builder {
            mUserCurrentLocation.lng = lng
            return this
        }

        fun setFloorPlanId(id: String?): Builder {
            mUserCurrentLocation.plan_id = id
            return this
        }

        fun setDate(date: String?): Builder {
            mUserCurrentLocation.date = date
            return this
        }

        fun build(): UserCurrentLocation {
            return mUserCurrentLocation
        }

        init {
            mUserCurrentLocation = UserCurrentLocation()
        }
    }
}