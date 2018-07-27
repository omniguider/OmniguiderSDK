package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;
import com.omni.omninavi.omninavi.model.group.GroupData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wiliiamwang on 26/05/2017.
 */

public class SendUserLocationResponse implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private List<GroupData> data;

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
        return data;
    }

    public void setData(List<GroupData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{ result : " + result + ", error_message : " + errorMessage + ", msg : " + msg + " }";
    }
}
