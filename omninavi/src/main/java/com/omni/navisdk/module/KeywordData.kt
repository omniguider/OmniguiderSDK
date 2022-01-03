package com.omni.navisdk.module

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList

class KeywordData() : Parcelable {

    var text: String? = null
    var weight: String? = null

    constructor(parcel: Parcel) : this() {
        text = parcel.readString()
        weight = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeString(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KeywordData> {
        override fun createFromParcel(parcel: Parcel): KeywordData {
            return KeywordData(parcel)
        }

        override fun newArray(size: Int): Array<KeywordData?> {
            return arrayOfNulls(size)
        }

    }
}