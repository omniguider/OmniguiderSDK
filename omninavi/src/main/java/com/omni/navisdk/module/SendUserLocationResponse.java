package com.omni.navisdk.module;

import com.google.gson.annotations.SerializedName;
import com.omni.navisdk.module.group.GroupData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendUserLocationResponse implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private GroupData[] data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<GroupData> getData() {
        List<GroupData> dataList = new ArrayList<>(Arrays.asList(data));
        return dataList;
    }

    public void setData(List<GroupData> data) {
        GroupData[] dataArray = new GroupData[data.size()];
        data.toArray(dataArray);
        this.data = dataArray;
    }

    @Override
    public String toString() {
        return "{ result : " + result + ", error_message : " + errorMessage + ", msg : " + msg + " }";
    }
}
