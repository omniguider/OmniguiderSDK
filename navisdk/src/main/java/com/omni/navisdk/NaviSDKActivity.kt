package com.omni.navisdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.*
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.THLight.USBeacon.App.Lib.BatteryPowerData
import com.android.volley.toolbox.NetworkImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.omni.navisdk.manager.DataCacheManager
import com.omni.navisdk.manager.DataCacheManager.Companion.instance
import com.omni.navisdk.module.*
import com.omni.navisdk.network.LocationApi
import com.omni.navisdk.network.NetworkManager
import com.omni.navisdk.network.NetworkManager.NetworkManagerListener
import com.omni.navisdk.service.OGService
import com.omni.navisdk.tool.DialogTools
import com.omni.navisdk.tool.NaviSDKText
import com.omni.navisdk.tool.NaviSDKText.LOG_TAG
import com.omni.navisdk.tool.Tools
import com.omni.navisdk.util.ThreadPoolHandler
import com.omni.navisdk.view.CircleNetworkImageView
import com.omni.navisdk.view.OmniClusterRender
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class NaviSDKActivity : BaseActivity(), OnMapReadyCallback, OnInfoWindowCloseListener,
    LeScanCallback {
    private var map_bearing = 0f
    private var autoHeading = false
    private var naviDirect = false
    private var searchPOI: POI? = null
    private var quickType: String? = null
    private var themeId: ArrayList<Int> = ArrayList()
    private var themePlanId: String? = null

    object NaviMode {
        const val USER_IN_NAVIGATION = 0
        const val NOT_NAVIGATION = 1
    }

    private var mTimeHandler: Handler? = null
    private val mTimeRunner: Runnable? = object : Runnable {
        override fun run() {
            mTimeHandler!!.removeCallbacks(this)
            checkMapInit()
        }
    }
    private var mMap: GoogleMap? = null
    private var mClusterManager: ClusterManager<OmniClusterItem<*>>? = null
    private var mLastLocation: Location? = null
    private var mFloorLevelTV: TextView? = null
    private var mUserMarker: Marker? = null
    private var mNaviStartMarker: Marker? = null
    private var mNaviEndMarker: Marker? = null
    private var mTileOverlayMap: MutableMap<String?, TileOverlay>? = null
    private var mCurrentRouteList: List<NavigationRoutePOI>? = null
    private var mNavigationMode = NaviMode.NOT_NAVIGATION
    private var mFloorsLayoutNew: LinearLayout? = null
    private var mMaskLayout: FrameLayout? = null
    private var mUserAccuracyCircle: Circle? = null
    private var itemList: MutableList<OmniClusterItem<*>>? = null
    private var menuList: MutableList<String>? = null
    private var mIsIndoor = false
    private var mIsAutoNavi = false
    private var mCurrentSelectedPOI: POI? = null
    private var mEndPOI: POI? = null
    private var mNaviInfoRL: RelativeLayout? = null
    private var mNaviInfoIconCNIV: CircleNetworkImageView? = null
    private var mNaviInfoTitleTV: TextView? = null
    private var mPOIInfoLayout: FrameLayout? = null
    private var mPOIInfoPicNIV: NetworkImageView? = null
    private var mPOIInfoContentTV: TextView? = null
    private var cameraMoveTimes = 0
    private var mIsMapInited = false
    private var mMapFragment: SupportMapFragment? = null
    private var mEventBus: EventBus? = null
    private var mOGService: OGService? = null
    private var mOmniClusterRender: OmniClusterRender? = null
    private var mPOIInfoHeaderLayout: RelativeLayout? = null
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var mPOIInfoIconCNIV: CircleNetworkImageView? = null
    private var mPOIInfoArrowIV: ImageView? = null
    private var mPOIInfoTitleTV: TextView? = null
    private var mPOIInfoNIV: NetworkImageView? = null
    private var mOriginalPOIMarker: Marker? = null
    private var mNavigationType: NavigationType? = null
    private var groundFloor: BuildingFloor? = null
    private val REQUEST_CODE = 1
    private val REQUEST_CODE_BLUETOOTH = 2
    private var searchIcon: ImageView? = null
    private var EndPOIId = ""
    private var NavigationRoutePOIListAllPosition = 0
    private var MarkerListPosition = 0
    private var mNavigationMarker: Marker? = null
    private var mapReadyNavi = false
    private var titleSetting = false
    private var headerNaviTV: TextView? = null
    private var headerNaviIV: ImageView? = null
    private var headerRouteLL: LinearLayout? = null
    private var gatherTV: TextView? = null
    private var stopNaviTV: TextView? = null
    private var stopNaviTitleTV: TextView? = null
    private var stopNaviRL: RelativeLayout? = null
    private var pointNaviLL: LinearLayout? = null
    private var startPointTV: TextView? = null
    private var endPointTV: TextView? = null
    private var changePointIV: ImageView? = null
    private var startNaviRL: LinearLayout? = null
    private var startNaviLL: LinearLayout? = null
    private var autoNaviLL: LinearLayout? = null
    private var priNormalLL: LinearLayout? = null
    private var priElevatorLL: LinearLayout? = null
    private var mWakeLock: WakeLock? = null
    private var distanceLine = 0.0
    private var distanceSegment = 0.0
    private var poiTypeSelectorList: RecyclerView? = null
    private var select_category = -1
    private var poiTypeListAdapter: PoiTypeListAdapter? = null
    private var selectInMapCenter_tv: TextView? = null
    private var selectInMapCenter_iv: ImageView? = null

    var autoNaviIdx = 0
    var autoNaviPoint = ArrayList<LatLng>()
    var autoNaviPointFloor = ArrayList<Int>()
    var autoNaviTurnHint = ArrayList<String>()
    var autoNaviTurnImg = ArrayList<Int>()

    var mDirectionHintLL: LinearLayout? = null
    var mDirectionHintIV: ImageView? = null
    var mDirectionHintTV: TextView? = null
    var currentHint: String? = null
    var directionHint = "route:\n "

    var isReversePoint = false
    var isRouteNavi = false
    var isDirect = true
    var isSelectPos = false
    var selectPosLat = 0.0
    var selectPosLng = 0.0
    var selectPosFloor: OmniFloor? = null

    var positionBtn: ImageView? = null
    var mSensorManager: SensorManager? = null
    var mHeadingHandler: Handler? = null
    var azimuth = 0f
    val mRotationMatrix = FloatArray(16)
    private var mDeclination = 0f
    var heading: Double? = 0.0

    val mBTAdapter = BluetoothAdapter.getDefaultAdapter()
    var mBBHandlerThread: HandlerThread? = null
    var mBBHandler: Handler? = null
    val MSG_LE_START_SCAN = 1000
    val MSG_LE_STOP_SCAN = 1001
    val MSG_GET_DATA = 1002
    val MSG_STOP_SCAN = 1003
    var mLastSendBatteryMac: String? = null

    var isSelectGather = false;

    var keyword: String? = ""

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: OmniEvent) {
        when (event.type) {
            OmniEvent.TYPE_USER_OUTDOOR_LOCATION -> {
                if (!mIsAutoNavi) {
                    mLastLocation = event.obj as? Location
                    mIsIndoor = false
                    showUserPosition()
                }
                Log.e("LOG", "TYPE_USER_OUTDOOR_LOCATION NaviSDK" + mLastLocation!!.latitude)
                if (mLastLocation != null) {
                    val field = GeomagneticField(
                        mLastLocation!!.latitude.toFloat(),
                        mLastLocation!!.longitude.toFloat(),
                        mLastLocation!!.altitude.toFloat(),
                        System.currentTimeMillis()
                    )
                    mDeclination = field.declination
                }
            }
            OmniEvent.TYPE_USER_INDOOR_LOCATION -> {
                Log.e(NaviSDKText.LOG_TAG, "TYPE_USER_INDOOR_LOCATION")
                if (!mIsAutoNavi) {
                    mLastLocation = event.obj as? Location
                    mIsIndoor = true
                    showUserPosition()
                }

                if (mLastLocation != null) {
                    val field = GeomagneticField(
                        mLastLocation!!.latitude.toFloat(),
                        mLastLocation!!.longitude.toFloat(),
                        mLastLocation!!.altitude.toFloat(),
                        System.currentTimeMillis()
                    )
                    mDeclination = field.declination
                }
            }
            OmniEvent.TYPE_FLOOR_PLAN_CHANGED -> {
                val floorPlanId = event.content
                if (instance.containsFloor(this, floorPlanId!!)) {
                    Log.e(NaviSDKText.LOG_TAG, "TYPE_FLOOR_PLAN_CHANGED id : $floorPlanId")
                    fetchFloorPlan(
                        floorPlanId,
                        true,
                        instance.getFloorNumberByPlanId(this, floorPlanId)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(NaviSDKText.LOG_TAG, "onCreate")
//        NetworkManager.DOMAIN_NAME = intent.extras!!.getString(ARG_KEY_DOMAIN_NAME)!!
//        NetworkManager.ENCRYPT_KEY = intent.extras!!.getString(ARG_KEY_ENCRYPT_KEY)!!
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val allSensors = sm.getSensorList(Sensor.TYPE_ALL)
        for (s in allSensors) {
            when (s.type) {
                Sensor.TYPE_ACCELEROMETER -> withAccelerometer = true
                Sensor.TYPE_GYROSCOPE -> withGyroscope = true
            }
        }
        if (!withAccelerometer || !withGyroscope) {
            DialogTools.instance.showErrorMessage(
                this,
                getString(R.string.error_dialog_title_text_normal),
                resources.getString(R.string.without_sensor_hint)
            )
        }
        setContentView(R.layout.navisdk_activity_main)
        if (mEventBus == null) {
            mEventBus = EventBus.getDefault()
        }
        mEventBus!!.register(this)
        if (mTimeHandler == null) {
            mTimeHandler = Handler()
            mTimeHandler!!.postDelayed(mTimeRunner!!, 1000)
        }
        map_bearing = intent.getFloatExtra(ARG_KEY_MAP_BEARING, 0f)
        autoHeading = intent.getBooleanExtra(ARG_KEY_AUTO_HEADING, true)
        naviDirect = intent.getBooleanExtra(ARG_KEY_NAVIGATE_DIRECT, false)

        if (intent.extras != null && intent.extras!!.getSerializable(ARG_KEY_SEARCH_POI) != null)
            searchPOI = intent.extras!!.getSerializable(ARG_KEY_SEARCH_POI) as POI
        if (intent.extras != null && intent.extras!!.getSerializable(ARG_KEY_QUICK_TYPE) != null)
            quickType = intent.extras!!.getString(ARG_KEY_QUICK_TYPE)!!
        if (intent.extras != null && intent.extras!!.getSerializable(ARG_KEY_KEYWORD) != null) {
            keyword = intent.extras!!.getString(ARG_KEY_KEYWORD)!!
            val searchIntent = Intent()
            searchIntent.setClass(this@NaviSDKActivity, PoiSearchActivity::class.java)
            searchIntent.putExtra(ARG_KEY_KEYWORD, keyword)
            startActivityForResult(searchIntent, REQUEST_CODE)
        }
        if (intent.extras != null && intent.extras!!.getSerializable(ARG_KEY_THEME_ID) != null) {
            themeId = intent.extras!!.getSerializable(ARG_KEY_THEME_ID) as ArrayList<Int>
            themePlanId = intent.extras!!.getString(ARG_KEY_THEME_PLAN_ID)!!
        }
        if (intent.extras != null && intent.extras!!.getSerializable(COLLECTION_PLACE) != null) {
            isSelectGather = true
        }

        Log.e(NaviSDKText.LOG_TAG, "naviDirect$naviDirect")
        instance.initAllBuildingsData(this)
        checkLocationService()
        //        checkBluetoothOn();
        initView()
        startScanBeacon()

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        mSensorManager!!.registerListener(
            mSensorEventListener,
            rensor,
            SensorManager.SENSOR_DELAY_GAME,
            mHeadingHandler
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.e(NaviSDKText.LOG_TAG, "onNewIntent")
        autoHeading = getIntent().getBooleanExtra(ARG_KEY_AUTO_HEADING, true)
        naviDirect = getIntent().getBooleanExtra(ARG_KEY_NAVIGATE_DIRECT, false)
        Log.e(NaviSDKText.LOG_TAG, "naviDirect$naviDirect")
    }

    fun startScanBeacon() {
        mBBHandlerThread = HandlerThread("HandlerThread")
        mBBHandlerThread!!.start()
        mBBHandler = object : Handler(mBBHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_LE_START_SCAN -> if (mBTAdapter.isEnabled) {
                        mBTAdapter.startLeScan(this@NaviSDKActivity)
                    }
                    MSG_LE_STOP_SCAN -> if (mBTAdapter.isEnabled) {
                        mBTAdapter.stopLeScan(this@NaviSDKActivity)
                    }
                    MSG_STOP_SCAN -> {
                        mBBHandler!!.removeMessages(MSG_LE_START_SCAN)
                        mBBHandler!!.removeMessages(MSG_LE_STOP_SCAN)
                        if (mBTAdapter.isEnabled) {
                            mBTAdapter.stopLeScan(this@NaviSDKActivity)
                        }
                    }
                    MSG_GET_DATA -> {
                    }
                }
                super.handleMessage(msg)
            }
        }
        mBBHandler!!.sendEmptyMessage(MSG_LE_START_SCAN)
    }

    private fun checkBluetoothOn() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_CODE_BLUETOOTH)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (mMapFragment != null) {
            mMapFragment!!.onLowMemory()
        }
    }

    override fun onResume() {
        if (mMapFragment != null) {
            mMapFragment!!.onResume()
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mMapFragment != null) {
            mMapFragment!!.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMapFragment != null) {
            mMapFragment!!.onDestroy()
        }
        if (mEventBus != null) {
            mEventBus!!.unregister(this)
        }
        if (mTimeHandler != null && mTimeRunner != null) {
            mTimeHandler!!.removeCallbacks(mTimeRunner)
        }
        if (mOGService != null) {
            mOGService!!.stopService()
            mOGService!!.destroy()
            Log.e(NaviSDKText.LOG_TAG, "mOGService.destroy()")
        }
        if (mLastLocation != null) {
            mLastLocation = null
        }

        if (mHeadingHandler != null) {
            mHeadingHandler!!.removeCallbacksAndMessages(null)
            mHeadingHandler = null
        }
    }

    private fun initView() {
        findViewById<View>(R.id.map_content_view_fl_action_bar_back).setOnClickListener {
            if (isSelectPos) {
                isSelectPos = false
                selectInMapCenter_tv!!.visibility = View.GONE
                selectInMapCenter_iv!!.visibility = View.GONE
                startNaviRL!!.visibility = View.VISIBLE
                pointNaviLL!!.visibility = View.VISIBLE
            } else if (isRouteNavi)
                leaveNavigation()
            else
                finish()
        }
        mMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_content_view_map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        mFloorLevelTV = findViewById(R.id.map_content_view_tv_floor_level)
        mFloorsLayoutNew = findViewById(R.id.map_content_view_floor_ll)
        mMaskLayout = findViewById(R.id.map_content_view_mask)
        mFloorLevelTV?.setOnClickListener(View.OnClickListener {
            if (floorNumber.isNotEmpty()) {
                mFloorsLayoutNew?.setVisibility(if (mFloorsLayoutNew?.isShown()!!) View.GONE else View.VISIBLE)
                mFloorLevelTV?.visibility = View.GONE
                mMaskLayout?.setVisibility(if (mMaskLayout?.isShown()!!) View.GONE else View.VISIBLE)
            }
        })
        mMaskLayout?.setOnClickListener(View.OnClickListener {
            mFloorsLayoutNew?.setVisibility(if (mFloorsLayoutNew?.isShown()!!) View.GONE else View.VISIBLE)
            mMaskLayout?.setVisibility(if (mMaskLayout?.isShown()!!) View.GONE else View.VISIBLE)
            mFloorLevelTV?.visibility = View.VISIBLE
        })

        stopNaviRL = findViewById(R.id.map_content_view_stop_navi_rl)
        stopNaviTV = findViewById(R.id.stop_navi_rl_tv_navi)
        stopNaviTV?.setOnClickListener {
            leaveNavigation()
        }
        stopNaviTitleTV = findViewById(R.id.stop_navi_rl_title)

        pointNaviLL = findViewById(R.id.map_content_view_navi_point_ll)
        startPointTV = findViewById(R.id.map_content_view_start_point_tv)
        startPointTV?.setOnClickListener {
            if (!isReversePoint) {
                isSelectPos = true
                selectInMapCenter_tv!!.visibility = View.VISIBLE
                selectInMapCenter_iv!!.visibility = View.VISIBLE
                startNaviRL!!.visibility = View.GONE
                pointNaviLL!!.visibility = View.GONE
            }
        }
        endPointTV = findViewById(R.id.map_content_view_end_point_tv)
        changePointIV = findViewById(R.id.map_content_view_change_point_iv)
        changePointIV?.setOnClickListener {
            isReversePoint = !isReversePoint
            val text = startPointTV!!.text
            startPointTV!!.text = endPointTV!!.text
            endPointTV!!.text = text
            navigationData
        }

        selectInMapCenter_tv = findViewById(R.id.map_content_view_selectPlaceInMapCenter_tv)
        selectInMapCenter_iv = findViewById(R.id.map_content_view_selectPlaceInMapCenter_iv)
        selectInMapCenter_tv?.setOnClickListener {
            isSelectPos = true
            selectInMapCenter_tv!!.visibility = View.GONE
            selectInMapCenter_iv!!.visibility = View.GONE
            startNaviRL!!.visibility = View.VISIBLE
            pointNaviLL!!.visibility = View.VISIBLE
            mMap!!.cameraPosition.target.also { center ->
                selectPosLat = center.latitude
                selectPosLng = center.longitude
                selectPosFloor = instance.currentShowFloor
                startPointTV!!.text = "任意位置"
                Log.e(LOG_TAG, "  center.latitude" + center.latitude)
                Log.e(LOG_TAG, "  center.longitude" + center.longitude)
                navigationData
            }
        }

        startNaviRL = findViewById(R.id.map_content_view_start_navi_rl)
        startNaviLL = findViewById(R.id.start_navi_rl_start_navi_ll)
        startNaviLL?.setOnClickListener(View.OnClickListener {
            stopNaviRL!!.visibility = View.VISIBLE
            startNaviRL!!.visibility = View.GONE
            pointNaviLL!!.visibility = View.GONE
            mDirectionHintLL!!.visibility = View.VISIBLE
            poiTypeSelectorList!!.visibility = View.INVISIBLE
            isRouteNavi = false
        })
        autoNaviLL = findViewById(R.id.start_navi_rl_auto_navi_ll)
        autoNaviLL?.setOnClickListener(View.OnClickListener {
            mIsAutoNavi = true
            isRouteNavi = false
            stopNaviRL!!.visibility = View.VISIBLE
            startNaviRL!!.visibility = View.GONE
            pointNaviLL!!.visibility = View.GONE
            mDirectionHintLL!!.visibility = View.VISIBLE
            poiTypeSelectorList!!.visibility = View.INVISIBLE

            if (mNavigationMode == NaviMode.USER_IN_NAVIGATION)
                startAutoNavi()
            else {
                startNaviLL!!.performClick()
                val handler = Handler()
                handler.postDelayed({
                    startAutoNavi()
                }, 2000)
            }
        })

        priNormalLL = findViewById(R.id.start_navi_rl_priority_normal_ll)
        priElevatorLL = findViewById(R.id.start_navi_rl_priority_elevator_ll)
        priNormalLL?.setOnClickListener {
            priNormalLL?.setBackgroundResource(R.drawable.solid_round_rectangle_blue_f5)
            priElevatorLL?.setBackgroundResource(R.drawable.stroke_round_rectangle_white)
            mNavigationType = NavigationType.NORMAL
            navigationData
        }
        priElevatorLL?.setOnClickListener {
            priNormalLL?.setBackgroundResource(R.drawable.stroke_round_rectangle_white)
            priElevatorLL?.setBackgroundResource(R.drawable.solid_round_rectangle_blue_f5)
            mNavigationType = NavigationType.ACCESSIBLE
            navigationData
        }

//        mFloorLevelTV?.setOnClickListener {
//            Log.e(LOG_TAG, "mFloorsLayout onClick")
//            mFloorsLayout?.visibility = if (mFloorsLayout?.isShown!!) View.GONE else View.VISIBLE
//        }

        positionBtn = findViewById(R.id.map_content_view_fab_current_position)
        positionBtn!!.setOnClickListener {
            if (mMap != null && mLastLocation != null) {
                if (isRouteNavi || mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
                    if (!isDirect) {
                        positionBtn!!.setImageResource(R.mipmap.nav_location_direct)
                        mNavigationMarker!!.rotation = heading!!.toFloat()
                    } else {
                        positionBtn!!.setImageResource(R.mipmap.nav_location_b)
                    }
                    isDirect = !isDirect
                } else {
                    val current = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
                    Log.e(
                        NaviSDKText.LOG_TAG,
                        "mLastLocation.getLatitude()" + mLastLocation!!.latitude
                    )
                    Log.e(
                        NaviSDKText.LOG_TAG,
                        "mLastLocation.getLongitude()" + mLastLocation!!.longitude
                    )
                    addUserMarker(current, mLastLocation!!)
                    val userCurrentFloorPlanId = instance.userCurrentFloorPlanId
                    if (mIsIndoor) {
                        fetchFloorPlan(
                            userCurrentFloorPlanId,
                            false,
                            instance.getFloorNumberByPlanId(
                                this@NaviSDKActivity,
                                userCurrentFloorPlanId!!
                            )
                        )
                    } else {
                        fetchFloorPlan(groundFloor!!.floorPlanId, false, "1")
                    }
                    mMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            current,
                            NaviSDKText.MAP_ZOOM_LEVEL.toFloat()
                        )
                    )
                }
            }
        }
        mPOIInfoLayout = findViewById(R.id.ntsdk_activity_main_poi_info)
        mPOIInfoPicNIV = mPOIInfoLayout?.findViewById(R.id.poi_info_view_niv)
        mPOIInfoContentTV = mPOIInfoLayout?.findViewById(R.id.poi_info_view_desc)
        mPOIInfoHeaderLayout = mPOIInfoLayout?.findViewById(R.id.poi_info_view_header)
        mPOIInfoIconCNIV = findViewById(R.id.item_poi_header_iv_icon)
        mPOIInfoArrowIV = findViewById(R.id.poi_info_header_view_iv_arrow)
        mPOIInfoTitleTV = mPOIInfoLayout?.findViewById(R.id.poi_info_header_view_tv_title)
        mPOIInfoNIV = mPOIInfoLayout?.findViewById(R.id.poi_info_header_view_niv)

        mPOIInfoArrowIV?.setOnClickListener {
            if (mBottomSheetBehavior!!.state == STATE_EXPANDED) {
                mBottomSheetBehavior!!.state = STATE_COLLAPSED
                mPOIInfoArrowIV!!.setImageResource(R.mipmap.icon_arrow_up)
            } else {
                mBottomSheetBehavior!!.state = STATE_EXPANDED
                mPOIInfoArrowIV!!.setImageResource(R.mipmap.icon_arrow_down)
            }
        }

        gatherTV = mPOIInfoHeaderLayout?.findViewById(R.id.poi_info_header_gather_point)
        gatherTV!!.setOnClickListener {
            val intent = Intent()
            intent.putExtra(NaviSDKText.INTENT_EXTRAS_GATHER_POI, mCurrentSelectedPOI)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        headerRouteLL = mPOIInfoHeaderLayout?.findViewById(R.id.poi_info_header_view_route_ll)
        headerRouteLL?.setOnClickListener {
            startNaviRL!!.visibility = View.VISIBLE
            pointNaviLL!!.visibility = View.VISIBLE
            collapseBottomSheet()
            isRouteNavi = true
            positionBtn!!.setImageResource(R.mipmap.nav_location_direct)
            navigationData
        }
        if (isSelectGather) {
            gatherTV!!.visibility = View.VISIBLE
            headerRouteLL!!.visibility = View.GONE
        } else {
            gatherTV!!.visibility = View.GONE
            headerRouteLL!!.visibility = View.VISIBLE
        }

        val fl = mPOIInfoLayout?.parent as FrameLayout
        mBottomSheetBehavior = BottomSheetBehavior.from(fl)
        mBottomSheetBehavior?.isHideable = false
        mBottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mPOIInfoLayout?.requestLayout()
                    mPOIInfoArrowIV!!.setImageResource(R.mipmap.icon_arrow_down)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mPOIInfoLayout?.requestLayout()
                    mPOIInfoArrowIV!!.setImageResource(R.mipmap.icon_arrow_up)
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })
        mNaviInfoRL = findViewById(R.id.ntsdk_activity_main_navi_view)
        mNaviInfoIconCNIV = mNaviInfoRL?.findViewById(R.id.navigation_info_view_cniv)
        mNaviInfoTitleTV = mNaviInfoRL?.findViewById(R.id.navigation_info_view_tv_title)
        mNaviInfoRL?.findViewById<View>(R.id.navigation_info_view_tv_leave_navi)
            ?.setOnClickListener { leaveNavigation() }
        mNaviInfoRL?.findViewById<View>(R.id.navigation_info_view_iv_leave_navi)
            ?.setOnClickListener { leaveNavigation() }
        searchIcon = findViewById(R.id.map_content_view_iv_action_bar_search)
        searchIcon?.setOnClickListener(View.OnClickListener {
            val searchIntent = Intent()
            searchIntent.setClass(this@NaviSDKActivity, PoiSearchActivity::class.java)
            startActivityForResult(searchIntent, REQUEST_CODE)
            collapseBottomSheet()
        })

        poiTypeSelectorList = findViewById(R.id.poiTypeSelectorList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        poiTypeSelectorList!!.layoutManager = layoutManager

        if (themeId.isNotEmpty()) {
            poiTypeSelectorList!!.visibility = View.INVISIBLE
        }

        LocationApi.instance.getAppCategory(this,
            object : NetworkManagerListener<Array<PoiCategory>> {
                override fun onSucceed(response: Array<PoiCategory>?) {
                    poiTypeListAdapter = PoiTypeListAdapter(response!!)
                    runOnUiThread {
                        poiTypeSelectorList!!.adapter = poiTypeListAdapter
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                }

            })

        mDirectionHintLL = findViewById(R.id.map_content_view_rl_navi_direction)
        mDirectionHintTV = findViewById(R.id.map_content_view_tv_turn_distance_hint)
        mDirectionHintIV = findViewById(R.id.map_content_view_iv_navi_direction)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                updateSelectedPOI(data!!.extras!![NaviSDKText.INTENT_EXTRAS_SELECTED_POI] as POI?)
            }
        }
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }

    private fun startAutoNavi() {
        Log.e("LOG", "startAutoNavi")
        var distance: Int?
        var vector_lat: Double?
        var vector_lng: Double?
        var base_lat: Double?
        var base_lng: Double?

        mDirectionHintLL!!.visibility = View.VISIBLE
        poiTypeSelectorList!!.visibility = View.INVISIBLE

        if (mCurrentRouteList != null) {
            for (index in 0 until mCurrentRouteList!!.size - 1) {
                distance = SphericalUtil.computeDistanceBetween(
                    mCurrentRouteList!![index].location,
                    mCurrentRouteList!![index + 1].location
                ).toInt()
                vector_lat = (mCurrentRouteList!![index + 1].latitude!!.toDouble() -
                        mCurrentRouteList!![index].latitude!!.toDouble()) / distance
                vector_lng = (mCurrentRouteList!![index + 1].longitude!!.toDouble() -
                        mCurrentRouteList!![index].longitude!!.toDouble()) / distance
                base_lat = mCurrentRouteList!![index].latitude!!.toDouble()
                base_lng = mCurrentRouteList!![index].longitude!!.toDouble()

                if (mCurrentRouteList!![index].floorNumber == mCurrentRouteList!![index + 1].floorNumber) {
                    for (i in 0 until distance) {
                        autoNaviPoint.add(LatLng(base_lat, base_lng))
                        autoNaviPointFloor.add(mCurrentRouteList!![index].floorNumber!!.toInt())
                        base_lat += vector_lat
                        base_lng += vector_lng

                        autoNaviTurnHint.add(mCurrentRouteList!![index].turnHint.toString())
                        autoNaviTurnImg.add(mCurrentRouteList!![index].turnImg)
                    }
                }
            }

//            var heading = -1.0
            var last_heading = -1.0
            var changeFloor = false
            ThreadPoolHandler.runOnMain(object : Runnable {
                override fun run() {
                    if (autoNaviIdx < autoNaviPoint.size && mIsAutoNavi) {
                        if (instance.currentShowFloor!!.floorLevel != autoNaviPointFloor[autoNaviIdx].toString()) {
                            fetchFloorPlan(
                                instance.getFloorPlanIdByNumber(
                                    this@NaviSDKActivity,
                                    autoNaviPointFloor[autoNaviIdx]
                                ), false,
                                autoNaviPointFloor[autoNaviIdx].toString()
                            )
                        }
                        Log.e("LOG", "autoNaviPointFloor" + autoNaviPointFloor[autoNaviIdx])
                        if (autoNaviIdx + 1 != autoNaviPoint.size) {
                            heading = SphericalUtil.computeHeading(
                                autoNaviPoint[autoNaviIdx], autoNaviPoint[autoNaviIdx + 1]
                            )

                            if (autoNaviPointFloor[autoNaviIdx] != autoNaviPointFloor[autoNaviIdx + 1]) {
                                Log.e(
                                    LOG_TAG,
                                    "getFloorPlanIdByNumber" + instance.getFloorPlanIdByNumber(
                                        this@NaviSDKActivity,
                                        autoNaviPointFloor[autoNaviIdx + 1]
                                    )
                                )
                                fetchFloorPlan(
                                    instance.getFloorPlanIdByNumber(
                                        this@NaviSDKActivity,
                                        autoNaviPointFloor[autoNaviIdx + 1]
                                    ), false,
                                    autoNaviPointFloor[autoNaviIdx + 1].toString()
                                )
                                changeFloor = true
                            }
                        }

                        if (mNavigationMarker == null) {
                            mNavigationMarker = mMap!!.addMarker(
                                MarkerOptions()
                                    .position(autoNaviPoint[autoNaviIdx])
                                    .rotation(heading!!.toFloat())
                                    .flat(true)
                                    .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_path_arrow_b))
                            );
                        } else {
                            if (changeFloor)
                                mNavigationMarker = mMap!!.addMarker(
                                    MarkerOptions()
                                        .position(autoNaviPoint[autoNaviIdx])
                                        .rotation(heading!!.toFloat())
                                        .flat(true)
                                        .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_path_arrow_b))
                                );
                            mNavigationMarker!!.position = autoNaviPoint[autoNaviIdx];
                            if (isDirect)
                                mNavigationMarker!!.rotation = heading!!.toFloat();
                            mNavigationMarker!!.zIndex = NaviSDKText.MARKER_Z_INDEX.toFloat();
                        }
                        mNavigationMarker!!.isVisible = true
                        val zoomLevel = mMap!!.cameraPosition.zoom.toInt()
                        val cameraPosition: CameraPosition =
                            CameraPosition.Builder()
                                .target(autoNaviPoint[autoNaviIdx])
                                .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                                .bearing(heading!!.toFloat())
                                .build()

                        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                        if (mCurrentRouteList != null) {
                            Log.e(NaviSDKText.LOG_TAG, "autoNaviIdx$autoNaviIdx")
                            if (autoNaviIdx + 1 == autoNaviPoint.size) {
                                DialogTools.instance.showErrorMessage(
                                    this@NaviSDKActivity,
                                    R.string.dialog_title_hint,
                                    R.string.dialog_message_arrive_destination
                                )
                                leaveNavigation()
                            }
                        }

                        if (autoNaviTurnHint.size != 0) {
                            mDirectionHintTV!!.text = autoNaviTurnHint[autoNaviIdx]
                            mDirectionHintIV!!.setImageResource(autoNaviTurnImg[autoNaviIdx])
                        }

                        autoNaviIdx++
                        if (changeFloor) {
                            changeFloor = false
                            ThreadPoolHandler.runOnMain(this, 2000)
                        } else {
                            if (abs(last_heading - heading!!) > 30) {
                                ThreadPoolHandler.runOnMain(this, 1000)
                            } else {
                                ThreadPoolHandler.runOnMain(this, 500)
                            }
                            last_heading = heading!!
                        }
                    }
                }
            }, 500)
        }
    }

    private fun updateSelectedPOI(selectedPoi: POI?) {
        Log.e(NaviSDKText.LOG_TAG, "updateSelectedPOI")
        val floor = instance.getSearchFloorPlanId(this@NaviSDKActivity, selectedPoi!!.id)
        fetchFloorPlan(floor!!.floorPlanId, false, floor.order!!)
        val buildingId = instance.getBuildingIdByFloorPlanId(this, floor.floorPlanId!!)
        val handler = Handler()
        handler.postDelayed({
            itemList =
                instance.getClusterListByBuildingId(buildingId) as MutableList<OmniClusterItem<*>>?
            Log.e(NaviSDKText.LOG_TAG, "itemList.size()" + itemList!!.size)
            for (i in itemList!!.indices) {
                if (itemList!![i].pOI.id == selectedPoi.id) {
                    selectPOI(itemList!![i])
                    break
                }
            }
        }, 1000)
    }

    private fun selectPOI(item: OmniClusterItem<*>) {
        Log.e(NaviSDKText.LOG_TAG, "selectPOI" + item.title)
        if (mOmniClusterRender != null) {
            val marker = mOmniClusterRender!!.getMarker(item)
            val poi = item.pOI
            if (marker != null) {
                if (marker.tag == null) {
                    marker.tag = poi
                }
                marker.showInfoWindow()
                marker.setIcon(BitmapDescriptorFactory.fromResource(poi.getPOIIconRes(true)))
                showPOIInfo(marker)
                mMap!!.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(mMap!!.cameraPosition)
                            .bearing(map_bearing)
                            .target(LatLng(poi.latitude, poi.longitude))
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                            .build()
                    )
                )
            }
        }
    }

    private fun showPOIInfo(marker: Marker) {
        Log.e(NaviSDKText.LOG_TAG, "marker getTitle : " + marker.title)
        if (!TextUtils.isEmpty(marker.title) && !isRouteNavi) {
            val poi = marker.tag as POI?
            mCurrentSelectedPOI = poi
            val naviTVParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            naviTVParams.setMargins(0, 0, 0, Tools.instance.dpToIntPx(applicationContext, 16f))
            naviTVParams.gravity = Gravity.CENTER

            NetworkManager.instance.setNetworkImage(
                this,
                mPOIInfoIconCNIV!!,
                poi!!.urlToPoisImage,
                poi.getPOIIconRes(false),
                poi.getPOIIconRes(false)
            )
            if (poi.is_office != null && poi.is_office!!) {
                mPOIInfoContentTV!!.text = poi.office!![0].desc
                if (poi.office[0].image.isNotEmpty()) {
                    NetworkManager.instance.setNetworkImage(
                        this,
                        mPOIInfoNIV!!,
                        poi.office[0].image
                    )
                    mPOIInfoNIV!!.visibility = View.VISIBLE
                } else {
                    mPOIInfoNIV!!.visibility = View.GONE
                }
            } else
                mPOIInfoContentTV!!.text = poi.desc
            NetworkManager.instance.setNetworkImage(this, mPOIInfoPicNIV!!, poi.logo)

            var floorNumber: String
            floorNumber = poi.number!!.toString()
            floorNumber = if (floorNumber.contains("-"))
                floorNumber.replace("-", "B") else floorNumber + "F"
            endPointTV!!.text = floorNumber + " - " + poi.name
            mPOIInfoTitleTV!!.text = floorNumber + " " + poi.name
            stopNaviTitleTV!!.text = floorNumber + " " + poi.name

            runOnUiThread {
                val handler = Handler()
                handler.postDelayed({
                    mBottomSheetBehavior!!.peekHeight = mPOIInfoHeaderLayout!!.height
                }, 200)
            }
        }
    }

    private fun collapseBottomSheet() {
        Log.e(NaviSDKText.LOG_TAG, "collapseBottomSheet$mNavigationMode")
        if (mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
            return
        }
        mBottomSheetBehavior!!.peekHeight = 0
        if (mOriginalPOIMarker != null) {
            mOriginalPOIMarker = null
        }
    }

    private val navigationData: Unit
        get() {
//            if (mNavigationMode == NaviMode.USER_IN_NAVIGATION && mEndPOI != null) {
//                leaveNavigation()
//            }
//            setNavigationMode(NaviMode.USER_IN_NAVIGATION)
            if (mEndPOI == null && mCurrentSelectedPOI != null) {
                mEndPOI = mCurrentSelectedPOI
            }
            if (mOriginalPOIMarker != null) {
                Log.e(NaviSDKText.LOG_TAG, "mIsIndoor : $mIsIndoor")
                if (mIsIndoor) {
                    getUserIndoorLocationToOutdoorPRoute(
                        mUserMarker!!.position.latitude,
                        mUserMarker!!.position.longitude,
                        mOriginalPOIMarker!!.position.latitude,
                        mOriginalPOIMarker!!.position.longitude
                    )
                }
            } else {
                if (NaviSDKText.isTestMode || mLastLocation == null) {
                    Log.e(NaviSDKText.LOG_TAG, "#1 TODOHere")
                    getLocationToPRoute(START_LOCATION.latitude, START_LOCATION.longitude)
                } else {
                    if (instance.isInBuilding(this)) {
                        getLocationToPRoute(mLastLocation!!.latitude, mLastLocation!!.longitude)
                    } else {
                        val floor = instance.getMainGroundFloorPlanId(this)
                        if (isSelectPos) {
                            getLocationToPRoute(selectPosLat, selectPosLng)
                        } else if (floor != null && instance.getEntrancePOI(floor) != null) {
                            val entrancePOI = instance.getEntrancePOI(floor)
                            getLocationToPRoute(entrancePOI!!.latitude, entrancePOI.longitude)
                        } else {
                            getLocationToPRoute(START_LOCATION.latitude, START_LOCATION.longitude)
                        }
                    }
                }
            }

            ThreadPoolHandler.runOnMain(Runnable {

                val userCurrentFloorPlanId = instance.userCurrentFloorPlanId
                Log.e(NaviSDKText.LOG_TAG, "userCurrentFloorPlanId : $userCurrentFloorPlanId")
                if (mIsIndoor) {
                    fetchFloorPlan(
                        userCurrentFloorPlanId,
                        false,
                        instance.getFloorNumberByPlanId(this, userCurrentFloorPlanId!!)
                    )
                } else {
                    Log.e(LOG_TAG, "isReversePoint" + isReversePoint)
                    if (isReversePoint) {
                        fetchFloorPlan(
                            instance.getFloorPlanIdByNumber(
                                this@NaviSDKActivity,
                                mCurrentSelectedPOI!!.number
                            ), false,
                            mCurrentSelectedPOI!!.number.toString()
                        )
                    } else if (isSelectPos) {
                        Log.e(LOG_TAG, "selectPosFloor" + selectPosFloor!!.floorLevel)
                        fetchFloorPlan(
                            selectPosFloor!!.floorPlanId, false,
                            selectPosFloor!!.floorLevel
                        )
                    } else if (groundFloor != null) {
                        fetchFloorPlan(groundFloor!!.floorPlanId, false, "1")
                    }
                }

                if (mCurrentRouteList != null)
                    mMap!!.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder(mMap!!.cameraPosition)
                                .bearing(map_bearing)
                                .target(mCurrentRouteList!![0].location)
                                .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                                .build()
                        )
                    )
            }, 1000)

        }

    private fun leaveNavigation() {
        Log.e(NaviSDKText.LOG_TAG, "leaveNavigation")
        if (mNavigationMode == NaviMode.USER_IN_NAVIGATION) {

            mIsAutoNavi = false
            autoNaviIdx = 0
            autoNaviPoint.clear()
            autoNaviPointFloor.clear()
            autoNaviTurnHint.clear()
            autoNaviTurnImg.clear()
            startNaviRL!!.visibility = View.GONE
            pointNaviLL!!.visibility = View.GONE
            stopNaviRL!!.visibility = View.GONE
            mDirectionHintLL!!.visibility = View.INVISIBLE
            mDirectionHintTV!!.text = ""
            mDirectionHintIV!!.setImageResource(0)
            poiTypeSelectorList!!.visibility = View.VISIBLE
            isReversePoint = false
            startPointTV!!.text = "您的位置"
            isSelectPos = false
            isRouteNavi = false
            positionBtn!!.setImageResource(R.mipmap.nav_location_b)
            isDirect = true

            setNavigationMode(NaviMode.NOT_NAVIGATION)
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            collapseBottomSheet()
            if (mWakeLock != null) mWakeLock!!.release()
            runOnUiThread { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            mapReadyNavi = false
            mEndPOI = null
            mCurrentRouteList = null
            if (mNaviStartMarker != null) {
                mNaviStartMarker!!.remove()
                mNaviStartMarker = null
            }
            if (mNaviEndMarker != null) {
                mNaviEndMarker!!.remove()
                mNaviEndMarker = null
            }
            if (mNavigationMarker != null) {
                mNavigationMarker!!.remove()
                mNavigationMarker = null
            }
            instance.clearAllPolyline()
            instance.clearAllArrowMarkers()
            val routePointsMap = instance.floorRoutePointsMap
            routePointsMap?.clear()
            Log.e(NaviSDKText.LOG_TAG, "mUserMarker == null ? " + (mUserMarker == null))
            val userCurrentFloorPlanId = instance.userCurrentFloorPlanId
            val cameraPosition: CameraPosition
            cameraPosition = if (TextUtils.isEmpty(userCurrentFloorPlanId) ||
                !TextUtils.isEmpty(userCurrentFloorPlanId) && !instance.isInBuilding(this) || mLastLocation == null
            ) {

                Log.e(LOG_TAG, "userCurrentFloorPlanId$userCurrentFloorPlanId")
                val floor = instance.getMainGroundFloorPlanId(this)
//                val entrancePOI = instance.getEntrancePOI(floor)
//                if (entrancePOI == null) {
//                    CameraPosition.Builder()
//                            .target(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
//                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
//                            // .bearing(mUserMarker.getRotation())
//                            .bearing(map_bearing)
////                            .tilt(0f)
//                            .build()
//                } else {
                fetchFloorPlan(floor!!.floorPlanId, false, floor.order!!)
                CameraPosition.Builder(mMap!!.cameraPosition)
                    .bearing(map_bearing)
//                            .target(LatLng(entrancePOI.latitude, entrancePOI.longitude))
                    .target(LatLng(floor.latitude, floor.longitude))
                    .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                    .build()
//                }
            } else {
                fetchFloorPlan(
                    userCurrentFloorPlanId,
                    true,
                    instance.getFloorNumberByPlanId(this, userCurrentFloorPlanId!!)
                )
                CameraPosition.Builder()
                    .target(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
                    .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                    // .bearing(mUserMarker.getRotation())
                    .bearing(map_bearing)
//                        .tilt(0f)
                    .build()
            }
            mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun getUserIndoorLocationToOutdoorPRoute(
        userLat: Double, userLng: Double,
        poiLat: Double, poiLng: Double
    ) {
        LocationApi.instance.getUserIndoorLocationToOutdoorPRoute(this,
            instance.getUserCurrentBuildingId(this)!!,
            instance.getUserCurrentFloorLevel(this),
            userLat, userLng, poiLat, poiLng,
            if (mNavigationType == NavigationType.ACCESSIBLE) "elevator" else "normal",
            object : NetworkManagerListener<Array<NavigationRoutePOI>> {
                override fun onSucceed(routePOIs: Array<NavigationRoutePOI>?) {
                    if (routePOIs!!.isNotEmpty()) {
                        startNavigation(routePOIs.toList())
                    } else {
                        DialogTools.instance.showErrorMessage(
                            this@NaviSDKActivity,
                            R.string.error_dialog_title_text_normal,
                            R.string.dialog_message_route_empty
                        )
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    DialogTools.instance.showErrorMessage(
                        this@NaviSDKActivity,
                        R.string.error_dialog_title_text_normal,
                        R.string.error_dialog_title_text_json_parse_error
                    )
                }
            }
        )
    }

    private fun getLocationToPRoute(startLat: Double, startLng: Double) {
        if (mEndPOI != null) {
            EndPOIId = mEndPOI!!.id.toString()
        }
        Log.e(NaviSDKText.LOG_TAG, "EndPOIId$EndPOIId")

        val floorLevel: String? = if (isSelectPos)
            instance.currentShowFloor!!.floorLevel
        else
            instance.getUserCurrentFloorLevel(this)

        LocationApi.instance.getUserLocationToIndoorPRoute(this,
            EndPOIId,
            startLat,
            startLng,
            floorLevel!!,
            if (mNavigationType == NavigationType.ACCESSIBLE) "elevator" else "normal",
            object : NetworkManagerListener<Array<NavigationRoutePOI>> {
                override fun onSucceed(routePOIs: Array<NavigationRoutePOI>?) {
                    if (routePOIs!!.isNotEmpty()) {
                        if (isReversePoint)
                            startNavigation(routePOIs.toList().reversed())
                        else
                            startNavigation(routePOIs.toList())
                    } else {
                        DialogTools.instance.showErrorMessage(
                            this@NaviSDKActivity,
                            R.string.error_dialog_title_text_normal,
                            R.string.dialog_message_route_empty
                        )
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    Log.e(NaviSDKText.LOG_TAG, "#3")
                }
            })
    }

    private fun showUserPosition() {
        Log.e(NaviSDKText.LOG_TAG, "showUserPosition")
        if (!mIsMapInited) {
            mIsMapInited = true
            mMapFragment!!.getMapAsync(this)
        }
        if (mLastLocation != null) {
            val current = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
            addUserMarker(current, mLastLocation!!)
        }
    }

    private fun checkMapInit() {
        if (!mIsMapInited) {
            mEventBus!!.post(OmniEvent(OmniEvent.TYPE_REQUEST_LAST_LOCATION, ""))
        }
    }

    override fun onInfoWindowClose(marker: Marker) {
        Log.e(NaviSDKText.LOG_TAG, "onInfoWindowClose")
        if (marker.tag == null) {
            return
        }

        if (!isRouteNavi) {
            marker.setIcon(
                BitmapDescriptorFactory.fromResource(
                    (marker.tag as POI?)!!.getPOIIconRes(
                        false
                    )
                )
            )
            collapseBottomSheet()

            if (mNavigationMode != NaviMode.USER_IN_NAVIGATION) {
                startNaviRL!!.visibility = View.GONE
                pointNaviLL!!.visibility = View.GONE
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.e(NaviSDKText.LOG_TAG, "onMapReady")
        mMap = googleMap
        mMap!!.setOnInfoWindowCloseListener(this)
        mMap!!.setOnCameraIdleListener {
            if (mClusterManager != null) {
                mClusterManager!!.cluster()
            }
            refreshMapTextMarkers(mMap!!.cameraPosition.zoom.toInt())
        }
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = false
        mMap!!.uiSettings.isMapToolbarEnabled = false
        mMap!!.setPadding(0, 140, 0, 0)
        mMap!!.isBuildingsEnabled = false
        mMap!!.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json
            )
        )
        mMap!!.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder(mMap!!.cameraPosition)
                    .bearing(map_bearing)
                    .target(START_LOCATION)
                    .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                    .build()
            )
        )

//        mMap!!.setOnMapClickListener { latLng ->
//            mLastLocation!!.latitude = latLng.latitude
//            mLastLocation!!.longitude = latLng.longitude
//            mIsIndoor = true
//            showUserPosition()
//        }

        groundFloor = instance.getMainGroundFloorPlanId(this)
        if (groundFloor != null) {
            fetchFloorPlan(groundFloor!!.floorPlanId, false, "1")
        }
        val userCurrentFloorPlanId = instance.userCurrentFloorPlanId
        Log.e(NaviSDKText.LOG_TAG, "userCurrentFloorPlanId$userCurrentFloorPlanId")
        setupClusterManager()
        if (TextUtils.isEmpty(userCurrentFloorPlanId) ||
            !TextUtils.isEmpty(userCurrentFloorPlanId) && !instance.isInBuilding(this)
        ) {
            val floor = instance.getMainGroundFloorPlanId(this)
            if (floor != null) {
                fetchFloorPlan(floor.floorPlanId, false, floor.order)
                mMap!!.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(mMap!!.cameraPosition)
                            .bearing(map_bearing)
                            .target(LatLng(floor.latitude, floor.longitude))
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                            .build()
                    )
                )
            }
        } else {
            if (mLastLocation != null) {
                fetchFloorPlan(
                    userCurrentFloorPlanId,
                    true,
                    instance.getFloorNumberByPlanId(this, userCurrentFloorPlanId!!)
                )
                val current = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
                addUserMarker(current, mLastLocation!!)
                mMap!!.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(mMap!!.cameraPosition)
                            .bearing(map_bearing)
                            .target(current)
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                            .build()
                    )
                )
            } else {
                mMap!!.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(mMap!!.cameraPosition)
                            .bearing(map_bearing)
                            .target(START_LOCATION)
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                            .build()
                    )
                )
            }
        }
        groundFloor = instance.getMainGroundFloorPlanId(this)

        if (searchPOI != null) {
            updateSelectedPOI(searchPOI)
        }
        if (quickType != null) {
            getEmergencyRoute(quickType!!)
        }
        if (themeId.isNotEmpty()) {
            fetchFloorPlan(
                themePlanId, false,
                instance.getFloorNumberByPlanId(this, themePlanId!!)
            )
        }
    }

    private fun setupClusterManager() {
        Log.e(NaviSDKText.LOG_TAG, " setupClusterManager()")
        if (mClusterManager == null) {
            mClusterManager = ClusterManager(this, mMap)
        }
        if (mOmniClusterRender == null) {
            mOmniClusterRender = OmniClusterRender(this, mMap, mClusterManager)
        }
        mClusterManager!!.renderer = mOmniClusterRender
        mClusterManager!!.setOnClusterItemClickListener(OnClusterItemClickListener { omniClusterItem ->
            if (mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
                return@OnClusterItemClickListener true
            }
            val marker = mOmniClusterRender!!.getMarker(omniClusterItem)
            if (marker.tag == null) {
                marker.tag = omniClusterItem.pOI
            }
            if (TextUtils.isEmpty(marker.title) || omniClusterItem.pOI.type!!.contains("Z") || isRouteNavi) {
                true
            } else {
                marker.setIcon(
                    BitmapDescriptorFactory.fromResource(
                        omniClusterItem.pOI.getPOIIconRes(
                            true
                        )
                    )
                )
                showPOIInfo(marker)
                false
            }
        })
        mClusterManager!!.setOnClusterClickListener { cluster ->
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    cluster.position,
                    Math.floor(mMap!!.cameraPosition.zoom + 1.toDouble()).toFloat()
                ),
                300,
                null
            )
            true
        }
        mMap!!.setOnMarkerClickListener(mClusterManager)
    }

    private fun fetchFloorPlan(id: String?, isEnterRegion: Boolean, floorLevel: String?) {
        runOnUiThread { fetchFloorPlan(id, isEnterRegion, false, floorLevel!!) }
    }

    private fun fetchFloorPlan(
        id: String?, isEnterRegion: Boolean,
        hasNavi: Boolean, floorLevel: String
    ) {
        Log.e(NaviSDKText.LOG_TAG, "fetchFloorPlan$id")
        if (!NetworkManager.instance.isNetworkAvailable(this)) {
//            DialogTools.getInstance().dismissProgress(this);
            Log.e(NaviSDKText.LOG_TAG, "--- fetchFloorPlan show no network")
            DialogTools.instance.showNoNetworkMessage(this)
            return
        }
        if (TextUtils.isEmpty(id)) {
            DialogTools.instance.showErrorMessage(
                this@NaviSDKActivity,
                "Loading building map error",
                "There's no floor plan id !"
            )
            return
        }
        instance.currentShowFloor = OmniFloor(floorLevel, id)
        Log.e(
            NaviSDKText.LOG_TAG,
            "fetchFloorPlan floorLevel : " + instance.currentShowFloor!!.floorLevel +
                    ", floorPlanId : " + instance.currentShowFloor!!.floorPlanId
        )
        Log.e(
            NaviSDKText.LOG_TAG,
            "getUserCurrentFloorLevel floorLevel : " + instance.getUserCurrentFloorLevel(this@NaviSDKActivity)
        )
        runOnUiThread {
            mFloorLevelTV!!.text =
                if (floorLevel.contains("-")) floorLevel.replace("-", "B") else floorLevel + "F"
            mFloorLevelTV!!.visibility = View.VISIBLE
        }
        if (isEnterRegion) {
            instance.setUserCurrentFloorLevel(floorLevel)
            instance.userCurrentFloorPlanId = id
        }
        if (mMap != null) {
            select_category = -1
            poiTypeListAdapter?.notifyDataSetChanged()

            var naviStartPOI: NavigationRoutePOI? = null
            var naviEndPOI: NavigationRoutePOI? = null
            Log.e(
                NaviSDKText.LOG_TAG, "mNaviStartMarker == null : " + (mNaviStartMarker == null) +
                        ", mNaviEndMarker == null : " + (mNaviEndMarker == null)
            )
            if (mNaviStartMarker != null) {
                naviStartPOI = mNaviStartMarker!!.tag as NavigationRoutePOI?
            }
            if (mNaviEndMarker != null) {
                naviEndPOI = mNaviEndMarker!!.tag as NavigationRoutePOI?
            }
            mMap!!.clear()
            if (mUserMarker != null) {
                mUserMarker!!.remove()
                mUserMarker = null
            }
            if (mLastLocation != null) {
                if (instance.currentShowFloor!!.floorLevel
                    == instance.getUserCurrentFloorLevel(this@NaviSDKActivity)
                ) {
                    addUserMarker(
                        LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude),
                        mLastLocation!!
                    )
                }
            }
            if (naviStartPOI != null && mNaviStartMarker != null) {
                mNaviStartMarker!!.tag = naviStartPOI
            }
            if (naviEndPOI != null && mNaviEndMarker != null) {
                mNaviEndMarker!!.tag = naviEndPOI
                mNaviEndMarker!!.isVisible = naviStartPOI!!.floorNumber == naviEndPOI.floorNumber
            }
            val tileProvider: TileProvider =
                object : UrlTileProvider(NaviSDKText.TILE_WIDTH, NaviSDKText.TILE_HEIGHT) {
                    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                        val s = String.format(
                            NetworkManager.DOMAIN_NAME + "map/tile/%s/%d/%d/%d.png",
                            id, zoom, x, y
                        )
                        return if (!checkTileExists(x, y, zoom)) {
                            null
                        } else try {
                            URL(s)
                        } catch (e: MalformedURLException) {
                            Log.e(
                                NaviSDKText.LOG_TAG, """
     getTileUrl get exception message === ${e.message}
     cause === ${e.cause}
     localizedMessage === ${e.localizedMessage}
     """.trimIndent()
                            )
                            throw AssertionError(e)
                        }
                    }
                }
            val buildingId = instance.getBuildingIdByFloorPlanId(this@NaviSDKActivity, id!!)
            if (mTileOverlayMap != null) {
                // when floor changed and in the same building, remove tile overlay
                val previousTile = mTileOverlayMap!![buildingId]
                if (previousTile != null) {
                    previousTile.remove()
                    previousTile.clearTileCache()
                }
            } else {
                mTileOverlayMap = HashMap()
            }

            // add current floor tile overlay
            val tile = mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider))
            mTileOverlayMap!![buildingId] = tile
            Log.e(
                NaviSDKText.LOG_TAG,
                "hasNavi : " + hasNavi + ", mCurrentRouteList = null : " + (mCurrentRouteList == null)
            )
            if (hasNavi || mCurrentRouteList != null) {
                if (instance.currentShowFloor!!.floorLevel != instance.getUserCurrentFloorLevel(this@NaviSDKActivity)) {
                    mapReadyNavi = false
                }
                startNavigation(mCurrentRouteList)
            }
            addPOIMarkers(buildingId, id)

            // show the floor's route poly line
            if (mNavigationMode != NaviMode.NOT_NAVIGATION || isRouteNavi) {
                showPolylineByFloorNumber(floorLevel)
            }
            val userCurrentFloorLevel = instance.getUserCurrentFloorLevel(this@NaviSDKActivity)
            if (mUserMarker != null) {
                mUserMarker?.setVisible(userCurrentFloorLevel == floorLevel)
            }
            if (mUserAccuracyCircle != null) {
                mUserAccuracyCircle!!.isVisible = userCurrentFloorLevel == floorLevel
            }

            val userCurrentFloorRoutePointList: List<LatLng>? =
                DataCacheManager.instance.getUserCurrentFloorRoutePointList(this)
            if (mNavigationMarker != null && userCurrentFloorRoutePointList != null && !userCurrentFloorRoutePointList.isEmpty()) {
                Log.e(
                    LOG_TAG,
                    "userCurrentFloorLevel.equals(String.valueOf(floorLevel))" + (userCurrentFloorLevel == floorLevel)
                )
                mNavigationMarker!!.isVisible = userCurrentFloorLevel == floorLevel

                if (!mIsIndoor && mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
                    mNavigationMarker!!.isVisible = naviStartPOI!!.floorNumber == floorLevel
                }
                if (mIsAutoNavi) {
                    mNavigationMarker!!.isVisible = true
                }
            }

            val floor = instance.getBuildingFloor(this, buildingId, id)
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(floor!!.latitude, floor.longitude))
                .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                .build()
            mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun startNavigation(routes: List<NavigationRoutePOI>?) {
        Log.e(NaviSDKText.LOG_TAG, "startNavigation")
        setNavigationMode(NaviMode.USER_IN_NAVIGATION)
        val pm = (getSystemService(Context.POWER_SERVICE) as PowerManager)
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WakeLock")
        mWakeLock?.setReferenceCounted(false)
        mWakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
        runOnUiThread { window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }

        mCurrentRouteList = routes!!
        if (!mapReadyNavi) {
            runOnUiThread {
                val handler = Handler()
                handler.postDelayed({
                    if (instance.currentShowFloor!!.floorLevel
                        == instance.getUserCurrentFloorLevel(this@NaviSDKActivity)
                    ) {
                        mapReadyNavi = true
                    }
                }, 2000)
            }
        }
        runOnUiThread {
            instance.clearAllPolyline()
            instance.clearAllArrowMarkers()
        }
        val pointList: MutableList<LatLng> = ArrayList()
        var previousPOI: NavigationRoutePOI? = null
        val colorIndex = 0
        for (i in routes.indices) {
            val poi = routes[i]
            val point = LatLng(
                java.lang.Double.valueOf(poi.latitude!!),
                java.lang.Double.valueOf(poi.longitude!!)
            )
            if (previousPOI == null || poi.floorNumber == previousPOI.floorNumber) {
                pointList.add(point)
                if (i == routes.size - 1) {
                    drawPolyline(
                        poi.floorNumber!!,
                        pointList,
                        this.resources.getColor(R.color.blue_47),
                        NaviSDKText.POLYLINE_WIDTH
                    )
                }
            } else {
                drawPolyline(
                    previousPOI.floorNumber!!,
                    pointList,
                    this.resources.getColor(R.color.blue_47),
                    NaviSDKText.POLYLINE_WIDTH
                )
                pointList.clear()
                pointList.add(point)
            }
            previousPOI = poi
        }

        val firstRoutePOI = routes[0]
        val lastRoutePOI = routes[routes.size - 1]
        val p = instance.getMainEntrancePOI(this)

        runOnUiThread {
            if (mNaviStartMarker != null) {
                mNaviStartMarker!!.remove()
                mNaviStartMarker = null
            }
            if (mNaviEndMarker != null) {
                mNaviEndMarker!!.remove()
                mNaviEndMarker = null
            }
            if (mNavigationMarker != null) {
                mNavigationMarker!!.remove()
                mNavigationMarker = null
            }

//            mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder(mMap!!.cameraPosition)
//                    .bearing(map_bearing)
//                    .target(firstRoutePOI.location)
//                    .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
//                    .build()))
            mNaviStartMarker = mMap!!.addMarker(
                MarkerOptions()
                    .flat(false) //                .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start_point))
                    .position(firstRoutePOI.location)
                    .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
            )
            Log.e(
                NaviSDKText.LOG_TAG,
                "mNaviStartMarker set tag firstRoutePOI == null : " + (false)
            )
            mNaviStartMarker?.tag = firstRoutePOI
            mNaviStartMarker?.isVisible = true
            mNaviEndMarker = mMap!!.addMarker(
                MarkerOptions()
                    .flat(false) //                .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_select))
                    .position(lastRoutePOI.location)
                    .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
            )
            mNaviEndMarker?.tag = lastRoutePOI
            val userCurrentFloorPlanId = instance!!.userCurrentFloorPlanId
            val mainGroundFloor = instance!!.getMainGroundFloorPlanId(this@NaviSDKActivity)
            mNaviEndMarker?.isVisible =
                (instance!!.getFloorNumberByPlanId(this@NaviSDKActivity, userCurrentFloorPlanId!!)
                        == lastRoutePOI.floorNumber || (!mIsIndoor && instance!!.getFloorNumberByPlanId(
                    this@NaviSDKActivity,
                    mainGroundFloor!!.floorPlanId!!
                )
                        == lastRoutePOI.floorNumber))
            val userFloorPointList =
                instance!!.getUserCurrentFloorRoutePointList(this@NaviSDKActivity)
            if (userFloorPointList != null) {
                Log.e(NaviSDKText.LOG_TAG, "mIsIndoor$mIsIndoor")
                if (mIsIndoor) {
                    moveCameraByUserLocation(userFloorPointList);
                } else {
                    Log.e(LOG_TAG, "mNavigationMarker");
                    mNavigationMarker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(firstRoutePOI.location)
                            .rotation(
                                SphericalUtil.computeHeading(
                                    routes[0].location,
                                    routes[1].location
                                ).toFloat()
                            )
                            .flat(true)
                            .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_path_arrow_b))
                    );
                    mNavigationMarker!!.isVisible = true
                    heading = SphericalUtil.computeHeading(routes[0].location, routes[1].location)
                }
            }
        }

        if (mCurrentRouteList != null) {
            for (i in mCurrentRouteList!!.indices) {
                if (i + 2 < mCurrentRouteList!!.size) {
                    val startLat1: Double = mCurrentRouteList!![i].latitude!!.toDouble()
                    val startLon1: Double = mCurrentRouteList!![i].longitude!!.toDouble()
                    val startLat2: Double = mCurrentRouteList!![i + 1].latitude!!.toDouble()
                    val startLon2: Double = mCurrentRouteList!![i + 1].longitude!!.toDouble()
                    val startLat3: Double = mCurrentRouteList!![i + 2].latitude!!.toDouble()
                    val startLon3: Double = mCurrentRouteList!![i + 2].longitude!!.toDouble()
//                    val bearA: Float = Tools.instance.getBearing(startLat1, startLon1, startLat2, startLon2)
//                    val bearB: Float = Tools.instance.getBearing(startLat2, startLon2, startLat3, startLon3)
                    val disFloat: Float =
                        Tools.instance.getDistance(startLat1, startLon1, startLat2, startLon2)
                    val dis = String.format("%.1f", disFloat) + "公尺"

                    val bearA = SphericalUtil.computeHeading(
                        mCurrentRouteList!![i].location,
                        mCurrentRouteList!![i + 1].location
                    )
                    val bearB = SphericalUtil.computeHeading(
                        mCurrentRouteList!![i + 1].location,
                        mCurrentRouteList!![i + 2].location
                    )
                    var absAtoB = abs(bearA - bearB)

                    if (mCurrentRouteList!![i + 1].floorNumber != mCurrentRouteList!![i + 2].floorNumber) {
                        var floorNumber: String = mCurrentRouteList!![i + 2].floorNumber.toString()
                        for (j in i + 2 until mCurrentRouteList!!.size - 1) {
                            if (mCurrentRouteList!![j].floorNumber == mCurrentRouteList!![j + 1].floorNumber) {
                                floorNumber = mCurrentRouteList!![j].floorNumber.toString()
                                break
                            }
                        }
                        floorNumber = if (floorNumber.contains("-"))
                            floorNumber.replace("-", "B") else floorNumber + "F"
                        mCurrentRouteList!![i].turnHint = dis + "後前往" + floorNumber
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_straight_icon_stright
                    } else if (absAtoB < 10) {
                        mCurrentRouteList!![i].turnHint = "沿著路線行走"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_straight_icon_stright
                    } else if (bearA - bearB < 0 && absAtoB < 20 && absAtoB > 10) {
                        mCurrentRouteList!![i].turnHint = "沿著路線行走"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_right_front
                    } else if (bearA - bearB > 0 && absAtoB < 20 && absAtoB > 10) {
                        mCurrentRouteList!![i].turnHint = "沿著路線行走"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_left_front
                    } else if (bearA - bearB < 0 && absAtoB > 30 && absAtoB < 180) {
                        mCurrentRouteList!![i].turnHint = dis + "後右轉"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_turn_right
                    } else if (bearA - bearB > 0 && absAtoB > 30 && absAtoB < 180) {
                        mCurrentRouteList!![i].turnHint = dis + "後左轉"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_turn_left
                    } else if (bearA - bearB < 0 && absAtoB > 180) {
                        mCurrentRouteList!![i].turnHint = dis + "後左轉"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_turn_left
                    } else if (bearA - bearB > 0 && absAtoB > 180) {
                        mCurrentRouteList!![i].turnHint = dis + "後右轉"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_turn_right
                    } else {
                        mCurrentRouteList!![i].turnHint = "沿著路線行走"
                        mCurrentRouteList!![i].turnImg = R.mipmap.ic_straight_icon_stright
                    }
                    Log.e(LOG_TAG, "bearA = " + bearA + "/" + bearB + "/" + absAtoB);
                }
                if (i + 2 == mCurrentRouteList!!.size) {
                    val startLat1: Double = mCurrentRouteList!![i].latitude!!.toDouble()
                    val startLon1: Double = mCurrentRouteList!![i].longitude!!.toDouble()
                    val startLat2: Double = mCurrentRouteList!![i + 1].latitude!!.toDouble()
                    val startLon2: Double = mCurrentRouteList!![i + 1].longitude!!.toDouble()
                    val disFloat: Float =
                        Tools.instance.getDistance(startLat1, startLon1, startLat2, startLon2)
                    val dis = String.format("%.1f", disFloat) + "公尺"
                    mCurrentRouteList!![i].turnHint = "沿著路線行走\n${dis}抵達終點".trimIndent()
                    mCurrentRouteList!![i].turnImg = R.mipmap.ic_straight_icon_stright
                }
            }
        }
    }

    private fun setNavigationMode(navigationMode: Int) {
        mNavigationMode = navigationMode
        runOnUiThread {
            if (mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
//                    mPOIInfoLayout.setVisibility(View.GONE);
//                headerNaviTV!!.setText(R.string.map_page_stop_navi)
                //                    headerNaviTV.setTextColor(getResources().getColor(R.color.red_91));
//                    headerNaviTV.setBackgroundResource(R.mipmap.rectangle_copy_7);
//                headerNaviTV!!.setOnClickListener { leaveNavigation() }
//                headerNaviIV!!.setImageResource(R.mipmap.stop_nav)
//                headerNaviIV!!.setOnClickListener { leaveNavigation() }
                if (mEndPOI != null) {
                    NetworkManager.instance.setNetworkImage(
                        this@NaviSDKActivity,
                        mNaviInfoIconCNIV!!,
                        mEndPOI!!.urlToPoisImage,
                        mEndPOI!!.getPOIIconRes(false),
                        mEndPOI!!.getPOIIconRes(false)
                    )
                    mNaviInfoTitleTV!!.text = mEndPOI!!.name
                }
                //            mEventBus.post(new OmniEvent(OmniEvent.TYPE_NAVIGATION_MODE_CHANGED, OmniEvent.EVENT_CONTENT_USER_IN_NAVIGATION));
            } else {
//                    mPOIInfoLayout.setVisibility(View.VISIBLE);
//                headerNaviTV!!.setText(R.string.map_page_start_navi)
                //                    headerNaviTV.setTextColor(getResources().getColor(android.R.color.white));
//                    headerNaviTV.setBackgroundResource(R.drawable.solid_round_rectangle_navi_blue);
//                headerNaviTV!!.setOnClickListener { navigationData }
//                headerNaviIV!!.setImageResource(R.mipmap.start_nav)
//                headerNaviIV!!.setOnClickListener { navigationData }
                //            mEventBus.post(new OmniEvent(OmniEvent.TYPE_NAVIGATION_MODE_CHANGED, OmniEvent.EVENT_CONTENT_NOT_NAVIGATION));
            }
        }
    }

    private fun addUserMarker(position: LatLng, location: Location) {
        if (mMap == null) {
            return
        }
        if (instance.currentShowFloor!!.floorLevel != instance.getUserCurrentFloorLevel(this@NaviSDKActivity)) {
            return
        }
        if (mUserMarker == null) {
            Log.e(NaviSDKText.LOG_TAG, "addUserMarker mUserMarker")
            mUserMarker = mMap!!.addMarker(
                MarkerOptions()
                    .flat(true)
                    .rotation(location.bearing)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location))
                    .anchor(0.5f, 0.5f)
                    .position(position)
                    .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
            )
            mUserAccuracyCircle = mMap!!.addCircle(
                CircleOptions()
                    .center(position)
                    .radius(location.accuracy / 2.toDouble())
                    .strokeColor(ContextCompat.getColor(this, R.color.map_circle_stroke_color))
                    .fillColor(ContextCompat.getColor(this, R.color.map_circle_fill_color))
                    .strokeWidth(5f)
                    .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
            )
        } else {
            mUserMarker!!.position = position
            mUserMarker!!.rotation = location.bearing
            mUserAccuracyCircle!!.center = position
            mUserAccuracyCircle!!.radius = location.accuracy / 2.toDouble()
        }
        if (mNavigationMode == NaviMode.USER_IN_NAVIGATION) {
//            if (!DataCacheManager.getInstance().isInBuilding(this) ||
//                    (getArguments().containsKey(ARG_KEY_BOOK_NAVIGATION_ROUTE) && !getArguments().getBoolean(ARG_KEY_NAVIGATION_IS_USER_IN_BUILDING))) {
//                return;
//            }
            val pointList = instance.getUserCurrentFloorRoutePointList(this)
            Log.e(NaviSDKText.LOG_TAG, "pointList == null ? " + (pointList == null))
            if (pointList != null) {
                Log.e(NaviSDKText.LOG_TAG, "mapReadyNavi$mapReadyNavi")
                if (mIsIndoor && mapReadyNavi) {
                    moveCameraByUserLocation(pointList)
                }
            }
        }
    }

    private fun addPOIMarkers(buildingId: String?, floorPlanId: String?) {
        val floor = instance.getBuildingFloor(this, buildingId, floorPlanId!!)
        if (floor != null) {
            removePreviousMarkers(buildingId)
            var item: OmniClusterItem<*>
            itemList =
                instance.getClusterListByBuildingId(buildingId) as MutableList<OmniClusterItem<*>>?
            if (itemList == null) {
                itemList = ArrayList()
            }
            if (menuList == null) {
                menuList = ArrayList()
            } else {
                menuList!!.clear()
            }

            // add floor markers
            for (poi in floor.pois!!) {
                if (poi.type != "map_text") {
                    if (themeId.isNotEmpty()) {
                        for (id in themeId) {
                            if (id == poi.id) {
                                item = OmniClusterItem<Any?>(poi)
                                itemList!!.add(item)
                                instance.setPOIClusterItemMap(poi.id.toString() + "", item)
                            }
                        }
                    } else {
                        item = OmniClusterItem<Any?>(poi)
                        itemList!!.add(item)
                        instance.setPOIClusterItemMap(poi.id.toString() + "", item)
                    }
                }
            }
            instance.setBuildingClusterItems(buildingId!!, itemList!!)
            if (mClusterManager != null) {
                mClusterManager!!.addItems(itemList)
                mClusterManager!!.cluster()
            }
            val floors = instance.getFloorsByBuildingId(this, buildingId)
            val adapter = FloorAdapter(this, floors!!.size)
            val grid = findViewById<GridView>(R.id.map_content_view_floor_gv)
            grid.adapter = adapter
            grid.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                if (floors.size > position) {
                    val f = floors[position]
                    fetchFloorPlan(f.floorPlanId, false, f.order)
                }
                mFloorsLayoutNew!!.visibility =
                    if (mFloorsLayoutNew!!.isShown) View.GONE else View.VISIBLE
                mMaskLayout!!.visibility = if (mMaskLayout!!.isShown) View.GONE else View.VISIBLE
                mFloorLevelTV?.visibility = View.VISIBLE
            }
            addZPOIMarkers(buildingId, floorPlanId)
        }
    }

    private fun refreshPOIMarkers(buildingId: String?, floorPlanId: String?, aac: Int?) {
        val floor = instance.getBuildingFloor(this, buildingId, floorPlanId!!)
        if (floor != null) {
            removePOIMarkers(buildingId)
            var item: OmniClusterItem<*>
            itemList =
                instance.getClusterListByBuildingId(buildingId) as MutableList<OmniClusterItem<*>>?
            if (itemList == null) {
                itemList = ArrayList()
            }
            if (menuList == null) {
                menuList = ArrayList()
            } else {
                menuList!!.clear()
            }

            // add floor markers
            for (poi in floor.pois!!) {
                if (poi.type != "map_text" && poi.aac_id == aac || aac == -1) {
                    item = OmniClusterItem<Any?>(poi)
                    itemList!!.add(item)
                    instance.setPOIClusterItemMap(poi.id.toString() + "", item)
                }
            }
            instance.setBuildingClusterItems(buildingId!!, itemList!!)
            if (mClusterManager != null) {
                mClusterManager!!.addItems(itemList)
                mClusterManager!!.cluster()
            }
        }
    }

    private fun showPolylineByFloorNumber(floorNumber: String) {
        Log.e(NaviSDKText.LOG_TAG, "showPolylineByFloorNumber$floorNumber")
        val map = instance.floorPolylineMap
        for (key in map.keys) {
            map[key]!!.isVisible = key == floorNumber
        }
        showArrowMarkersByFloorNumber(floorNumber)
        if (mNaviStartMarker != null) {
            val poi = mNaviStartMarker!!.tag as NavigationRoutePOI?
            mNaviStartMarker!!.isVisible = floorNumber == poi!!.floorNumber
        }
        if (mNaviEndMarker != null) {
            val poi = mNaviEndMarker!!.tag as NavigationRoutePOI?
            mNaviEndMarker!!.isVisible = floorNumber == poi!!.floorNumber
            Log.e(
                NaviSDKText.LOG_TAG,
                "showPolylineByFloorNumber" + poi.floorNumber + (floorNumber == poi.floorNumber)
            )
        }
    }

    private fun showArrowMarkersByFloorNumber(floorNumber: String) {
        val map = instance.floorArrowMarkersMap
        for (key in map.keys) {
            val list = map[key]
            for (marker in list!!) {
                marker.isVisible = key == floorNumber
            }
        }
        showNavigationMarkerByFloorNumber(floorNumber)
    }

    private fun showNavigationMarkerByFloorNumber(floorNumber: String) {
//        if (mNavigationMarker != null) {
//            mNavigationMarker.setVisible(false);
//        }
    }

    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0f, baseline, paint)
        return image
    }

    private fun addZPOIMarkers(buildingId: String?, floorPlanId: String?) {
        val floor = instance.getBuildingFloor(this, buildingId, floorPlanId!!)
        if (floor != null) {
            for (zPoi in floor.zPois) {
                Log.e(NaviSDKText.LOG_TAG, "addZPOIMarkers:${zPoi.desc}")
                val zMarker = mMap!!.addMarker(
                    MarkerOptions()
                        .flat(false)
                        .anchor(0.5f, 0.5f)
                        .position(LatLng(zPoi.latitude, zPoi.longitude))
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                textAsBitmap(
                                    zPoi.name,
                                    30f,
                                    Color.BLACK
                                )
                            )
                        )
                        .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                )
                zMarker.tag = zPoi
                instance.setZMarkerByBuildingId(buildingId, zMarker)
            }
        }
    }

    private fun removePreviousMarkers(buildingId: String?) {
        if (mClusterManager != null) {
            val previousClusterItemList = instance.getClusterListByBuildingId(buildingId)
            if (previousClusterItemList != null) {
                mClusterManager!!.clearItems()
                previousClusterItemList.clear()
            }
            mClusterManager!!.cluster()
        }
        val zMarkerList = instance.getZMarkerListByBuildingId(buildingId)
        if (zMarkerList != null) {
            for (zMarker in zMarkerList) {
                zMarker!!.remove()
            }
            zMarkerList.clear()
        }
    }

    private fun removePOIMarkers(buildingId: String?) {
        if (mClusterManager != null) {
            val previousClusterItemList = instance.getClusterListByBuildingId(buildingId)
            if (previousClusterItemList != null) {
                mClusterManager!!.clearItems()
                previousClusterItemList.clear()
            }
            mClusterManager!!.cluster()
        }
    }

    private fun moveCameraByUserLocation(pointList: List<LatLng>?) {
        Log.e(NaviSDKText.LOG_TAG, "moveCameraByUserLocation")
        if (pointList != null && mUserMarker != null) {
            val userPosition = mUserMarker!!.position
            var previousPoint: LatLng? = null
            var closestPoint: LatLng? = null
            var closestDistance = -1.0
            var heading = -1.0

            Thread(Runnable {
                Log.e(NaviSDKText.LOG_TAG, " new Thread")
                for (point in pointList) {
                    if (previousPoint != null) {
                        val r_numerator =
                            (userPosition.longitude - previousPoint!!.longitude) * (point.longitude - previousPoint!!.longitude) + (userPosition.latitude - previousPoint!!.latitude) * (point.latitude - previousPoint!!.latitude)
                        val r_denominator =
                            (point.longitude - previousPoint!!.longitude) * (point.longitude - previousPoint!!.longitude) + (point.latitude - previousPoint!!.latitude) * (point.latitude - previousPoint!!.latitude)
                        val r = r_numerator / r_denominator

                        val px =
                            previousPoint!!.longitude + r * (point.longitude - previousPoint!!.longitude)
                        val py =
                            previousPoint!!.latitude + r * (point.latitude - previousPoint!!.latitude)

                        val s =
                            ((previousPoint!!.latitude - userPosition.latitude) * (point.longitude - previousPoint!!.longitude) - (previousPoint!!.longitude - userPosition.longitude) * (point.latitude - previousPoint!!.latitude)) / r_denominator

                        distanceLine = Math.abs(s) * Math.sqrt(r_denominator)

                        var xx = px
                        var yy = py

                        if (r in 0.0..1.0) {
                            distanceSegment = distanceLine
                        } else {

                            val dist1 =
                                (userPosition.longitude - previousPoint!!.longitude) * (userPosition.longitude - previousPoint!!.longitude) + (userPosition.latitude - previousPoint!!.latitude) * (userPosition.latitude - previousPoint!!.latitude)
                            val dist2 =
                                (userPosition.longitude - point.longitude) * (userPosition.longitude - point.longitude) + (userPosition.latitude - point.latitude) * (userPosition.latitude - point.latitude)
                            if (dist1 < dist2) {
                                xx = previousPoint!!.longitude
                                yy = previousPoint!!.latitude
                                distanceSegment = Math.sqrt(dist1)
                            } else {
                                xx = point.longitude
                                yy = point.latitude
                                distanceSegment = Math.sqrt(dist2)
                            }
                        }

                        if (closestDistance == -1.0 || closestDistance > distanceSegment) {
                            closestDistance = distanceSegment
                            closestPoint = LatLng(yy, xx)
                            heading = SphericalUtil.computeHeading(previousPoint, point)
                        }
                    }
                    previousPoint = point
                }
                if (closestPoint == null) {
                    Log.e(NaviSDKText.LOG_TAG, " closestPoint == null")
                    return@Runnable
                }
                val pointOnRoute =
                    SphericalUtil.computeOffset(closestPoint, closestDistance, heading)

                runOnUiThread {
                    if (mNavigationMarker == null) {
                        mNavigationMarker = mMap!!.addMarker(
                            MarkerOptions()
                                .position(pointOnRoute)
                                .rotation(heading.toFloat())
                                .flat(true)
                                .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_path_arrow_b))
                        );
                        mNavigationMarker!!.isVisible =
                            DataCacheManager.instance.currentShowFloor!!.floorLevel == DataCacheManager.instance.getUserCurrentFloorLevel(
                                this
                            );
                    } else {
                        mNavigationMarker!!.setPosition(pointOnRoute);
                        mNavigationMarker!!.rotation = heading.toFloat();
                        mNavigationMarker!!.zIndex = NaviSDKText.MARKER_Z_INDEX.toFloat();
                        mNavigationMarker!!.isVisible =
                            DataCacheManager.instance.currentShowFloor!!.floorLevel == DataCacheManager.instance.getUserCurrentFloorLevel(
                                this
                            );
                    }
                    val zoomLevel = mMap!!.cameraPosition.zoom.toInt()
                    val cameraPosition: CameraPosition
                    cameraPosition = if (autoHeading) {
                        CameraPosition.Builder() //                    .target(pointOnRoute)
                            .target(userPosition)
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat()) //                    .zoom((cameraMoveTimes < 5 && zoomLevel < NLPIText.MAP_ZOOM_LEVEL) ? NLPIText.MAP_ZOOM_LEVEL : zoomLevel)
                            .bearing(mUserMarker!!.rotation) //                    .bearing((float) heading)
                            //                    .tilt(20)
                            .build()
                    } else {
                        CameraPosition.Builder()
                            .target(userPosition)
                            .zoom(NaviSDKText.MAP_ZOOM_LEVEL.toFloat())
                            .build()
                    }
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    cameraMoveTimes++

                    if (mCurrentRouteList != null) {
                        Log.e(NaviSDKText.LOG_TAG, "mCurrentRouteList != null")
                        val lastOfArr = mCurrentRouteList!!.size - 1
                        val routeLat = mCurrentRouteList!![lastOfArr].latitude!!.toDouble()
                        val routeLon = mCurrentRouteList!![lastOfArr].longitude!!.toDouble()
                        val userDisToTarget = getDistance(pointOnRoute, routeLat, routeLon)
                        Log.e(NaviSDKText.LOG_TAG, "userDisToTarget$userDisToTarget")
                        if (userDisToTarget <= 5 && userDisToTarget > 0.5 && (mCurrentRouteList!![lastOfArr].floorNumber
                                    == instance.getUserCurrentFloorLevel(this@NaviSDKActivity))
                        ) {
                            DialogTools.instance.showErrorMessage(
                                this@NaviSDKActivity,
                                R.string.dialog_title_hint,
                                R.string.dialog_message_arrive_destination
                            )
                            NavigationRoutePOIListAllPosition++
                            MarkerListPosition++
                            leaveNavigation()
                        } else {
                            var closeIndex = 0
                            var closeDistance = 100f
                            for (i in mCurrentRouteList!!.indices) {
                                if (getDistance(
                                        pointOnRoute, mCurrentRouteList!![i].latitude!!.toDouble(),
                                        mCurrentRouteList!![i].longitude!!.toDouble()
                                    ) < closeDistance &&
                                    mCurrentRouteList!![i].floorNumber == instance.currentShowFloor!!.floorLevel
                                ) {
                                    closeIndex = i
                                    closeDistance = getDistance(
                                        pointOnRoute, mCurrentRouteList!![i].latitude!!.toDouble(),
                                        mCurrentRouteList!![i].longitude!!.toDouble()
                                    )
                                }
                            }
                            if (closeIndex == 0) {
                                mDirectionHintTV!!.text = mCurrentRouteList!![0].turnHint
                                mDirectionHintIV!!.setImageResource(mCurrentRouteList!![0].turnImg)
                            } else {
                                if (getDistance(
                                        pointOnRoute,
                                        mCurrentRouteList!![closeIndex - 1].latitude!!.toDouble(),
                                        mCurrentRouteList!![closeIndex - 1].longitude!!.toDouble()
                                    ) <
                                    getDistance(
                                        mCurrentRouteList!![closeIndex].location,
                                        mCurrentRouteList!![closeIndex - 1].latitude!!.toDouble(),
                                        mCurrentRouteList!![closeIndex - 1].longitude!!.toDouble()
                                    )
                                ) {
                                    mDirectionHintTV!!.text =
                                        mCurrentRouteList!![closeIndex - 1].turnHint
                                    mDirectionHintIV!!.setImageResource(mCurrentRouteList!![closeIndex - 1].turnImg)
                                } else {
                                    mDirectionHintTV!!.text =
                                        mCurrentRouteList!![closeIndex].turnHint
                                    mDirectionHintIV!!.setImageResource(mCurrentRouteList!![closeIndex].turnImg)
                                }
                            }
                        }
                    }
                }
            }).start()
        }
    }

    private fun getDistance(userlatlong: LatLng, routeLat: Double, routeLon: Double): Float {
        val l1 = Location("One")
        l1.latitude = userlatlong.latitude
        l1.longitude = userlatlong.longitude
        Log.e(NaviSDKText.LOG_TAG, "userlatlong.latitude" + userlatlong.latitude)
        Log.e(NaviSDKText.LOG_TAG, "userlatlong.longitude" + userlatlong.longitude)
        val l2 = Location("Two")
        l2.latitude = routeLat
        l2.longitude = routeLon
        Log.e(NaviSDKText.LOG_TAG, "routeLat$routeLat")
        Log.e(NaviSDKText.LOG_TAG, "routeLon$routeLon")
        var distance = l1.distanceTo(l2)
        var dist = "$distance M"
        if (distance > 1000.0f) {
            distance /= 1000000.0f
            dist = distance.toString() + "M"
        }
        return distance
    }

    private fun checkTileExists(x: Int, y: Int, zoom: Int): Boolean {
        return !(zoom < NaviSDKText.MAP_MIN_ZOOM_LEVEL || zoom > NaviSDKText.MAP_MAX_ZOOM_LEVEL)
    }

    private fun refreshMapTextMarkers(zoomLevel: Int) {
        val omniFloor = instance.currentShowFloor
        if (omniFloor != null) {
            val buildingId = instance.getBuildingIdByFloorPlanId(this, omniFloor.floorPlanId!!)
            val zMarkerList: List<Marker?>? = instance.getZMarkerListByBuildingId(buildingId)
            if (zMarkerList != null) {
                for (marker in zMarkerList) {
                    val poi = marker!!.tag as POI?
                }
            }
        }
    }

    private fun drawPolyline(
        floorNumber: String, pointList: List<LatLng>,
        colorIndex: Int, width: Float
    ) {
        Log.e(NaviSDKText.LOG_TAG, "drawPolyline colorIndex$colorIndex")
        var lineOptions: PolylineOptions? = null
        if (pointList.isNotEmpty()) {
            lineOptions = PolylineOptions()
                .addAll(pointList)
                .width(width)
                .color(colorIndex)
                .zIndex(NaviSDKText.POLYLINE_Z_INDEX.toFloat())
        }
        if (lineOptions != null) {
//            val finalLineOptions: PolylineOptions = lineOptions
            runOnUiThread {
                val polyline = mMap!!.addPolyline(lineOptions)
                polyline.zIndex = NaviSDKText.POLYLINE_Z_INDEX.toFloat()
                // only show the floor's route poly line
                polyline.isVisible = instance.currentShowFloor!!.floorLevel == floorNumber

                Log.e(
                    NaviSDKText.LOG_TAG,
                    "drawPolyline floorNumber : $floorNumber, polyline : $polyline"
                )
                instance.floorPolylineMap.put(floorNumber, polyline)
                instance.setFloorRoutePointsMap(floorNumber, pointList)
                if (pointList.size == 4) {
                    val arrowMarker = mMap!!.addMarker(
                        MarkerOptions()
                            .flat(true)
                            .rotation(
                                SphericalUtil.computeHeading(
                                    LatLng(pointList[2].latitude, pointList[2].longitude),
                                    LatLng(pointList[3].latitude, pointList[3].longitude)
                                ).toFloat()
                            )
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_path_arrow_b))
                            .position(
                                LatLng(
                                    pointList[pointList.size - 1].latitude,
                                    pointList[pointList.size - 1].longitude
                                )
                            )
                            .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                    )
                    arrowMarker.isVisible = instance.currentShowFloor!!.floorLevel == floorNumber
                }

                addArrowOnPolyline(pointList, floorNumber)
            }
        } else {
            DialogTools.instance.showErrorMessage(
                this,
                R.string.error_dialog_title_text_normal,
                "There's no POI!"
            )
        }
    }

    private fun addArrowOnPolyline(pointList: List<LatLng>, floorNumber: String) {
        var previousPoint: LatLng? = null
        val d = 3
        var marker: Marker
        val markerList: MutableList<Marker> = java.util.ArrayList()
        for (point in pointList) {
            if (previousPoint != null) {
                val distance = SphericalUtil.computeDistanceBetween(previousPoint, point)
                val total = Math.round(distance / d).toInt()
                val heading = SphericalUtil.computeHeading(previousPoint, point).toFloat()
                for (i in 0 until total) {
                    val coordinate = SphericalUtil.computeOffset(
                        previousPoint,
                        i * d.toDouble(),
                        heading.toDouble()
                    )
                    marker = mMap!!.addMarker(
                        MarkerOptions()
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromBitmap(getArrowBitmap()))
                            .position(coordinate)
                            .rotation(heading)
                            .zIndex(NaviSDKText.MARKER_Z_INDEX.toFloat())
                    )
                    marker.isVisible = instance.currentShowFloor!!.floorLevel == floorNumber
                    markerList.add(marker)
                }
            }
            previousPoint = point
        }
        instance.setFloorArrowMarkersMap(floorNumber, markerList)
    }

    private var mArrowBitmap: Bitmap? = null

    private fun getArrowBitmap(): Bitmap? {
        if (mArrowBitmap == null) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.mipmap.icon_arrow_route, options)
            var scale = 1
            if (options.outWidth != NaviSDKText.POLYLINE_WIDTH.toInt() ||
                options.outHeight != NaviSDKText.POLYLINE_WIDTH.toInt()
            ) {
                scale = Math.ceil(options.outHeight / NaviSDKText.POLYLINE_WIDTH.toDouble()).toInt()
            }
            val optionsResized = BitmapFactory.Options()
            optionsResized.inSampleSize = scale
            mArrowBitmap =
                BitmapFactory.decodeResource(resources, R.mipmap.icon_arrow_route, optionsResized)
        }
        return mArrowBitmap
    }

    private fun checkLocationService() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions()
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("位置服務尚未開啟，請設定")
            dialog.setPositiveButton("open settings") { paramDialogInterface, paramInt -> // TODO Auto-generated method stub
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            dialog.setNegativeButton("cancel") { paramDialogInterface, paramInt ->
                // TODO Auto-generated method stub
                finish()
                //                    DialogTools.getInstance().showErrorMessage(NaviSDKActivity.this,
//                            getString(R.string.error_dialog_title_text_normal),
//                            "沒有開啟位置服務，地圖無法顯示");
            }
            dialog.show()
        }
    }

    private fun ensurePermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CHANGE_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                ),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
