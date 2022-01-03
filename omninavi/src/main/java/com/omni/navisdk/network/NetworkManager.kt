package com.omni.navisdk.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.DrawableRes
import com.android.volley.*
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.omni.navisdk.R
import com.omni.navisdk.module.CommonArrayResponse
import com.omni.navisdk.module.CommonResponse
import com.omni.navisdk.tool.AeSimpleSHA256
import com.omni.navisdk.tool.DialogTools
import com.omni.navisdk.tool.NaviSDKText
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.UnsupportedEncodingException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NetworkManager {
    private var mGson: Gson? = null
    private var mRetrofit: Retrofit? = null
    private var mGoogleApiRetrofit: Retrofit? = null
    private var mRequestQueue: RequestQueue? = null

    interface NetworkManagerListener<T> {
        fun onSucceed(response: T?)
        fun onFail(errorMsg: String, shouldRetry: Boolean)
    }

    val retrofit:
            Retrofit?
        get() {
            val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                    .build()
            mRetrofit = Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(DOMAIN_NAME)
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return mRetrofit
        }

    val googleApiRetrofit: Retrofit?
        get() {
            if (mGoogleApiRetrofit == null) {
                val okHttpClient = OkHttpClient.Builder()
                        .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                        .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                        .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                        .build()
                mGoogleApiRetrofit = Retrofit.Builder()
                        .client(okHttpClient)
                        .baseUrl(GOOGLE_API_DOMAIN_NAME)
                        .callbackExecutor(Executors.newSingleThreadExecutor())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return mGoogleApiRetrofit
        }

    fun getRequestQueue(context: Context?): RequestQueue? {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context)
        }
        return mRequestQueue
    }

    fun setNetworkImage(context: Context?, networkImageView: NetworkImageView, url: String?) {
        setNetworkImage(context, networkImageView, url, -1, -1)
    }

    fun setNetworkImage(context: Context?, networkImageView: NetworkImageView, url: String?, @DrawableRes errorIconResId: Int) {
        setNetworkImage(context, networkImageView, url, -1, -1)
    }

    fun setNetworkImage(context: Context?, networkImageView: NetworkImageView, url: String?,
                        @DrawableRes defaultIconResId: Int, @DrawableRes errorIconResId: Int) {
        val lruImageCache: LruImageCache = LruImageCache.Companion.instance!!
        val imageLoader = ImageLoader(getRequestQueue(context), lruImageCache)
        networkImageView.setDefaultImageResId(defaultIconResId)
        networkImageView.setErrorImageResId(if (errorIconResId == -1) R.mipmap.syn_poi_information else errorIconResId)
        networkImageView.setImageUrl(url, imageLoader)
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        return if (context != null) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiNetwork != null && wifiNetwork.isConnected) {
                return true
            }
            val mobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mobileNetwork != null && mobileNetwork.isConnected) {
                return true
            }
            manager.activeNetworkInfo != null && manager.activeNetworkInfo!!.isConnectedOrConnecting
        } else {
            false
        }
    }

    val gson: Gson
        get() {
            if (mGson == null) {
                mGson = Gson()
            }
            return mGson!!
        }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun checkNetworkStatus(context: Context): Boolean {
        if (!isNetworkAvailable(context)) {
            DialogTools.instance.dismissProgress(context as Activity)
            DialogTools.instance.showNoNetworkMessage(context)
            return false
        }
        return true
    }

    /**
     * Failed response from retrofit.
     */
    private fun sendResponseFailMessage(context: Context,
                                        response: Response<*>,
                                        listener: NetworkManagerListener<*>) {
        val errorMsg = response.message()
        listener.onFail(errorMsg, false)
    }

    /**
     * The error message is from Christine or Mike.
     */
    private fun sendAPIFailMessage(context: Context, errorMsg: String, listener: NetworkManagerListener<*>) {
        listener.onFail(errorMsg, false)
    }

    fun <T> addGetRequestToCommonObj(activity: Activity,
                                     call: Call<CommonResponse>,
                                     responseClass: Class<T>,
                                     listener: NetworkManagerListener<T>) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        call.enqueue(object : Callback<CommonResponse?> {
            override fun onResponse(call: Call<CommonResponse?>, response: Response<CommonResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false)
                    } else {
                        val `object` = gson.fromJson(response.toString(), responseClass)
                        listener.onSucceed(`object`)
                    }
                } else {
                    sendResponseFailMessage(activity, response, listener)
                }
                DialogTools.instance.dismissProgress(activity)
            }

            override fun onFailure(call: Call<CommonResponse?>, t: Throwable) {
                activity.runOnUiThread {
                    listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false)
                    DialogTools.instance.dismissProgress(activity)
                }
            }
        })
    }

    fun <T> addGetRequest(
        activity: Activity,
        call: Call<T>,
        responseClass: Class<T>?,
        listener: NetworkManagerListener<T?>
    ) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        call.enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                activity.runOnUiThread {
                    if (response.isSuccessful) {
                        if (response.body() == null) {
                            listener.onFail(
                                activity.getString(R.string.error_dialog_title_text_unknown),
                                false
                            )
                        } else {
                            listener.onSucceed(response.body())
                        }
                    } else {
                        sendResponseFailMessage(activity, response, listener)
                    }
                    DialogTools.instance.dismissProgress(activity)
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                activity.runOnUiThread { DialogTools.instance.dismissProgress(activity) }
            }
        })
    }

    fun <T> addGetRequestToCommonArrayObj(activity: Activity,
                                          call: Call<CommonArrayResponse>,
                                          responseClass: Class<Array<T>>,
                                          listener: NetworkManagerListener<Array<T>>) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        call.enqueue(object : Callback<CommonArrayResponse?> {
            override fun onResponse(call: Call<CommonArrayResponse?>, response: Response<CommonArrayResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        Log.e(NaviSDKText.LOG_TAG, "#11")
                        listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false)
                    } else {
                        if (response.body()!!.result == API_RESULT_TRUE) {
                            val json = gson.toJson(response.body()!!.data)
                            val data: Array<T>? = gson.fromJson(json, responseClass)
                            if (data != null) {
                                listener.onSucceed(data)
                            }
                        } else {
                            sendAPIFailMessage(activity, response.body()!!.errorMessage, listener)
                        }
                    }
                } else {
                    sendResponseFailMessage(activity, response, listener)
                }
                DialogTools.instance.dismissProgress(activity)
            }

            override fun onFailure(call: Call<CommonArrayResponse?>, t: Throwable) {
                activity.runOnUiThread {
                    listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false)
                    DialogTools.instance.dismissProgress(activity)
                }
            }
        })
    }

    fun <T> addPostRequest(activity: Activity,
                           call: Call<T>,
                           responseClass: Class<T>?,
                           listener: NetworkManagerListener<T?>) {
        addPostRequest(activity, false, call, responseClass, listener)
    }

    fun <T> addPostRequest(activity: Activity,
                           shouldDismissProgress: Boolean,
                           call: Call<T>,
                           responseClass: Class<T>?,
                           listener: NetworkManagerListener<T?>) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        call.enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false)
                    } else {
                        listener.onSucceed(response.body())
                    }
                } else {
                    sendResponseFailMessage(activity, response, listener)
                }
                if (shouldDismissProgress) {
                    DialogTools.instance?.dismissProgress(activity)
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                activity.runOnUiThread {
                    listener.onFail(t.message!!, false)
                    if (shouldDismissProgress) {
                        DialogTools.instance?.dismissProgress(activity)
                    }
                }
            }
        })
    }

    fun <T> addPostRequestToCommonObj(activity: Activity,
                                      call: Call<CommonResponse>,
                                      responseClass: Class<T>?,
                                      listener: NetworkManagerListener<T>) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        call.enqueue(object : Callback<CommonResponse?> {
            override fun onResponse(call: Call<CommonResponse?>, response: Response<CommonResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false)
                    } else {
                        if (response.body()!!.result == API_RESULT_TRUE) {
                            if (response.body()!!.data.toString().isNotEmpty()) {
                                val json = gson.toJson(response.body()!!.data)
                                val data = gson.fromJson(json, responseClass)
                                listener.onSucceed(data)
                            }
                        } else {
                            sendAPIFailMessage(activity, response.body()!!.errorMessage, listener)
                        }
                    }
                } else {
                    sendResponseFailMessage(activity, response, listener)
                }
                DialogTools.instance.dismissProgress(activity)
            }

            override fun onFailure(call: Call<CommonResponse?>, t: Throwable) {
                activity.runOnUiThread {
                    listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false)
                    DialogTools.instance.dismissProgress(activity)
                }
            }
        })
    }

    fun <T> addPostStringRequest(
        context: Context,
        url: String,
        params: MutableMap<String, String>,
        responseClass: Class<T>?,
        timeoutMs: Int,
        listener: NetworkManagerListener<T>
    ) {
        if (!isNetworkAvailable(context)) {
            DialogTools.instance.dismissProgress(context as Activity)
            DialogTools.instance.showNoNetworkMessage(context)
            return
        }
        val request: StringRequest = object : StringRequest(
            Method.POST, url,
            com.android.volley.Response.Listener { response ->
                Log.e("ASD", "url : $url, response : $response")
                val `object`: T = mGson!!.fromJson(response, responseClass)
                listener.onSucceed(`object`)
                DialogTools.instance.dismissProgress(context as Activity)
            },
            com.android.volley.Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    Log.e(
                        "@W@",
                        "Error NetworkResponse statusCode === " + error.networkResponse.statusCode
                    )
                } else {
                    if (error.javaClass == TimeoutError::class.java) {
                        Log.e(
                            "@W@",
                            "*** Error NetworkResponse Timeout, timeMs : " + error.networkTimeMs
                        )
                    }
                }
                listener.onFail(
                    error.toString(),
                    TextUtils.isEmpty(error.message) && error.cause == null && error.networkResponse == null
                )
                DialogTools.instance.dismissProgress(context as Activity)
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val currentTimestamp = (System.currentTimeMillis() / 1000.0f).toDouble()
                val mac = getMacStr(currentTimestamp)
                params["timestamp"] = "" + currentTimestamp
                params["mac"] = mac!!
                Log.e("@W@", "params : $params")
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            timeoutMs,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        addToRequestQueue<String>(request, context.javaClass.simpleName, context)
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String?, context: Context?) {
        // set the default tag if tag is empty
        req.tag = if (TextUtils.isEmpty(tag)) NetworkManager.NETWORKMANAGER_TAG else tag
        getRequestQueue(context)!!.add(req)
    }

    fun <T> addPostRequestToCommonArrayObj(activity: Activity,
                                           call: Call<CommonArrayResponse>,
                                           responseClass: Class<Array<T>>,
                                           listener: NetworkManagerListener<Array<T>>) {
        addPostRequestToCommonArrayObj(activity, false, call, responseClass, listener)
    }

    fun <T> addPostRequestToCommonArrayObj(activity: Activity,
                                           shouldDismissProgress: Boolean,
                                           call: Call<CommonArrayResponse>,
                                           responseClass: Class<Array<T>>,
                                           listener: NetworkManagerListener<Array<T>>) {
        if (!checkNetworkStatus(activity)) {
            return
        }
        Log.e(NaviSDKText.LOG_TAG, "addPostRequestToCommonArrayObj")
        call.enqueue(object : Callback<CommonArrayResponse?> {
            override fun onResponse(call: Call<CommonArrayResponse?>, response: Response<CommonArrayResponse?>) {
                if (response.isSuccessful) {
                    if (response.body() == null) {
                        Log.e(NaviSDKText.LOG_TAG, "#1")
                        listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false)
                    } else {
                        if (response.body()!!.result == API_RESULT_TRUE) {
                            val json = gson.toJson(response.body()!!.data)
                            val data: Array<T>? = gson.fromJson(json, responseClass)
                            if (data != null) {
                                listener.onSucceed(data)
                            }
                        } else {
                            Log.e(NaviSDKText.LOG_TAG, "#2")
                            sendAPIFailMessage(activity, response.body()!!.errorMessage, listener)
                        }
                    }
                } else {
                    Log.e(NaviSDKText.LOG_TAG, "#3 response : $response")
                    sendResponseFailMessage(activity, response, listener)
                }
                if (shouldDismissProgress) {
                    DialogTools.instance.dismissProgress(activity)
                }
            }

            override fun onFailure(call: Call<CommonArrayResponse?>, t: Throwable) {
                activity.runOnUiThread {
                    Log.e(NaviSDKText.LOG_TAG, "#4 localized msg : " + t.localizedMessage + ", msg : " + t.message + ", cause : " + t.cause)
                    listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false)
                    if (shouldDismissProgress) {
                        DialogTools.instance.dismissProgress(activity)
                    }
                    Log.e(NaviSDKText.LOG_TAG, "#5")
                }
            }
        })
    }

    fun getMacStr(currentTimestamp: Double): String? {
        return try {
            AeSimpleSHA256.SHA256(ENCRYPT_KEY + currentTimestamp)
        } catch (e: NoSuchAlgorithmException) {
            ""
        } catch (e: UnsupportedEncodingException) {
            ""
        }
    }

    companion object {
        const val DOMAIN_NAME = "https://navi.taipei/"
        var ENCRYPT_KEY = "doitapp://"
        val NETWORKMANAGER_TAG = NetworkManager::class.java.simpleName
        const val GOOGLE_API_DOMAIN_NAME = "https://maps.googleapis.com/"
        const val API_RESULT_TRUE = "true"
        const val TIME_OUT = 120
        private var sNetworkManager: NetworkManager? = null
        val instance: NetworkManager
            get() {
                if (sNetworkManager == null) {
                    sNetworkManager = NetworkManager()
                }
                return sNetworkManager!!
            }
    }
}