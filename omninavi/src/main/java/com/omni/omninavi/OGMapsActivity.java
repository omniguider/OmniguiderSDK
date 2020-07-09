package com.omni.omninavi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.omni.omninavi.manager.IndoorInfoManager;
import com.omni.omninavi.manager.NetworkManager;
import com.omni.omninavi.model.OGFloor;
import com.omni.omninavi.model.OGFloors;
import com.omni.omninavi.model.OGNaviRoute;
import com.omni.omninavi.model.OGNaviRoutePOI;
import com.omni.omninavi.model.OGPOI;
import com.omni.omninavi.service.OGService;
import com.omni.omninavi.tool.DialogTools;
import com.omni.omninavi.view.OmniClusterItem;
import com.omni.omninavi.view.OmniClusterRender;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OGMapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        OGService.LocationListener,
        GoogleMap.OnInfoWindowCloseListener {

    private static final String ARG_DESTINATION_ID = "arg_navigation_destination_id";
    private static final String ARG_DESTINATION_NAME = "arg_navigation_destination_name";
    private static final String ARG_USER_CURRENT_FLOOR_LEVEL = "arg_navigation_user_current_floor_level";
    private static final String ARG_GO_TO_FLOOR_LEVEL = "arg_navigation_go_to_floor_level";
    private static final String ARG_GO_TO_FLOOR_PLAN_ID = "arg_navigation_go_to_floor_plan_id";

    private static final String ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE = "arg_emergency_navigation_emergency_type";
    private static final String ARG_EMERGENCY_NAVIGATION_USER_CURRENT_FLOOR_LEVEL = "arg_emergency_navigation_user_current_floor_level";

    /**
     * Open this map page with navigation to destination poi.
     *
     * @param previousActivity
     * @param destinationId         the destination ogpoi id.
     * @param destinationName       the destination ogpoi name.
     * @param userCurrentFloorLevel if user in 1F, send "1".
     */
    public static void navigationTo(Activity previousActivity,
                                    String destinationId,
                                    String destinationName,
                                    String userCurrentFloorLevel,
                                    String gotoFloor,
                                    String gotoFloorPlanId) {

        Intent intent = new Intent(previousActivity, OGMapsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_DESTINATION_ID, destinationId);
        bundle.putString(ARG_DESTINATION_NAME, destinationName);
        bundle.putString(ARG_USER_CURRENT_FLOOR_LEVEL, userCurrentFloorLevel);
        bundle.putString(ARG_GO_TO_FLOOR_LEVEL, gotoFloor);
        bundle.putString(ARG_GO_TO_FLOOR_PLAN_ID, gotoFloorPlanId);

        intent.putExtras(bundle);
        previousActivity.startActivity(intent);
    }

    /**
     * Open this map page with emergency route.
     *
     * @param previousActivity
     * @param userCurrentFloorLevel
     * @param emergencyType         Stair, Entrance/Exit, Hydrant, AED
     */
    public static void emergencyNaviTo(Activity previousActivity,
                                       String userCurrentFloorLevel,
                                       String emergencyType) {

        Intent intent = new Intent(previousActivity, OGMapsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE, emergencyType);
        bundle.putString(ARG_EMERGENCY_NAVIGATION_USER_CURRENT_FLOOR_LEVEL, userCurrentFloorLevel);

        intent.putExtras(bundle);
        previousActivity.startActivity(intent);
    }

    double taipeiGovLat = 25.037512538033;
    double taipeiGovLng = 121.56507643016;
    LatLng tiapeiGovLocation = new LatLng(taipeiGovLat, taipeiGovLng);

    class NavigationMode {
        static final int USER_IN_NAVIGATION = 0;
        static final int WATCH_OTHER_PLACE = 1;
        static final int NOT_NAVIGATION = 2;
    }

    private static final int REQUEST_CODE_PERMISSIONS = 90;
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int MAP_ZOOM_LEVEL = 20;
    private static final int MARKER_Z_INDEX = 150;
    private static final int MAP_MIN_ZOOM_LEVEL = 16;
    private static final int MAP_MAX_ZOOM_LEVEL = 22;
    private static final float POLYLINE_WIDTH = 50.0f;
    private static final int POLYLINE_Z_INDEX = 100;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    //    @BindView(R.id.map_content_view_ll_floors)
    private LinearLayout mFloorsLayout;
    //    @BindView(R.id.map_content_view_fab_floor_selector)
    private FloatingActionButton mFloorSelectorFAB;
    //    @BindView(R.id.map_fragment_view_fab_current_position)
    private FloatingActionButton mCurrentPositionFAB;
    //    @BindView(R.id.map_content_view_poi_info)
    private LinearLayout mPOIInfoLayout;
    //    @BindView(R.id.poi_info_view_cl)
    private CoordinatorLayout mPOIInfoTitleCL;
    //    @BindView(R.id.poi_info_view_tv_poi_title)
    private TextView mPOIInfoTitleTV;
    //    @BindView(R.id.poi_info_view_niv_poi_icon)
    private NetworkImageView mPOIInfoIconNIV;
    private Button mLeaveNaviBtn;
    private TextView mNaviInfoTV;

    private Location mLastLocation;
    private OGService mOGService;
    private Marker mUserMarker;
    private Circle mUserAccuracyCircle;
    private Map<String, TileOverlay> mTileOverlayMap;
    private ClusterManager<OmniClusterItem> mClusterManager;
    private OmniClusterRender mOmniClusterRender;
    private OGPOI mCurrentSelectedPOI;
    private BottomSheetBehavior mBottomSheetBehavior;
    private boolean mFABShouldSlide = true;

    private boolean mIsIndoor;
    private boolean mIsInTaipeiGov = false;
    private int mNavigationMode = NavigationMode.NOT_NAVIGATION;
    private OGPOI mStartPOI;
    private OGPOI mEndPOI;
    private Marker mNavigationMarker;

    @ColorInt
    private int[] mPolyLineColors = {Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.BLACK};

    private Marker mOriginalPOIMarker;

    private Handler mNaviDirectlyCountHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ogmaps);

