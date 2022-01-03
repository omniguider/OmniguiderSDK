package com.omni.navisdk.module;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OGPOIOffice implements Serializable {

    @SerializedName("dep_name")
    private String dep_name;
    @SerializedName("area")
    private String area;
    @SerializedName("name")
    private String name;
    @SerializedName("desc")
    private String desc;
    @SerializedName("image")
    private String image;
    @SerializedName("audio")
    private String audio;
    @SerializedName("video")
    private String video;
    @SerializedName("web")
    private String web;

    public String getDep_name() {
        return dep_name;
    }

    public String getArea() {
        return area;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public String getAudio() {
        return audio;
    }

    public String getVideo() {
        return video;
    }

    public String getWeb() {
        return web;
    }
}
