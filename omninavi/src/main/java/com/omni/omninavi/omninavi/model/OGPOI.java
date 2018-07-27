package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;
import com.omni.omninavi.R;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 31/10/2017.
 */

public class OGPOI implements Serializable {

    public static final int TYPE_ELEVATOR = 1;//    Elevator
    public static final int TYPE_STAIR = 2;//            Stair
    public static final int TYPE_RESTROOM = 3;//    Restroom
    public static final int TYPE_RESTROOM_FOR_THE_DISABLED = 4;//    Restroom or the Disabled
    public static final int TYPE_AED = 5;//    AED
    public static final int TYPE_ENTRANCE_OR_EXIT = 6;//    Entrance/Exit
    public static final int TYPE_POST_OFFICE = 7;//    Post Office
    public static final int TYPE_ATM = 8;//    ATM
    public static final int TYPE_CONVENIENCE_STORE = 9;//    Convenience Store
    public static final int TYPE_DIVISION = 10;//    Division
    public static final int TYPE_HYDRANT = 11;//            Hydrant
    public static final int TYPE_RESTAURANT = 12;//    Restaurant
    public static final int TYPE_BANK = 13;//            Bank
    public static final int TYPE_ESCALATOR = 14;//    Escalator
    public static final int TYPE_INFORMATION = 15;//            Information
    public static final int TYPE_DRINKING_FOUNTAINS = 16;//    Drinking fountains
    public static final int TYPE_FEEDING_ROOM = 17;//    Breastfeeding Room
    public static final int TYPE_MEETING_ROOM = 18;//    Meeting Room

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("desc")
    private String desc;
    @SerializedName("type")
    private String type;
    @SerializedName("type_zh")
    private String typeZh;
    @SerializedName("icon")
    private OGPOIIcon[] ogPoiIcons;
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;
    @SerializedName("is_entrance")
    private String isEntrance;
    @SerializedName("is_door")
    private String isDoor;
    @SerializedName("is_office")
    private boolean isOffice;
    @SerializedName("office")
    private OGOffice[] ogOffices;

