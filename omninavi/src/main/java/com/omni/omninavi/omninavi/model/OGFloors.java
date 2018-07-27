package com.omni.omninavi.omninavi.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wiliiamwang on 14/11/2017.
 */

public class OGFloors implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("data")
    private OGFloor[] data;
    private List<OGPOI> mAllFloorsShopsList;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OGFloor[] getData() {
        return data;
    }

    public void setData(OGFloor[] data) {
        this.data = data;
    }

//    public List<OGPOI> getAllFloorsPOIs() {
//        if (mAllFloorsShopsList == null) {
//            mAllFloorsShopsList = new ArrayList<>();
//        }
//        mAllFloorsShopsList.clear();
//        for (OGFloor floor : data) {
//            if (!mAllFloorsShopsList.contains(floor.getShouldShowPOIList())) {
//                mAllFloorsShopsList.addAll(floor.getShouldShowPOIList());
//            }
//        }
//        return mAllFloorsShopsList;
//    }
//
//    public List<OGPOI> getPOIsByFloorPlanId(String floorPlanId) {
//        List<OGPOI> list = new ArrayList<>();
//        for (OGFloor floor : data) {
//            if (!TextUtils.isEmpty(floor.getFloorPlanId()) && floor.getFloorPlanId().equals(floorPlanId)) {
//                list = floor.getShouldShowPOIList();
//                break;
//            }
//        }
//        return list;
//    }

    public OGFloor getGroundFloor() {
        OGFloor floor = null;
        for (OGFloor f : data) {
            if (f.getIsMap()) {
                floor = f;
                break;
            }
        }
        return floor;
    }
}
