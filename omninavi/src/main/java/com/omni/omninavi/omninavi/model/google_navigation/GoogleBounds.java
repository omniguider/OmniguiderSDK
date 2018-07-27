package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleBounds implements Serializable {

    @SerializedName("northeast")
    private OmniLatLng northeast;
    @SerializedName("southwest")
    private OmniLatLng southwest;

    public OmniLatLng getNortheast() {
        return northeast;
    }

    public void setNortheast(OmniLatLng northeast) {
        this.northeast = northeast;
    }

    public OmniLatLng getSouthwest() {
        return southwest;
    }

    public void setSouthwest(OmniLatLng southwest) {
        this.southwest = southwest;
    }
}
