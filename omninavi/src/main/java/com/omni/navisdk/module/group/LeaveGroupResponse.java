package com.omni.navisdk.module.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LeaveGroupResponse implements Serializable {

    public static final String LGR_MSG_SUCCESS = "SUCCESS";

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("msg")
    private String message;
    @SerializedName("data")
    private Object[] data;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }
}
