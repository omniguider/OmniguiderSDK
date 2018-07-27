package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleWaypoint implements Serializable {

    @SerializedName("geocoder_status")
    private String geoCoderStatus;
    @SerializedName("place_id")
    private String placeId;
    @SerializedName("types")
    private String[] types;

    public String getGeoCoderStatus() {
        return geoCoderStatus;
    }

    public void setGeoCoderStatus(String geoCoderStatus) {
        this.geoCoderStatus = geoCoderStatus;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }
}
