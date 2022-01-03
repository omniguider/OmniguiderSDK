package com.omni.navisdk.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.module.POI;
import com.omni.navisdk.module.group.CreateGroupCollectionJsonObject;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.tool.PreferencesTools;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.omni.navisdk.manager.MyAlarmManager.ALARM_CALLED_BY;
import static com.omni.navisdk.manager.MyAlarmManager.CUSTOMIZE_EVENT_NOTICE;
import static com.omni.navisdk.manager.MyAlarmManager.FIND_FRIEND_NOTICE;
import static com.omni.navisdk.module.group.CreateGroupCollectionJsonObject.GROUP_COLLECTION_PLACE;
import static com.omni.navisdk.module.group.CreateGroupCollectionJsonObject.GROUP_COLLECTION_TIME;
import static com.omni.navisdk.module.group.CreateGroupCollectionJsonObject.GROUP_NAME;
import static com.omni.navisdk.module.group.CreateGroupCollectionJsonObject.GROUP_NOTIFICATION_ID;

public class MyAlarmNoticeReceiver extends BroadcastReceiver {
    public static final String TAG = MyAlarmNoticeReceiver.class.getSimpleName();



    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            initFindFriendNotice (context, intent);
        } else {
            Map<String, String> datas = new HashMap<>();
            if (intent.getExtras() != null) {
                for (String key : intent.getExtras().keySet()) {
                    String value = intent.getExtras().getString(key);
                    Log.e(TAG, "onReceive Key: " + key + " Value: " + value);
                    if (key.equals(Intent.EXTRA_ALARM_COUNT))continue;
                    if (key.equals(ALARM_CALLED_BY))continue;
                    if (value == null)continue;
                    datas.put(key, value);
                }
            } else {
                return;
            }
            String type = intent.getStringExtra(ALARM_CALLED_BY);
            if (type.equals(FIND_FRIEND_NOTICE)) {
                JSONObject obj=new JSONObject(datas);
                CreateGroupCollectionJsonObject cgcjo = NetworkManager.Companion.getInstance().getGson().fromJson(obj.toString(), CreateGroupCollectionJsonObject.class);

                Intent new_intent = new Intent();
                new_intent.setAction(TAG);
                context.sendBroadcast(new_intent);

                Bundle b = intent.getExtras();
                    //delay and check
                    final Handler h = new Handler(Looper.getMainLooper());
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            sendFindFriendNotification(context, b);
                            //set group data to pushed, and add Notification id
                            SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(context);
                            String oldGroupData = s.getString(FIND_FRIEND_NOTICE, "");
                            String []oldGroups = oldGroupData.split(",,,");

                            String updateGroupData = "";
                            for (String g:oldGroups) {
                                if (g.length() == 0)continue;
                                CreateGroupCollectionJsonObject tmp = NetworkManager.Companion.getInstance().getGson().fromJson(g, CreateGroupCollectionJsonObject.class);
                                if (tmp == null) continue;
                                if (!tmp.getGroupName().equals(cgcjo.getGroupName())
                                        || !tmp.getCreater().equals(cgcjo.getCreater())
                                        || tmp.getEndTime() != cgcjo.getEndTime()
                                        || tmp.getCollectionTime() != cgcjo.getCollectionTime()
                                        || tmp.getCollectionNoticeTime() != cgcjo.getCollectionNoticeTime()){
                                    updateGroupData += g + ",,,";
                                    continue;
                                }
                                //if (!tmp.getMeetingTime().equals(cgcjo.getMeetingTime()))continue;
                                tmp.setPushed(true);
                                updateGroupData += NetworkManager.Companion.getInstance().getGson().toJson(tmp) + ",,,";
                            }

                            Log.e(TAG, "updateGroupData:" + updateGroupData);
                            s.edit().putString(FIND_FRIEND_NOTICE, updateGroupData).apply();
                        }
                    };
                    h.postDelayed(r, 3000);
            }
        }
    }

    private void initFindFriendNotice (Context context, Intent intent) {
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                String value = intent.getExtras().getString(key);
                Log.e(TAG, "Key: " + key + " Value: " + value);
            }
        } else {
            return;
        }

        SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(context);
        String oldGroupData = s.getString(FIND_FRIEND_NOTICE, "");
        if (oldGroupData.length() == 0) return;
        String[] oldGroups = oldGroupData.split(",,,");
        if (oldGroups.length == 0) return;
        for (String g : oldGroups) {
            Log.e(TAG, "old GroupData:" + g);
            if (g.length() == 0) continue;
            CreateGroupCollectionJsonObject datas = NetworkManager.Companion.getInstance().getGson().fromJson(g, CreateGroupCollectionJsonObject.class);
            //add alarm
            //collection time
            long notice = datas.getCollectionNoticeTime();
            if (notice == 0) continue;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(notice);

            MyAlarmManager mAM = new MyAlarmManager(context);
            if (!datas.getPushed())
                mAM.addAlarm(cal, datas, FIND_FRIEND_NOTICE);
            //mAM.cancelAlarm(datas, FIND_FRIEND_NOTICE);
        }

    }

    private void initCustomizeEventNotice (Context context, Intent intent) {
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                String value = intent.getExtras().getString(key);
                Log.e(TAG, "Key: " + key + " Value: " + value);
            }
        } else {
            return;
        }
    }

    private void sendFindFriendNotification(Context context, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, HomeActivity.class);
        intent.putExtras(bundle);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        final long[] vibrate_effect = {1000, 1000};
        //Toast.makeText(getApplicationContext(), minor+","+pushContent.getContent_zh(), Toast.LENGTH_SHORT).show();
        String channel_id;
        int id = Integer.parseInt(bundle.getString(GROUP_NOTIFICATION_ID, "1"));
        channel_id = context.getString(R.string.notify_channel_name_meeting);
Log.e(TAG, "channel_id:" + channel_id);
        DataCacheManager.getInstance().initNotificationManager(context);
        NotificationManager notificationManager = DataCacheManager.getInstance().getNotificationManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id,
                    context.getString(R.string.notify_channel_name_meeting),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(context.getString(R.string.home_page_option_group_location) + "-" + context.getString(R.string.notify_channel_name_meeting));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String content = bundle.getString(GROUP_NAME, "");
        inboxStyle.addLine(bundle.getString(GROUP_NAME, ""));
        String data = bundle.getString(GROUP_COLLECTION_PLACE, "");
        if (data.length() > 0) {
            POI p = NetworkManager.Companion.getInstance().getGson().fromJson(data, POI.class);
            content += "/" + context.getString(R.string.dialog_hint_create_group_meeting_place) + ":" + p.getName();
            inboxStyle.addLine(context.getString(R.string.dialog_hint_create_group_meeting_place) + ":" + p.getName());
        }
        data = bundle.getString(GROUP_COLLECTION_TIME, "");
        if (data.length() > 0) {
            Date d = new Date();
            d.setTime(Long.parseLong(data));
            content += "/" + context.getString(R.string.dialog_hint_create_group_meeting_time)  + ":" + dateFormatter.format(d);
            inboxStyle.addLine(context.getString(R.string.dialog_hint_create_group_meeting_time)  + ":" + dateFormatter.format(d));
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id);
        builder.setSmallIcon(R.drawable.ic_push)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                //.setLargeIcon(bitmap)
                .setContentTitle(context.getString(R.string.home_page_option_group_location) + "-" + context.getString(R.string.notify_channel_name_meeting))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(vibrate_effect)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setStyle(inboxStyle);

        notificationManager.notify(""+id, id, builder.build());
    }
}
