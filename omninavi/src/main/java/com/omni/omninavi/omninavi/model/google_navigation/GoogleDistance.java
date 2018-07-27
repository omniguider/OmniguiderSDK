package com.omni.omninavi.omninavi.model.google_navigation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 14/06/2017.
 */

public class GoogleDistance implements Serializable {

    @SerializedName("text")
    private String text;
    @SerializedName("value")
    private int value;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
