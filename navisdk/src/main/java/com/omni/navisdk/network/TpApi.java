package com.omni.navisdk.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.omni.navisdk.module.group.CreateGroupResponse;
import com.omni.navisdk.module.group.JoinGroupResponse;
import com.omni.navisdk.module.group.LeaveGroupResponse;
import com.omni.navisdk.module.group.UpdateGroupResponse;

import java.util.HashMap;
import java.util.Map;

public class TpApi {

    private final String TAG = getClass().getName();
    private final int TIMEOUT = 25000;

    private static TpApi mTpApi;

    public static TpApi getInstance() {
        if (mTpApi == null) {
            mTpApi = new TpApi();
        }
        return mTpApi;
    }

    interface TpService {
    }

    private TpService getTpService() {
        return NetworkManager.Companion.getInstance().getRetrofit().create(TpService.class);
    }


    public void createGroup(Context context, String title, String name, String tel, String hour,
                            String notice_time, String collection_time, String p_type, String p_id,
                            @Nullable String loginToken, NetworkManager.NetworkManagerListener<CreateGroupResponse> listener) {

        //DialogTools.Companion.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/add_group";
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("name", name);
        params.put("tel", tel);
        params.put("hour", hour);
        params.put("remind_time", notice_time);
        params.put("collection_time", collection_time);
        params.put("p_type", p_type);
        params.put("p_id", p_id);
        params.put("login_token", loginToken);
        params.put("device_id", NetworkManager.Companion.getInstance().getDeviceId(context));

        Log.e(TAG, "createGroup: device id ~ :" + NetworkManager.Companion.getInstance().getDeviceId(context));

        NetworkManager.Companion.getInstance().addPostStringRequest(context,
                url,
                params,
                CreateGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void joinGroup(Context context, String key, String name, String tel, @Nullable String loginToken,
                          NetworkManager.NetworkManagerListener<JoinGroupResponse> listener) {

        //DialogTools.Companion.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/join_group";
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("name", name);
        params.put("tel", tel);
        params.put("login_token", loginToken);
        params.put("device_id", NetworkManager.Companion.getInstance().getDeviceId(context));

        NetworkManager.Companion.getInstance().addPostStringRequest(context,
                url,
                params,
                JoinGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void updateGroup(Context context, @NonNull String key, @NonNull String enabled,
                            @Nullable boolean del, @Nullable String title, @Nullable String hour,
                            @Nullable String tel, @Nullable String noticeTime, @Nullable String meeting_time,
                            @Nullable String p_type, @Nullable String p_id, @Nullable String loginToken,
                            NetworkManager.NetworkManagerListener<UpdateGroupResponse> listener) {

        //DialogTools.Companion.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/update_group";
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("enabled", enabled);
        if (del)
            params.put("del", "Y");
        if (title != null) if (title.length() > 0)
            params.put("title", title);
        if (hour != null) if (hour.length() > 0)
            params.put("hour", hour);
        if (tel != null) if (tel.length() > 0)
            params.put("tel", tel);
        if (noticeTime != null)/*if (noticeTime.length() > 0)*/
            params.put("remind_time", noticeTime);
        if (meeting_time != null)/*if (collection_time.length() > 0)*/
            params.put("collection_time", meeting_time);
        if (p_id != null) if (p_id.length() > 0)
            params.put("ap_id", p_id);
//        if (p_type != null) if (p_type.length() > 0)
//            params.put("p_type", p_type);
        params.put("device_id", NetworkManager.Companion.getInstance().getDeviceId(context));
        if (loginToken != null) if (loginToken.length() > 0)
            params.put("login_token", loginToken);

        NetworkManager.Companion.getInstance().addPostStringRequest(context,
                url,
                params,
                UpdateGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void leaveGroup(Context context, String key, @Nullable String loginToken,
                           NetworkManager.NetworkManagerListener<LeaveGroupResponse> listener) {
        //DialogTools.Companion.getInstance().showProgress(context);
        String url = NetworkManager.DOMAIN_NAME + "api/leave_group";
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("login_token", loginToken);
        params.put("device_id", NetworkManager.Companion.getInstance().getDeviceId(context));

        NetworkManager.Companion.getInstance().addPostStringRequest(context,
                url,
                params,
                LeaveGroupResponse.class,
                TIMEOUT,
                listener);
    }
}
