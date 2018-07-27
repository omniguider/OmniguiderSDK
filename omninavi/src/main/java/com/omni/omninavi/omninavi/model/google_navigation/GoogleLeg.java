package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleLeg implements Serializable {

//    {
//        "distance":{
//        "text":"35 m",
//                "value":35
//    },
//        "duration":{
//        "text":"1 min",
//                "value":8
//    },
//        "end_address":"No. 7-17, Jihu Road, Neihu District, Taipei City, Taiwan 114",
//            "end_location":{
//        "lat":25.0810055,
//                "lng":121.5650804
//    },
//        "start_address":"No. 28, Jihu Road, Neihu District, Taipei City, Taiwan 114",
//            "start_location":{
//        "lat":25.0807009,
//                "lng":121.5649898
//    },


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



//        "traffic_speed_entry":[
//        ],
//        "via_waypoint":[
//        ]
//    }

    @SerializedName("distance")
    private GoogleDistance distance;
    @SerializedName("duration")
    private GoogleDistance duration;
    @SerializedName("end_address")
    private String endAddress;
    @SerializedName("end_location")
    private OmniLatLng endLocation;
    @SerializedName("start_address")
    private String startAddress;
    @SerializedName("start_location")
    private OmniLatLng startLocation;
    @SerializedName("steps")
    private GoogleStep[] steps;
    @SerializedName("traffic_speed_entry")
    private Object[] trafficSpeedEntries;
    @SerializedName("via_waypoint")
    private Object[] viaWaypoints;

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

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public OmniLatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(OmniLatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public OmniLatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(OmniLatLng startLocation) {
        this.startLocation = startLocation;
    }

    public GoogleStep[] getSteps() {
        return steps;
    }

    public void setSteps(GoogleStep[] steps) {
        this.steps = steps;
    }

    public Object[] getTrafficSpeedEntries() {
        return trafficSpeedEntries;
    }

    public void setTrafficSpeedEntries(Object[] trafficSpeedEntries) {
        this.trafficSpeedEntries = trafficSpeedEntries;
    }

    public Object[] getViaWaypoints() {
        return viaWaypoints;
    }

    public void setViaWaypoints(Object[] viaWaypoints) {
        this.viaWaypoints = viaWaypoints;
    }
}
