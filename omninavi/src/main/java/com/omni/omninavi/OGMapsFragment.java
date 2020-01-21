package com.omni.omninavi;

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
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by wiliiamwang on 05/12/2017.
 */

public class OGMapsFragment extends Fragment implements OnMapReadyCallback,
        OGService.LocationListener,
        GoogleMap.OnInfoWindowCloseListener {

    private static final int REQUEST_CODE_PERMISSIONS = 90;
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int MAP_ZOOM_LEVEL = 20;
    private static final int MARKER_Z_INDEX = 150;
    private static final int MAP_MIN_ZOOM_LEVEL = 16;
    private static final int MAP_MAX_ZOOM_LEVEL = 22;
    private static final float POLYLINE_WIDTH = 50.0f;
    private static final int POLYLINE_Z_INDEX = 100;

    private static final String TAIPEI_CITY_GOV_BUILDING_ID = "2";

    double taipeiGovLat = 25.037512538033;
    double taipeiGovLng = 121.56507643016;
    LatLng tiapeiGovLocation = new LatLng(taipeiGovLat, taipeiGovLng);

    class NavigationMode {
        static final int USER_IN_NAVIGATION = 0;
        static final int WATCH_OTHER_PLACE = 1;
        static final int NOT_NAVIGATION = 2;
    }

    private Context mContext;
    private View mView;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Location mLastLocation;
    private OGService mOGService;
    private Marker mUserMarker;
    private Circle mUserAccuracyCircle;
    private Map<String, TileOverlay> mTileOverlayMap;
    private LinearLayout mFloorsLayout;
    private FloatingActionButton mFloorSelectorFAB;
    private FloatingActionButton mCurrentPositionFAB;
    private ClusterManager<OmniClusterItem> mClusterManager;
    private OmniClusterRender mOmniClusterRender;
    private OGPOI mCurrentSelectedPOI;
    private BottomSheetBehavior mBottomSheetBehavior;
    private LinearLayout mPOIInfoLayout;
    private CoordinatorLayout mPOIInfoTitleCL;
    private TextView mPOIInfoTitleTV;
    private NetworkImageView mPOIInfoIconNIV;
    private boolean mFABShouldSlide = true;

    private boolean mIsIndoor;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.activity_ogmaps, container, false);

            getBuildingList();

            mFloorsLayout = (LinearLayout) mView.findViewById(R.id.map_content_view_ll_floors);

            mFloorSelectorFAB = (FloatingActionButton) mView.findViewById(R.id.map_content_view_fab_floor_selector);
            mFloorSelectorFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
                    OGFloors floors = null;
                    if (omniFloor != null) {
                        String buildingId = DataCacheManager.getInstance().getBuildingIdByFloorPlanId(omniFloor.getFloorPlanId());
                        DataCacheManager.getInstance().getBuildingFloors(getActivity(),
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

            mCurrentPositionFAB = (FloatingActionButton) mView.findViewById(R.id.map_fragment_view_fab_current_position);
            mCurrentPositionFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                if (mMap != null && mLastLocation != null) {
//                    LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                    addUserLocationMarker(current, mLastLocation);
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM_LEVEL));
//
//                    fetchFloorPlan(DataCacheManager.getInstance().getUserCurrentFloorPlanId(), false, "");
//                }

                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tiapeiGovLocation, 20.0f));

                        DataCacheManager.getInstance().getBuildingFloors(getActivity(),
                                TAIPEI_CITY_GOV_BUILDING_ID,
                                new DataCacheManager.GetBuildingFloorsListener() {
                                    @Override
                                    public void onFinished(OGFloors floors) {
                                        OGFloor groundFloor = null;
                                        for (OGFloor floor : floors.getData()) {
                                            if (floor.getNumber().equals("1")) {
                                                groundFloor = floor;
                                                break;
                                            }
                                        }
                                        if (groundFloor != null) {

                                            fetchFloorPlan(groundFloor.getFloorPlanId(), true, "1");

                                            collapseBottomSheet();
                                        }
                                    }
                                });
                    }
                }
            });

            mMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

            mPOIInfoLayout = (LinearLayout) mView.findViewById(R.id.map_content_view_poi_info);
            mPOIInfoTitleCL = (CoordinatorLayout) mPOIInfoLayout.findViewById(R.id.poi_info_view_cl);
            mPOIInfoTitleTV = (TextView) mPOIInfoLayout.findViewById(R.id.poi_info_view_tv_poi_title);
            mPOIInfoIconNIV = (NetworkImageView) mPOIInfoLayout.findViewById(R.id.poi_info_view_niv_poi_icon);
            FrameLayout fl = (FrameLayout) mPOIInfoLayout.getParent();
            mBottomSheetBehavior = BottomSheetBehavior.from(fl);
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
                                    getUserIndoorLocationToOutdoorPRoute(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                            mOriginalPOIMarker.getPosition().latitude, mOriginalPOIMarker.getPosition().longitude);
                                } else {

                                }
                            } else {
                                getLocationToPRoute(mLastLocation.getLatitude(), mLastLocation.getLongitude(), false);
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(mContext,
                                    R.string.error_dialog_title_text_normal, "沒設定起點");
                        }
                    } else {
                        if (mEndPOI != null) {
                            getPToPRoute();

                        } else {
                            DialogTools.getInstance().showErrorMessage(mContext,
                                    R.string.error_dialog_title_text_normal, "沒設定終點");
                        }
                    }
                }
            });
        }

        return mView;
    }

    private void getBuildingList() {

        IndoorInfoManager.create().getFloors(mContext, new NetworkManager.NetworkManagerListener<OGFloors>() {
            @Override
            public void onSucceed(OGFloors ogFloors) {
                DataCacheManager.getInstance().setBuildingFloors(getActivity(), TAIPEI_CITY_GOV_BUILDING_ID, ogFloors);
            }

            @Override
            public void onFail(VolleyError volleyError, boolean b) {
                DialogTools.getInstance().showErrorMessage(mContext,
                        R.string.error_dialog_title_text_normal,
                        getString(R.string.error_dialog_message_text_api));
                Log.e("@W@", "*** Get building floors failed");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOGService != null) {
            mOGService.unRegisterLocationService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOGService != null) {
            mOGService.unRegisterLocationService();
            mOGService.destroy();
        }
    }

    private void registerService() {
        if (mOGService == null) {
            mOGService = new OGService(mContext);
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
            mClusterManager = new ClusterManager<OmniClusterItem>(mContext, mMap);
        }
        if (mOmniClusterRender == null) {
            mOmniClusterRender = new OmniClusterRender(mContext, mMap, mClusterManager);
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
        Log.e("@W@", "marker getTitle : " + marker.getTitle());
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
    }

    @Override
    public void onLocationChanged(Location location, boolean isIndoor, float certainty) {
        mLastLocation = location;

        mIsIndoor = isIndoor;

        if (mMap == null) {
            mMapFragment.getMapAsync(this);
        } else {
            LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            addUserLocationMarker(current, mLastLocation);
        }
    }

    @Override
    public void onEnterVenue(String venueId) {
        Log.e("@W@", "enter venue id : " + venueId);
    }

    @Override
    public void onEnterFloor(String floorId) {
        Log.e("@W@", "enter floor id : " + floorId);
//        List<String> allFloorPlanIdList = DataCacheManager.getInstance().getAllFloorPlanIdList();
//        if (allFloorPlanIdList == null || allFloorPlanIdList.isEmpty()) {
//            getBuildingList();
//        }
        fetchFloorPlan(floorId, true, "");
    }

    private void fetchFloorPlan(final String id, final boolean isEnterRegion, final String floorLevel) {
        if (!NetworkManager.getInstance().isNetworkAvailable(mContext)) {
            DialogTools.getInstance().dismissProgress(mContext);
            DialogTools.getInstance().showNoNetworkMessage(mContext);
            return;
        }
        if (TextUtils.isEmpty(id)) {
            DialogTools.getInstance().showErrorMessage(mContext, "Loading building map error", "There's no floor plan id !");
            return;
        }

        if (id.equals(DataCacheManager.USER_OUTDOOR)) {
            return;
        }

//        OmniFloor omniFloor = DataCacheManager.getInstance().getCurrentShowFloor();
//        if (omniFloor == null ||
//                (omniFloor != null && omniFloor.getFloorPlanId() != null && !omniFloor.getFloorPlanId().equals(correctFloorPlanId))) {

        DataCacheManager.getInstance().setCurrentShowFloor(new OmniFloor(String.valueOf(floorLevel), id));

        if (isEnterRegion) {
            DataCacheManager.getInstance().setUserCurrentFloorLevel(String.valueOf(floorLevel));
            DataCacheManager.getInstance().setUserCurrentFloorPlanId(id);
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
            DataCacheManager.getInstance().getBuildingFloors(getActivity(),
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

        }
    }

    private boolean checkTileExists(int x, int y, int zoom) {
        if ((zoom < MAP_MIN_ZOOM_LEVEL || zoom > MAP_MAX_ZOOM_LEVEL)) {
            return false;
        }

        return true;
    }

    private void addFloorLevels(final String buildingId) {

        DataCacheManager.getInstance().getBuildingFloors(getActivity(),
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
                                TextView textView = new TextView(mContext);
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
                                            DialogTools.getInstance().showErrorMessage(mContext, R.string.error_dialog_title_text_normal,
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
        DataCacheManager.getInstance().getBuildingFloor(getActivity(), buildingId, floorPlanId,
                new DataCacheManager.GetBuildingFloorListener() {
                    @Override
                    public void onFinished(OGFloor floor) {
                        if (floor != null) {
                            LatLng current = new LatLng(floor.getLatitude(), floor.getLongitude());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM_LEVEL));

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

//        EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_POIS_ADDED, ""));
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
//                for (OmniClusterItem item : previousClusterItemList) {
//                    mClusterManager.removeItem(item);
//                }
                mClusterManager.clearItems();
                previousClusterItemList.clear();
            }
            mClusterManager.cluster();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkLocationService() {
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
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
                    DialogTools.getInstance().showErrorMessage(mContext,
                            getString(R.string.error_dialog_title_text_normal),
                            "沒有開啟位置服務，無法顯示正確位置");
                }
            });
            dialog.show();
        }
    }

    private void ensurePermissions() {

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CHANGE_WIFI_STATE,
                            android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
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
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CHANGE_WIFI_STATE,
                                    android.Manifest.permission.ACCESS_WIFI_STATE,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
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

    private void getLocationToPRoute(double startLat, double startLng, boolean isReNavi) {

        // TODO remove this workaround
//        String floorNumber = DataCacheManager.getInstance().getUserCurrentFloorLevel();
//        if (mEndPOI.getId().equals("175") || mEndPOI.getId().equals("176") || mEndPOI.getId().equals("177")) {
//            floorNumber = "1";
//            DataCacheManager.getInstance().setUserCurrentFloorLevel("1");
//            DataCacheManager.getInstance().setCurrentShowFloor(new OmniFloor("1", "612de379-4109-43dc-b816-e4c7e32923cc"));
//        }

        IndoorInfoManager.create().getOutdoorToIndoorPRoute(mContext,
                mEndPOI.getId(),
                startLat,
                startLng,
                DataCacheManager.getInstance().getUserCurrentFloorLevel(),
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute route) {
                        if (route.getResult().equals("true")) {
                            if (route.getNavigationalRoutePOIs().size() != 0) {
                                startNavigation(route, false);
                            } else {
                                DialogTools.getInstance().showErrorMessage(mContext, "Error", "route POIs array size = false");
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(mContext, "Error", "route result = false");
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
                        DialogTools.getInstance().showErrorMessage(mContext,
                                R.string.error_dialog_title_text_normal,
                                getString(R.string.error_dialog_message_text_api));
                        Log.e("@W@", "*** Get user position to p route failed");
                    }
                });
    }

    private void getPToPRoute() {
        IndoorInfoManager.create().getIndoorPToPRoute(mContext, mStartPOI.getId(), mEndPOI.getId(),
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute route) {
                        if (route.getResult().equals("true")) {
                            if (route.getNavigationalRoutePOIs().size() != 0) {
                                startNavigation(route, false);
                            } else {
                                DialogTools.getInstance().showErrorMessage(mContext, "Error", "route POIs array size = false");
                            }
                        } else {
                            DialogTools.getInstance().showErrorMessage(mContext, "Error", "route result = false");
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
                        DialogTools.getInstance().showErrorMessage(mContext,
                                R.string.error_dialog_title_text_normal,
                                getString(R.string.error_dialog_message_text_api));
                        Log.e("@W@", "*** Get p to p route failed");
                    }
                });
    }

    private void getUserIndoorLocationToOutdoorPRoute(double userLat, double userLng, double poiLat, double poiLng) {
        IndoorInfoManager.create().getUserIndoorLocationToOutdoorPRoute(mContext, userLat, userLng, poiLat, poiLng,
                DataCacheManager.getInstance().getUserCurrentBuildingId(),
                DataCacheManager.getInstance().getUserCurrentFloorLevel(),
                new NetworkManager.NetworkManagerListener<OGNaviRoute>() {
                    @Override
                    public void onSucceed(OGNaviRoute response) {
                        if (response.getResult().equals("true")) {

                            startNavigation(response, false);
                        }
                    }

                    @Override
                    public void onFail(VolleyError error, boolean shouldRetry) {
                        DialogTools.getInstance().showErrorMessage(mContext,
                                R.string.error_dialog_title_text_normal,
                                getString(R.string.error_dialog_message_text_api));
                        Log.e("@W@", "*** Get user indoor location to outdoor poi failed");
                    }
                });
    }

    private void startNavigation(OGNaviRoute route, boolean isReverseRoute) {
        if (route.getResult().equals("true")) {
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
                            drawPolyline(poi.getFloorNumber(), pointList, colorIndex);
                        }
                    } else {
                        drawPolyline(previousPOI.getFloorNumber(), pointList, colorIndex);
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
                            drawPolyline(poi.getFloorNumber(), pointList, colorIndex);
                        }
                    } else {
                        drawPolyline(previousPOI.getFloorNumber(), pointList, colorIndex);
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
            DialogTools.getInstance().showErrorMessage(mContext,
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
            DialogTools.getInstance().showErrorMessage(mContext,
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

            /**
             * re-get navigation here
             * */
            if (closestDistance > 4) {
                getLocationToPRoute(mLastLocation.getLatitude(), mLastLocation.getLongitude(), true);
                return;
            }

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
