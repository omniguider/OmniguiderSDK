package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleRoute implements Serializable {

    @SerializedName("bounds")
    private GoogleBounds bounds;
    @SerializedName("copyrights")
    private String copyRights;
    @SerializedName("legs")
    private GoogleLeg[] legs;
    @SerializedName("overview_polyline")
    private GooglePolyline overviewPolyline;
    @SerializedName("summary")
    private String summary;
    @SerializedName("warnings")
    private Object[] warnings;
    @SerializedName("waypoint_order")
    private Object[] waypointOrders;

    public GoogleBounds getBounds() {
        return bounds;
    }

    public void setBounds(GoogleBounds bounds) {
        this.bounds = bounds;
    }

    public String getCopyRights() {
        return copyRights;
    }

    public void setCopyRights(String copyRights) {
        this.copyRights = copyRights;
    }

    public GoogleLeg[] getLegs() {
        return legs;
    }

    public void setLegs(GoogleLeg[] legs) {
        this.legs = legs;
    }

    public GooglePolyline getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(GooglePolyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Object[] getWarnings() {
        return warnings;
    }

    public void setWarnings(Object[] warnings) {
        this.warnings = warnings;
    }

    public Object[] getWaypointOrders() {
        return waypointOrders;
    }

    public void setWaypointOrders(Object[] waypointOrders) {
        this.waypointOrders = waypointOrders;
    }
}
