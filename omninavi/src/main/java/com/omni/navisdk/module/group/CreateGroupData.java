package com.omni.navisdk.module.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class CreateGroupData implements Serializable {

    @SerializedName("key")
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
