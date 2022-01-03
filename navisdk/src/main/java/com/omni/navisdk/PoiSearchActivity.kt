package com.omni.navisdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.omni.navisdk.manager.DataCacheManager
import com.omni.navisdk.module.BuildingFloor
import com.omni.navisdk.module.HotSearchData
import com.omni.navisdk.module.OmniEvent
import com.omni.navisdk.module.POI
import com.omni.navisdk.network.LocationApi
import com.omni.navisdk.network.NetworkManager
import com.omni.navisdk.tool.NaviSDKText
import com.omni.navisdk.tool.NaviSDKText.LOG_TAG
import com.omni.navisdk.tool.PreferencesTools
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class PoiSearchActivity : Activity() {

    private val ARG_KEY_KEYWORD = "arg_key_keyword"
    private var mView: View? = null

    private var noDataTv: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var mPoiList: MutableList<POI>? = null
    private var poiListAdapter: PoiListAdapter? = null
    private var mSearchText: String? = ""
    var searchEdt: EditText? = null

    var quick_search: LinearLayout? = null
    var hot_search: ChipGroup? = null
    var hotSearchList: MutableList<String>? = null
    var searchRecordRV: RecyclerView? = null
    var mSearchRecordAdapter: SearchRecordAdapter? = null
    var recordStrArray: List<String>? = null
    var clearRecordTV: TextView? = null

    var mLastLocation: Location? = null
    var mEventBus: EventBus? = null

    var keyword: String? = ""

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: OmniEvent) {
        when (event.type) {
            OmniEvent.TYPE_USER_OUTDOOR_LOCATION -> {
                mLastLocation = event.obj as? Location
            }
            OmniEvent.TYPE_USER_INDOOR_LOCATION -> {
                Log.e(NaviSDKText.LOG_TAG, "TYPE_USER_INDOOR_LOCATION")
                mLastLocation = event.obj as? Location
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.poi_search_fragment_view)
        initView()

        if (mEventBus == null) {
            mEventBus = EventBus.getDefault()
        }
        mEventBus!!.register(this)

        if (intent.extras != null && intent.extras!!.getSerializable(ARG_KEY_KEYWORD) != null) {
            keyword = intent.extras!!.getString(ARG_KEY_KEYWORD)!!
            doSearchPOI(keyword)
            searchEdt!!.setText(keyword)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mEventBus != null) {
            mEventBus!!.unregister(this)
        }
    }

    private fun initView() {

        recyclerView = findViewById(R.id.drawer_exhibits_fragment_view_rv)
        hot_search = findViewById(R.id.poi_search_activity_hot_search_cg)
        quick_search = findViewById(R.id.poi_search_activity_quick_search_ll)
        searchRecordRV = findViewById(R.id.poi_search_activity_search_record_rv)
        clearRecordTV = findViewById(R.id.poi_search_activity_clear_record_tv)

        findViewById<RelativeLayout>(R.id.activity_search_rl).setBackgroundResource(R.color.black_3d)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = layoutManager

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.color.gray_d8)!!)
        recyclerView!!.addItemDecoration(divider)
        recyclerView!!.itemAnimator = DefaultItemAnimator()

        val toolbar = findViewById<Toolbar>(R.id.poi_search_activity_action_bar)
        val backFL = toolbar.findViewById(R.id.poi_search_activity_back) as FrameLayout
        backFL.setOnClickListener { finish() }

        searchEdt = findViewById<EditText>(R.id.poi_search_activity_search_et)
        val searchBtn = findViewById<ImageView>(R.id.poi_search_activity_search_btn)
        searchBtn.setOnClickListener {
            val searchText = searchEdt!!.text.toString()
            if (searchText.isNotEmpty()) {
                mSearchText = searchText
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
                doSearchPOI(mSearchText)
            }
        }
        searchEdt!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = searchEdt!!.text.toString()
                mSearchText = searchText
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
                doSearchPOI(mSearchText)
                return@OnEditorActionListener true
            }
            false
        })
        val clearBtn = findViewById<ImageView>(R.id.poi_search_activity_clear_btn)
        clearBtn.setOnClickListener {
            searchEdt!!.setText("")
            quick_search!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
            noDataTv!!.visibility = View.GONE
        }

        noDataTv = findViewById(R.id.no_data_tv)

        LocationApi.instance.getHotSearch(this,
            object : NetworkManager.NetworkManagerListener<Array<HotSearchData>> {
                override fun onSucceed(response: Array<HotSearchData>?) {
                    hotSearchList = ArrayList()
                    for (data in response!!) {
                        hotSearchList!!.add(data.title_zh!!)
                    }
                    runOnUiThread {
                        hotSearchList!!.forEach { title ->
                            val chip = this@PoiSearchActivity.layoutInflater
                                .inflate(R.layout.layout_chip_choice, null, false) as Chip
                            chip.text = title
                            chip.setOnClickListener() {
                                val searchText = chip.text.toString()
                                searchEdt!!.setText(searchText)
                                mSearchText = searchText
                                doSearchPOI(mSearchText)
                            }
                            hot_search!!.addView(chip)
                        }
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                }
            })

        val recordStr = PreferencesTools.instance.getPropertyString(
            this@PoiSearchActivity, PreferencesTools.KEY_SEARCH_RECORD
        )
        if (recordStr!!.isNotEmpty())
            recordStrArray = recordStr.split(",").reversed()
        else
            recordStrArray = ArrayList()

        mSearchRecordAdapter = SearchRecordAdapter()
        searchRecordRV!!.adapter = mSearchRecordAdapter
        mSearchRecordAdapter!!.notifyDataSetChanged()

        clearRecordTV!!.setOnClickListener {
            PreferencesTools.instance.saveProperty(this, PreferencesTools.KEY_SEARCH_RECORD, "")
            recordStrArray = ArrayList()
            mSearchRecordAdapter!!.notifyDataSetChanged()
        }
    }

    internal inner class PoiListAdapter(val clusterItems: List<POI>) :
        RecyclerView.Adapter<PoiListAdapter.mViewHolder>() {
        inner class mViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var icon: ImageView = itemView.findViewById(R.id.search_list_item_view_iv) as ImageView
            var title: TextView =
                itemView.findViewById(R.id.search_list_item_view_tv_title) as TextView
            var distance: TextView =
                itemView.findViewById(R.id.search_list_item_view_tv_distance) as TextView
            var mainLayout: LinearLayout =
                itemView.findViewById(R.id.search_list_item_view) as LinearLayout
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): mViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_list_item_view, parent, false)
            return mViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: mViewHolder, position: Int) {
            val poi = clusterItems[position]
            val floor =
                DataCacheManager.instance.getSearchFloorPlanId(this@PoiSearchActivity, poi.id)
            holder.icon.setImageResource(poi.getPOIIconRes(false))
            holder.title.text = floor!!.name + " - " + poi.name
            holder.distance.text = poi.distance + "m"
            holder.mainLayout.setOnClickListener {
                val intent = Intent()
                intent.putExtra(NaviSDKText.INTENT_EXTRAS_SELECTED_POI, poi)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        override fun getItemCount(): Int {
            return clusterItems.size
        }
    }

    inner class SearchRecordAdapter() :
        RecyclerView.Adapter<SearchRecordAdapter.mViewHolder>() {
        inner class mViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title: TextView =
                itemView.findViewById(R.id.search_record_list_item_view_tv_title) as TextView
            var mainLayout: LinearLayout =
                itemView.findViewById(R.id.search_record_list_item_view) as LinearLayout
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): mViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_record_list_item_view, parent, false)
            return mViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: mViewHolder, position: Int) {
            val title = recordStrArray!![position]
            holder.title.text = title
            holder.mainLayout.setOnClickListener {
                doSearchPOI(title)
            }
        }

        override fun getItemCount(): Int {
            return if (recordStrArray!!.size > 10)
                10
            else
                recordStrArray!!.size
        }
    }

    private fun doSearchPOI(keyword: String?) {
        Log.e(LOG_TAG, "doSearchPOI")
        var isRepeat = false
        for (s in recordStrArray!!) {
            if (s == keyword)
                isRepeat = true
        }
        if (!isRepeat)
            DataCacheManager.instance.setSearchRecord(this, keyword!!)

        val recordStr = PreferencesTools.instance.getPropertyString(
            this@PoiSearchActivity, PreferencesTools.KEY_SEARCH_RECORD
        )
        recordStrArray = recordStr!!.split(",").reversed()
        mSearchRecordAdapter!!.notifyDataSetChanged()

        var lat: Double
        var lng: Double
        if (DataCacheManager.instance.isInBuilding(this)) {
            if (mLastLocation != null) {
                lat = mLastLocation!!.latitude
                lng = mLastLocation!!.longitude
            } else {
                lat = NaviSDKActivity.START_LOCATION.latitude
                lng = NaviSDKActivity.START_LOCATION.longitude
            }
        } else {
            val floor = DataCacheManager.instance.getMainGroundFloorPlanId(this)
            if (floor != null && DataCacheManager.instance.getEntrancePOI(floor) != null) {
                val entrancePOI = DataCacheManager.instance.getEntrancePOI(floor)
                lat = entrancePOI!!.latitude
                lng = entrancePOI!!.longitude
            } else {
                lat = NaviSDKActivity.START_LOCATION.latitude
                lng = NaviSDKActivity.START_LOCATION.longitude
            }
        }
        var id = DataCacheManager.instance!!.userCurrentFloorPlanId!!
        if (id == "outdoor")
            id = DataCacheManager.instance!!.getMainGroundFloorPlanId(this)!!.floorPlanId.toString()
        val buildingId = DataCacheManager.instance!!.getBuildingIdByFloorPlanId(this, id)
        if (buildingId != null) {
            LocationApi.instance.doSearch(this, buildingId, keyword!!, lat, lng,
                object : NetworkManager.NetworkManagerListener<Array<BuildingFloor>> {
                    override fun onSucceed(response: Array<BuildingFloor>?) {
                        mPoiList = ArrayList()
                        for (buildingFloor in response!!) {
                            for (poi in buildingFloor.pois!!) {
                                if (poi.type != "map_text") {
                                    mPoiList!!.add(poi)
                                }
                            }
                        }
                        runOnUiThread {
                            quick_search!!.visibility = View.GONE
                            recyclerView!!.visibility = View.VISIBLE
                            setRVData(mPoiList!!)
                        }
                    }

                    override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
        }
    }

    private fun setRVData(poiList: List<POI>) {
        if (poiList.isEmpty()) {
            noDataTv!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
        } else {
            noDataTv!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
        }

        poiListAdapter = PoiListAdapter(poiList)
        recyclerView!!.adapter = poiListAdapter
        poiListAdapter!!.notifyDataSetChanged()
    }
}
