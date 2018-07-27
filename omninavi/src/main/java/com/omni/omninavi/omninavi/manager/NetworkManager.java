package com.omni.omninavi.omninavi.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.omni.omninavi.R;
import com.omni.omninavi.omninavi.model.CommonResponse;
import com.omni.omninavi.omninavi.tool.AeSimpleSHA1;
import com.omni.omninavi.omninavi.tool.DialogTools;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by wiliiamwang on 31/10/2017.
 */

public class NetworkManager {

    public interface NetworkManagerListener<T> {
        void onSucceed(T object);

        void onFail(VolleyError error, boolean shouldRetry);
    }

    public static final String NETWORKMANAGER_TAG = NetworkManager.class.getSimpleName();
//    public static final String DOMAIN_NAME = "http://nmp.utobonus.com/";
    public static final String DOMAIN_NAME = "https://doit.utobonus.com/";
    public static final String API_RESULT_TRUE = "true";
    public static final String ERROR_MESSAGE_API_TIME_OUT = "Call API timeout";
    public static final int DEFAULT_TIMEOUT = 30000;

    private static NetworkManager mNetworkManager;
    private RequestQueue mRequestQueue;
    private Gson mGson;

    public static NetworkManager getInstance() {
        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager();
        }
        return mNetworkManager;
    }

    public boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isNetworkEnable = (manager != null &&
                    manager.getActiveNetworkInfo() != null &&
                    manager.getActiveNetworkInfo().isConnectedOrConnecting());

            return isNetworkEnable;
        } else {
            return false;
        }
    }

    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public <T> void addToRequestQueue(Request<T> req, String tag, Context context) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? NETWORKMANAGER_TAG : tag);
        getRequestQueue(context).add(req);
    }

    public <T> void addToRequestQueue(Request<T> req, Context context) {
        req.setTag(NETWORKMANAGER_TAG);
        getRequestQueue(context).add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private String getMacStr(double currentTimestamp) {
        try {
            return AeSimpleSHA1.SHA1("doitapp://" + currentTimestamp);
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String addMacInParams(String paramsStr) {
        double currentTimestamp = System.currentTimeMillis() / 1000.0f;
        String mac = getMacStr(currentTimestamp);
        if (!TextUtils.isEmpty(paramsStr)) {
            paramsStr = paramsStr + "&";
        }
        paramsStr = paramsStr + "timestamp=" + currentTimestamp + "&mac=" + mac;

        return paramsStr;
    }

    public <T> void addJsonRequest(final Context context,
                                   int requestMethod,
                                   final String url,
                                   Map<String, String> params,
                                   final Class<T> responseClass,
                                   int timeoutMs,
                                   final NetworkManagerListener<T> listener) {

        if (!isNetworkAvailable(context)) {
            DialogTools.getInstance().dismissProgress(context);
            DialogTools.getInstance().showNoNetworkMessage(context);
            return;
        }

        String paramsString = "";
        if (params != null) {
            for (String key : params.keySet()) {
                if (!TextUtils.isEmpty(paramsString)) {
                    paramsString = paramsString + "&";
                }
                paramsString = paramsString + key + "=" + params.get(key);
            }
        }
        addMacInParams(paramsString);

        final String requestUrl = (TextUtils.isEmpty(paramsString)) ? url : url + "?" + paramsString;
        JsonObjectRequest request = new JsonObjectRequest(requestMethod,
                requestUrl,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        T object = getGson().fromJson(response.toString(), responseClass);
                        listener.onSucceed(object);

                        DialogTools.getInstance().dismissProgress(context);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null && error.getClass().equals(TimeoutError.class)) {
                            error = new VolleyError("Call API timeout");
                        }
                        listener.onFail(error, (TextUtils.isEmpty(error.getMessage()) && error.getCause() == null && error.networkResponse == null));

                        DialogTools.getInstance().dismissProgress(context);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMs,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addToRequestQueue(request, context.getClass().getSimpleName(), context);
    }

    public <T> void addJsonRequestToCommonObj(Context context,
                                              int requestMethod,
                                              final String url,
                                              Map<String, String> params,
                                              final Class<T[]> responseClass,
                                              final NetworkManagerListener<T[]> listener) {

        addJsonRequestToCommonObj(context, requestMethod, url, params, responseClass, DEFAULT_TIMEOUT, listener);
    }

    public <T> void addJsonRequestToCommonObj(final Context context,
                                              int requestMethod,
                                              final String url,
                                              Map<String, String> params,
                                              final Class<T[]> responseClass,
                                              int timeoutMs,
                                              final NetworkManagerListener<T[]> listener) {

        if (!isNetworkAvailable(context)) {
            DialogTools.getInstance().dismissProgress(context);
            DialogTools.getInstance().showNoNetworkMessage(context);
            return;
        }

        String paramsString = "";
        if (params != null) {
            for (String key : params.keySet()) {
                if (!TextUtils.isEmpty(paramsString)) {
                    paramsString = paramsString + "&";
                }
                paramsString = paramsString + key + "=" + params.get(key);
            }
        }
        addMacInParams(paramsString);

        final String requestUrl = (TextUtils.isEmpty(paramsString)) ? url : url + "?" + paramsString;
        JsonObjectRequest request = new JsonObjectRequest(requestMethod,
                requestUrl,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        CommonResponse commonResponse = getGson().fromJson(response.toString(), CommonResponse.class);
                        if (commonResponse.getResult().equals(API_RESULT_TRUE)) {
                            String json = new Gson().toJson(commonResponse.getData());
                            T[] data = getGson().fromJson(json, responseClass);

                            listener.onSucceed(data);

                            DialogTools.getInstance().dismissProgress(context);
                        } else {
                            DialogTools.getInstance().dismissProgress(context);
                            DialogTools.getInstance().showErrorMessage(context, R.string.error_dialog_title_text_unknown, commonResponse.getErrorMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null && error.getClass().equals(TimeoutError.class)) {
                            error = new VolleyError("Call API timeout");
                        }
                        listener.onFail(error, (TextUtils.isEmpty(error.getMessage()) && error.getCause() == null && error.networkResponse == null));

                        DialogTools.getInstance().dismissProgress(context);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMs,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addToRequestQueue(request, context.getClass().getSimpleName(), context);
    }

    public <T> void addPostStringRequest(final Context context,
                                         final String url,
                                         final Map<String, String> params,
                                         final Class<T> responseClass,
                                         int timeoutMs,
                                         final NetworkManagerListener<T> listener) {
        if (!isNetworkAvailable(context)) {
            if (!url.contains("send_user_location") && !url.contains("set_beacon")) {
                DialogTools.getInstance().dismissProgress(context);
                DialogTools.getInstance().showNoNetworkMessage(context);
            }
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.e("@W@", "url : " + url + ", response : " + response);
                        T object = getGson().fromJson(response, responseClass);
                        listener.onSucceed(object);

                        DialogTools.getInstance().dismissProgress(context);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            Log.e("NetworkManager", "Error NetworkResponse statusCode === " + error.networkResponse.statusCode);
                        } else {
                            if (error.getClass().equals(TimeoutError.class)) {
                                Log.e("NetworkManager", "*** Error NetworkResponse Timeout, timeMs : " + error.getNetworkTimeMs());
                                error = new VolleyError(ERROR_MESSAGE_API_TIME_OUT);
                            }
                        }
                        listener.onFail(error, (TextUtils.isEmpty(error.getMessage()) && error.getCause() == null && error.networkResponse == null));

                        DialogTools.getInstance().dismissProgress(context);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                double currentTimestamp = System.currentTimeMillis() / 1000.0f;
                String mac = getMacStr(currentTimestamp);
                params.put("timestamp", "" + currentTimestamp);
                params.put("mac", mac);

//                Log.e("@W@", "params : " + params.toString());
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMs,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addToRequestQueue(request, context.getClass().getSimpleName(), context);
    }
}
