package com.omni.omninavi.omninavi.model.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 03/06/2017.
 */

public class GroupInfo implements Serializable {

    @SerializedName("id")
    private String id;
    @SerializedName("key")
    private String key;
    @SerializedName("title")
    private String title;
    @SerializedName("end_timestamp")
    private String endTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        return ((GroupInfo) obj).getKey().equals(this.getKey());
    }
}
