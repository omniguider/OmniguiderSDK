package com.omni.omninavi.omninavi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAResourceManager;

/**
 * Created by wiliiamwang on 15/11/2017.
 */

public class OGService implements IARegion.Listener,
        IALocationListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int MAP_ZOOM_LEVEL = 20;
    public static final int MARKER_Z_INDEX = 150;

    public interface LocationListener {
//        void onLocationChanged(Location location,
//                               String venueId,
//                               String floorId,
//                               boolean haveToFetch,
//                               boolean isIndoor,
//                               float certainty);
        void onLocationChanged(Location location,
                               boolean isIndoor,
                               float certainty);
        void onEnterVenue(String venueId);
        void onEnterFloor(String floorId);
    }

    private Context mContext;
    private IALocationManager mIALocationManager;
    private IAResourceManager mIAResourceManager;
    private boolean mIsIndoor = false;
    private String mCurrentVenueId;
    private String mCurrentFloorId;
    private String mPreviousFloorId;
    private Location mLocation;
    private LocationListener mOGLocationListener;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public OGService(Context context) {
        mContext = context;
    }

    public void registerLocationService(LocationListener listener) {

        mOGLocationListener = listener;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        initLocationService();
        IALocationRequest request = IALocationRequest.create();
        request.setFastestInterval(1000);
        request.setSmallestDisplacement(0.6f);

        mIALocationManager.removeLocationUpdates(this);
        mIALocationManager.requestLocationUpdates(request, this);
    }

    private void initLocationService() {
        if (mIALocationManager == null) {
            mIALocationManager = IALocationManager.create(mContext);
        } else {
            mIALocationManager.unregisterRegionListener(this);
        }
        mIALocationManager.registerRegionListener(this);
        if (mIAResourceManager == null) {
            mIAResourceManager = IAResourceManager.create(mContext);
        }
    }

    public void unRegisterLocationService() {
        if (mIALocationManager != null) {
            mIALocationManager.removeLocationUpdates(this);
            mIALocationManager.unregisterRegionListener(this);
        }
    }

    public void destroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mIALocationManager != null) {
            mIALocationManager.destroy();
            mIALocationManager = null;
        }
    }

    @Override
    public void onEnterRegion(IARegion iaRegion) {
        if (iaRegion.getType() == IARegion.TYPE_UNKNOWN) {
            mIsIndoor = false;
        } else if (iaRegion.getType() == IARegion.TYPE_VENUE) {
            mIsIndoor = false;

//            mCurrentVenueId = iaRegion.getId();
            mOGLocationListener.onEnterVenue(iaRegion.getId());
        } else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN) {
            mIsIndoor = true;

//            mCurrentFloorId = iaRegion.getId();
            mOGLocationListener.onEnterFloor(iaRegion.getId());
        }
    }

    @Override
    public void onExitRegion(IARegion iaRegion) {
        if (iaRegion.getType() == IARegion.TYPE_UNKNOWN) {
            mIsIndoor = false;
        } else if (iaRegion.getType() == IARegion.TYPE_VENUE) {
            mCurrentVenueId = "";
            mIsIndoor = false;
        } else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN) {
            mCurrentFloorId = "";
            mIsIndoor = false;
        }
    }

    @Override
    public void onLocationChanged(IALocation iaLocation) {
        mLocation = iaLocation.toLocation();

        if (mIsIndoor) {
            boolean haveToFetch = false;
            if (TextUtils.isEmpty(mPreviousFloorId) ||
                    (!TextUtils.isEmpty(mPreviousFloorId)) && !mCurrentFloorId.equals(mPreviousFloorId)) {
                haveToFetch = true;

                mPreviousFloorId = mCurrentFloorId;
            }

            mOGLocationListener.onLocationChanged(mLocation,
                    mIsIndoor,
                    iaLocation.getFloorCertainty());

//            mOGLocationListener.onLocationChanged(mLocation,
//                    mCurrentVenueId,
//                    mCurrentFloorId,
//                    haveToFetch,
//                    mIsIndoor,
//                    iaLocation.getFloorCertainty());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            if (mLocationRequest == null) {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(1000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (!mIsIndoor) {
            mOGLocationListener.onLocationChanged(location,
                    mIsIndoor,
                    1);

//            mOGLocationListener.onLocationChanged(location,
//                    "",
//                    "",
//                    false,
//                    mIsIndoor,
//                    1);
        }
    }
}
