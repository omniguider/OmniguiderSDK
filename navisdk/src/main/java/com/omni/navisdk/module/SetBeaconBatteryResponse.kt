package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName

data class SetBeaconBatteryResponse(@SerializedName("result") val result: String,
                                    @SerializedName("error_message") val errorMsg: String) {
    fun isSuccess(): Boolean {
        return result == "true"
    }
}
