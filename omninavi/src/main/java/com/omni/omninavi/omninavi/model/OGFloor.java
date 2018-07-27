package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wiliiamwang on 31/10/2017.
 */

public class OGFloor implements Serializable {

    @SerializedName("id")
    private String id;
    @SerializedName("number")
    private String number;
    @SerializedName("name")
    private String name;
    @SerializedName("desc")
    private String desc;
    @SerializedName("order")
    private String order;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;
    @SerializedName("bl_lat")
    private double blLatitude;
    @SerializedName("bl_lng")
    private double blLongitude;
    @SerializedName("tr_lat")
    private double trLatitude;
    @SerializedName("tr_lng")
    private double trLongitude;
    @SerializedName("pois")
    private OGPOI[] pois;
    @SerializedName("is_map")
    private String isMap;
    @SerializedName("plan_id")
    private String floorPlanId;
    private List<OGPOI> mAllShopsList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getBlLatitude() {
        return blLatitude;
    }

    public void setBlLatitude(double blLatitude) {
        this.blLatitude = blLatitude;
    }

    public double getBlLongitude() {
        return blLongitude;
    }

    public void setBlLongitude(double blLongitude) {
        this.blLongitude = blLongitude;
    }

    public double getTrLatitude() {
        return trLatitude;
    }

    public void setTrLatitude(double trLatitude) {
        this.trLatitude = trLatitude;
    }

    public double getTrLongitude() {
        return trLongitude;
    }

    public void setTrLongitude(double trLongitude) {
        this.trLongitude = trLongitude;
    }

    public OGPOI[] getPois() {
        return pois;
    }

    public void setPois(OGPOI[] pois) {
        this.pois = pois;
    }

    public boolean getIsMap() {
        return isMap.equals("Y");
    }

    public void setIsMap(String isMap) {
        this.isMap = isMap;
    }

    public String getFloorPlanId() {
        return floorPlanId;
    }

    public void setFloorPlanId(String floorPlanId) {
        this.floorPlanId = floorPlanId;
    }

//    public List<OGPOI> getShouldShowPOIList() {
//        if (mAllShopsList == null) {
//            mAllShopsList = new ArrayList<>();
//        }
//        for (OGPOI poi : pois) {
//            int poiType = poi.getPOIType();
//            if ((poiType == OGPOI.TYPE_OTHER ||
//                    poiType == OGPOI.TYPE_OFFICE ||
//                    poiType == OGPOI.TYPE_TOILETS ||
//                    poiType == OGPOI.TYPE_ELEVATOR ||
//                    poiType == OGPOI.TYPE_ENTRANCE ||
//                    poiType == OGPOI.TYPE_STAIR) &&
//                    !mAllShopsList.contains(poi)) {
//                mAllShopsList.add(poi);
//            }
//        }
//        return mAllShopsList;
//    }
}
