package com.omni.navisdk.tool

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.omni.navisdk.R
import java.util.*
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber


class Tools {

    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    fun dpToIntPx(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        //        int dpAsPixels = (int) (dp * scale + 0.5f);
        return (dp * scale).toInt()
        //        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    fun hideKeyboard(context: Context, rootView: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    fun getColor(context: Context?, colorId: Int): Int {
        return ContextCompat.getColor(context!!, colorId)
    }

    fun isValidMobileNumber(phone: String?, locationCode: String?): Boolean {
        if (TextUtils.isEmpty(phone)) return false
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        try {
            var phoneNumber: Phonenumber.PhoneNumber? = null
            phoneNumber = if (locationCode == null) {
                phoneNumberUtil.parse(phone, Locale.getDefault().country)
            } else {
                phoneNumberUtil.parse(phone, locationCode)
            }
            val phoneNumberType: PhoneNumberUtil.PhoneNumberType =
                phoneNumberUtil.getNumberType(phoneNumber)
            return phoneNumberType === PhoneNumberUtil.PhoneNumberType.MOBILE
        } catch (e: Exception) {
        }
        return false
    }

    fun getDistance(p1Lat: Double, p1Lon: Double, p2Lat: Double, p2Lon: Double): Float {
        val l1 = Location("One")
        l1.latitude = p1Lat
        l1.longitude = p1Lon
        val l2 = Location("Two")
        l2.latitude = p2Lat
        l2.longitude = p2Lon
        return l1.distanceTo(l2)
    }

    fun getDistanceStr(p1Lat: Double, p1Lon: Double, p2Lat: Double, p2Lon: Double): String {
        var distance = getDistance(p1Lat, p1Lon, p2Lat, p2Lon)
        var dist = "$distance M"
        if (distance > 1000.0f) {
            distance = distance / 1000000.0f
            dist = distance.toString() + "M"
        }
        return dist
    }

    fun getDrawable(context: Context?, drawableId: Int): Drawable? {
        return ContextCompat.getDrawable(context!!, drawableId)
    }

    val androidVersion: Int
        get() = Build.VERSION.SDK_INT

    val notificationSmallIcon: Int
        get() = if (androidVersion >= Build.VERSION_CODES.LOLLIPOP) {
            R.mipmap.syn_poi_information
        } else {
            R.mipmap.syn_poi_information
        }

    fun changeDrawableBGWithColor(context: Context, view: View, @ColorRes strokeColorRes: Int, @ColorRes fillColorRes: Int) {
        val evacuationRouteShape = view.background as GradientDrawable
        evacuationRouteShape.setColor(ContextCompat.getColor(context, fillColorRes))
        evacuationRouteShape.setStroke(dpToIntPx(context, 1f), getColor(context, strokeColorRes))
    }

    fun getBearing(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Float {
        val lat = Math.abs(startLat - endLat)
        val lng = Math.abs(startLon - endLon)
        if (startLat < endLat && startLon < endLon) {
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        } else if (startLat >= endLat && startLat < endLon) {
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        } else if (startLat >= endLat && startLon >= endLon) {
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        } else if (startLat < endLat && startLon >= endLon) {
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        }
        return (-1).toFloat()
    }

    fun findDisBetweenTwoP(a: Point, b: Point): Double {
        return Math.sqrt(
            Math.pow((a.x - b.x).toDouble(), 2.0) + Math.pow(
                (a.y - b.y).toDouble(),
                2.0
            )
        )
    }

    companion object {
        private var mTools: Tools? = null
        @JvmStatic
        val instance: Tools
            get() {
                if (mTools == null) {
                    mTools = Tools()
                }
                return mTools!!
            }

        const val beaconTrigger05 = 0.7f
        const val beaconTrigger2 = 2f

        //    private static float beaconTrigger = beaconTrigger10;
        const val beaconTrigger = 10f
    }
}