//        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        IndoorInfoManager.create().getBuildings(this, new NetworkManager.NetworkManagerListener<OGBuilding[]>() {
//            @Override
//            public void onSucceed(OGBuilding[] ogBuildings) {
//                DataCacheManager.getInstance().setAllBuildings(OGMapsActivity.this, ogBuildings);
//
//                getBuildingList(ogBuildings);
//            }
//
//            @Override
//            public void onFail(VolleyError volleyError, boolean b) {
//                DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "API Error", "get buildings error");
//            }
//        });

        mFloorsLayout = (LinearLayout) findViewById(R.id.map_content_view_ll_floors);
        mFloorSelectorFAB = (FloatingActionButton) findViewById(R.id.map_content_view_fab_floor_selector);
        mCurrentPositionFAB = (FloatingActionButton) findViewById(R.id.map_fragment_view_fab_current_position);
        mPOIInfoLayout = (LinearLayout) findViewById(R.id.map_content_view_poi_info);
        mPOIInfoTitleCL = (CoordinatorLayout) findViewById(R.id.poi_info_view_cl);
        mPOIInfoTitleTV = (TextView) findViewById(R.id.poi_info_view_tv_poi_title);
        mPOIInfoIconNIV = (NetworkImageView) findViewById(R.id.poi_info_view_niv_poi_icon);

        mLeaveNaviBtn = (Button) findViewById(R.id.map_content_view_btn_leave_navi);
        mLeaveNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveNavigation();
            }
        });
        mNaviInfoTV = (TextView) findViewById(R.id.map_content_view_tv_navi_info);

        mFloorSelectorFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
                if (omniFloor != null) {
                    String buildingId = DataCacheManager.getInstance().getBuildingIdByFloorPlanId(omniFloor.getFloorPlanId());
                    DataCacheManager.getInstance().getBuildingFloors(OGMapsActivity.this,
                            buildingId,
                            new DataCacheManager.GetBuildingFloorsListener() {
                                @Override
                                public void onFinished(OGFloors floors) {
                                    if (floors != null && floors.getData().length != 0) {
                                        mFloorsLayout.setVisibility(mFloorsLayout.isShown() ? View.GONE : View.VISIBLE);
                                    }
                                }
                            });
                }
            }
        });

        mCurrentPositionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (OptionsActivity.isTestVersion()) {
//                    if (mMap != null) {
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tiapeiGovLocation, 20.0f));
//
//                        DataCacheManager.getInstance().getBuildingFloors(OGMapsActivity.this,
//                                new DataCacheManager.GetBuildingFloorsListener() {
//                                    @Override
//                                    public void onFinished(OGFloors floors) {
//                                        OGFloor defaultFloor = null;
//                                        for (OGFloor floor : floors.getData()) {
//                                            if (floor.getIsMap()) {
//                                                defaultFloor = floor;
//                                                break;
//                                            }
//                                        }
//                                        if (defaultFloor != null) {
////Log.e("@W@", "fetchFloorPlan #1");
//                                            fetchFloorPlan(defaultFloor.getFloorPlanId(), true, defaultFloor.getNumber());
//
//                                            collapseBottomSheet();
//                                        }
//                                    }
//                                });
//                    }
//                } else {
                if (mMap != null && mUserMarker != null && mLastLocation != null) {

                    LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                        LatLng current = new LatLng(mUserMarker.getPosition().latitude, mUserMarker.getPosition().longitude);
                    addUserLocationMarker(current, mLastLocation);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM_LEVEL));

                    Log.e("OGMapsActivity", "User current floor id : " + DataCacheManager.getInstance().getUserCurrentFloorPlanId());

                    fetchFloorPlan(DataCacheManager.getInstance().getUserCurrentFloorPlanId(), false, DataCacheManager.getInstance().getUserCurrentFloorLevel());
                }
//                }
            }
        });

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

