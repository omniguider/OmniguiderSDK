package com.omni.navisdk.tool

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import java.lang.reflect.Type
import java.util.*

class PreferencesTools {
    private var mGson: Gson? = null
    val gson: Gson
        get() {
            if (mGson == null) {
                mGson = Gson()
            }
            return mGson!!
        }

    fun getPreferences(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences(KEY_NMP_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun removePreviousDaysBeaconProperty(context: Context?, key: String, dateStr: String) {
        val preferences = getPreferences(context)
        val map = preferences!!.all
        for (k in map.keys) {
            if (k.contains(key) && k != key + dateStr) {
                removeProperty(context, k)
            }
        }
    }

    fun saveProperty(context: Context?, name: String?, value: String?) {
        val e = getPreferences(context)!!.edit()
        e.putString(name, value)
        e.commit()
    }

    fun <T> saveProperty(context: Context?, name: String?, value: T) {
        val e = getPreferences(context)!!.edit()
        e.putString(name, gson.toJson(value))
        e.commit()
    }

    fun removeProperty(context: Context?, name: String?) {
        val e = getPreferences(context)!!.edit()
        e.remove(name)
        e.commit()
    }

    fun getProperty(context: Context?, name: String?, def: String?): String? {
        return getPreferences(context)!!.getString(name, def)
    }

    fun getProperty(context: Context?, name: String?): String? {
        return getPreferences(context)!!.getString(name, null)
    }

    fun getPropertyString(context: Context?, name: String?): String? {
        return getPreferences(context)!!.getString(name, "")
    }

    fun <T> getProperty(context: Context?, name: String?, c: Class<T>?): T? {
        val valueStr = getProperty(context, name)
        return if (valueStr != null) {
            gson.fromJson(valueStr, c)
        } else null
    }

    fun <T, X> getProperties(context: Context?, name: String?, type: Type?,
                             keyClass: Class<T>?, valueClass: Class<X>?): Map<*, *> {
        val valueStr = getProperty(context, name)
        val map: Map<T, X>
        map = if (valueStr == null) {
            HashMap()
        } else {
            gson.fromJson(valueStr, type)
        }
        return map
    }

    fun getProperties(context: Context?, name: String?): Set<String>? {
        return getProperties(context, name, null)
    }

    fun getProperties(context: Context?, name: String?, def: Set<String?>?): Set<String>? {
        return getPreferences(context)!!.getStringSet(name, def)
    }

    fun saveProperties(context: Context?, name: String?, values: Set<String>?) {
        val e = getPreferences(context)!!.edit()
        e.putStringSet(name, values)
        e.commit()
    }

    fun addProperties(context: Context?, name: String?, value: String) {
        val previousSet = getPreferences(context)!!.getStringSet(name, null)
        val set: MutableSet<String>
        set = previousSet?.let { HashSet(it) } ?: HashSet()
        if (!set.contains(value)) {
            set.add(value)
        }
        saveProperties(context, name, set)
    }

    fun <T, X> addProperties(context: Context?, name: String?, newMap: HashMap<T, X?>): Boolean {
        val valueStr = getProperty(context, name)
        val previousMap: HashMap<T, X?>
        previousMap = if (TextUtils.isEmpty(valueStr)) {
            HashMap()
        } else {
            gson.fromJson(valueStr, newMap.javaClass)
        }
        var haveNewKey = false
        for (key in newMap.keys) {
            if (!previousMap.containsKey(key)) {
                haveNewKey = true
                break
            }
        }
        newMap.keys.removeAll(previousMap.keys)
        previousMap.putAll(newMap)
        saveProperty(context, name, previousMap)
        return haveNewKey
    }

    companion object {
        private const val KEY_NMP_PREFERENCES_NAME = "key_preferences_nmp_preferences_name"
        const val KEY_ALL_BUILDINGS = "key_preferences_all_buildings"
        const val KEY_FLOORS = "key_preferences_floors"
        const val KEY_EXHIBIT_FLOORS = "key_preferences_exhibit_floors"
        const val KEY_EXHIBIT_THEMES = "key_preferences_exhibit_themes"
        const val KEY_BEACON_NOTIFICATION_HISTORY = "key_preferences_beacon_notification_history"
        const val KEY_BEACON_NOTIFICATION_HISTORY_UNREAD = "key_preferences_beacon_notification_history_unread"
        const val KEY_GEO_FENCE_NOTIFICATION_HISTORY = "key_preferences_geo_fence_notification_history"
        const val KEY_GEO_FENCE_NOTIFICATION_HISTORY_UNREAD = "key_preferences_geo_fence_notification_history_unread"
        const val KEY_RECORDED_CAR_LOCATION = "key_preferences_recorded_car_location"
        const val KEY_PARKING_SPACE_INFO = "key_preferences_parking_space_info"
        const val KEY_PRIVACY = "key_preferences_privacy"
        const val KEY_PICKUP = "key_preferences_pickup"
        const val KEY_XDAY = "key_preferences_xday"
        const val KEY_STORE_ROUTE_A = "key_preferences_store_route_a"
        const val KEY_STORE_ROUTE_B = "key_preferences_store_route_b"
        const val KEY_SEARCH_RECORD = "key_preferences_search_record"
        const val KEY_CURRENT_GROUP = "key_preferences_current_group"
        const val KEY_SHOW_INTRO_FIND_FRIEND = "key_show_intro_find_friend"
        const val KEY_LEFT_GROUP_ENDTIME = "key_preferences_left_group_endtime"

        const val KEY_ALL_GROUP = "key_preferences_all_group"
        private var mPreferencesTools: PreferencesTools? = null
        val instance: PreferencesTools
            get() {
                if (mPreferencesTools == null) {
                    mPreferencesTools = PreferencesTools()
                }
                return mPreferencesTools!!
            }
    }
}