package com.omni.navisdk.module

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class OfficialEvent() : Parcelable {

    var theme_id: Int? = null
    var title: String? = null
    var desc: String? = null
    var image: String? = null
    var result: EventResult? = null

    constructor(parcel: Parcel) : this() {
        theme_id = parcel.readValue(Int::class.java.classLoader) as? Int
        title = parcel.readString()
        desc = parcel.readString()
        image = parcel.readString()
        result = parcel.readParcelable(EventResult::class.java.classLoader)

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(theme_id)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeString(image)
        parcel.writeValue(result)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OfficialEvent> {

//        val initData by lazy {
//            arrayListOf(
//                OfficialEvent(),
//                OfficialEvent()
//            )
//        }

        override fun createFromParcel(parcel: Parcel): OfficialEvent {
            return OfficialEvent(parcel)
        }

        override fun newArray(size: Int): Array<OfficialEvent?> {
            return arrayOfNulls(size)
        }
    }

}

class EventResult() : Parcelable {
    var ab_id: String? = null
    var locid: String? = null
    var exhibits: ArrayList<Exhibits> = ArrayList()
    var e_ids: ArrayList<Int> = ArrayList()

    constructor(parcel: Parcel) : this() {
        ab_id = parcel.readString()
        locid = parcel.readString()
        e_ids = parcel.readArrayList(String::class.java.classLoader) as ArrayList<Int>
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ab_id)
        parcel.writeString(locid)
        parcel.writeList(exhibits.toList())
        parcel.writeList(e_ids.toList())
    }

    companion object CREATOR : Parcelable.Creator<EventResult> {
        override fun createFromParcel(parcel: Parcel): EventResult {
            return EventResult(parcel)
        }

        override fun newArray(size: Int): Array<EventResult?> {
            return arrayOfNulls(size)
        }
    }

}

class Exhibits() : Parcelable {
    var poi_id: Int? = null
    var puid: String? = null
    var poi_lat: String? = null
    var poi_lng: String? = null
    var floor_number: String? = null
    var floor_id: String? = null
    var plan_id: String? = null
    var title_zh: String? = null
    var dep_title_zh: String? = null
    var area_title_zh: String? = null
    var desc_zh: String? = null
    var tel: String? = null
    var image: String? = null
    var audio: String? = null
    var video: String? = null

    constructor(parcel: Parcel) : this() {
        poi_id = parcel.readValue(Int::class.java.classLoader) as? Int
        puid = parcel.readString()
        poi_lat = parcel.readString()
        poi_lng = parcel.readString()
        floor_number = parcel.readString()
        floor_id = parcel.readString()
        plan_id = parcel.readString()
        title_zh = parcel.readString()
        dep_title_zh = parcel.readString()
        area_title_zh = parcel.readString()
        desc_zh = parcel.readString()
        tel = parcel.readString()
        image = parcel.readString()
        audio = parcel.readString()
        video = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(poi_id)
        parcel.writeString(puid)
        parcel.writeString(poi_lat)
        parcel.writeString(poi_lng)
        parcel.writeString(floor_number)
        parcel.writeString(floor_id)
        parcel.writeString(plan_id)
        parcel.writeString(title_zh)
        parcel.writeString(dep_title_zh)
        parcel.writeString(area_title_zh)
        parcel.writeString(desc_zh)
        parcel.writeString(tel)
        parcel.writeString(image)
        parcel.writeString(audio)
        parcel.writeString(video)
    }

    companion object CREATOR : Parcelable.Creator<EventResult> {
        override fun createFromParcel(parcel: Parcel): EventResult {
            return EventResult(parcel)
        }

        override fun newArray(size: Int): Array<EventResult?> {
            return arrayOfNulls(size)
        }
    }

}
