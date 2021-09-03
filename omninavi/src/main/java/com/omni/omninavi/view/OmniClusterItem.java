package com.omni.omninavi.view;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.omni.omninavi.model.OGPOI;

/**
 * Created by wiliiamwang on 04/12/2017.
 */

public class OmniClusterItem implements ClusterItem {

    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private int mIconRes = -1;
    private OGPOI mPOI;

//    public OmniClusterItem(double lat, double lng) {
//        mPosition = new LatLng(lat, lng);
//        mTitle = null;
//        mSnippet = null;
//    }
//
//    public OmniClusterItem(double lat, double lng, String title, String snippet) {
//        mPosition = new LatLng(lat, lng);
//        mTitle = title;
//        mSnippet = snippet;
//    }

    public OmniClusterItem(OGPOI poi) {
        mPOI = poi;
        mPosition = new LatLng(poi.getLatitude(), poi.getLongitude());
        mTitle = poi.getName();
        mSnippet = poi.getDesc();
        mIconRes = poi.getPOIIconRes(false);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OmniClusterItem)) {
            return false;
        }

        return super.equals(obj);
    }

    public String getTitle() { return mTitle; }

    public String getSnippet() { return mSnippet; }

    public int getIconRes() {
        return mIconRes;
    }

    public OGPOI getPOI() {
        return mPOI;
    }

//    class MyAlgorithm extends NonHierarchicalDistanceBasedAlgorithm<OmniClusterItem> {
//        @Override
//        public void removeItem(OmniClusterItem item) {
//            super.removeItem(item);
//        }
//    }

}
