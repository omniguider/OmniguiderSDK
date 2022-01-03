package com.omni.navisdk.module.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GroupMember implements Serializable {

    public static final String ADMIN = "admin";
    public static final String USER = "user";

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("tel")
    private String tel;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("role")
    private String role;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;
    @SerializedName("update_timestamp")
    private String updateTimestamp;

    @SerializedName("number")
    private String floorNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }


    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }
}
