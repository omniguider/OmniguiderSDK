package com.omni.omninavi.omninavi.manager;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.omni.omninavi.omninavi.model.SendUserLocationResponse;
import com.omni.omninavi.omninavi.model.UserCurrentLocation;
import com.omni.omninavi.omninavi.model.group.CreateGroupResponse;
import com.omni.omninavi.omninavi.model.group.JoinGroupResponse;
import com.omni.omninavi.omninavi.model.group.LeaveGroupResponse;
import com.omni.omninavi.omninavi.tool.DialogTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiliiamwang on 15/12/2017.
 */

public class GroupInfoManager {

    private final int TIMEOUT = 15000;

    private static GroupInfoManager sGroupInfoManager;

    public static GroupInfoManager create() {
        if (sGroupInfoManager == null) {
            sGroupInfoManager = new GroupInfoManager();
        }
        return sGroupInfoManager;
    }

    private GroupInfoManager() {

    }

    public void createGroup(Context context, String title, String name, String hour, @Nullable String loginToken,
                            NetworkManager.NetworkManagerListener<CreateGroupResponse> listener) {

        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/add_group";
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("name", name);
        params.put("hour", hour);
        params.put("device_id", NetworkManager.getInstance().getDeviceId(context));
//        params.put("login_token", loginToken);

        NetworkManager.getInstance().addPostStringRequest(context,
                url,
                params,
                CreateGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void joinGroup(Context context, String key, String name, @Nullable String loginToken,
                          NetworkManager.NetworkManagerListener<JoinGroupResponse> listener) {

        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/join_group";
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("name", name);
        params.put("device_id", NetworkManager.getInstance().getDeviceId(context));
//        params.put("login_token", loginToken);

        NetworkManager.getInstance().addPostStringRequest(context,
                url,
                params,
                JoinGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void leaveGroup(Context context, String key, @Nullable String loginToken,
                           NetworkManager.NetworkManagerListener<LeaveGroupResponse> listener) {

        DialogTools.getInstance().showProgress(context);

        String url = NetworkManager.DOMAIN_NAME + "api/leave_group";
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("device_id", NetworkManager.getInstance().getDeviceId(context));
//        params.put("login_token", loginToken);

        NetworkManager.getInstance().addPostStringRequest(context,
                url,
                params,
                LeaveGroupResponse.class,
                TIMEOUT,
                listener);
    }

    public void sendUserLocation(Context context, List<UserCurrentLocation> list, NetworkManager.NetworkManagerListener<SendUserLocationResponse> listener) {

        String jsonStr = NetworkManager.getInstance().getGson().toJson(list);

        String url = NetworkManager.DOMAIN_NAME + "api/send_user_location";
        Map<String, String> params = new HashMap<>();
//        params.put("u_id", "");
        params.put("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        params.put("jsondata", jsonStr);

        NetworkManager.getInstance().addPostStringRequest(context, url, params, SendUserLocationResponse.class, TIMEOUT, listener);
    }
}
