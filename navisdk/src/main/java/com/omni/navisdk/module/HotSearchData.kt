package com.omni.navisdk.module

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList

class HotSearchData() : Parcelable {

    var ac_id: Int? = null
    var title_zh: String? = null
    var title_en: String? = null
    var enabled: String? = null

    constructor(parcel: Parcel) : this() {
        ac_id = parcel.readValue(Int::class.java.classLoader) as? Int
        title_zh = parcel.readString()
        title_en = parcel.readString()
        enabled = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(ac_id)
        parcel.writeString(title_zh)
        parcel.writeString(title_en)
        parcel.writeString(enabled)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HotSearchData> {
        override fun createFromParcel(parcel: Parcel): HotSearchData {
            return HotSearchData(parcel)
        }

        override fun newArray(size: Int): Array<HotSearchData?> {
            return arrayOfNulls(size)
        }

    }
}