//        FrameLayout fl = (FrameLayout) mPOIInfoLayout.getParent();
        mBottomSheetBehavior = BottomSheetBehavior.from((FrameLayout) mPOIInfoLayout.getParent());
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                /**
                 * When slideOffset < 0, collapseBottomSheet to let BottomSheet view back start position.
                 * The method collapseBottomSheet will set peekHeight = 0, it will trigger BottomSheet view slide (ex: from -0.00235 to 0),
                 * so the boolean mFABShouldSlide to check the slide event should move FAB's Y or not.
                 */

                if (mFABShouldSlide) {

                    if (slideOffset < 0) {
                        mFABShouldSlide = false;

                        collapseBottomSheet();
                    } else {

                        float fabOffset = -(mPOIInfoTitleCL.getMeasuredHeight() +
                                (mPOIInfoLayout.getMeasuredHeight() - mPOIInfoTitleCL.getMeasuredHeight()) * slideOffset);
                        mCurrentPositionFAB.setTranslationY(fabOffset > -mPOIInfoTitleCL.getMeasuredHeight() ? 0 : fabOffset);
                    }
                } else {
                    if (slideOffset == 0 && mBottomSheetBehavior.getPeekHeight() == 0) {
                        mFABShouldSlide = true;
                    }
                }

            }
        });

        FloatingActionButton routeStartFAB = (FloatingActionButton) mPOIInfoLayout.findViewById(R.id.poi_info_view_fab);
        routeStartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNavigationMode == NavigationMode.USER_IN_NAVIGATION) {
                    leaveNavigation();
                }

                if (mStartPOI == null) {
                    if (mEndPOI == null) {
                        mEndPOI = mCurrentSelectedPOI;
                    }

                    if (mLastLocation != null) {
                        if (mOriginalPOIMarker != null) {
                            if (mIsIndoor) {
                                getUserIndoorLocationToOutdoorPRoute(mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude(),
                                        mOriginalPOIMarker.getPosition().latitude,
                                        mOriginalPOIMarker.getPosition().longitude,
                                        DataCacheManager.getInstance().getUserCurrentBuildingId(),
                                        DataCacheManager.getInstance().getUserCurrentFloorLevel());
                            } else {

                            }
                        } else {
                            mNaviInfoTV.setText("目的地 : " + mEndPOI.getName());
                            getLocationToPRoute(mEndPOI.getId(),
                                    mLastLocation.getLatitude(),
                                    mLastLocation.getLongitude(),
                                    DataCacheManager.getInstance().getUserCurrentFloorLevel(),
                                    false);
                        }
                    } else {
                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, R.string.error_dialog_title_text_normal, "沒設定起點");
                    }
                } else {
                    if (mEndPOI != null) {
                        mNaviInfoTV.setText("目的地 : " + mEndPOI.getName());
                        getPToPRoute();

                    } else {
                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, R.string.error_dialog_title_text_normal, "沒設定終點");
                    }
                }
            }
        });

        showNaviDirectlyProgress();
    }

    private void showNaviDirectlyProgress() {
        /** If navigation directly, show progress and count 30 sec */
        if (getIntent().getExtras() != null) {
            DialogTools.getInstance().showProgress(OGMapsActivity.this);

            mNaviDirectlyCountHandler = new Handler();
            if (mRunnable == null) {
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        DialogTools.getInstance().dismissProgress(OGMapsActivity.this);
                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "提示", "發生未預期錯誤，請檢查網路設定");
                    }
                };
            }
            mNaviDirectlyCountHandler.postDelayed(mRunnable, 30 * 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOGService != null) {
            mOGService.unRegisterLocationService();
        }
        DialogTools.getInstance().dismissProgress(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOGService != null) {
            mOGService.unRegisterLocationService();
            mOGService.destroy();
        }
    }

    private void registerService() {
        if (mOGService == null) {
            mOGService = new OGService(this);
        }
        mOGService.registerLocationService(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Trigger clusterManager to cluster POIs that I added on map.
                if (mClusterManager != null) {
                    mClusterManager.cluster();
                }
            }
        });

        if (mLastLocation != null) {
            LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            addUserLocationMarker(current, mLastLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, OGService.MAP_ZOOM_LEVEL));
        }

        setupClusterManager();

        final Bundle args = getIntent().getExtras();
        if (args != null && OptionsActivity.isTestVersion()) {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tiapeiGovLocation, 20.0f));

            DataCacheManager.getInstance().getBuildingFloors(OGMapsActivity.this,
                    new DataCacheManager.GetBuildingFloorsListener() {
                        @Override
                        public void onFinished(OGFloors floors) {
                            OGFloor defaultFloor = null;
                            for (OGFloor floor : floors.getData()) {
                                if (floor.getIsMap()) {
                                    defaultFloor = floor;
                                    break;
                                }
                            }
                            if (defaultFloor != null) {

//                                Log.e("@W@", "fetchFloorPlan #2");
                                fetchFloorPlan(defaultFloor.getFloorPlanId(), true, defaultFloor.getNumber());
//                                fetchFloorPlan(args.getString(ARG_GO_TO_FLOOR_PLAN_ID), true, args.getString(ARG_GO_TO_FLOOR_LEVEL));

                                collapseBottomSheet();
                            }

                        }
                    });
        }
    }

    /**
     * OnInfoWindowCloseListener callback
     */
    @Override
    public void onInfoWindowClose(Marker marker) {
        if (marker.getTag() == null) {
            return;
        }

        marker.setIcon(BitmapDescriptorFactory.fromResource(((OGPOI) marker.getTag()).getPOIIconRes(false)));

        collapseBottomSheet();
    }

    private void setupClusterManager() {
        if (mClusterManager == null) {
            mClusterManager = new ClusterManager<OmniClusterItem>(OGMapsActivity.this, mMap);
        }
        if (mOmniClusterRender == null) {
            mOmniClusterRender = new OmniClusterRender(OGMapsActivity.this, mMap, mClusterManager);
        }
        mClusterManager.setRenderer(mOmniClusterRender);
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<OmniClusterItem>() {
            @Override
            public boolean onClusterItemClick(OmniClusterItem omniClusterItem) {
                Marker marker = mOmniClusterRender.getMarker(omniClusterItem);
                if (marker.getTag() == null) {
                    marker.setTag(omniClusterItem.getPOI());
                }

                if (TextUtils.isEmpty(marker.getTitle())) {
                    return true;
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(omniClusterItem.getPOI().getPOIIconRes(true)));
                    showPOIInfo(marker);
                    return false;
                }
            }
        });
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<OmniClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<OmniClusterItem> cluster) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(),
                        (float) Math.floor(mMap.getCameraPosition().zoom + 1)),
                        300,
                        null);
                return true;
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void showPOIInfo(final Marker marker) {
//        Log.e("@W@", "marker getTitle : " + marker.getTitle());
        if (!TextUtils.isEmpty(marker.getTitle())) {
            final OGPOI poi = (OGPOI) marker.getTag();

            mCurrentSelectedPOI = poi;

            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ||
                    mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING) {
                collapseBottomSheet();
            }
            mBottomSheetBehavior.setPeekHeight(mPOIInfoTitleCL.getHeight());
            mPOIInfoTitleCL.requestLayout();
            mPOIInfoTitleTV.setText(poi.getName());

            mCurrentPositionFAB.setTranslationY(-mPOIInfoTitleCL.getMeasuredHeight());
        }
    }

    private void collapseBottomSheet() {
        mBottomSheetBehavior.setPeekHeight(0);
        mCurrentPositionFAB.setTranslationY(0);

        if (mOriginalPOIMarker != null) {
            mOriginalPOIMarker = null;
        }
    }

    boolean isUserMarkerReady = false;

    private void addUserLocationMarker(LatLng position, Location location) {
        if (mMap == null) {
            return;
        }

        if (mUserMarker == null) {
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .flat(true)
                    .rotation(location.getBearing())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location))
                    .anchor(0.5f, 0.5f)
                    .position(position)
                    .zIndex(OGService.MARKER_Z_INDEX));

            mUserAccuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(position)
                    .radius(location.getAccuracy() / 2)
                    .strokeWidth(10)
                    .zIndex(OGService.MARKER_Z_INDEX));
        } else {
            mUserMarker.setPosition(position);
            mUserMarker.setRotation(location.getBearing());

            mUserAccuracyCircle.setCenter(position);
            mUserAccuracyCircle.setRadius(location.getAccuracy() / 2);
        }

        if (mNavigationMode == NavigationMode.USER_IN_NAVIGATION) {
            List<LatLng> pointList = DataCacheManager.getInstance().getUserCurrentFloorRoutePointList();
            if (pointList != null) {
                showNavigationLocation(pointList);
            }
        }

        if (mUserLocationCertainty > 0.6) {
            isUserMarkerReady = true;
        }
        Bundle args = getIntent().getExtras();
        if (args != null && !OptionsActivity.isTestVersion() && isFirstNavi && mIsInTaipeiGov) {
            if (args.containsKey(ARG_DESTINATION_NAME) && args.containsKey(ARG_GO_TO_FLOOR_LEVEL)) {
                naviDirectly();
            } else if (args.containsKey(ARG_EMERGENCY_NAVIGATION_USER_CURRENT_FLOOR_LEVEL) && args.containsKey(ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE)) {
                naviDirectly();
            }
        }
    }

    float mUserLocationCertainty = -1;
    boolean shouldCheckUserInTaipeiCityHall = true;

    @Override
    public void onLocationChanged(Location location, boolean isIndoor, float certainty) {
//        if (isIndoor) {
        mUserLocationCertainty = certainty;
        mLastLocation = location;
//        }

        Log.e("@W@", "location lat : " + location.getLatitude() + ", isIndoor : " + isIndoor + ", certainty : " + certainty);

        mIsIndoor = isIndoor;

        if (mMap == null) {
            mMapFragment.getMapAsync(this);
        } else {
            LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            /**
             *  If navigation directly, check user is in Taipei city hall or not.
             *  Not in Taipei city hall, dismiss progress dialog and show message.
             *  In Taipei city hall, waiting for fetch tile and
             * */
            if (getIntent().getExtras() != null && shouldCheckUserInTaipeiCityHall) {
                shouldCheckUserInTaipeiCityHall = false;
                DataCacheManager.getInstance().isUserInTaipeiCityHall(OGMapsActivity.this, current, new DataCacheManager.CheckUserInTaipeiCityHallCallback() {
                    @Override
                    public void onFinished(boolean isInTaipeiCityHall) {
                        if (!isInTaipeiCityHall) {
                            mNaviDirectlyCountHandler.removeCallbacks(mRunnable);

                            DialogTools.getInstance().dismissProgress(OGMapsActivity.this);
                            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "提示", "您不在台北市政府內");
                        }
                    }
                });
            }

            addUserLocationMarker(current, mLastLocation);
        }
    }

    @Override
    public void onEnterVenue(String venueId) {
        Log.e("OGMapsActivity", "Enter venue id : " + venueId);
    }

    @Override
    public void onEnterFloor(final String floorId) {
        DataCacheManager.getInstance().getBuildingFloor(OGMapsActivity.this,
                DataCacheManager.TAIPEI_CITY_GOV_BUILDING_ID,
                floorId,
                new DataCacheManager.GetBuildingFloorListener() {
                    @Override
                    public void onFinished(OGFloor floor) {
                        if (floor != null) {
                            Log.e("OGMapsActivity", "Enter floor id : " + floorId);
//                            Log.e("@W@", "fetchFloorPlan #3");
                            mIsInTaipeiGov = true;
                            fetchFloorPlan(floorId, true, floor.getNumber());
                        } else {
                            mIsInTaipeiGov = false;
                            Log.e("OGMapsActivity", "Enter floor id : " + floorId + ", but it's not Taipei gov.");
                        }
                    }
                });
    }

    boolean isFirstNavi = true;
    boolean isIndoorMapReady = false;

    private void fetchFloorPlan(final String id, final boolean isEnterRegion, final String floorLevel) {
        if (!NetworkManager.getInstance().isNetworkAvailable(this)) {
            DialogTools.getInstance().dismissProgress(this);
            DialogTools.getInstance().showNoNetworkMessage(this);
            return;
        }
        if (TextUtils.isEmpty(id)) {
            DialogTools.getInstance().showErrorMessage(this, "Loading building map error", "There's no floor plan id !");
            return;
        }

        if (id.equals(DataCacheManager.USER_OUTDOOR)) {
            return;
        }

//        OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
//        if (omniFloor == null ||
//                (omniFloor != null && omniFloor.getFloorPlanId() != null && !omniFloor.getFloorPlanId().equals(id))) {

        DataCacheManager.getInstance().setCurrentShowFloor(new OmniFloor(String.valueOf(floorLevel), id));

        if (isEnterRegion) {
//            Log.e("@W@", "setUserCurrentFloorLevel : " + floorLevel + ", id : " + id);
            DataCacheManager.getInstance().setUserCurrentFloorLevel(String.valueOf(floorLevel));
            DataCacheManager.getInstance().setUserCurrentFloorPlanId(id);

            DataCacheManager.getInstance().setUserCurrentFloor(new OmniFloor(floorLevel + "", id));
        }

        if (mMap != null) {
            TileProvider tileProvider = new UrlTileProvider(TILE_WIDTH, TILE_HEIGHT) {
                @Override
                public URL getTileUrl(int x, int y, int zoom) {
                    String s = String.format(NetworkManager.DOMAIN_NAME + "map/tile/%s/%d/%d/%d.png",
                            id, zoom, x, y);

                    if (!checkTileExists(x, y, zoom)) {
                        return null;
                    }

                    try {
                        return new URL(s);
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                }
            };

            String buildingId = DataCacheManager.getInstance().getBuildingIdByFloorPlanId(id);
            DataCacheManager.getInstance().getBuildingFloors(OGMapsActivity.this,
                    buildingId,
                    new DataCacheManager.GetBuildingFloorsListener() {
                        @Override
                        public void onFinished(OGFloors floors) {
                            if (floors != null) {
                                for (OGFloor floor : floors.getData()) {
                                    if (floor.getFloorPlanId().equals(id)) {
                                        mFloorSelectorFAB.setVisibility(View.VISIBLE);
//                                            mFloorSelectorFAB.setText(String.valueOf(floor.getName()));
                                    }
                                }
                            }
                        }
                    });

            if (mTileOverlayMap != null) {
                // when floor changed and in the same building, remove tile overlay
                TileOverlay previousTile = mTileOverlayMap.get(buildingId);
                if (previousTile != null) {
                    previousTile.remove();
                }
            } else {
                mTileOverlayMap = new HashMap<>();
            }

            // add current floor tile overlay
            TileOverlay tile = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
            mTileOverlayMap.put(buildingId, tile);

            addFloorLevels(buildingId);
            addPOIMarkers(buildingId, id);
            // show the floor's route poly line
            if (mNavigationMode != NavigationMode.NOT_NAVIGATION) {
                showPolylineByFloorNumber(String.valueOf(floorLevel));
            }

            String userCurrentFloorPlanId = DataCacheManager.getInstance().getUserCurrentFloorPlanId();
            if (mUserMarker != null) {
                mUserMarker.setVisible(userCurrentFloorPlanId.equals(id));
            }
            if (mUserAccuracyCircle != null) {
                mUserAccuracyCircle.setVisible(userCurrentFloorPlanId.equals(id));
            }
            List<LatLng> userCurrentFloorRoutePointList = DataCacheManager.getInstance().getUserCurrentFloorRoutePointList();
            if (mNavigationMarker != null && userCurrentFloorRoutePointList != null && !userCurrentFloorRoutePointList.isEmpty()) {
                mNavigationMarker.setVisible(DataCacheManager.getInstance().getUserCurrentFloorLevel().equals(String.valueOf(floorLevel)));
            }

            isIndoorMapReady = true;

            Bundle args = getIntent().getExtras();
            if (args != null) {
                if (OptionsActivity.isTestVersion()) {
                    mNaviInfoTV.setText("目的地 : " + args.getString(ARG_DESTINATION_NAME) + ", 目的地樓層 : " + args.get(ARG_GO_TO_FLOOR_LEVEL));

                    isFirstNavi = false;

                    getLocationToPRoute(args.getString(ARG_DESTINATION_ID),
                            taipeiGovLat,
                            taipeiGovLng,
                            args.getString(ARG_USER_CURRENT_FLOOR_LEVEL),
                            false);

                } else {
                    if (isFirstNavi && mIsInTaipeiGov) {
                        if (args.containsKey(ARG_DESTINATION_NAME) && args.containsKey(ARG_GO_TO_FLOOR_LEVEL)) {
                            naviDirectly();
                        } else if (args.containsKey(ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE) && args.containsKey(ARG_EMERGENCY_NAVIGATION_USER_CURRENT_FLOOR_LEVEL)) {
                            naviDirectly();
                        }
                    }
                }
            }
        }
//        }
    }

    private void naviDirectly() {
        Log.e("@W@", "~~~ naviDirectly ~~~");
        Bundle args = getIntent().getExtras();
        if (args != null && isUserMarkerReady && isIndoorMapReady && isFirstNavi && mIsInTaipeiGov) {

            if (args.containsKey(ARG_DESTINATION_NAME) && args.containsKey(ARG_GO_TO_FLOOR_LEVEL)) {
                DialogTools.getInstance().showProgress(OGMapsActivity.this);

                mNaviInfoTV.setText("目的地 : " + args.getString(ARG_DESTINATION_NAME) + ", 目的地樓層 : " + args.get(ARG_GO_TO_FLOOR_LEVEL));

                OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
                if (omniFloor != null) {
                    if (mUserMarker != null) {
                        isFirstNavi = false;

                        getLocationToPRoute(args.getString(ARG_DESTINATION_ID),
                                mUserMarker.getPosition().latitude,
                                mUserMarker.getPosition().longitude,
                                omniFloor.getFloorLevel(),
                                false);
                    }
                }
            } else if (args.containsKey(ARG_EMERGENCY_NAVIGATION_USER_CURRENT_FLOOR_LEVEL) && args.containsKey(ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE)) {
                DialogTools.getInstance().showProgress(OGMapsActivity.this);

                OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
                if (omniFloor != null) {
                    if (mUserMarker != null) {
                        isFirstNavi = false;

                        getEmergencyRoute(args.getString(ARG_EMERGENCY_NAVIGATION_EMERGENCY_TYPE));
                    }
                }
            }
        }
    }

    private boolean checkTileExists(int x, int y, int zoom) {
        if ((zoom < MAP_MIN_ZOOM_LEVEL || zoom > MAP_MAX_ZOOM_LEVEL)) {
            return false;
        }

        return true;
    }

    private void addFloorLevels(final String buildingId) {

        DataCacheManager.getInstance().getBuildingFloors(OGMapsActivity.this,
                buildingId,
                new DataCacheManager.GetBuildingFloorsListener() {
                    @Override
                    public void onFinished(OGFloors floors) {
                        mFloorsLayout.removeAllViews();

                        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
                        int marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
                        params.setMargins(0, 0, 0, marginBottom);
                        if (floors != null) {
                            for (final OGFloor f : floors.getData()) {
                                TextView textView = new TextView(OGMapsActivity.this);
                                textView.setLines(1);
                                textView.setText(f.getName());
                                textView.setBackgroundResource(R.drawable.round_background);
                                textView.setGravity(Gravity.CENTER);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!TextUtils.isEmpty(f.getFloorPlanId())) {

                                            fetchFloorPlan(f.getFloorPlanId(), false, f.getNumber());
                                        } else {
                                            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, R.string.error_dialog_title_text_normal,
                                                    "There's no building " + buildingId + "'s floor " + f.getName() + " map.");
                                        }
                                    }
                                });

                                mFloorsLayout.addView(textView, params);
                            }
                        }
                    }
                });
    }

    private void addPOIMarkers(final String buildingId, String floorPlanId) {
        DataCacheManager.getInstance().getBuildingFloor(OGMapsActivity.this,
                buildingId,
                floorPlanId,
                new DataCacheManager.GetBuildingFloorListener() {
                    @Override
                    public void onFinished(OGFloor floor) {
                        if (floor == null) {

                        } else {
//                            LatLng current = new LatLng(floor.getLatitude(), floor.getLongitude());
//
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM_LEVEL));

                            removePreviousMarkers(buildingId);

                            OmniClusterItem item;
                            List<OmniClusterItem> itemList = DataCacheManager.getInstance().getClusterListByBuildingId(buildingId);
                            if (itemList == null) {
                                itemList = new ArrayList<>();
                            }

                            // add floor markers
                            for (OGPOI poi : floor.getPois()) {

                                item = new OmniClusterItem(poi);
                                itemList.add(item);
                                DataCacheManager.getInstance().setPOIClusterItemMap(poi.getId(), item);
                            }
                            DataCacheManager.getInstance().setBuildingClusterItems(buildingId, itemList);

                            mClusterManager.addItems(itemList);
                            mClusterManager.cluster();
                        }
                    }
                });
    }

    /**
     * Removes same building's other floor markers.
     *
     * @param buildingId The id of building that will show in screen or change floor.
     */
    private void removePreviousMarkers(String buildingId) {

        if (mClusterManager != null) {
            List<OmniClusterItem> previousClusterItemList = DataCacheManager.getInstance().getClusterListByBuildingId(buildingId);
            if (previousClusterItemList != null) {
                mClusterManager.clearItems();
                previousClusterItemList.clear();
            }
            mClusterManager.cluster();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("位置服務尚未開啟，請設定");
            dialog.setPositiveButton("open settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
                            getString(R.string.error_dialog_title_text_normal),
                            "沒有開啟位置服務，無法顯示正確位置");
                }
            });
            dialog.show();
        }
    }

    private void ensurePermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSIONS);

        } else {
            registerService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0) {

                boolean shouldRegetPermission = false;

                for (int result : grantResults) {
                    if (result != 0) {
                        shouldRegetPermission = true;
                        break;
                    }
                }

                if (shouldRegetPermission) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                                    Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSIONS);
                } else {
                    registerService();
                }

            }
        }
    }

    /**
     * Show the floor's route poly line.
     *
     * @param floorNumber The floor that will change or show.
     */
    private void showPolylineByFloorNumber(String floorNumber) {
        Map<String, Polyline> map = DataCacheManager.getInstance().getFloorPolylineMap();
        for (String key : map.keySet()) {
            map.get(key).setVisible(key.equals(floorNumber));
        }

        showArrowMarkersByFloorNumber(floorNumber);
    }

    /**
     * Show the floor's arrow markers on route poly line.
     *
     * @param floorNumber The floor that will change or show.
     */
    private void showArrowMarkersByFloorNumber(String floorNumber) {
        Map<String, List<Marker>> map = DataCacheManager.getInstance().getFloorArrowMarkersMap();
        for (String key : map.keySet()) {
            List<Marker> list = map.get(key);
            for (Marker marker : list) {
                marker.setVisible(key.equals(floorNumber));
            }
        }

        showNavigationMarkerByFloorNumber(floorNumber);
    }

    /**
     * Show the floor's navigation marker on route poly line.
     *
     * @param floorNumber The floor that will change or show.
     */
    private void showNavigationMarkerByFloorNumber(String floorNumber) {
        if (mNavigationMarker != null) {
            mNavigationMarker.setVisible(DataCacheManager.getInstance().getCurrentShowFloor().getFloorLevel().equals(floorNumber));
        }
    }

    private void getLocationToPRoute(String endPOIId, double startLat, double startLng, String userCurrentLevel, boolean isReNavi) {
        IndoorInfoManager.create().getOutdoorToIndoorPRoute(OGMapsActivity.this,
                endPOIId,
                startLat,
                startLng,
                userCurrentLevel,
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute route) {
                        if (route.getResult().equals("true")) {
                            if (route.getNavigationalRoutePOIs().size() != 0) {
                                startNavigation(route, false);
                            } else {
                                DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route POIs array size = false");
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route result = false");
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
//                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
//                                R.string.error_dialog_title_text_normal,
//                                getString(R.string.error_dialog_message_text_api));
                    }
                });
    }

    private void getPToPRoute() {
        IndoorInfoManager.create().getIndoorPToPRoute(OGMapsActivity.this, mStartPOI.getId(), mEndPOI.getId(),
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute route) {
                        if (route.getResult().equals("true")) {
                            if (route.getNavigationalRoutePOIs().size() != 0) {
                                startNavigation(route, false);
                            } else {
                                DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route POIs array size = false");
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route result = false");
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
//                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
//                                R.string.error_dialog_title_text_normal,
//                                getString(R.string.error_dialog_message_text_api));
                    }
                });
    }

    private void getUserIndoorLocationToOutdoorPRoute(double userLat, double userLng, double poiLat, double poiLng,
                                                      String userCurrentBuildingId, String userCurrentFloorLevel) {
//        Log.e("@W@", "userCurrentFloorLevel : " + userCurrentFloorLevel);
        IndoorInfoManager.create().getUserIndoorLocationToOutdoorPRoute(OGMapsActivity.this, userLat, userLng, poiLat, poiLng,
                userCurrentBuildingId,
                userCurrentFloorLevel,
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute response) {
                        if (response.getResult().equals("true")) {

                            startNavigation(response, false);
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
//                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
//                                R.string.error_dialog_title_text_normal,
//                                getString(R.string.error_dialog_message_text_api));
                    }
                });
    }

    private void getEmergencyRoute(String type) {

        IndoorInfoManager.create().getEmergencyRoute(OGMapsActivity.this,
                DataCacheManager.TAIPEI_CITY_GOV_BUILDING_ID,
                mUserMarker.getPosition().latitude,
                mUserMarker.getPosition().longitude,
                DataCacheManager.getInstance().getUserCurrentFloorLevel(),
                type,
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute ogNaviRoute) {
                        if (ogNaviRoute.getResult().equals("true")) {
                            if (ogNaviRoute.getNavigationalRoutePOIs().size() != 0) {
                                startNavigation(ogNaviRoute, false);
                            } else {
                                DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route POIs array size = false");
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this, "Error", "route result = false");
                        }
                    }

                    @Override
                    public void onFail(VolleyError volleyError, boolean b) {
//                        DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
//                                R.string.error_dialog_title_text_normal,
//                                getString(R.string.error_dialog_message_text_api));
                    }
                });

    }

    private void startNavigation(OGNaviRoute route, boolean isReverseRoute) {
        if (route.getResult().equals("true")) {
            /** dismiss progress dialog that show by direct navigation*/
            DialogTools.getInstance().dismissProgress(OGMapsActivity.this);

            DataCacheManager.getInstance().clearAllPolyline();
            DataCacheManager.getInstance().clearAllArrowMarkers();

//            List<LatLng> pointList = new ArrayList<>();
            List<LatLng> pointList = DataCacheManager.getInstance().getCurrentRoutePointList();
            if (!pointList.isEmpty()) {
                pointList.clear();
            }
            OGNaviRoutePOI previousPOI = null;
            int colorIndex = 0;

            if (isReverseRoute) {
                for (int i = route.getNavigationalRoutePOIs().size() - 1; i == 0; i--) {
                    OGNaviRoutePOI poi = route.getNavigationalRoutePOIs().get(i);
                    LatLng point = new LatLng(Double.valueOf(poi.getLatitude()), Double.valueOf(poi.getLongitude()));

                    if (previousPOI == null || poi.getFloorNumber().equals(previousPOI.getFloorNumber())) {
                        pointList.add(point);
                        if (i == route.getNavigationalRoutePOIs().size() - 1) {
                            drawPolyline(poi.getFloorNumber(), pointList, colorIndex % mPolyLineColors.length);
                        }
                    } else {
                        drawPolyline(previousPOI.getFloorNumber(), pointList, colorIndex % mPolyLineColors.length);
                        pointList.clear();
                        pointList.add(point);
                        colorIndex++;
                    }
                    previousPOI = poi;
                }
            } else {

                for (int i = 0; i < route.getNavigationalRoutePOIs().size(); i++) {

                    OGNaviRoutePOI poi = route.getNavigationalRoutePOIs().get(i);
                    LatLng point = new LatLng(Double.valueOf(poi.getLatitude()), Double.valueOf(poi.getLongitude()));

                    if (previousPOI == null || poi.getFloorNumber().equals(previousPOI.getFloorNumber())) {
                        pointList.add(point);
                        if (i == route.getNavigationalRoutePOIs().size() - 1) {
                            drawPolyline(poi.getFloorNumber(), pointList, colorIndex % mPolyLineColors.length);
                        }
                    } else {
                        drawPolyline(previousPOI.getFloorNumber(), pointList, colorIndex % mPolyLineColors.length);
                        pointList.clear();
                        pointList.add(point);
                        colorIndex++;
                    }
                    previousPOI = poi;
                }

//                mRouteInfoLL.setVisibility(View.VISIBLE);
//                mSearchTV.setVisibility(View.GONE);
            }

            DataCacheManager.getInstance().setCurrentRoutePointList(pointList);

            setNavigationMode(NavigationMode.USER_IN_NAVIGATION);
            List<LatLng> userFloorPointList = DataCacheManager.getInstance().getUserCurrentFloorRoutePointList();
            if (userFloorPointList != null) {
                showNavigationLocation(userFloorPointList);
            }

        } else {
            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
                    R.string.error_dialog_title_text_normal,
                    route.getErrorMessage());
        }
    }

    private void leaveNavigation() {
        if (mNavigationMode == NavigationMode.USER_IN_NAVIGATION) {

            mStartPOI = null;
            mEndPOI = null;

            collapseBottomSheet();

            setNavigationMode(NavigationMode.NOT_NAVIGATION);

            if (mNavigationMarker != null) {
                mNavigationMarker.setVisible(false);
            }

            DataCacheManager.getInstance().clearAllPolyline();
            DataCacheManager.getInstance().clearAllArrowMarkers();

            Map<String, List<LatLng>> routePointsMap = DataCacheManager.getInstance().getFloorRoutePointsMap();
            if (routePointsMap != null) {
                routePointsMap.clear();
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mUserMarker.getPosition())
                    .zoom(MAP_ZOOM_LEVEL)
                    .bearing(mUserMarker.getRotation())
                    .tilt(0)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void drawPolyline(String floorNumber, List<LatLng> pointList, int colorIndex) {
        PolylineOptions lineOptions = null;

        if (!pointList.isEmpty()) {
            lineOptions = new PolylineOptions()
                    .addAll(pointList)
                    .width(POLYLINE_WIDTH)
                    .color(mPolyLineColors[colorIndex]);
        }
        if (lineOptions != null) {
            Polyline polyline = mMap.addPolyline(lineOptions);
            polyline.setZIndex(POLYLINE_Z_INDEX);
            // only show the floor's route poly line
            polyline.setVisible(DataCacheManager.getInstance().getCurrentShowFloor().getFloorLevel().equals(floorNumber));

            addArrowOnPolyline(pointList, floorNumber);

            DataCacheManager.getInstance().getFloorPolylineMap().put(floorNumber, polyline);
            DataCacheManager.getInstance().setFloorRoutePointsMap(floorNumber, pointList);
        } else {
            DialogTools.getInstance().showErrorMessage(OGMapsActivity.this,
                    R.string.error_dialog_title_text_normal,
                    "There's no POI!");
        }
    }

    private void addArrowOnPolyline(List<LatLng> pointList, String floorNumber) {

        LatLng previousPoint = null;
        int d = 3;
        Marker marker;
        List<Marker> markerList = new ArrayList<>();
        for (LatLng point : pointList) {
            if (previousPoint != null) {
                Double distance = SphericalUtil.computeDistanceBetween(previousPoint, point);
                int total = (int) Math.round(distance / d);
                float heading = (float) SphericalUtil.computeHeading(previousPoint, point);
                for (int i = 0; i < total; i++) {

                    LatLng coordinate = SphericalUtil.computeOffset(previousPoint, i * d, heading);

                    marker = mMap.addMarker(new MarkerOptions()
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromBitmap(getArrowBitmap()))
                            .position(coordinate)
                            .rotation(heading)
                            .zIndex(MARKER_Z_INDEX));
                    marker.setVisible(DataCacheManager.getInstance().getCurrentShowFloor().getFloorLevel().equals(floorNumber));

                    markerList.add(marker);
                }
            }
            previousPoint = point;
        }

        DataCacheManager.getInstance().setFloorArrowMarkersMap(floorNumber, markerList);
    }

    private Bitmap mArrowBitmap = null;

    private Bitmap getArrowBitmap() {
        if (mArrowBitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.mipmap.icon_arrow_black, options);

            int scale = 1;
            if (options.outWidth != 40 || options.outHeight != 40) {

                scale = (int) Math.ceil(options.outHeight / 40.0);
            }

            BitmapFactory.Options optionsResized = new BitmapFactory.Options();
            optionsResized.inSampleSize = scale;
            mArrowBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_arrow_black, optionsResized);
        }

        return mArrowBitmap;
    }

    private void setNavigationMode(int navigationMode) {
        mLeaveNaviBtn.setVisibility(navigationMode == NavigationMode.USER_IN_NAVIGATION ? View.VISIBLE : View.GONE);
        mNaviInfoTV.setVisibility(navigationMode == NavigationMode.USER_IN_NAVIGATION ? View.VISIBLE : View.GONE);
        mNavigationMode = navigationMode;
//        mLeaveNavigationTextView.setVisibility(navigationMode == NavigationMode.NOT_NAVIGATION ? View.GONE : View.VISIBLE);
    }

    private void showNavigationLocation(List<LatLng> pointList) {

        if (pointList != null && pointList.size() > 1) {
            LatLng userPosition = mUserMarker.getPosition();
            LatLng previousPoint = null;
            LatLng closestPoint = null;
            double closestDistance = -1;
            double heading = -1;

            double distanceLine;
            double distanceSegment;

            for (LatLng point : pointList) {

                if (previousPoint != null) {
//                    double distanceToLine = PolyUtil.distanceToLine(userPosition, previousPoint, point);
//                    if (closestDistance == -1 || closestDistance > distanceToLine) {
//                        closestDistance = distanceToLine;
//                        closestStartPoint = previousPoint;
//                        heading = SphericalUtil.computeHeading(previousPoint, point);
//                    }

                    double r_numerator = (userPosition.longitude - previousPoint.longitude) * (point.longitude - previousPoint.longitude) +
                            (userPosition.latitude - previousPoint.latitude) * (point.latitude - previousPoint.latitude);
                    double r_denominator = (point.longitude - previousPoint.longitude) * (point.longitude - previousPoint.longitude) +
                            (point.latitude - previousPoint.latitude) * (point.latitude - previousPoint.latitude);
                    double r = r_numerator / r_denominator;

                    double px = previousPoint.longitude + r * (point.longitude - previousPoint.longitude);
                    double py = previousPoint.latitude + r * (point.latitude - previousPoint.latitude);

                    double s = ((previousPoint.latitude - userPosition.latitude) * (point.longitude - previousPoint.longitude) -
                            (previousPoint.longitude - userPosition.longitude) * (point.latitude - previousPoint.latitude)) / r_denominator;

                    distanceLine = Math.abs(s) * Math.sqrt(r_denominator);

                    double xx = px;
                    double yy = py;

                    if ((r >= 0) && (r <= 1)) {
                        distanceSegment = distanceLine;
                    } else {

                        double dist1 = (userPosition.longitude - previousPoint.longitude) * (userPosition.longitude - previousPoint.longitude) +
                                (userPosition.latitude - previousPoint.latitude) * (userPosition.latitude - previousPoint.latitude);
                        double dist2 = (userPosition.longitude - point.longitude) * (userPosition.longitude - point.longitude) +
                                (userPosition.latitude - point.latitude) * (userPosition.latitude - point.latitude);
                        if (dist1 < dist2) {
                            xx = previousPoint.longitude;
                            yy = previousPoint.latitude;
                            distanceSegment = Math.sqrt(dist1);
                        } else {
                            xx = point.longitude;
                            yy = point.latitude;
                            distanceSegment = Math.sqrt(dist2);
                        }
                    }

                    if (closestDistance == -1 || closestDistance > distanceSegment) {
                        closestDistance = distanceSegment;
                        closestPoint = new LatLng(yy, xx);
                        heading = SphericalUtil.computeHeading(previousPoint, point);
                    }
                }
                previousPoint = point;
            }

//            /**
//             * re-get navigation here
//             * */
//            if (closestDistance > 4) {
//                getLocationToPRoute(mEndPOI.getId(),
//                        mLastLocation.getLatitude(),
//                        mLastLocation.getLongitude(),
//                        DataCacheManager.getInstance().getUserCurrentFloorLevel(),
//                        true);
//                return;
//            }

            LatLng pointOnRoute = SphericalUtil.computeOffset(closestPoint, closestDistance, heading);
            if (mNavigationMarker == null) {
                mNavigationMarker = mMap.addMarker(new MarkerOptions().position(pointOnRoute)
                        .rotation((float) heading)
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_navigation_start)));
                mNavigationMarker.setVisible(DataCacheManager.getInstance().getCurrentShowFloor().getFloorLevel()
                        .equals(DataCacheManager.getInstance().getUserCurrentFloorLevel()));
            } else {
                mNavigationMarker.setPosition(pointOnRoute);
                mNavigationMarker.setRotation((float) heading);
                mNavigationMarker.setVisible(DataCacheManager.getInstance().getCurrentShowFloor().getFloorLevel()
                        .equals(DataCacheManager.getInstance().getUserCurrentFloorLevel()));
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pointOnRoute)
                    .zoom(MAP_ZOOM_LEVEL)
                    .bearing((float) heading)
                    .tilt(20)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

}
