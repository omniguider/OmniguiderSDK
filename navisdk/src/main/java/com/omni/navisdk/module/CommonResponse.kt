package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommonResponse(
        @SerializedName("result") val result: String,
        @SerializedName("error_message") val errorMessage: String,
        @SerializedName("data") val data: Any
)