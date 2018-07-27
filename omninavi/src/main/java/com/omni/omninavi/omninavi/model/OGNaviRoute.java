package com.omni.omninavi.omninavi.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by omniguidermac on 2017/1/19.
 */

public class OGNaviRoute {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("data")
    private List<OGNaviRoutePOI> navigationalRoutePOIs;

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

    public List<OGNaviRoutePOI> getNavigationalRoutePOIs() {
        return navigationalRoutePOIs;
    }

    public void setNavigationalRoutePOIs(List<OGNaviRoutePOI> navigationalRoutePOIs) {
        this.navigationalRoutePOIs = navigationalRoutePOIs;
    }
}
