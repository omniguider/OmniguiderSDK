package com.omni.omninavi.omninavi.model.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 26/05/2017.
 */

public class CreateGroupResponse implements Serializable {

    public static final String CGR_MSG_INSERT_SUCCESS = "INSERT SUCCESS";

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private CreateGroupData[] data;

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

    public CreateGroupData[] getData() {
        return data;
    }

    public void setData(CreateGroupData[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{ result : " + result + ", errorMessage : " + errorMessage + "\ndata : " + data.toString() + " }";
    }
}
