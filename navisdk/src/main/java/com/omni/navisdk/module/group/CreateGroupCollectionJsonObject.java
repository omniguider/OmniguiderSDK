package com.omni.navisdk.module.group;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class CreateGroupCollectionJsonObject implements Serializable {

    public static final String GROUP_KEY = "GROUP_KEY";
    public static final String GROUP_NAME = "GROUP_NAME";
    public static final String GROUP_CREATER = "GROUP_CREATER";
    public static final String GROUP_TEL = "GROUP_TEL";
    public static final String GROUP_EMD_TIME = "GROUP_EMD_TIME";
    public static final String GROUP_COLLECTION_TIME = "GROUP_COLLECTION_TIME";
    public static final String GROUP_COLLECTION_PLACE = "GROUP_COLLECTION_PLACE";
    public static final String GROUP_COLLECTION_NOTICE_TIME = "GROUP_COLLECTION_NOTICE_TIME";
    public static final String GROUP_NOTIFICATION_ID = "GROUP_NOTIFICATION_ID";
    public static final String GROUP_NOTIFICATION_PUSHED = "GROUP_NOTIFICATION_PUSHED";
    public static final String GROUP_NOTIFICATION_ISUNREAD = "GROUP_NOTIFICATION_ISUNREAD";
    public static final String GROUP_NOTIFICATION_PRESENT = "GROUP_NOTIFICATION_PRESENT";

    @SerializedName(GROUP_KEY)
    private String key;

    @SerializedName(GROUP_NAME)
    private String name;

    @SerializedName(GROUP_CREATER)
    private String creater;

    @SerializedName(GROUP_TEL)
    private String tel;

    @SerializedName(GROUP_EMD_TIME)
    private long endTime;

    @SerializedName(GROUP_COLLECTION_TIME)
    private long collectionTime = 0;

    @SerializedName(GROUP_COLLECTION_PLACE)
    private String collectionPlace;

    @SerializedName(GROUP_COLLECTION_NOTICE_TIME)
    private long collectionNoticeTime = 0;

    @SerializedName(GROUP_NOTIFICATION_ID)
    private int notificationId;

    @SerializedName(GROUP_NOTIFICATION_PUSHED)
    private boolean pushed = false;

    @SerializedName(GROUP_NOTIFICATION_ISUNREAD)
    private boolean isUnread = true;

    @SerializedName(GROUP_NOTIFICATION_PRESENT)
    private boolean present = true;

    public void setGroupKey(String key) {
        this.key = key;
        setNotificationId(Integer.parseInt(key));
    }

    public String getGroupKey() {
        return key;
    }

    public void setGroupName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return name;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public String getCreater() {
        return creater;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String getCollectionPlace() {
        return collectionPlace;
    }

    /**
     *
     * @param collectionPlace POI JSON string
     */
    public void setCollectionPlace(String collectionPlace) {
        this.collectionPlace = collectionPlace;
    }

    /**
     *
     * @return The POI JSON string
     */
    public long getCollectionNoticeTime() {
        return collectionNoticeTime;
    }

    public void setCollectionNoticeTime(long collectionNoticeTime) {
        this.collectionNoticeTime = collectionNoticeTime;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public boolean getPushed() {
        return pushed;
    }

    public void setPushed(boolean pushed) {
        this.pushed = pushed;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public boolean getPresent() {
        return present;
    }

    public void setpresent(boolean present) {
        this.present = present;
    }

    public Map<String, String> getDataMap() {
        Map<String, String> datas = new HashMap<>();
        datas.put(GROUP_KEY, getGroupKey());
        datas.put(GROUP_NAME, getGroupName());
        datas.put(GROUP_CREATER, getCreater());
        datas.put(GROUP_TEL, getTel());
        datas.put(GROUP_EMD_TIME, "" + getEndTime());
        datas.put(GROUP_COLLECTION_TIME, "" + getCollectionTime());
        datas.put(GROUP_COLLECTION_PLACE, getCollectionPlace());
        datas.put(GROUP_COLLECTION_NOTICE_TIME, "" + getCollectionNoticeTime());
        datas.put(GROUP_NOTIFICATION_ID, "" + getNotificationId());
        datas.put(GROUP_NOTIFICATION_PUSHED, "" + getPushed());
        datas.put(GROUP_NOTIFICATION_ISUNREAD, "" + isUnread());
        datas.put(GROUP_NOTIFICATION_PRESENT, "" + getPresent());
        return datas;
    }
}
