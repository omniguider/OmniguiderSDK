package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleStep implements Serializable {

//        "steps":[
//        {
//            "distance":{
//            "text":"35 m",
//                    "value":35
//        },
//            "duration":{
//            "text":"1 min",
//                    "value":8
//        },
//            "end_location":{
//            "lat":25.0810055,
//                    "lng":121.5650804
//        },
//            "html_instructions":"Head \u003cb\u003enorth\u003c/b\u003e on \u003cb\u003e基湖路\u003c/b\u003e toward \u003cb\u003e瑞光路\u003c/b\u003e\u003cdiv style=\"font-size:0.9em\"\u003eDestination will be on the left\u003c/div\u003e",
//                "polyline":{
//            "points":"kqaxCed~dV[Ga@I"
//        },
//            "start_location":{
//            "lat":25.0807009,
//                    "lng":121.5649898
//        },
//            "travel_mode":"DRIVING"
//        }
//        ],

    @SerializedName("distance")
    private GoogleDistance distance;
    @SerializedName("duration")
    private GoogleDistance duration;
    @SerializedName("end_location")
    private OmniLatLng endLocation;
    @SerializedName("html_instructions")
    private String htmlInstruction;
    @SerializedName("polyline")
    private GooglePolyline polyline;
    @SerializedName("start_location")
    private OmniLatLng startLocation;
    @SerializedName("travel_mode")
    private String travelMode;

    public GoogleDistance getDistance() {
        return distance;
    }

    public void setDistance(GoogleDistance distance) {
        this.distance = distance;
    }

    public GoogleDistance getDuration() {
        return duration;
    }

    public void setDuration(GoogleDistance duration) {
        this.duration = duration;
    }

    public OmniLatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(OmniLatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getHtmlInstruction() {
        return htmlInstruction;
    }

    public void setHtmlInstruction(String htmlInstruction) {
        this.htmlInstruction = htmlInstruction;
    }

    public GooglePolyline getPolyline() {
        return polyline;
    }

    public void setPolyline(GooglePolyline polyline) {
        this.polyline = polyline;
    }

    public OmniLatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(OmniLatLng startLocation) {
        this.startLocation = startLocation;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }
}
