package com.omni.omninavi.omninavi.model;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 24/05/2017.
 */

public class UserCurrentLocation implements Serializable {

    private String lat;
    private String lng;
    private String plan_id;
    private String date;

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getFloorPlanId() {
        return plan_id;
    }

    public void setFloorPlanId(String floorPlanId) {
        this.plan_id = floorPlanId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "UserCurrentLocation : { Date : " + getDate() + ",\nplan_id : " + getFloorPlanId() + ",\nlat : " + getLat() + ",\nlng : " + getLng() + " }";
    }

    public static class Builder {

        private UserCurrentLocation mUserCurrentLocation;

        public Builder() {
            mUserCurrentLocation = new UserCurrentLocation();
        }

        public Builder setLat(String lat) {
            mUserCurrentLocation.setLat(lat);
            return this;
        }

        public Builder setLng(String lng) {
            mUserCurrentLocation.setLng(lng);
            return this;
        }

        public Builder setFloorPlanId(String id) {
            mUserCurrentLocation.setFloorPlanId(id);
            return this;
        }

        public Builder setDate(String date) {
            mUserCurrentLocation.setDate(date);
            return this;
        }

        public UserCurrentLocation build() {
            return mUserCurrentLocation;
        }
    }
}
