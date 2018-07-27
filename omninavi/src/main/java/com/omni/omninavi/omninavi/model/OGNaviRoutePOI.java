package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by omniguidermac on 2017/1/19.
 */

public class OGNaviRoutePOI {

    @SerializedName("buid")
    private String bUID;
    @SerializedName("floor_number")
    private String floorNumber;
    @SerializedName("lat")
    private String latitude;
    @SerializedName("lon")
    private String longitude;
    @SerializedName("pois_type")
    private String poisType;
    @SerializedName("puid")
    private String pUID;

    public String getBUID() {
        return bUID;
    }

    public void setBUID(String buid) {
        this.bUID = buid;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPoisType() {
        return poisType;
    }

    public void setPoisType(String poisType) {
        this.poisType = poisType;
    }

    public String getpUID() {
        return pUID;
    }

    public void setpUID(String pUID) {
        this.pUID = pUID;
    }
}
