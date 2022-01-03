package com.omni.navisdk.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.omni.navisdk.module.group.CreateGroupCollectionJsonObject;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.tool.PreferencesTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MyAlarmManager {
    public final static String ALARM_CALLED_BY = "ALARM_CALLED_BY";
    public final static String FIND_FRIEND_NOTICE = "FIND_FRIEND_NOTICE";
    public final static String CUSTOMIZE_EVENT_NOTICE = "CUSTOMIZE_EVENT_NOTICE";
    public static final String TAG = "MyAlarmManager";
    private static Context context;
    private static AlarmManager am;
    public MyAlarmManager(Context context) {
        if (context != null)
            if (MyAlarmManager.context == null)
                MyAlarmManager.context = context;
        if (MyAlarmManager.am == null)
            if (MyAlarmManager.context != null)
                MyAlarmManager.am = (AlarmManager) MyAlarmManager.context.getSystemService(Context.ALARM_SERVICE);
    }

    private Intent lastAdd;
    public boolean addAlarm(Calendar cal, CreateGroupCollectionJsonObject datas, String type) {
        Intent intent = new Intent(context, MyAlarmNoticeReceiver.class);
        Bundle b = new Bundle();

        for (String k:datas.getDataMap().keySet()) {
            b.putString(k, datas.getDataMap().get(k));
        }
        b.putString(ALARM_CALLED_BY, type);
        intent.putExtras(b);
        //save alarm for device reset to re all alarm
        SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(context);
        String oldGroupData = s.getString(type, "");
        String tmp = NetworkManager.Companion.getInstance().getGson().toJson(datas, CreateGroupCollectionJsonObject.class);

        if (oldGroupData.length() == 0) {
            oldGroupData = tmp;
        } else {
            oldGroupData += ",,,"+tmp;
        }
        s.edit().putString(type, oldGroupData).apply();

        tmp = datas.getGroupKey() + " " + datas.getCollectionNoticeTime();
        intent.setData(Uri.parse(tmp));
        intent.setAction(TAG);
        lastAdd = intent;
        Log.e(TAG, "add alarm:" + tmp);
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.SECOND, 10);
        Log.e(TAG, "NoticeTime:" + cal.getTime());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        //Exact
        //am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        d.setTime(cal.getTimeInMillis());
        Log.e(TAG, "add alarm:" + tmp + " notice time:" + dateFormatter.format(d));
        return true;
    }

    public void cancelAlarm(CreateGroupCollectionJsonObject data, String type) {
        Intent intent = new Intent(context, MyAlarmNoticeReceiver.class);
        Bundle b = new Bundle();
        Map<String, String> dataMap = data.getDataMap();
        for (String k:dataMap.keySet()) {
            b.putString(k, dataMap.get(k));
        }
        b.putString(ALARM_CALLED_BY, type);
        intent.putExtras(b);

        String updateGroupData = "";
        //remove group data
        SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(context);
        String oldGroupData = s.getString(type, "");
        if (oldGroupData.length() > 0) {
            String[] oldGroups = oldGroupData.split(",,,");
            if (oldGroups.length > 0) {
                for (String g : oldGroups) {
                    if (g == null)continue;
                    if (g.length() == 0)continue;
                    CreateGroupCollectionJsonObject old = NetworkManager.Companion.getInstance().getGson().fromJson(g, CreateGroupCollectionJsonObject.class);
                    Log.e(TAG, g);
                    Log.e(TAG, old.getGroupKey() + " " + data.getGroupKey());
                    if (!old.getGroupKey().equals(data.getGroupKey())) {
                        updateGroupData += g + ",,,";
                    }
                }
                Log.e(TAG, "updateGroupData:" + updateGroupData);
                s.edit().putString(type, updateGroupData).apply();
            }
        }

        String reference = data.getGroupKey() + " " + data.getCollectionNoticeTime();
        intent.setData(Uri.parse(reference));
        intent.setAction(TAG);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        d.setTime(data.getCollectionNoticeTime());
        Log.e(TAG, "cancel alarm:" + reference + " notice time:" + dateFormatter.format(d));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pendingIntent);
    }
}
