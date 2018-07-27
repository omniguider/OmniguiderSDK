package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 04/12/2017.
 */

public class OGPOIIcon implements Serializable {

    @SerializedName("s100")
    private String s100Url;
    @SerializedName("s60")
    private String s60Url;
    @SerializedName("s40")
    private String s40Url;
    @SerializedName("s20")
    private String s20Url;

    public String getS100Url() {
        return s100Url;
    }

    public void setS100Url(String s100Url) {
        this.s100Url = s100Url;
    }

    public String getS60Url() {
        return s60Url;
    }

    public void setS60Url(String s60Url) {
        this.s60Url = s60Url;
    }

    public String getS40Url() {
        return s40Url;
    }

    public void setS40Url(String s40Url) {
        this.s40Url = s40Url;
    }

    public String getS20Url() {
        return s20Url;
    }

    public void setS20Url(String s20Url) {
        this.s20Url = s20Url;
    }
}