    private int poiType = -1;
    private boolean isOriginalPOI = false;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPOIType() {
        if (type != null) {
            switch (type) {
                case "Elevator":
                    poiType = TYPE_ELEVATOR;
                    break;

                case "Stair":
                    poiType = TYPE_STAIR;
                    break;

                case "Restroom":
                    poiType = TYPE_RESTROOM;
                    break;

                case "Restroom or the Disabled":
                    poiType = TYPE_RESTROOM_FOR_THE_DISABLED;
                    break;

                case "AED":
                    poiType = TYPE_AED;
                    break;

                case "Entrance/Exit":
                    poiType = TYPE_ENTRANCE_OR_EXIT;
                    break;

                case "Post Office":
                    poiType = TYPE_POST_OFFICE;
                    break;

                case "ATM":
                    poiType = TYPE_ATM;
                    break;

                case "Convenience Store":
                    poiType = TYPE_CONVENIENCE_STORE;
                    break;

                case "Division":
                    poiType = TYPE_DIVISION;
                    break;

                case "Hydrant":
                    poiType = TYPE_HYDRANT;
                    break;

                case "Restaurant":
                    poiType = TYPE_RESTAURANT;
                    break;

                case "Bank":
                    poiType = TYPE_BANK;
                    break;

                case "Escalator":
                    poiType = TYPE_ESCALATOR;
                    break;

                case "Information":
                    poiType = TYPE_INFORMATION;
                    break;

                case "Drinking fountains":
                    poiType = TYPE_DRINKING_FOUNTAINS;
                    break;

                case "Breastfeeding Room":
                    poiType = TYPE_FEEDING_ROOM;
                    break;

                case "Meeting Room":
                    poiType = TYPE_MEETING_ROOM;
                    break;

            }
        }

        return poiType;
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

    public boolean getIsEntrance() {
        return isEntrance.equals("Y");
    }

    public void setIsEntrance(String isEntrance) {
        this.isEntrance = isEntrance;
    }

    public boolean getIsDoor() {
        return isDoor.equals("Y");
    }

    public void setIsDoor(String isDoor) {
        this.isDoor = isDoor;
    }

    public String getTypeZh() {
        return typeZh;
    }

    public void setTypeZh(String typeZh) {
        this.typeZh = typeZh;
    }

    public OGPOIIcon[] getOgPoiIcons() {
        return ogPoiIcons;
    }

    public void setOgPoiIcons(OGPOIIcon[] ogPoiIcons) {
        this.ogPoiIcons = ogPoiIcons;
    }

    public boolean isOffice() {
        return isOffice;
    }

    public void setOffice(boolean office) {
        isOffice = office;
    }

    public OGOffice[] getOgOffices() {
        return ogOffices;
    }

    public void setOgOffices(OGOffice[] ogOffices) {
        this.ogOffices = ogOffices;
    }

    public int getPOIIconRes(boolean isSelected) {
        int resId;
        switch (getPOIType()) {
            case OGPOI.TYPE_ELEVATOR:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_elevator;
                break;

            case OGPOI.TYPE_STAIR:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_stairs;
                break;

            case OGPOI.TYPE_RESTROOM:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_toilet;
                break;

            case OGPOI.TYPE_RESTROOM_FOR_THE_DISABLED:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_restroom_for_the_disable;
                break;

            case OGPOI.TYPE_AED:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_aed;
                break;

            case OGPOI.TYPE_ENTRANCE_OR_EXIT:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_emergency_exit;
                break;

            case OGPOI.TYPE_POST_OFFICE:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_post;
                break;

            case OGPOI.TYPE_ATM:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_atm;
                break;

            case OGPOI.TYPE_CONVENIENCE_STORE:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_ok;
                break;

            case OGPOI.TYPE_DIVISION:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_tpe_gov;
                break;

            case OGPOI.TYPE_HYDRANT:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_hydrant;
                break;

            case OGPOI.TYPE_RESTAURANT:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_dining;
                break;

            case OGPOI.TYPE_BANK:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_fubon;
                break;

            case OGPOI.TYPE_ESCALATOR:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_escalator;
                break;

            case OGPOI.TYPE_INFORMATION:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_information;
                break;

            case OGPOI.TYPE_DRINKING_FOUNTAINS:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_drinking_fountain;
                break;

            case OGPOI.TYPE_FEEDING_ROOM:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_feeding_room;
                break;

            case OGPOI.TYPE_MEETING_ROOM:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_meeting_room;
                break;

            default:
                resId = isSelected ? R.mipmap.icon_select : R.mipmap.icon_select;
                break;
        }
        return resId;
    }

    public boolean isOriginalPOI() {
        return isOriginalPOI;
    }

    public void setIsOriginalPOI(boolean originalPOI) {
        isOriginalPOI = originalPOI;
    }

//    public boolean isUtilityPOI() {
//        return getPOIType() == OGPOI.TYPE_STAIR ||
//                getPOIType() == OGPOI.TYPE_ELEVATOR ||
////                getPOIType() == OGPOI.TYPE_ROOM ||
//                getPOIType() == OGPOI.TYPE_OFFICE ||
//                getPOIType() == OGPOI.TYPE_TOILETS ||
//                getPOIType() == OGPOI.TYPE_DISABLED_TOILETS ||
//                getPOIType() == OGPOI.TYPE_RAMP ||
////                getPOIType() == OGPOI.TYPE_KITCHEN ||
//                getPOIType() == OGPOI.TYPE_MEETING_ROOM ||
//                getPOIType() == OGPOI.TYPE_INFORMATION ||
//                getPOIType() == OGPOI.TYPE_INTRODUCE ||
//                getPOIType() == OGPOI.TYPE_DINING ||
//                getPOIType() == OGPOI.TYPE_OTHER;
//    }
//
//    public boolean isEmergencyPOI() {
//        return getPOIType() == OGPOI.TYPE_AED ||
//                getPOIType() == OGPOI.TYPE_FIRE_EXTINGUISHER ||
//                getPOIType() == OGPOI.TYPE_SECURITY_GUARD ||
//                getPOIType() == OGPOI.TYPE_EMERGENCY_EXIT ||
//                getPOIType() == OGPOI.TYPE_FIRE_HYDRANT ||
//                getPOIType() == OGPOI.TYPE_ESCAPE_SLING ||
//                getPOIType() == OGPOI.TYPE_ENTRANCE;
//    }
}
