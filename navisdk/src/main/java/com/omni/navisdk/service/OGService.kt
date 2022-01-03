package com.omni.navisdk.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.indooratlas.android.sdk.*
import com.omni.navisdk.manager.DataCacheManager.Companion.instance
import com.omni.navisdk.module.OmniEvent
import com.omni.navisdk.module.UserCurrentLocation
import com.omni.navisdk.module.UserLocationRequestType
import com.omni.navisdk.tool.NaviSDKText
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class OGService(private val mActivity: Activity) : IARegion.Listener, IALocationListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    interface GoogleApiClientConnectCallBack {
        fun onGoogleApiClientConnected()
    }

    private var mIALocationManager: IALocationManager? = null

    //    private IAResourceManager mIAResourceManager;
    private var mLocationRequest: LocationRequest? = null
    var googleApiClient: GoogleApiClient? = null
        private set
    private var mIsIndoor = false
    private var mGoogleApiClientConnectCallBack: GoogleApiClientConnectCallBack? = null
    private var mCurrentVenueId: String? = null
    private var mCurrentFloorId: String? = null
    private var mPreviousFloorId: String? = null
    private var mLocation: Location? = null
    val traceIdRecordList: List<Array<String>> = ArrayList()
    private var mRequestUserIndoorLocation = UserLocationRequestType.AUTOMATICALLY
    private var mTimeHandler: Handler? = null
    private var mTimeHandlerThread: HandlerThread? = null
    private var mCountTime = 0
    private val timerRun: Runnable? = object : Runnable {
        override fun run() {
            ++mCountTime // 經過的秒數 + 1
            addUserLocationToList(mLocation, if (mIsIndoor) instance!!.userCurrentFloorPlanId else NaviSDKText.USER_OUTDOOR)
            if (mCountTime == 6) {
                mCountTime = 0
                //                sendUserLocationToServer();
            }
            mTimeHandler!!.removeCallbacks(this)
            mTimeHandler!!.postDelayed(this, 10000)
        }
    }
    private var mUserLocationList: MutableList<UserCurrentLocation>? = null
    private var mEventBus: EventBus? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: OmniEvent) {
        when (event.type) {
            OmniEvent.TYPE_REQUEST_LAST_LOCATION -> if (instance!!.userCurrentFloorPlanId == NaviSDKText.USER_OUTDOOR) {
                mEventBus!!.post(OmniEvent(OmniEvent.TYPE_USER_OUTDOOR_LOCATION, mLocation))
            } else {
                mEventBus!!.post(OmniEvent(OmniEvent.TYPE_USER_INDOOR_LOCATION, mLocation))
            }
            OmniEvent.TYPE_REQUEST_USER_LOCATION_INDOOR_ONLY -> mRequestUserIndoorLocation = UserLocationRequestType.INDOOR_ONLY
            OmniEvent.TYPE_REQUEST_USER_LOCATION_OUTDOOR_ONLY -> mRequestUserIndoorLocation = UserLocationRequestType.OUTDOOR_ONLY
            OmniEvent.TYPE_REQUEST_USER_LOCATION_AUTOMATICALLY -> mRequestUserIndoorLocation = UserLocationRequestType.AUTOMATICALLY
            OmniEvent.TYPE_CHECK_USER_LOCATION_REQUEST_TYPE -> mEventBus!!.post(OmniEvent(OmniEvent.TYPE_USER_LOCATION_REQUEST_TYPE, mRequestUserIndoorLocation))
        }
    }

    fun startService(callBack: GoogleApiClientConnectCallBack?) {

//        mOGLocationListener = listener;
        mGoogleApiClientConnectCallBack = callBack
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(mActivity).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(Places.GEO_DATA_API)
                    .build()
        }
        if (!googleApiClient!!.isConnected) {
            googleApiClient!!.connect()
        }
        initLocationService()
        val request = IALocationRequest.create()
        request.fastestInterval = 1000
        request.smallestDisplacement = 0.6f
        mIALocationManager!!.removeLocationUpdates(this)
        mIALocationManager!!.requestLocationUpdates(request, this)
    }

    private fun initLocationService() {
        if (mIALocationManager == null) {
            mIALocationManager = IALocationManager.create(mActivity)
            mIALocationManager?.lockIndoors(true)
        } else {
            mIALocationManager!!.unregisterRegionListener(this)
        }
        mIALocationManager!!.registerRegionListener(this)
        //        if (mIAResourceManager == null) {
//            mIAResourceManager = IAResourceManager.create(mActivity);
//        }
    }

    fun stopService() {
        if (mIALocationManager != null) {
            mIALocationManager!!.removeLocationUpdates(this)
            mIALocationManager!!.unregisterRegionListener(this)
        }
    }

    fun destroy() {
        if (mEventBus != null) {
            mEventBus?.unregister(this)
        }
        if (mTimeHandler != null && timerRun != null) {
            mTimeHandler!!.removeCallbacks(timerRun)
        }
        if (mTimeHandlerThread != null) {
            mTimeHandlerThread!!.interrupt()
            mTimeHandlerThread!!.quit()
        }
        if (googleApiClient != null && googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
        if (mIALocationManager != null) {
            mIALocationManager!!.destroy()
            mIALocationManager = null
        }
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            if (mLocationRequest == null) {
                mLocationRequest = LocationRequest()
                mLocationRequest!!.interval = 1000
                mLocationRequest!!.fastestInterval = 1000
                mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this)
            if (mGoogleApiClientConnectCallBack != null) {
                mGoogleApiClientConnectCallBack!!.onGoogleApiClientConnected()
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onLocationChanged(location: Location) {
        when (mRequestUserIndoorLocation) {
            UserLocationRequestType.INDOOR_ONLY -> return
            UserLocationRequestType.OUTDOOR_ONLY -> {
                mLocation = location
                EventBus.getDefault().post(OmniEvent(OmniEvent.TYPE_USER_OUTDOOR_LOCATION, mLocation))
                startSendUserLocation()
                addUserLocationToList(mLocation, NaviSDKText.USER_OUTDOOR)
            }
            UserLocationRequestType.AUTOMATICALLY -> //                Log.e(LOG_TAG, "normal location is indoor : " + mIsIndoor);
                if (!mIsIndoor) {
                    mLocation = location
                    EventBus.getDefault().post(OmniEvent(OmniEvent.TYPE_USER_OUTDOOR_LOCATION, mLocation))
                    startSendUserLocation()
                    addUserLocationToList(mLocation, NaviSDKText.USER_OUTDOOR)
                }
        }
    }

    override fun onLocationChanged(iaLocation: IALocation) {
//        Log.e(LOG_TAG, "floor certainty : " + iaLocation.getFloorCertainty() + ", accuracy : " + iaLocation.getAccuracy());
//        mTraceIdRecordList.add(new String[]{mIALocationManager.getExtraInfo().traceId});
        if (iaLocation != null && iaLocation.floorCertainty > 0.8) {
            when (mRequestUserIndoorLocation) {
                UserLocationRequestType.INDOOR_ONLY -> {
                    mLocation = iaLocation.toLocation()
                    if (iaLocation.region != null) {
                        addUserLocationToList(mLocation, iaLocation.region.id)
                        EventBus.getDefault().post(OmniEvent(OmniEvent.TYPE_USER_INDOOR_LOCATION, mLocation))
                        startSendUserLocation()
                    }
                }
                UserLocationRequestType.OUTDOOR_ONLY -> return
                UserLocationRequestType.AUTOMATICALLY -> //                    Log.e(LOG_TAG, "iaLocation is indoor : " + mIsIndoor);
                    if (mIsIndoor) {
                        mLocation = iaLocation.toLocation()
                        var haveToFetch = false
                        if (TextUtils.isEmpty(mPreviousFloorId) ||
                                !TextUtils.isEmpty(mPreviousFloorId) && mCurrentFloorId != mPreviousFloorId) {
                            haveToFetch = true
                            mPreviousFloorId = mCurrentFloorId
                        }
                        if (iaLocation.region != null) {
                            addUserLocationToList(mLocation, iaLocation.region.id)
                            EventBus.getDefault().post(OmniEvent(OmniEvent.TYPE_USER_INDOOR_LOCATION, mLocation))
                            startSendUserLocation()
                        }
                    }
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle?) {}
    override fun onEnterRegion(iaRegion: IARegion) {
        if (iaRegion.type == IARegion.TYPE_UNKNOWN) {
            Log.e(NaviSDKText.LOG_TAG, "onEnterRegion unknown : ")
            mIsIndoor = false
        } else if (iaRegion.type == IARegion.TYPE_VENUE) {
            Log.e(NaviSDKText.LOG_TAG, "onEnterRegion venue : " + iaRegion.id)
            mIsIndoor = false
        } else if (iaRegion.type == IARegion.TYPE_FLOOR_PLAN) {
            if (iaRegion.id != "919f0ac4-62e4-48ae-8217-dcb707bbcdc9"
                    && iaRegion.id != "4793238f-4784-4c4e-b7f2-af71cd2344a2") {
                Log.e(NaviSDKText.LOG_TAG, "onEnterRegion floor plan : " + iaRegion.id)
                mIsIndoor = true
                instance!!.userCurrentFloorPlanId = iaRegion.id
                EventBus.getDefault().post(OmniEvent(OmniEvent.TYPE_FLOOR_PLAN_CHANGED, iaRegion.id))
            }
        }
    }

    override fun onExitRegion(iaRegion: IARegion) {
        if (iaRegion.type == IARegion.TYPE_UNKNOWN) {
            Log.e(NaviSDKText.LOG_TAG, "onExitRegion : unknown")
            mIsIndoor = false
        } else if (iaRegion.type == IARegion.TYPE_VENUE) {
            Log.e(NaviSDKText.LOG_TAG, "onExitRegion venue : " + iaRegion.id)
            mCurrentVenueId = ""
            mIsIndoor = false
        } else if (iaRegion.type == IARegion.TYPE_FLOOR_PLAN) {
            Log.e(NaviSDKText.LOG_TAG, "onExitRegion floor plan : " + iaRegion.id)
            mCurrentFloorId = ""
            mIsIndoor = false
            instance!!.userCurrentFloorPlanId = ""
        }
    }

    private fun addUserLocationToList(location: Location?, floorPlanId: String?) {
        if (location == null) {
            return
        }
        val userCurrentLocation = UserCurrentLocation.Builder()
                .setFloorPlanId(floorPlanId)
                .setLat(location.latitude.toString())
                .setLng(location.longitude.toString())
                .setDate(SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date()))
                .build()
        if (mUserLocationList == null) {
            mUserLocationList = ArrayList()
        }
        mUserLocationList!!.add(userCurrentLocation)
    }

    private val userLocationList: List<UserCurrentLocation>
        private get() = mUserLocationList ?: ArrayList()

    private fun startSendUserLocation() {
        if (mTimeHandlerThread == null) {
            mTimeHandlerThread = HandlerThread("send_user_location_time_handler_thread")
            mTimeHandlerThread!!.start()
        }
        if (mTimeHandler == null) {
            mTimeHandler = Handler(mTimeHandlerThread!!.looper)
            mTimeHandler!!.postDelayed(timerRun!!, 100)
        }
    }

    init {
        if (mEventBus == null) {
            mEventBus = EventBus.getDefault()
        }
        mEventBus!!.register(this)
    }
}