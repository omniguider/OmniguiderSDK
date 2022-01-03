package com.omni.navisdk

import android.Manifest
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.VolleyError
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.omni.navisdk.manager.AnimationFragmentManager
import com.omni.navisdk.manager.DataCacheManager
import com.omni.navisdk.manager.DataCacheManager.Companion.instance
import com.omni.navisdk.manager.MyAlarmManager
import com.omni.navisdk.manager.MyAlarmManager.FIND_FRIEND_NOTICE
import com.omni.navisdk.module.*
import com.omni.navisdk.module.group.CreateGroupCollectionJsonObject
import com.omni.navisdk.module.group.GroupData
import com.omni.navisdk.module.group.GroupInfo
import com.omni.navisdk.network.LocationApi
import com.omni.navisdk.network.NetworkManager
import com.omni.navisdk.service.OGService
import com.omni.navisdk.tool.NaviSDKText
import com.omni.navisdk.tool.NaviSDKText.LOG_TAG
import com.omni.navisdk.tool.PreferencesTools
import com.omni.navisdk.tool.PreferencesTools.Companion.KEY_LEFT_GROUP_ENDTIME
import com.omni.navisdk.view.group_location.GroupMainFragment
import com.omni.navisdk.view.group_location.GroupMembersTestFragment
import com.omni.navisdk.view.guide.GuideFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class HomeActivity : BaseActivity() {

    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private val ARG_KEY_DOMAIN_NAME = "arg_key_domain_name"
    private val ARG_KEY_MAP_BEARING = "arg_key_map_bearing"
    private val ARG_KEY_AUTO_HEADING = "arg_key_auto_heading"
    private val ARG_KEY_ENCRYPT_KEY = "arg_key_encrypt_key"
    private val ARG_KEY_SEARCH_POI = "arg_key_search_poi"
    private val ARG_KEY_QUICK_TYPE = "arg_key_quick_type"
    private val ARG_KEY_KEYWORD = "arg_key_keyword"

    private var mLastLocation: Location? = null
    private val mIsIndoor = false
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mEventBus: EventBus? = null

    var homeFL: FrameLayout? = null
    var classifySearchTv: TextView? = null
    var quickGuideTv: TextView? = null
    var searchLL: LinearLayout? = null
    var classifySearchLL: LinearLayout? = null
    var quickGuildLL: LinearLayout? = null
    var fastLL: LinearLayout? = null
    var mapLL: LinearLayout? = null
    var themeLL: LinearLayout? = null
    var friendLL: LinearLayout? = null
    var organSpinner: Spinner? = null
    val organList: MutableList<String> = ArrayList()
    var departSpinner: Spinner? = null
    val departList: MutableList<String> = ArrayList()
    val floorList: MutableList<String> = ArrayList()
    val poiList: MutableList<POI> = ArrayList()
    val meetingRoomList: MutableList<String> = ArrayList()
    var departmentTv: TextView? = null
    var meetingRoomTv: TextView? = null
    var searchTV: TextView? = null
    var searchIV: ImageView? = null
    var searchPOI: POI? = null
    var searchEt: EditText? = null
    var searchType = "department"
    var hotSearch: ChipGroup? = null
    var hotSearchList: MutableList<String>? = null

    var serviceCenterLL: LinearLayout? = null
    var toiletLL: LinearLayout? = null
    var aedLL: LinearLayout? = null
    var fireExtinguisherLL: LinearLayout? = null
    var elevatorLL: LinearLayout? = null
    var exitLL: LinearLayout? = null

    var inFindFriendMode = false
    private var mFindFriendHandler: Handler? = null
    private var mFindFriendHandlerThread: HandlerThread? = null
    val inFindFriendModeUpdateFrq = 3000
    val notFindFriendModeUpdateFrq = 10000
    var currentFindFriendModeUpdateFrq = notFindFriendModeUpdateFrq
    private var mOGService: OGService? = null

    private var mMISCHandlerThread: Array<HandlerThread?>? = null
    private var mMISCHandler: Array<Handler?>? = null
    private val workNumOfMISC = 1
    var nextWorker = 0


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: OmniEvent) {
        when (event.type) {
            OmniEvent.TYPE_USER_OUTDOOR_LOCATION -> {
                mLastLocation = event.obj as? Location
                Log.e("LOG", "TYPE_USER_OUTDOOR_LOCATION" + mLastLocation!!.latitude)
                updateUserCurrentLocation()
            }
            OmniEvent.TYPE_USER_INDOOR_LOCATION -> {
                mLastLocation = event.obj as? Location
                updateUserCurrentLocation()
            }
        }
    }

    private val mSendUserLocationResponseToOtherClassListeners: HashMap<String, SendUserLocationResponseToOtherClass> =
        HashMap()

    interface SendUserLocationResponseToOtherClass {
        fun success(response: SendUserLocationResponse?)
        fun fail(error: VolleyError?, shouldRetry: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (mEventBus == null) {
            mEventBus = EventBus.getDefault()
        }
        mEventBus!!.register(this)
        checkLocationService()

        DataCacheManager.instance.initAllBuildingsData(this)

        organList.add("請選擇機關")
        organList.add("公共服務設施及區域")
        organList.add("衛生局")
        organList.add("秘書處")
        organList.add("環保局")
        organList.add("工務局水利工程處")
        organList.add("臺北市市政大樓公共事務管理中心")
        organList.add("地政局")
        organList.add("教育局")
        organList.add("產業局")
        organList.add("臺北市建築管理工程處")
        organList.add("臺北市商業處")
        organList.add("性別平等辦公室")
        organList.add("都發局")
        organList.add("法務局")
        organList.add("觀傳局")
        organList.add("會議室")
        organList.add("聯合採購發包中心")
        organList.add("工務局")
        organList.add("文化局")
        organList.add("工務局新建工程處")
        organList.add("主計處")
        organList.add("勞動局")
        organList.add("原民會")
        organList.add("交通局")
        organList.add("資訊局")
        organList.add("都委會")
        organList.add("人事處")
        organList.add("政風處")
        organList.add("研考會")

        departList.add("請選擇科組室")

        floorList.add("請選擇樓層")
        floorList.add("2F")
        floorList.add("3F")
        floorList.add("4F")
        floorList.add("8F")
        floorList.add("9F")
        floorList.add("10F")
        floorList.add("11F")
        floorList.add("12F")

        meetingRoomList.add("請選擇會議室")

        val organAdapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner,
                organList
            )

        val departAdapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner,
                departList
            )

        val floorAdapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner,
                floorList
            )
        val meetingRoomAdapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner,
                meetingRoomList
            )

        organSpinner = findViewById(R.id.organ)
        departSpinner = findViewById(R.id.depart)

        organSpinner!!.adapter = organAdapter
        organSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (searchType == "department") {
                    val selectOrg = organSpinner!!.getItemAtPosition(position)
                    departList.clear()
                    departList.add("請選擇科組室")
                    poiList.clear()
                    poiList.add(POI())
                    val allBuildingFloorsMap =
                        DataCacheManager.instance.getAllBuildingFloorsMap(this@HomeActivity)
                    if (allBuildingFloorsMap != null) {
                        for (buildingId in allBuildingFloorsMap.keys) {
                            val floors = allBuildingFloorsMap[buildingId]!!
                            for (floor in floors) {
                                for (poi in floor.pois!!) {
                                    if (poi.office!!.isNotEmpty()) {
                                        if (poi.office[0].dep_name == selectOrg) {
                                            departList.add(floor.name + "-" + poi.name)
                                            poiList.add(poi)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    departAdapter.notifyDataSetChanged()
                    departSpinner!!.setSelection(0)
                } else {
                    val selectFloor = organSpinner!!.getItemAtPosition(position)
                    meetingRoomList.clear()
                    meetingRoomList.add("請選擇會議室")
                    poiList.clear()
                    poiList.add(POI())
                    val allBuildingFloorsMap =
                        DataCacheManager.instance.getAllBuildingFloorsMap(this@HomeActivity)
                    if (allBuildingFloorsMap != null) {
                        for (buildingId in allBuildingFloorsMap.keys) {
                            val floors = allBuildingFloorsMap[buildingId]!!
                            for (floor in floors) {
                                if (floor.name == selectFloor) {
                                    for (poi in floor.pois!!) {
                                        if (poi.office!!.isNotEmpty()) {
                                            if (poi.office[0].dep_name == "會議室") {
                                                meetingRoomList.add(poi.name!!)
                                                poiList.add(poi)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    meetingRoomAdapter.notifyDataSetChanged()
                    departSpinner!!.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        departSpinner!!.adapter = departAdapter
        departSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                searchPOI = poiList[position]
                Log.e("LOG", "poiList" + poiList[position].name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        homeFL = findViewById(R.id.activity_home_fl)
        classifySearchTv = findViewById(R.id.activity_home_classify_search_tv)
        quickGuideTv = findViewById(R.id.activity_home_quick_guide_tv)
        searchLL = findViewById(R.id.activity_home_search_ll)
        classifySearchLL = findViewById(R.id.classify_search_ll)
        quickGuildLL = findViewById(R.id.quick_guild_ll)
        fastLL = findViewById(R.id.fast_ll)
        mapLL = findViewById(R.id.activity_home_map)
        themeLL = findViewById(R.id.activity_home_theme)
        friendLL = findViewById(R.id.activity_home_friend)
        departmentTv = findViewById(R.id.activity_home_department_tv)
        meetingRoomTv = findViewById(R.id.activity_home_meeting_room_tv)
        searchTV = findViewById(R.id.search)
        searchIV = findViewById(R.id.activity_home_search_iv)
        searchEt = findViewById(R.id.activity_home_search_et)
        serviceCenterLL = findViewById(R.id.service_center)
        toiletLL = findViewById(R.id.toilet)
        aedLL = findViewById(R.id.aed)
        fireExtinguisherLL = findViewById(R.id.fire_extinguisher)
        elevatorLL = findViewById(R.id.elevator)
        exitLL = findViewById(R.id.exit)
        hotSearch = findViewById(R.id.activity_home_search_cg)

        LocationApi.instance.getKeyword(this,
            object : NetworkManager.NetworkManagerListener<Array<KeywordData>?> {
                override fun onSucceed(response: Array<KeywordData>?) {
                    Log.e("LOG", "getKeyword" + response!!.size)
                    hotSearchList = ArrayList()
                    for (data in response!!) {
                        hotSearchList!!.add(data.text!!)
                    }
                    runOnUiThread {
                        hotSearchList!!.forEach { title ->
                            val chip = this@HomeActivity.layoutInflater
                                .inflate(R.layout.layout_chip_choice, null, false) as Chip
                            chip.text = title
                            chip.setOnClickListener() {
                                val searchText = chip.text.toString()
                                val searchIntent =
                                    Intent(this@HomeActivity, NaviSDKActivity::class.java)
                                searchIntent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/")
                                searchIntent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://")
                                searchIntent.putExtra(ARG_KEY_MAP_BEARING, 0f)
                                searchIntent.putExtra(ARG_KEY_AUTO_HEADING, true)
                                searchIntent.putExtra(ARG_KEY_KEYWORD, searchText)
                                startActivity(searchIntent)
                            }
                            hotSearch!!.addView(chip)
                        }
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    Log.e("LOG", "getKeyword" + errorMsg)
                }
            })

        searchIV!!.setOnClickListener {
            if (searchEt!!.text.toString().isNotEmpty()) {
                val searchIntent = Intent(this@HomeActivity, NaviSDKActivity::class.java)
                searchIntent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/")
                searchIntent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://")
                searchIntent.putExtra(ARG_KEY_MAP_BEARING, 0f)
                searchIntent.putExtra(ARG_KEY_AUTO_HEADING, true)
                searchIntent.putExtra(ARG_KEY_KEYWORD, searchEt!!.text.toString())
                startActivity(searchIntent)
            }
        }

        searchTV!!.setOnClickListener {
            if (searchPOI!!.id != 0) {
                val intent = Intent(this@HomeActivity, NaviSDKActivity::class.java)
                intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/")
                intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://")
                intent.putExtra(ARG_KEY_MAP_BEARING, 0f)
                intent.putExtra(ARG_KEY_AUTO_HEADING, true)
                intent.putExtra(ARG_KEY_SEARCH_POI, searchPOI)
                startActivity(intent)
            }
        }

        serviceCenterLL!!.setOnClickListener {
            gotoQuickNavi("Information")
        }
        toiletLL!!.setOnClickListener {
            gotoQuickNavi("Restroom")
        }
        aedLL!!.setOnClickListener {
            gotoQuickNavi("AED")
        }
        fireExtinguisherLL!!.setOnClickListener {
            gotoQuickNavi("Hydrant")
        }
        elevatorLL!!.setOnClickListener {
            gotoQuickNavi("Elevator")
        }
        exitLL!!.setOnClickListener {
            gotoQuickNavi("Entrance/Exit")
        }

        findViewById<TextView>(R.id.activity_home_phone_tv).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + "1999")
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(callIntent)
            }
        }

        classifySearchTv!!.setOnClickListener {
            classifySearchTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_00)
            quickGuideTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_dark)
            searchLL!!.visibility = View.VISIBLE
            fastLL!!.visibility = View.GONE
            classifySearchLL!!.visibility = View.VISIBLE
            quickGuildLL!!.visibility = View.GONE
            searchTV!!.visibility = View.VISIBLE
        }

        quickGuideTv!!.setOnClickListener {
            quickGuideTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_00)
            classifySearchTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_dark)
            searchLL!!.visibility = View.VISIBLE
            fastLL!!.visibility = View.GONE
            classifySearchLL!!.visibility = View.GONE
            quickGuildLL!!.visibility = View.VISIBLE
            searchTV!!.visibility = View.GONE
        }

        departmentTv!!.setOnClickListener {
            departmentTv!!.setBackgroundResource(R.drawable.stroke_round_rectangle_blue_30)
            departmentTv!!.setTextColor(resources.getColor(R.color.blue_00))
            meetingRoomTv!!.setBackgroundResource(R.drawable.stroke_round_rectangle_white_stroke)
            meetingRoomTv!!.setTextColor(resources.getColor(android.R.color.white))
            organSpinner!!.adapter = organAdapter
            departSpinner!!.adapter = departAdapter
            searchType = "department"
        }

        meetingRoomTv!!.setOnClickListener {
            meetingRoomTv!!.setBackgroundResource(R.drawable.stroke_round_rectangle_blue_30)
            meetingRoomTv!!.setTextColor(resources.getColor(R.color.blue_00))
            departmentTv!!.setBackgroundResource(R.drawable.stroke_round_rectangle_white_stroke)
            departmentTv!!.setTextColor(resources.getColor(android.R.color.white))
            organSpinner!!.adapter = floorAdapter
            departSpinner!!.adapter = meetingRoomAdapter
            searchType = "meetingRoom"
        }

        mapLL!!.setOnClickListener {
            val intent = Intent(this@HomeActivity, NaviSDKActivity::class.java)
            intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/")
            intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://")
            intent.putExtra(ARG_KEY_MAP_BEARING, 0f)
            intent.putExtra(ARG_KEY_AUTO_HEADING, true)
            startActivity(intent)
        }

        themeLL!!.setOnClickListener {
            openFragmentPage(GuideFragment.newInstance(), GuideFragment.TAG)
        }

        friendLL!!.setOnClickListener {
            openFragmentPage(GroupMainFragment.newInstance(), GroupMainFragment.TAG)
        }

        homeFL!!.setOnClickListener {
            classifySearchTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_dark)
            quickGuideTv!!.setBackgroundResource(R.drawable.solid_round_rectangle_blue_dark)
            searchLL!!.visibility = View.GONE
            fastLL!!.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        taskToMISC(object : Runnable {
            override fun run() {
                try {
                    //start to update your location, it'mean the app just pause or not close normally
                    var findFriend = false
                    val lastGroupsJsonData: String = PreferencesTools.instance.getProperty(
                        applicationContext, PreferencesTools.KEY_ALL_GROUP
                    )!!
                    if (lastGroupsJsonData != null) {
                        val oldGroupData: SendUserLocationResponse =
                            NetworkManager.instance.gson.fromJson(
                                lastGroupsJsonData,
                                SendUserLocationResponse::class.java
                            )
                        if (oldGroupData != null) {
                            val groupDatas = oldGroupData.data
                            if (groupDatas != null) if (groupDatas.size > 0) {
                                for (gd in groupDatas) {
                                    val gi: GroupInfo = gd.groupInfos[0]
                                    val end: String = gi.getEndTimestamp()
                                    if (end.length > 0) {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        try {
                                            val oldDate = sdf.parse(end)
                                            if (System.currentTimeMillis() < oldDate.time) {
//                                                findFriend = true
                                                break
                                            }
                                        } catch (e: ParseException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Log.e(LOG_TAG, "findFriend" + findFriend)
                    findFriendMode(findFriend)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    taskToMISC(this, 500)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOGService != null) {
            mOGService!!.stopService()
            mOGService!!.destroy()
            Log.e(NaviSDKText.LOG_TAG, "mOGService.destroy()")
        }

        if (mEventBus != null) {
            mEventBus!!.unregister(this)
        }
        if (mFindFriendHandler != null) {
            mFindFriendHandler!!.removeCallbacksAndMessages(null)
            mFindFriendHandler = null
        }
        if (mFindFriendHandlerThread != null) if (mFindFriendHandlerThread!!.isAlive) mFindFriendHandlerThread!!.quitSafely()
        if (mMISCHandler != null) {
            for (i in mMISCHandler!!.indices) {
                if (mMISCHandler!![i] != null) {
                    mMISCHandler!![i]!!.removeCallbacksAndMessages(null)
                    mMISCHandler!![i] = null
                }
            }
            mMISCHandler = emptyArray()
        }
        if (mMISCHandlerThread != null) {
            for (i in mMISCHandlerThread!!.indices) {
                if (mMISCHandlerThread!![i] != null) {
                    if (mMISCHandlerThread!![i]!!.isAlive) {
                        mMISCHandlerThread!![i]!!.quitSafely()
                    }
                }
            }
            mMISCHandlerThread = emptyArray()
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
        registerService()
    }

    fun sendUserLocationToServer() {
        Log.e("LOG", "sendUserLocationToServer ")
        if (mLastUserLocation == null) return
        val userLocationList: MutableList<UserCurrentLocation> =
            java.util.ArrayList<UserCurrentLocation>()
        mLastUserLocation!!.date = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
        userLocationList.add(mLastUserLocation!!)
        LocationApi.instance.sendUserLocation(
            this,
            userLocationList, "",
            object : NetworkManager.NetworkManagerListener<SendUserLocationResponse?> {
                override fun onSucceed(response: SendUserLocationResponse?) {
                    if (response != null) if (response.result != null) {
                        Log.e(
                            "LOG",
                            "sendUserLocationToServer response.getResult()" + response.result
                        )
                        val r = Runnable {
                            if (response.result.equals("true")) {
                                val groupDatas: List<GroupData>? = response.data
                                if (groupDatas != null) {
                                    EventBus.getDefault()
                                        .post(OmniEvent(OmniEvent.TYPE_GROUP_RESPONSE, groupDatas))
                                    //Log.e(TAG, "mSendUserLocationResponseToOtherClassListeners:" + mSendUserLocationResponseToOtherClassListeners.size());

                                    //remove the group was left, god damn bug
                                    val s: SharedPreferences =
                                        PreferencesTools.instance.getPreferences(applicationContext)!!
                                    val oldLeftGroupData = s.getString(KEY_LEFT_GROUP_ENDTIME, "")
                                    var oldLeftGroups: Array<GroupData>
                                    if (oldLeftGroupData!!.isNotEmpty()) {
                                        oldLeftGroups =
                                            NetworkManager.instance.gson.fromJson(
                                                oldLeftGroupData,
                                                Array<GroupData>::class.java
                                            )
                                        val saftyData: MutableList<GroupData> =
                                            java.util.ArrayList<GroupData>()
                                        for (n in groupDatas) {
                                            if (n.groupInfos.isNotEmpty()) {
                                                val k: String = n.groupInfos[0].key
                                                var save = true
                                                for (tmp in oldLeftGroups) {
                                                    if (tmp == null) continue
                                                    val ok: String =
                                                        tmp.groupInfos[0].key
                                                    if (k == ok) {
                                                        save = false
                                                        Log.e(
                                                            "LOG",
                                                            "sendUserLocationToServer group key $k old k:$ok should not shown"
                                                        )
                                                        break
                                                    }
                                                }
                                                if (save) saftyData.add(n)
                                            }
                                        }
                                        response.data = saftyData
                                    }
                                    runOnUiThread {
                                        for (k in mSendUserLocationResponseToOtherClassListeners.keys) if (mSendUserLocationResponseToOtherClassListeners[k] != null) {
                                            mSendUserLocationResponseToOtherClassListeners[k]!!.success(
                                                response
                                            )
                                            Log.e(
                                                "LOG",
                                                "send SendUserLocationResponse data to $k"
                                            )
                                        }
                                    }

                                    DataCacheManager.Companion.instance
                                        .setmSendUserLocationResponse(response)

                                    //is any group disband?
                                    if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                                        findFriendGroupDisbandHandler(response)
                                    }
                                    if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) findFriendNoticeHandler(
                                        response
                                    )

                                    //detect the meeting function was changed or not
                                    if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) findFriendMeetingFunctionChangedHandler(
                                        response
                                    )

                                    //update old group data
                                    val groupData: String =
                                        NetworkManager.Companion.instance.gson.toJson(
                                            response,
                                            SendUserLocationResponse::class.java
                                        )
                                    PreferencesTools.instance.saveProperty(
                                        applicationContext,
                                        PreferencesTools.KEY_ALL_GROUP,
                                        groupData
                                    )
                                }
                            }
                        }
                        taskToMISC(r)
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    Log.e(
                        "LOG",
                        "sendUserLocationToServer onFail" + errorMsg
                    )
                }
            })
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkLocationService() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions()
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("位置服務尚未開啟，請設定")
            dialog.setPositiveButton(
                "前往設定"
            ) { paramDialogInterface, paramInt -> // TODO Auto-generated method stub
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            dialog.setNegativeButton(
                "取消"
            ) { paramDialogInterface, paramInt ->
                // TODO Auto-generated method stub
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
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
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
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.CALL_PHONE
                ),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
//            registerService();
        }
    }

    fun addSendUserLocationResponseToOtherClassListener(
        c: Class<*>,
        listener: SendUserLocationResponseToOtherClass?
    ) {
        if (listener != null) runOnUiThread {
            mSendUserLocationResponseToOtherClassListeners.remove(c.simpleName)
            mSendUserLocationResponseToOtherClassListeners[c.simpleName] = listener
        }
    }

    fun removeSendUserLocationResponseToOtherClassListener(c: Class<*>) {
        runOnUiThread { mSendUserLocationResponseToOtherClassListeners.remove(c.simpleName) }
    }

    fun showKeyboard(v: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(windowToken: IBinder?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun findFriendMode(startSync: Boolean) {
        Log.e("LOG", "findFriendMode" + startSync)
        if (startSync) {
            currentFindFriendModeUpdateFrq = inFindFriendModeUpdateFrq
        } else {
            currentFindFriendModeUpdateFrq = notFindFriendModeUpdateFrq
        }
        val timerRun: Runnable = object : Runnable {
            override fun run() {
                sendUserLocationToServer()
                if (mFindFriendHandler != null) mFindFriendHandler!!.postDelayed(
                    this,
                    currentFindFriendModeUpdateFrq.toLong()
                )
            }
        }
        if (mFindFriendHandler == null) {
            mFindFriendHandlerThread = HandlerThread("mFindFriendHandlerThread")
            mFindFriendHandlerThread!!.start()
            mFindFriendHandler = Handler(mFindFriendHandlerThread!!.getLooper())
        } else {
            mFindFriendHandler!!.removeCallbacksAndMessages(null)
        }
        if (startSync)
            mFindFriendHandler!!.post(timerRun)
        inFindFriendMode = startSync
    }

    public fun openFragmentPage(fragment: Fragment, tag: String) {
        AnimationFragmentManager.getInstance().addFragmentPage(
            this,
            R.id.activity_home_fl, fragment, tag
        )
    }

    private var mLastUserLocation: UserCurrentLocation? = null

    private fun updateUserCurrentLocation() {
        Log.e("LOG", "updateUserCurrentLocation" + mLastLocation!!.latitude)
        if (mLastLocation == null) return
        val currentFloorPlanId: String = DataCacheManager.instance.userCurrentFloorPlanId!!
        if (mLastUserLocation == null) {
            mLastUserLocation = UserCurrentLocation.Builder()
                .setFloorPlanId(currentFloorPlanId)
                .setLat(mLastLocation!!.latitude.toString())
                .setLng(mLastLocation!!.longitude.toString())
                .setDate(SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date()))
                .build()
        } else {
            mLastUserLocation!!.plan_id = currentFloorPlanId
            mLastUserLocation!!.lat = mLastLocation!!.latitude.toString()
            mLastUserLocation!!.lng = mLastLocation!!.longitude.toString()
            mLastUserLocation!!.date = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
        }
    }

    private fun findFriendGroupDisbandHandler(response: SendUserLocationResponse) {
        val oldResponse: SendUserLocationResponse =
            DataCacheManager.instance.getmSendUserLocationResponse()
                ?: return
        if (oldResponse.data == null) return
        val newData = response.data
        val oldData = oldResponse.data
        if (newData == null) return
        if (oldData == null) return
        for (ogd in oldData) {
            val ogi = ogd.groupInfos[0] ?: continue
            var find = false
            val isCreator = false
            for (ngd in newData) {
                val ngi = ngd.groupInfos[0]
                val nK = ngi.key
                val oK = ogi.key
                if (nK == oK) {
                    find = true
                    break
                }
            }
            if (!find) {
                //cancel alarm
                val noticeTime = ogi.noticeTime
                Log.e("LOG", "findFriendGroupDisbandHandler:" + noticeTime + "\t" + ogi.key)
                if (noticeTime != null) if (noticeTime.length > 0) {
                    val key = ogi.key
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    var nt: Date? = null
                    try {
                        nt = dateFormatter.parse(noticeTime)
                        //to cancel alarm, consider group key and notice time only
                        val c = CreateGroupCollectionJsonObject()
                        c.setGroupKey(key)
                        c.setCollectionNoticeTime(nt.time)
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = c.getCollectionNoticeTime()
                        val mAM = MyAlarmManager(applicationContext)
                        mAM.cancelAlarm(c, FIND_FRIEND_NOTICE)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }

                //show disband notification if the device is member and the current page is not GroupMembersTestFragment
                var showNitification = true
                val f = supportFragmentManager.findFragmentById(R.id.activity_home_fl)
                if (f != null) {
                    val className = f.javaClass.simpleName
                    if (className == GroupMembersTestFragment::class.java.simpleName) {
                    } else {
                        showNitification = true
                    }
                }
                //creator?
                val myDeviceId: String =
                    instance.getDeviceId(this@HomeActivity)!!
                for (gm in ogd.groupMembers) {
                    if (gm.deviceId.equals(myDeviceId)) {
                        if (gm.role.equals("admin")) showNitification = false
                        break
                    }
                }
                if (showNitification) {
                    val vibrate_effect = longArrayOf(1000, 1000)
                    //Toast.makeText(getApplicationContext(), minor+","+pushContent.getContent_zh(), Toast.LENGTH_SHORT).show();
                    var channel_id: String
                    val id = ogi.id.toInt()
                    channel_id = getString(R.string.notify_channel_name_meeting)
                    val notificationManager: NotificationManager =
                        DataCacheManager.instance.getNotificationManager()
                            ?: return
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channel_id,
                            getString(R.string.notify_channel_name_meeting),
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        notificationManager.createNotificationChannel(channel)
                    }
                    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
                    val content =
                        java.lang.String.format(getString(R.string.group_disband_text), ogi.title)
                    val builder = NotificationCompat.Builder(this, channel_id)
                    builder.setSmallIcon(R.drawable.ic_push)
                        .setColor(
                            ContextCompat.getColor(
                                this,
                                R.color.colorPrimary
                            )
                        ) //.setLargeIcon(bitmap)
                        .setContentTitle(getString(R.string.home_page_option_group_location))
                        .setContentText(content)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVibrate(vibrate_effect)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true)

                    //cancel notification of this group
                    notificationManager.cancel("" + id, id) //MeetingFunctionChanged
                    notificationManager.cancel(ogi.key, ogi.key.toInt()) //Notice
                    notificationManager.notify("" + id, id, builder.build())
                }
            }
        }
    }

    private fun findFriendNoticeHandler(response: SendUserLocationResponse) {
        val mAM = MyAlarmManager(applicationContext)
        if (response.data.size === 0) {
            return
        }
        //add alarm
        for (gd in response.data) {
            val gi = gd.groupInfos[0]
            val key = gi.key
            val noticeTime = gi.noticeTime ?: continue
            if (noticeTime.length == 0) continue
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                var addAlarm = true
                val nt = dateFormatter.parse(noticeTime)

                //compare old data
                val s: SharedPreferences = PreferencesTools.instance.getPreferences(
                    applicationContext
                )!!
                val oldGroupData = s.getString(FIND_FRIEND_NOTICE, "")
                if (oldGroupData!!.length > 0) {
                    val oldGroups = oldGroupData.split(",,,").toTypedArray()
                    if (oldGroups.size > 0) for (g in oldGroups) {
                        if (g.length == 0) continue
                        val oldData: CreateGroupCollectionJsonObject =
                            NetworkManager.instance.gson.fromJson(
                                g,
                                CreateGroupCollectionJsonObject::class.java
                            )
                        val key_old: String = oldData.getGroupKey()
                        if (key != key_old) continue
                        try {
                            Log.e("LOG", "key" + key + " noticeTime:" + nt.time + " " + noticeTime)
                            val tmp = Date()
                            tmp.time = oldData.getCollectionNoticeTime()
                            Log.e(
                                "LOG",
                                "old GroupData key" + oldData.getGroupKey()
                                    .toString() + " old noticeTime:" + oldData.getCollectionTime()
                                    .toString() + " " + dateFormatter.format(tmp)
                            )
                        } catch (e: Exception) {
                        }

                        //start to compara with old data
                        //notice time
                        val noticeTime_old: Long = oldData.getCollectionNoticeTime()
                        if (noticeTime_old == 0L) break
                        if (nt.time != noticeTime_old) {
                            //cancel old alarm
                            mAM.cancelAlarm(oldData, FIND_FRIEND_NOTICE)
                        } else {
                            //notice time not change
                            addAlarm = false
                        }
                    }
                }
                if (addAlarm) {
                    val c = CreateGroupCollectionJsonObject()
                    c.setGroupKey(key)
                    c.setGroupName(gi.title)
                    c.setCollectionNoticeTime(nt.time)
                    try {
                        val ct = dateFormatter.parse(gi.meetingTime)
                        c.setCollectionTime(ct.time)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    c.setCreater(gd.groupMembers[0].name)
                    try {
                        val et = dateFormatter.parse(gi.endTimestamp)
                        c.setEndTime(et.time)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    if (gi.ap_id != null) if (gi.ap_id.isNotEmpty()) {
                        //try find POI from current building I
                        val poiList: List<POI> = DataCacheManager.instance.getPOIs()!!
                        var p_type = ""
                        //                        String currentBuildingId = DataCacheManager.getInstance().getCurrentSelectedBuildingId();
//                        BuildingFloors bfs = DataCacheManager.getInstance().getBuildingFloors(this, currentBuildingId);
                        var tmp: POI? = null
                        for (p in poiList) {
                            p_type = "poi"
                            if (p.id.equals(gi.ap_id) && p_type == gi.p_type) {
                                tmp = p
                                break
                            }
                            if (tmp != null) break
                        }
                        if (tmp != null) {
                            c.setCollectionPlace(
                                NetworkManager.instance.gson.toJson(
                                    tmp,
                                    POI::class.java
                                )
                            )
                        } else {
                            //didn't find POI, don't show info about POI > MyAlarmNoticeReceiver.sendFindFriendNotification
                        }
                    }
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = c.getCollectionNoticeTime()
                    /*                                //for test
                                cal.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
                                cal.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND) + 5);*/mAM.addAlarm(
                        cal,
                        c,
                        FIND_FRIEND_NOTICE
                    )
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                continue
            }
        }
    }

    private fun findFriendMeetingFunctionChangedHandler(response: SendUserLocationResponse) {
        var oldResponse: SendUserLocationResponse? =
            DataCacheManager.instance.getmSendUserLocationResponse()!!
        if (oldResponse == null) {
            val oldGroupData: String = PreferencesTools.instance.getProperty(
                applicationContext, PreferencesTools.KEY_ALL_GROUP
            ) ?: return
            oldResponse = NetworkManager.instance.gson.fromJson(
                oldGroupData,
                SendUserLocationResponse::class.java
            )
        }
        val newData = response.data
        val oldData = oldResponse!!.data
        if (newData == null) return
        if (oldData == null) return
        for (ngd in newData) {
            val ngi = ngd.groupInfos[0] ?: continue
            //creator? do not show notification
            var isCreater = false
            for (gm in ngd.groupMembers) {
                Log.e("LOG", gm.role.toString() + " " + gm.name)
                if (gm.role.equals("admin")) {
                    val myDeviceId: String =
                        instance.getDeviceId(this@HomeActivity)!!
                    if (myDeviceId.trim { it <= ' ' } == gm.deviceId.trim()) isCreater = true
                    break
                }
            }
            if (isCreater) continue
            for (ogd in oldData) {
                val ogi = ogd.groupInfos[0] ?: continue
                val nK = ngi.key
                val oK = ogi.key
                if (nK == oK) {
                    var change = false
                    val nAPID = ngi.ap_id
                    val nMeetingTime = ngi.meetingTime
                    val nNoticeTime = ngi.noticeTime
                    val oAPID = ogi.ap_id
                    val oMeetingTime = ogi.meetingTime
                    val oNoticeTime = ogi.noticeTime
                    val meetingPlaceChanged = nAPID != oAPID
                    val meetingTimeChanged = nMeetingTime != oMeetingTime
                    val noticeTimeChanged = nNoticeTime != oNoticeTime
                    if (meetingPlaceChanged
                        || meetingTimeChanged
                        || noticeTimeChanged
                    ) change = true
                    //send notification
                    if (change) {
                        val vibrate_effect = longArrayOf(1000, 1000)
                        //Toast.makeText(getApplicationContext(), minor+","+pushContent.getContent_zh(), Toast.LENGTH_SHORT).show();
                        var channel_id: String
                        val id = ngi.id.toInt()
                        channel_id = getString(R.string.notify_channel_name_meeting)
                        Log.e("LOG", "channel_id:$channel_id")
                        //Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.twirl);
                        val notificationManager: NotificationManager =
                            DataCacheManager.instance.getNotificationManager()!!
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
/*                            AudioAttributes attributes = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build();*/
                            val channel = NotificationChannel(
                                channel_id,
                                getString(R.string.notify_channel_name_meeting),
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            channel.enableVibration(true)
                            //channel.setSound(uri, attributes);
                            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                            notificationManager.createNotificationChannel(channel)
                        }
                        val inboxStyle = NotificationCompat.InboxStyle()
                        inboxStyle.setBigContentTitle(getString(R.string.string_find_friend_setting_changed_notification))
                        val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        var content = ngi.title
                        inboxStyle.addLine(ngi.title)
                        var meetingPlaceInfo: String? = null
                        var meetingTimeInfo: String? = null
                        var noticeTimeInfo: String? = null
                        //is meeting place changed?
                        if (meetingPlaceChanged) if (nAPID.length > 0) {
                            var newMeetingPOI: POI? = null
                            val floors = DataCacheManager.instance.getFloorsByBuildingId(this, "2")
                            for (floor in floors!!) {
                                for (p in floor.pois!!) {
                                    Log.e(
                                        "LOG",
                                        "" + p.id.toString() + "\t" + p.name
                                    )
                                    if (p.id.equals(nAPID)) {
                                        newMeetingPOI = p
                                        break
                                    }
                                }
                                if (newMeetingPOI != null) break
                            }
                            if (newMeetingPOI != null) break
                            if (newMeetingPOI != null) {
                                val floor: String
                                floor = if (newMeetingPOI.floorNumber.equals("-1")) {
                                    "B1"
                                } else newMeetingPOI.floorNumber.toString() + "F"
                                content += "/" + getString(R.string.dialog_hint_create_group_meeting_place) + ":" + newMeetingPOI.name + "-" + floor
                                meetingPlaceInfo =
                                    getString(R.string.dialog_hint_create_group_meeting_place) + ":" + newMeetingPOI.name + "-" + floor
                            }
                        } else {
                            content += "/" + getString(R.string.dialog_hint_create_group_meeting_place) + ":" + getString(
                                R.string.none
                            )
                            meetingPlaceInfo =
                                getString(R.string.dialog_hint_create_group_meeting_place) + ":" + getString(
                                    R.string.none
                                )
                        }
                        //is meeting time changed?
                        if (nMeetingTime.length > 0) {
                            if (meetingTimeChanged) content += "/" + getString(R.string.dialog_hint_create_group_meeting_time) + ":" + nMeetingTime
                            meetingTimeInfo =
                                getString(R.string.dialog_hint_create_group_meeting_time) + ":" + nMeetingTime
                        } else {
                            if (meetingTimeChanged) content += "/" + getString(R.string.dialog_hint_create_group_meeting_time) + ":" + getString(
                                R.string.none
                            )
                            meetingTimeInfo =
                                getString(R.string.dialog_hint_create_group_meeting_time) + ":" + getString(
                                    R.string.none
                                )
                        }
                        //if(noticeTimeChanged)
                        if (nNoticeTime.length > 0) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val c1 = Calendar.getInstance()
                            val c2 = Calendar.getInstance()
                            try {
                                val d1 = sdf.parse(nMeetingTime)
                                c1.time = d1
                                val d2 = sdf.parse(nNoticeTime)
                                c2.time = d2
                                val difference = c1.timeInMillis - c2.timeInMillis
                                val notice_time = (difference / (60 * 1000)).toInt()
                                if (noticeTimeChanged) content += "/" + getString(R.string.dialog_hint_create_group_meeting_notice) + ":" + notice_time + " " + getString(
                                    R.string.dialog_hint_create_group_meeting_notice_hint
                                )
                                noticeTimeInfo =
                                    getString(R.string.dialog_hint_create_group_meeting_notice) + ":" + notice_time + " " + getString(
                                        R.string.dialog_hint_create_group_meeting_notice_hint
                                    )
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        } else {
                            if (noticeTimeChanged) content += "/" + getString(R.string.dialog_hint_create_group_meeting_notice) + ":" + getString(
                                R.string.none
                            )
                            noticeTimeInfo =
                                getString(R.string.dialog_hint_create_group_meeting_notice) + ":" + getString(
                                    R.string.none
                                )
                        }
                        if (nAPID.length == 0 && nMeetingTime.length == 0 && nNoticeTime.length == 0) {
                            inboxStyle.addLine(getString(R.string.string_cancel_meeting_function))
                        } else {
                            if (meetingPlaceChanged) inboxStyle.addLine(meetingPlaceInfo)
                            if (meetingTimeChanged || noticeTimeChanged) {
                                inboxStyle.addLine(meetingTimeInfo)
                            }
                            if (noticeTimeChanged) inboxStyle.addLine(noticeTimeInfo)
                        }
                        val builder = NotificationCompat.Builder(this, channel_id)
                        builder.setSmallIcon(R.drawable.ic_push)
                            .setColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.colorPrimary
                                )
                            ) //.setLargeIcon(bitmap)
                            .setContentTitle(getString(R.string.string_find_friend_setting_changed_notification))
                            .setContentText(content)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setVibrate(vibrate_effect)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setStyle(inboxStyle)
                        notificationManager.notify("" + id, id, builder.build())
                    }
                }
            }
        }
    }

    fun gotoQuickNavi(type: String) {
        val intent = Intent(this@HomeActivity, NaviSDKActivity::class.java)
        intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/")
        intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://")
        intent.putExtra(ARG_KEY_MAP_BEARING, 0f)
        intent.putExtra(ARG_KEY_AUTO_HEADING, true)
        intent.putExtra(ARG_KEY_QUICK_TYPE, type)
        startActivity(intent)
    }

    @Synchronized
    fun taskToMISC(r: Runnable?) {
        taskToMISC(r, 0)
    }

    @Synchronized
    fun taskToMISC(r: Runnable?, delay: Int) {
        var delay = delay
        if (mMISCHandlerThread == null) {
            mMISCHandlerThread = arrayOfNulls(workNumOfMISC)
            for (i in 0 until workNumOfMISC) {
                mMISCHandlerThread!![i] = HandlerThread("mMISCHandlerThread$i")
                mMISCHandlerThread!![i]!!.priority = Thread.MIN_PRIORITY
                mMISCHandlerThread!![i]!!.start()
            }
        } else {
            for (i in mMISCHandlerThread!!.indices) {
                if (mMISCHandlerThread!![i] == null) {
                    mMISCHandlerThread!![i] = HandlerThread("mMISCHandlerThread$i")
                    mMISCHandlerThread!![i]!!.priority = Thread.MIN_PRIORITY
                    mMISCHandlerThread!![i]!!.start()
                } else if (!mMISCHandlerThread!![i]!!.isAlive) {
                    mMISCHandlerThread!![i] = HandlerThread("mMISCHandlerThread$i")
                    mMISCHandlerThread!![i]!!.priority = Thread.MIN_PRIORITY
                    mMISCHandlerThread!![i]!!.start()
                }
            }
        }
        if (mMISCHandler == null) {
            mMISCHandler = arrayOfNulls(workNumOfMISC)
            for (i in 0 until workNumOfMISC) {
                mMISCHandler!![i] = Handler(mMISCHandlerThread!![i]!!.looper)
            }
        } else {
            for (i in 0 until workNumOfMISC) {
                if (mMISCHandler!!.isNotEmpty() && mMISCHandler!![i] == null) {
                    mMISCHandler!![i] = Handler(mMISCHandlerThread!![i]!!.looper)
                }
            }
        }
        val targetHandler = mMISCHandler!![nextWorker]
        val targetThread = mMISCHandlerThread!![nextWorker]
        //Log.e(TAG, "taskToMISC, send work to " + targetThread.getName());
        if (targetHandler != null) if (mMISCHandlerThread != null) if (targetThread!!.isAlive) if (targetHandler.looper == targetThread.looper) if (r != null) {
            if (delay < 0) delay = 0
            try {
                targetHandler.postDelayed(r, delay.toLong())
            } catch (e: IllegalStateException) {
                val line = Thread.currentThread().stackTrace[2].lineNumber - 1
                //String currentMethodName = new Object(){}.getClass().getEnclosingMethod().getName();
                val currentMethodName = Thread.currentThread().stackTrace[2].methodName
                //Log.e(TAG, "notice!!!" + currentMethodName + " line:" + line + ", fail. Error msg is " + e.getMessage() + ", try renew mMISCHandlerThread.");
                mMISCHandlerThread = emptyArray()
                taskToMISC(r, delay)
            }
        }
        nextWorker = (nextWorker + 1) % workNumOfMISC
    }

}