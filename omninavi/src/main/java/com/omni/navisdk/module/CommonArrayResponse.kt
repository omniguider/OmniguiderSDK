package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommonArrayResponse(@SerializedName("result") val result: String,
                               @SerializedName("error_message") val errorMessage: String,
                               @SerializedName("data") val data: Array<Any>) {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}