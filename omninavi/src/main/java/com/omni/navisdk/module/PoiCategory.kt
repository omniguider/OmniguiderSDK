package com.omni.navisdk.module

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList

class PoiCategory() : Parcelable {

    var aac_id: Int? = null
    var ac_id: Int? = null
    var category: String? = null
    var category_en: String? = null
    var list = ArrayList<Ac>()

    fun getCategoryName(): String? = when (Locale.getDefault().language) {
        "zh-TW" -> if (!category.isNullOrEmpty()) category else category_en
        else -> if (!category_en.isNullOrEmpty()) category_en else category
    }
    constructor(parcel: Parcel) : this() {
        aac_id = parcel.readValue(Int::class.java.classLoader) as? Int
        ac_id = parcel.readValue(Int::class.java.classLoader) as? Int
        category = parcel.readString()
        category_en = parcel.readString()
        list = parcel.readArrayList(Ac::class.java.classLoader) as ArrayList<Ac>
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(aac_id)
        parcel.writeValue(ac_id)
        parcel.writeString(category)
        parcel.writeString(category_en)
        parcel.writeList(list.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PoiCategory> {
        const val TYPE_ALL = -1
        const val TYPE_APC = -2
        override fun createFromParcel(parcel: Parcel): PoiCategory {
            return PoiCategory(parcel)
        }

        override fun newArray(size: Int): Array<PoiCategory?> {
            return arrayOfNulls(size)
        }

        fun typeAll(): PoiCategory = PoiCategory().apply {
            aac_id = TYPE_ALL
            category = "全部"
            category_en = "All"
        }

        fun typeAPC(): PoiCategory = PoiCategory().apply {
            aac_id = TYPE_APC
            category = "全部"
            category_en = "All"
        }
    }

}

class Ac() : Parcelable {
    var ac_id: Int? = null
    var type: String? = null
    var type_en: String? = null
    fun getTypeName(): String? = when (Locale.getDefault().language) {
        "zh-TW" -> if (!type.isNullOrEmpty()) type else type_en
        else -> if (!type_en.isNullOrEmpty()) type_en else type
    }

    constructor(parcel: Parcel) : this() {
        ac_id = parcel.readValue(Int::class.java.classLoader) as? Int
        type = parcel.readString()
        type_en = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(ac_id)
        parcel.writeString(type)
        parcel.writeString(type_en)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ac> {
        override fun createFromParcel(parcel: Parcel): Ac {
            return Ac(parcel)
        }

        override fun newArray(size: Int): Array<Ac?> {
            return arrayOfNulls(size)
        }
    }

}