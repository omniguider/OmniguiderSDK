package com.omni.navisdk.module.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GroupInfo implements Serializable {

    @SerializedName("id")
    private String id;
    @SerializedName("key")
    private String key;
    @SerializedName("title")
    private String title;
    @SerializedName("remind_time")
    private String remind_time;
    @SerializedName("collection_time")
    private String collection_time;
    @SerializedName("p_type")
    private String p_type;
    @SerializedName("p_id")
    private String p_id;
    @SerializedName("ap_id")
    private String ap_id;
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

    public String getNoticeTime() {
        return remind_time;
    }

    public void setNoticeTime(String remind_time) {
        this.remind_time = remind_time;
    }


    public String getMeetingTime() {
        return collection_time;
    }

    public void setCollectionTime(String collection_time) {
        this.collection_time = collection_time;
    }

    public String getP_type() {
        return p_type;
    }

    public String getPid() {
        return p_id;
    }

    public String getAp_id() {
        return ap_id;
    }

    public void setPid(String p_id) {
        this.p_id = p_id;
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