//            registerService();
        }
    }

    private fun registerService() {
        if (mOGService == null) {
            mOGService = OGService(this)
        }
        Log.e(NaviSDKText.LOG_TAG, "registerService")
        mOGService!!.startService(object : OGService.GoogleApiClientConnectCallBack {
            override fun onGoogleApiClientConnected() {
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                var i = 0
                while (i < grantResults.size) {
                    if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[i] == PackageManager.PERMISSION_DENIED
                    ) {
                        finish()
                    }
                    i++
                }
            }
        }
//        registerService()
    }

    private fun getEmergencyRoute(type: String) {
        if (!mIsMapInited) {
            mEventBus!!.post(OmniEvent(OmniEvent.TYPE_REQUEST_LAST_LOCATION, ""))
        } else {
            Log.e(NaviSDKText.LOG_TAG, "getEmergencyRoute type$type")
            mEventBus!!.post(OmniEvent(OmniEvent.TYPE_REQUEST_LAST_LOCATION, ""))
            val floorPlanId = instance.userCurrentFloorPlanId
            var buildingId = instance.getBuildingIdByFloorPlanId(this, floorPlanId!!)
            var floorLevel = instance.getUserCurrentFloorLevel(this)
            val lat: Double
            val lng: Double
            val floor = instance.getMainGroundFloorPlanId(this)
            if (mIsIndoor && mLastLocation != null) {
                fetchFloorPlan(
                    floorPlanId,
                    false,
                    instance.getFloorNumberByPlanId(this, floorPlanId)
                )
                lat = mLastLocation!!.latitude
                lng = mLastLocation!!.longitude
            } else {
                fetchFloorPlan(floor!!.floorPlanId, false, "1")
                val entrancePOI = instance.getEntrancePOI(floor)
                lat = entrancePOI!!.latitude
                lng = entrancePOI.longitude
                buildingId = "2"
                floorLevel = "1"
            }
            Log.e(NaviSDKText.LOG_TAG, "getEmergencyRoute type$buildingId$floorLevel$lat$lng$type")
            LocationApi.instance.getEmergencyRoute(this,
                buildingId,
                floorLevel,
                lat,
                lng,
                type,
                object : NetworkManagerListener<Array<NavigationRoutePOI>> {
                    override fun onSucceed(routePOIs: Array<NavigationRoutePOI>?) {
                        DialogTools.instance.dismissProgress(this@NaviSDKActivity)
                        if (routePOIs!!.isNotEmpty()) {
                            runOnUiThread {
                                stopNaviRL!!.visibility = View.VISIBLE
                                mDirectionHintLL!!.visibility = View.VISIBLE
                                poiTypeSelectorList!!.visibility = View.INVISIBLE
                                isRouteNavi = false
                            }

                            startNavigation(routePOIs.toList())
                            val lastPOIFloorLevel = routePOIs[routePOIs.size - 1].floorNumber
                            runOnUiThread {
                                if (!titleSetting) {
                                    mNaviInfoTitleTV!!.text =
                                        (if (lastPOIFloorLevel!!.contains("-")) lastPOIFloorLevel?.replace(
                                            "-",
                                            "B"
                                        ) else lastPOIFloorLevel + "F") + " " + mNaviInfoTitleTV!!.text
                                    titleSetting = true
                                }
                            }
                        } else {
                            runOnUiThread {
                                DialogTools.instance.showErrorMessage(
                                    this@NaviSDKActivity,
                                    R.string.error_dialog_title_text_normal,
                                    R.string.dialog_message_route_empty
                                )
                            }
                        }
                    }

                    override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                        Log.e(NaviSDKText.LOG_TAG, "getEmergencyRoute onFail" + errorMsg);
                        runOnUiThread {
                            DialogTools.instance.showErrorMessage(
                                this@NaviSDKActivity,
                                R.string.error_dialog_title_text_normal,
                                R.string.dialog_message_route_empty
                            )
                        }
                    }
                })
        }
    }

    inner class FloorAdapter(private val context: Context, val size: Int) : BaseAdapter() {
        override fun getCount(): Int {
            return size
        }

        override fun getViewTypeCount(): Int {
            return count
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val grid: View
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (convertView == null) {
                grid = layoutInflater.inflate(R.layout.item_floor_gridview, null)
                val textView = grid.findViewById<TextView>(R.id.item_floor_gridview_fl_tv)
                if (floorNumber[position] < 0) {
                    textView.text = floorNumber[position].toString().replace("-", "B")
                } else {
                    textView.text = floorNumber[position].toString() + "F"
                }
            } else {
                grid = convertView
            }
            return grid
        }

    }

    inner class PoiTypeListAdapter(private val category: Array<PoiCategory>) :
        RecyclerView.Adapter<PoiTypeListAdapter.PoiTypeListHolder>() {

        inner class PoiTypeListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var cardView: CardView = itemView.findViewById(R.id.type_poi_item_cv) as CardView
            var title: TextView = itemView.findViewById(R.id.type_poi_item_tv) as TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiTypeListHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.type_poi_item, parent, false)
            return PoiTypeListHolder(itemView)
        }

        override fun onBindViewHolder(holder: PoiTypeListHolder, position: Int) {
            if (position == 0) {
                holder.title.text = "全部"
                if (position == select_category || select_category == -1) {
                    holder.title.setTextColor(Color.parseColor("#FFFFFF"))
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#0071b6"))
                } else {
                    holder.title.setTextColor(Color.parseColor("#0071b6"))
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                }

                holder.cardView.setOnClickListener {
                    Log.e(LOG_TAG, "cardView position$position")
                    select_category = position
                    notifyDataSetChanged()
                    val id = instance.currentShowFloor!!.floorPlanId
                    Log.e(LOG_TAG, "cardView id$id")
                    refreshPOIMarkers(
                        instance.getBuildingIdByFloorPlanId(this@NaviSDKActivity, id!!),
                        id,
                        -1
                    )
                }
            } else {
                val c = category[position - 1]
                holder.title.text = c.category
                if (position == select_category) {
                    holder.title.setTextColor(Color.parseColor("#FFFFFF"))
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#0071b6"))
                } else {
                    holder.title.setTextColor(Color.parseColor("#0071b6"))
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                }

                holder.cardView.setOnClickListener {
                    Log.e(LOG_TAG, "cardView position$position")
                    select_category = position
                    notifyDataSetChanged()
                    val id = instance.currentShowFloor!!.floorPlanId
                    Log.e(LOG_TAG, "cardView id$id")
                    refreshPOIMarkers(
                        instance.getBuildingIdByFloorPlanId(this@NaviSDKActivity, id!!),
                        id,
                        c.aac_id
                    )
                }
            }
        }

        override fun getItemCount() = category!!.size + 1

        override fun getItemViewType(position: Int) = position
    }

    private var lastDegree = 0
    private val mSensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values
                )
                val orientation = FloatArray(4)
                val orientationAngles = FloatArray(3)
                SensorManager.getOrientation(mRotationMatrix, orientationAngles)
                azimuth = (((Math.toDegrees(
                    SensorManager.getOrientation(
                        mRotationMatrix,
                        orientation
                    )[0].toDouble()
                ) + 360) % 360 -
                        Math.toDegrees(
                            SensorManager.getOrientation(
                                mRotationMatrix,
                                orientation
                            )[2].toDouble()
                        ) + 360) % 360).toInt() + mDeclination
                azimuth = (azimuth + 360) % 360
                if (mNavigationMarker != null && azimuth.toInt() != lastDegree && !isDirect) {
                    lastDegree = azimuth.toInt()
                    mNavigationMarker!!.rotation = azimuth
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
        val powerData = BatteryPowerData.generateBatteryBeacon(scanRecord)
        if (powerData != null && powerData.BatteryUuid.toUpperCase()
                .startsWith("00112233-4455-6677-8899-AABBCCDDEEFF")
        ) {
            mBBHandler!!.obtainMessage(MSG_GET_DATA).sendToTarget()
            if (device!!.address != mLastSendBatteryMac) {
                LocationApi.instance.setBeaconBatteryLevel(this@NaviSDKActivity,
                    device.address, powerData.batteryPower.toString() + "",
                    object : NetworkManagerListener<SetBeaconBatteryResponse?> {
                        override fun onSucceed(response: SetBeaconBatteryResponse?) {
                            if (response!!.isSuccess()) {
                                mLastSendBatteryMac = device.address
                            }
                        }

                        override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                        }
                    })
            }
        }
    }

    companion object {
        val START_LOCATION = LatLng(25.036570845775, 121.56432608219)
        private const val ARG_KEY_DOMAIN_NAME = "arg_key_domain_name"
        private const val ARG_KEY_MAP_BEARING = "arg_key_map_bearing"
        private const val ARG_KEY_AUTO_HEADING = "arg_key_auto_heading"
        private const val ARG_KEY_NAVIGATE_DIRECT = "arg_key_navigate_direct"
        private const val ARG_KEY_ENCRYPT_KEY = "arg_key_encrypt_key";
        private const val ARG_KEY_SEARCH_POI = "arg_key_search_poi";
        private const val ARG_KEY_QUICK_TYPE = "arg_key_quick_type";
        private const val ARG_KEY_KEYWORD = "arg_key_keyword"
        private const val ARG_KEY_THEME_ID = "arg_key_theme_id"
        private const val ARG_KEY_THEME_PLAN_ID = "arg_key_theme_plan_id"
        private const val COLLECTION_PLACE = "COLLECTION_PLACE"
        private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100
        var floorNum = 1
        var floorNumber: IntArray = IntArray(0)
        var withAccelerometer = false
        var withGyroscope = false
    }
}