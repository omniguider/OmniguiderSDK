package com.omni.navisdk.view.group_location;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.omni.navisdk.BuildConfig;
import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.NaviSDKActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.manager.DataCacheManager;
import com.omni.navisdk.manager.MyAlarmManager;
import com.omni.navisdk.module.BuildingFloor;
import com.omni.navisdk.module.OmniEvent;
import com.omni.navisdk.module.POI;
import com.omni.navisdk.module.SendUserLocationResponse;
import com.omni.navisdk.module.group.CreateGroupCollectionJsonObject;
import com.omni.navisdk.module.group.GroupData;
import com.omni.navisdk.module.group.GroupInfo;
import com.omni.navisdk.module.group.GroupMember;
import com.omni.navisdk.module.group.LeaveGroupResponse;
import com.omni.navisdk.module.group.UpdateGroupResponse;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.network.TpApi;
import com.omni.navisdk.tool.DialogTools;
import com.omni.navisdk.tool.NaviSDKText;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.tool.Tools;
import com.omni.navisdk.view.FragmentOnKeyEvent;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.omni.navisdk.tool.NaviSDKText.LOG_TAG;
import static com.omni.navisdk.tool.PreferencesTools.KEY_LEFT_GROUP_ENDTIME;

public class GroupMembersTestFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "GroupMTFragment";
    public static final String ARG_KEY_GROUP_DATA = "arg_key_group_data";
    public static final String TARGET_MEMBER = "TARGET_MEMBER";
    public static final String TARGET_MEMBER_ALL = "TARGET_MEMBER_ALL";
    public static final String TARGET_MEMBER_POSITION = "TARGET_MEMBER_POSITION";
    public static final String COLLECTION_PLACE = "COLLECTION_PLACE";
    public static final String COLLECTION_PLACE_POI = "COLLECTION_PLACE_POI";

    private static final int REQUEST_CODE = 1;

    private Context mContext;
    private View mView;
    private RecyclerView mRV;
    private TextView collectionTime;
    private TextView collectionPlace;
    private TextView noticeTime;
    private ImageView navigation;
    private MaterialButton reset;
    private NumberPicker collectionTimeH;
    private NumberPicker collectionTimeM;
    private NumberPicker collectionNoticeTime;
    private final int notice_interval = 5;
    private String groupId;
    private String groupKey;
    private String groupCreater;
    private String groupCreaterDeviceId;
    private String groupCollectionPID;
    private String groupCollectionPType;
    private POI groupCollectionPOI;
    private GroupData groupData;
    private GroupMember creatorInfo;
    private GroupMember userInfo;
    private boolean isCreater = false;
    int height, width;
    ConstraintLayout editLayout;
    private boolean editViewIsShown = false;
    private String select_hint;

    public static GroupMembersTestFragment newInstance(GroupData groupData) {
        GroupMembersTestFragment fragment = new GroupMembersTestFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_KEY_GROUP_DATA, groupData);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.group_members_fragment_view1, null, false);
            select_hint = getContext().getString(R.string.string_select_please);
            groupData = (GroupData) getArguments().getSerializable(ARG_KEY_GROUP_DATA);
            for (GroupMember gm : groupData.getGroupMembers()) {
                Log.e(TAG, gm.getRole() + " " + gm.getName());
                if (gm.getRole().equals("admin")) {
                    creatorInfo = gm;
                    groupCreater = gm.getName();
                    groupCreaterDeviceId = gm.getDeviceId();
                    String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                    if (myDeviceId.trim().equals(groupCreaterDeviceId.trim()))
                        isCreater = true;
                    break;
                }
            }
            String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
            for (GroupMember gm : groupData.getGroupMembers()) {
                Log.e(TAG, gm.getRole() + " " + gm.getName());
                if (gm.getDeviceId().equals(myDeviceId)) {
                    userInfo = gm;
                    break;
                }
            }

            groupId = groupData.getGroupInfos()[0].getId();
            groupKey = groupData.getGroupInfos()[0].getKey();
            groupCollectionPID = groupData.getGroupInfos()[0].getAp_id();
            groupCollectionPType = groupData.getGroupInfos()[0].getP_type();
            collectionPlace = mView.findViewById(R.id.collection_place_setting_btn);
            navigation = mView.findViewById(R.id.collection_place_navi_btn);
            navigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupCollectionPID != null)
                        openGroupMapPage((GroupMember) null, groupCollectionPID);
                }
            });

            TextView actionBarTitleTV = mView.findViewById(R.id.group_members_fragment_view_tv_title);
            actionBarTitleTV.setText(groupData.getGroupInfos()[0].getTitle());
            final FrameLayout editFL = mView.findViewById(R.id.detail_action_bar_fl_edit);

            mView.findViewById(R.id.group_members_fragment_view_tv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editViewIsShown) {
                        editLayout.setVisibility(View.GONE);
                        editViewIsShown = false;
                        editFL.setVisibility(View.VISIBLE);
                        return;
                    }
                    getActivity().getSupportFragmentManager().popBackStack();

                }
            });


            if (isCreater) {
                editLayout = mView.findViewById(R.id.edit_meeting_function_layout);
                editFL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editViewIsShown) {
                            editFL.setVisibility(View.INVISIBLE);
                            initEditView();
                            editLayout.setVisibility(View.VISIBLE);
                            editViewIsShown = true;
                        }
                    }
                });
            } else {
                editFL.setVisibility(View.GONE);
            }

/*            navigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGroupMapPage(null, groupCollectionPID);
                }
            });*/
            mView.findViewById(R.id.member_group_dialog_tv_leave).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showCustomLeaveGroupDialog();

//                    new AlertDialog.Builder(mContext)
//                            .setMessage(R.string.dialog_msg_leave_group)
//                            .setPositiveButton(R.string.dialog_button_ok_text, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(final DialogInterface dialog, int which) {
//                                    NMPApi.getInstance().leaveGroup(mContext, groupKey, "", new NetworkManager.NetworkManagerListener<LeaveGroupResponse>() {
//                                        @Override
//                                        public void onSucceed(LeaveGroupResponse object) {
//                                            EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_LEFT_GROUP, ""));
//                                            dialog.dismiss();
//
//                                            //GroupMembersFragment.this.dismiss();
//                                            getChildFragmentManager().beginTransaction().detach(GroupMembersFragment.this).commit();
//                                        }
//
//                                        @Override
//                                        public void onFail(VolleyError error, boolean shouldRetry) {
//                                            dialog.dismiss();
//                                            DialogTools.getInstance().showErrorMessage(mContext,
//                                                    getString(R.string.error_dialog_title_text_normal),
//                                                    getString(R.string.error_dialog_title_text_unknown));
//                                        }
//                                    });
//                                }
//                            })
//                            .setNegativeButton(R.string.dialog_button_no_text, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }).create().show();
                }
            });

            mRV = mView.findViewById(R.id.group_members_fragment_view_rv);

            GroupMembersAdapter adapter = new GroupMembersAdapter(groupData.getGroupMembers());
            mRV.setLayoutManager(new LinearLayoutManager(mContext));
            mRV.setAdapter(adapter);
        }

        return mView;
    }

    public void onStart() {
        super.onStart();

    }

    public void onResume() {
        super.onResume();
        refreshView();
        if (getActivity() != null)
            ((HomeActivity) getActivity()).addSendUserLocationResponseToOtherClassListener(getClass(), mSendUserLocationResponseToOtherClass);
    }

    public void onStop() {
        super.onStop();
        if (getActivity() != null)
            ((HomeActivity) getActivity()).removeSendUserLocationResponseToOtherClassListener(getClass());
    }

    private void refreshView() {
        Log.e("OKOK", "refreshView");
        groupCreater = null;
        if (groupData == null) return;
        if (groupData.getGroupMembers() == null) return;
        String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
        for (GroupMember gm : groupData.getGroupMembers()) {
            Log.e(TAG, gm.getRole() + " " + gm.getName());
            if (gm.getRole().equals("admin")) {
                creatorInfo = gm;
                groupCreater = gm.getName();
                groupCreaterDeviceId = gm.getDeviceId();

                if (myDeviceId.trim().equals(groupCreaterDeviceId.trim()))
                    isCreater = true;
                break;
            }
        }
        for (GroupMember gm : groupData.getGroupMembers()) {
            Log.e(TAG, gm.getRole() + " " + gm.getName());
            if (gm.getDeviceId().equals(myDeviceId)) {
                userInfo = gm;
                break;
            }
        }

        groupKey = groupData.getGroupInfos()[0].getKey();

        boolean hasData = false;
        String meetingTime_ = groupData.getGroupInfos()[0].getMeetingTime();
        String endTime = groupData.getGroupInfos()[0].getEndTimestamp();
        String noticeTime_ = groupData.getGroupInfos()[0].getNoticeTime();

        //up date creator info
        TextView groupKeyHint = mView.findViewById(R.id.group_key);
        String tmp = getString(R.string.group_key) + groupKey;
        groupKeyHint.setText(tmp);

        //don't update creator info
        if (groupCreater != null) {
            View creator_info_view = mView.findViewById(R.id.group_member_creater);
            creator_info_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGroupMapPage(creatorInfo, null);
                }
            });
            TextView creatorName = creator_info_view.findViewById(R.id.group_members_list_item_tv_name);
            if (isCreater) {
                tmp = creatorInfo.getName() + " (" + getString(R.string.string_you) + ")";
                creatorName.setText(tmp);
            } else {
                tmp = creatorInfo.getName();
                creatorName.setText(tmp);
            }
            TextView creatorFloor = creator_info_view.findViewById(R.id.group_members_list_item_tv_floor_number);
            ImageView creatorPhone = creator_info_view.findViewById(R.id.group_members_list_item_iv_tel);
            boolean creatorHasPhone = false;
            if (creatorInfo.getTel() != null) if (creatorInfo.getTel().length() > 0)
                creatorHasPhone = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (isCreater) {
                    //modify phone
//                    creatorPhone.setImageResource(R.mipmap.button_phone_edit);
                    creatorPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showModifyPhoneDialog();
                        }
                    });
                } else {
                    if (creatorHasPhone) {
                        creatorPhone.setTag(creatorInfo.getTel());
                        creatorPhone.setVisibility(View.VISIBLE);
                        creatorPhone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v.getTag() != null) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", (String) v.getTag(), null));
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {
                        creatorPhone.setTag(null);
                        creatorPhone.setVisibility(View.GONE);
                    }
                }
        }

        //meeting time
        hasData = false;
        TextView meetingime = mView.findViewById(R.id.group_member_meeting_time);
        if (meetingTime_ != null) {
            if (meetingTime_.length() > 0) {
                hasData = true;
            } else {

            }
        }
        if (hasData) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date d = sdf.parse(meetingTime_);
                String cTime = d.getHours() + ":" + String.format("%1$02d", d.getMinutes());
                meetingime.setText(cTime);
                mView.findViewById(R.id.meeting_time_layout).setVisibility(View.VISIBLE);
                meetingime.setVisibility(View.VISIBLE);

                hasData = false;
                TextView noticeTime = mView.findViewById(R.id.group_member_notice_time);
                Log.e(TAG, "noticeTime:" + noticeTime_);
                if (noticeTime_ != null) if (noticeTime_.length() > 0)
                    hasData = true;
                if (hasData) {
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();
                    Date d1 = sdf.parse(meetingTime_);
                    c1.setTime(d1);
                    Date d2 = sdf.parse(noticeTime_);
                    c2.setTime(d2);
                    long difference = c1.getTimeInMillis() - c2.getTimeInMillis();
                    int notice_time = (int) (difference / (60 * 1000));
                    if (notice_time > 30)
                        notice_time = 30;
                    String t = notice_time + getString(R.string.dialog_hint_create_group_meeting_notice_hint) + getString(R.string.string_notice);
                    noticeTime.setText(t);
                    noticeTime.setVisibility(View.VISIBLE);
                } else {
                    noticeTime.setVisibility(View.INVISIBLE);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            mView.findViewById(R.id.meeting_time_layout).setVisibility(View.GONE);
        }

        //collect place
//        final TextView collectionPlaceFloor = mView.findViewById(R.id.collection_place_more_info);
        Log.e("OKOK", "groupCollectionPID" + groupCollectionPID);
        if (groupCollectionPID == null || groupCollectionPID.length() == 0) {
            collectionPlace.setText(null);
            collectionPlace.setVisibility(View.INVISIBLE);
            navigation.setVisibility(View.INVISIBLE);
//            collectionPlaceFloor.setText(null);
//            collectionPlaceFloor.setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.meeting_place_layout).setVisibility(View.GONE);
        } else {
            //find POI name
            if (groupCollectionPOI == null) {
                Log.e("OKOK", "groupCollectionPOI111");
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null) return;
//                        List<POI> poiList = DataCacheManager.getInstance().getPOIs();
//                        String p_type = "";
//                        for (POI p : poiList) {
//                            switch (p.getType()) {
//                                default:
//                                    p_type = "poi";
//                                    break;
//                            }

                        Map<String, BuildingFloor[]> buildingFloorsMap =
                                DataCacheManager.getInstance().getAllBuildingFloorsMap(getActivity());

                        if (buildingFloorsMap != null) {
                            for (String blId : buildingFloorsMap.keySet()) {
                                BuildingFloor[] floors = buildingFloorsMap.get(blId);
                                for (BuildingFloor floor : floors) {
                                    for (POI p : floor.getPois()) {
                                        if (String.valueOf(p.getId()).equals(groupCollectionPID)) {
                                            groupCollectionPOI = p;
                                            if (targetPOI == null)
                                                targetPOI = p;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
//                            }
//                        }

                        if (groupCollectionPOI != null) {
                            final String tmp = groupCollectionPOI.getName();

                            collectionPlace.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("OKOK", "groupCollectionPOI.getName()" + tmp);
                                    collectionPlace.setText(tmp);
                                    collectionPlace.setVisibility(View.VISIBLE);
                                    navigation.setVisibility(View.VISIBLE);
//                                    collectionPlaceFloor.setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.meeting_place_layout).setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                };
                if (getActivity() != null) ((HomeActivity) getActivity()).taskToMISC(r);
            } else {
                Log.e("OKOK", "groupCollectionPOI.getName()" + groupCollectionPOI.getName());
                collectionPlace.setText(groupCollectionPOI.getName());
                collectionPlace.setVisibility(View.VISIBLE);
                mView.findViewById(R.id.meeting_place_layout).setVisibility(View.VISIBLE);
            }
        }

        //member count
        TextView memberCnt = mView.findViewById(R.id.group_member_count);
        tmp = getString(R.string.string_member) + " " + (groupData.getGroupMembers().length - 1);
        memberCnt.setText(tmp);
        GroupMembersAdapter adapter = (GroupMembersAdapter) mRV.getAdapter();
        adapter.updateList(groupData.getGroupMembers());
    }

    int collect_interval = 10;
    int notice_time, notice_time_min, notice_time_max;

    private void initEditView() {
        targetPOI = groupCollectionPOI;
        final String meetingTime_ = groupData.getGroupInfos()[0].getMeetingTime();
        String endTime = groupData.getGroupInfos()[0].getEndTimestamp();
        final String noticeTime_ = groupData.getGroupInfos()[0].getNoticeTime();

        Log.e(TAG, "meetingTime:" + meetingTime_ + " endTime:" + endTime + " noticeTime:" + noticeTime_);
        final ImageView clean_meeting_setting = mView.findViewById(R.id.clean_setting);
        final TextView edit_place_btn = editLayout.findViewById(R.id.edit_meeting_place_setting_btn);
        edit_place_btn.setText(R.string.string_select_please);
        final MaterialButton edit = editLayout.findViewById(R.id.edit_meeting_function);
        final TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
        final ImageView notice_time_add = mView.findViewById(R.id.notice_increase_time);
        final ImageView notice_time_reduce = mView.findViewById(R.id.notice_decrease_time);
        notice_time_add.setEnabled(false);
        notice_time_reduce.setEnabled(false);
        final TextView meetingTime = editLayout.findViewById(R.id.edit_meeting_time_info);
        meetingTime.setText(R.string.string_select_please);
        if (meetingTime_ != null) {
            if (meetingTime_.length() > 0) {
                final Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date d = sdf.parse(meetingTime_);
                    String cTime = d.getHours() + ":" + String.format("%1$02d", d.getMinutes());
                    Log.e(TAG, "cTime:" + cTime);
                    meetingTime.setText(cTime);

                    TextView noticeTime = editLayout.findViewById(R.id.notice_time_info);
                    noticeTime.setText("0");
                    if (noticeTime_ == null) {

                    } else {
                        if (noticeTime_.length() == 0) {

                        } else {
                            Calendar c1 = Calendar.getInstance();
                            Calendar c2 = Calendar.getInstance();
                            try {
                                Date d1 = sdf.parse(meetingTime_);
                                c1.setTime(d1);
                                Date d2 = sdf.parse(noticeTime_);
                                c2.setTime(d2);
                                long difference = c1.getTimeInMillis() - c2.getTimeInMillis();
                                notice_time = (int) (difference / (60 * 1000));


                                int range = (d.getHours() - c.get(Calendar.HOUR_OF_DAY)) * 60 + d.getMinutes() - c.get(Calendar.MINUTE);

                                if (range >= 30) {
                                    notice_time_min = 5;
                                    notice_time_max = 30;
                                } else if (range >= 5) {
                                    notice_time_min = 5;
                                    notice_time_max = (range / notice_interval) * 5;
                                } else {
                                    notice_time_min = 0;
                                    notice_time_max = 0;
                                    notice_time = 0;
                                }

                                if (notice_time <= notice_time_min) {
                                    notice_time_add.setEnabled(true);
                                    notice_time_reduce.setEnabled(false);
                                } else if (notice_time > notice_time_min && notice_time < notice_time_max) {
                                    notice_time_add.setEnabled(true);
                                    notice_time_reduce.setEnabled(true);
                                } else if (notice_time == notice_time_max) {
                                    notice_time_add.setEnabled(false);
                                    notice_time_reduce.setEnabled(true);
                                }
                                if (notice_time > notice_time_max) {
                                    notice_time = notice_time_max;
                                } else if (notice_time < notice_time_min && notice_time > 0)
                                    notice_time = notice_time_min;

                                noticeTime.setText("" + notice_time);

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == edit_place_btn.getId()) {
//                    if (isVisible()) if (getActivity() != null)
//                        ((HomeActivity) getActivity()).openFragmentPage(GroupSelectCollectionPlaceMapFragment.newInstance(GroupMembersTestFragment.this));

                    Intent gatherIntent = new Intent();
                    gatherIntent.setClass(getActivity(), NaviSDKActivity.class);
                    gatherIntent.putExtra(COLLECTION_PLACE, true);
                    startActivityForResult(gatherIntent, REQUEST_CODE);
                } else if (v.getId() == meetingTime.getId()) {
                    showMeetingTimeSettingDialog();
                } else if (v.getId() == clean_meeting_setting.getId()) {
                    edit_place_btn.setText(R.string.string_select_please);
//                    edit_place_floor.setText(null);
//                    edit_place_floor.setVisibility(View.GONE);
                    targetPOI = null;
                    meetingTime.setText(R.string.string_select_please);
                    TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
                    notice_time_info.setText("" + 0);
                    notice_time = 0;
                    notice_time_min = 0;
                    notice_time_max = 0;
                    final ImageView notice_time_add = mView.findViewById(R.id.notice_increase_time);
                    final ImageView notice_time_reduce = mView.findViewById(R.id.notice_decrease_time);
                    notice_time_add.setEnabled(false);
                    notice_time_reduce.setEnabled(false);
                    detectMeetingDataIsDifferent();
                } else if (v.getId() == notice_time_add.getId()) {
                    notice_time += 5;
                    if (notice_time == notice_time_min) {
                        notice_time_add.setEnabled(true);
                        notice_time_reduce.setEnabled(false);
                    } else if (notice_time > notice_time_min && notice_time < notice_time_max) {
                        notice_time_add.setEnabled(true);
                        notice_time_reduce.setEnabled(true);
                    } else if (notice_time == notice_time_max) {
                        notice_time_add.setEnabled(false);
                        notice_time_reduce.setEnabled(true);
                    }
                    if (notice_time > notice_time_max)
                        notice_time = notice_time_max;
                    notice_time_info.setText("" + notice_time);

                    detectMeetingDataIsDifferent();
                } else if (v.getId() == notice_time_reduce.getId()) {
                    notice_time -= 5;
                    if (notice_time == notice_time_min) {
                        notice_time_add.setEnabled(true);
                        notice_time_reduce.setEnabled(false);
                    } else if (notice_time > notice_time_min && notice_time < notice_time_max) {
                        notice_time_add.setEnabled(true);
                        notice_time_reduce.setEnabled(true);
                    } else if (notice_time == notice_time_max) {
                        notice_time_add.setEnabled(false);
                        notice_time_reduce.setEnabled(true);
                    }
                    if (notice_time < notice_time_min)
                        notice_time = notice_time_min;
                    notice_time_info.setText("" + notice_time);

                    detectMeetingDataIsDifferent();
                } else if (v.getId() == edit.getId()) {
                    //updateGroup(Context context, @NonNull String key, @NonNull String enabled, @Nullable boolean del, @Nullable String title, @Nullable String hour, @Nullable String tel, @Nullable String noticeTime, @Nullable String collection_time, @Nullable String ap_id, @Nullable String loginToken,
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    DateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
                    String meeting_time = "false";//default format is not use
                    String notice_time_ = "false";//default format is not use
                    String ap_id = "false";//default format is not use
                    Calendar tmp = Calendar.getInstance();
                    if (!meetingTime.getText().toString().equals(select_hint)) {
                        String[] meeting_time_string = meetingTime.getText().toString().split(":");
                        tmp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(meeting_time_string[0]));
                        tmp.set(Calendar.MINUTE, Integer.parseInt(meeting_time_string[1]));
                        try {
                            String date_ = "" + String.format("%04d", tmp.get(Calendar.YEAR)) + String.format("%2d", tmp.get(Calendar.MONTH) + 1) + String.format("%02d", tmp.get(Calendar.DAY_OF_MONTH)) +
                                    " " + String.format("%02d", tmp.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", tmp.get(Calendar.MINUTE)) + "00";
                            meeting_time = dateFormatter.format(df.parse(date_));
                            Log.e(TAG, "meeting_time:" + meeting_time + " meeting_timeH:" + meeting_time_string[0] + " meeting_timeM:" + meeting_time_string[1] + "\n" + date_);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (notice_time > 0) {
                        int before = notice_time;
                        tmp.add(Calendar.MINUTE, -before);
                        try {
                            String date_ = "" + String.format("%04d", tmp.get(Calendar.YEAR)) + String.format("%2d", tmp.get(Calendar.MONTH) + 1) + String.format("%02d", tmp.get(Calendar.DAY_OF_MONTH)) +
                                    " " + String.format("%02d", tmp.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", tmp.get(Calendar.MINUTE)) + "00";
                            notice_time_ = dateFormatter.format(df.parse(date_));
                            Log.e(TAG, "meeting_time:" + meeting_time + "\t" + date_);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    String p_id = "";
                    String p_type = "";
                    if (targetPOI != null) {
                        p_id = String.valueOf(targetPOI.getId());
                        switch (targetPOI.getType()) {
                            default:
                                p_type = "poi";
                                break;
                        }
                    }
                    mView.findViewById(R.id.group_member_view_loading).setVisibility(View.VISIBLE);
                    TpApi.getInstance().updateGroup(mContext, groupKey, "Y", false, null, null, null,
                            notice_time_, meeting_time, p_type, p_id, "",
                            new NetworkManager.NetworkManagerListener<UpdateGroupResponse>() {
                                @Override
                                public void onSucceed(UpdateGroupResponse response) {
                                    mView.findViewById(R.id.group_member_view_loading).setVisibility(View.GONE);
//                                    TextView collectionPlaceFloor = mView.findViewById(R.id.collection_place_more_info);
                                    if (targetPOI != null) {
                                        collectionPlace.setText(targetPOI.getName());
                                        navigation.setVisibility(View.VISIBLE);
//                                        collectionPlaceFloor.setVisibility(View.VISIBLE);
                                    } else {
                                        collectionPlace.setText(null);
                                        navigation.setVisibility(View.INVISIBLE);
//                                        collectionPlaceFloor.setVisibility(View.GONE);
//                                        collectionPlaceFloor.setText(null);
                                    }
                                    if (getActivity() != null) if (isVisible()) {
                                        DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.setting_success);
                                        final FrameLayout editFL = mView.findViewById(R.id.detail_action_bar_fl_edit);
                                        editLayout.setVisibility(View.GONE);
                                        editViewIsShown = false;
                                        editFL.setVisibility(View.VISIBLE);
                                    }
                                    groupCollectionPOI = targetPOI;
                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                    DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.error_code_timeout);
                                }

                            });
                }
            }
        };

        Log.e(TAG, "-------------------:" + groupCollectionPID);
        if (groupCollectionPID == null || groupCollectionPID.length() == 0) {
            edit_place_btn.setOnClickListener(mOnClickListener);
        } else {
            //find POI name
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
//                    Map<String, BuildingFloors> buildingFloorsMap = DataCacheManager.getInstance().getAllBuildingFloorsMap();
//
//                    if (buildingFloorsMap != null) {
//                        for (String blId : buildingFloorsMap.keySet()) {
//                            BuildingFloors floors = buildingFloorsMap.get(blId);
//                            for (BuildingFloor floor : floors.getData()) {
                    List<POI> poiList = DataCacheManager.getInstance().getPOIs();
                    String p_type = "";
//                                for (BuildingFloor floor : floors.getData()) {
                    for (POI p : poiList) {
                        switch (p.getType()) {
                            default:
                                p_type = "poi";
                                break;
                        }
                        if (String.valueOf(p.getId()).equals(groupCollectionPID) && p_type.equals(groupCollectionPType)) {
                            groupCollectionPOI = p;
                            if (targetPOI == null)
                                targetPOI = p;
                            break;
                        }
                    }
//                                if (groupCollectionPOI != null) break;
//                            }
//                            if (groupCollectionPOI != null) break;
//                        }
//                    }
                    if (groupCollectionPOI != null) {
                        final String tmp = groupCollectionPOI.getName();

                        edit_place_btn.post(new Runnable() {
                            @Override
                            public void run() {
                                edit_place_btn.setText(tmp);
//                                edit_place_floor.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    edit_place_btn.setOnClickListener(mOnClickListener);
                }
            };
            if (getActivity() != null) ((HomeActivity) getActivity()).taskToMISC(r);
        }


        notice_time_add.setOnClickListener(mOnClickListener);
        notice_time_reduce.setOnClickListener(mOnClickListener);
        clean_meeting_setting.setOnClickListener(mOnClickListener);
        meetingTime.setOnClickListener(mOnClickListener);
        edit.setOnClickListener(mOnClickListener);
    }

    public void showMeetingTimeSettingDialog() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        final Dialog req = adb.setView(new View(getActivity())).create();

        req.show();
        /*
         * make sure soft keyboard showing in AlertDialog Builder
         */
        req.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        req.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //for make custom dialog with rounded corners in android
        //set the background of your dialog to transparent
        req.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        req.setContentView(R.layout.h_m_picker_dialog);

        final NumberPicker collectionTimeH = req.findViewById(R.id.meeting_time_hour);
        final NumberPicker collectionTimeM = req.findViewById(R.id.meeting_time_minute);
        Calendar c = Calendar.getInstance();
        //Does the building open? set to opening time if device time smaller than opening time
        boolean openTime = true;
        if (c.get(Calendar.HOUR_OF_DAY) < 9) {
            c.set(Calendar.HOUR_OF_DAY, 9);
            c.set(Calendar.MINUTE, 0);
            openTime = false;
        }

        if (openTime) {
            int t = c.get(Calendar.MINUTE) / 10;
/*            if (c.get(Calendar.MINUTE) % 10 > 0) {
                c.set(Calendar.MINUTE, t * 10);
                c.add(Calendar.MINUTE, 20);
            } else {
                c.set(Calendar.MINUTE, t * 10);
                c.add(Calendar.MINUTE, 10);
            }*/
            c.set(Calendar.MINUTE, t * 10);
            c.add(Calendar.MINUTE, 10);
        }
        collectionTimeH.setMinValue(c.get(Calendar.HOUR_OF_DAY));
        collectionTimeH.setMaxValue(24);
/*            collectionTimeM.setMinValue(c.get(Calendar.MINUTE) + 1);
            collectionTimeM.setMaxValue(59);*/
        final ArrayList<String> collectionTimes = new ArrayList<>();


        for (int i = c.get(Calendar.MINUTE); i < 60; i += collect_interval) {
            collectionTimes.add("" + i);
        }
        String[] tmp0 = new String[1];
        if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 24) {
            tmp0 = collectionTimes.toArray(new String[0]);
        } else {
            tmp0[0] = "0";
        }
        collectionTimeM.setDisplayedValues(null);
        collectionTimeM.setMinValue(0);
        collectionTimeM.setMaxValue(tmp0.length - 1);
        collectionTimeM.setDisplayedValues(tmp0);
        NumberPicker.OnValueChangeListener mOnValueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Calendar c = Calendar.getInstance();
                //Does the building open? set to opening time if device time smaller than opening time
                boolean openTime = false;
                if (c.get(Calendar.HOUR_OF_DAY) < 9) {
                    c.set(Calendar.HOUR_OF_DAY, 9);
                    c.set(Calendar.MINUTE, 0);
                    openTime = true;
                }

                if (!openTime) {
                    int t = c.get(Calendar.MINUTE) / 10;
                    if (c.get(Calendar.MINUTE) % 10 > 0) {
                        c.set(Calendar.MINUTE, t * 10);
                        c.add(Calendar.MINUTE, 20);
                    } else {
                        c.set(Calendar.MINUTE, t * 10);
                        c.add(Calendar.MINUTE, 10);
                    }
                }

                if (picker.getId() == collectionTimeH.getId()) {
                    //update collection time range
                    Log.e(TAG, "---------------:" + collectionTimeH.getValue() + "\t" + collectionTimeH.getMinValue());

                    if (collectionTimeH.getValue() == collectionTimeH.getMinValue()) {
                        //make sure the collection time can not smaller than current time
                        final ArrayList<String> collectionTimes = new ArrayList<>();
                        for (int i = c.get(Calendar.MINUTE); i < 60; i += collect_interval) {
                            collectionTimes.add("" + i);
                        }
                        String[] tmp0 = collectionTimes.toArray(new String[0]);
                        collectionTimeM.setDisplayedValues(null);
                        collectionTimeM.setMinValue(0);
                        collectionTimeM.setMaxValue(tmp0.length - 1);
                        collectionTimeM.setDisplayedValues(tmp0);
                    } else if (collectionTimeH.getValue() == collectionTimeH.getMaxValue()) {
                        //make sure the collection time can not bigger than close time
                        collectionTimeM.setDisplayedValues(null);
                        collectionTimeM.setMinValue(0);
                        collectionTimeM.setMaxValue(0);
                        String tmp[] = {"0"};
                        collectionTimeM.setDisplayedValues(tmp);
                    } else {
                        final ArrayList<String> collectionTimes = new ArrayList<>();
                        for (int i = 0; i < 60; i += collect_interval) {
                            collectionTimes.add("" + i);
                        }
                        String[] tmp0 = collectionTimes.toArray(new String[0]);
                        collectionTimeM.setDisplayedValues(null);
                        collectionTimeM.setMinValue(0);
                        collectionTimeM.setMaxValue(tmp0.length - 1);
                        collectionTimeM.setDisplayedValues(tmp0);
                    }
                }
            }
        };

        collectionTimeH.setOnValueChangedListener(mOnValueChangeListener);
        collectionTimeM.setOnValueChangedListener(mOnValueChangeListener);


        TextView okBtn = req.findViewById(R.id.ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                TextView meetingTime = editLayout.findViewById(R.id.edit_meeting_time_info);
                String info = collectionTimeH.getValue() + ":" + String.format("%1$02d", Integer.parseInt(collectionTimeM.getDisplayedValues()[collectionTimeM.getValue()]));
                meetingTime.setText(info);

                //update notice time range
                ImageView notice_time_add = editLayout.findViewById(R.id.notice_increase_time);
                ImageView notice_time_reduce = editLayout.findViewById(R.id.notice_decrease_time);

                Calendar c = Calendar.getInstance();
                //Does the building open? set to opening time if device time smaller than opening time
                if (c.get(Calendar.HOUR_OF_DAY) < 9) {
                    c.set(Calendar.HOUR_OF_DAY, 9);
                    c.set(Calendar.MINUTE, 0);
                }
                String[] mValues = collectionTimeM.getDisplayedValues();
                int range = (collectionTimeH.getValue() - c.get(Calendar.HOUR_OF_DAY)) * 60 + Integer.parseInt(mValues[collectionTimeM.getValue()]) - c.get(Calendar.MINUTE);

                if (range >= 30) {
                    notice_time_min = 5;
                    notice_time_max = 30;
                } else if (range >= 5) {
                    notice_time_min = 5;
                    notice_time_max = (range / notice_interval) * 5;
                } else {
                    notice_time_min = 0;
                    notice_time_max = 0;
                    notice_time = 0;
                }
                Log.e(TAG, "range:" + range + " notice_time:" + notice_time + " notice_time_min:" + notice_time_min + " notice_time_max:" + notice_time_max);
                if (notice_time <= notice_time_min) {
                    notice_time_add.setEnabled(true);
                    notice_time_reduce.setEnabled(false);
                } else if (notice_time > notice_time_min && notice_time < notice_time_max) {
                    notice_time_add.setEnabled(true);
                    notice_time_reduce.setEnabled(true);
                } else if (notice_time == notice_time_max) {
                    notice_time_add.setEnabled(false);
                    notice_time_reduce.setEnabled(true);
                }
                if (notice_time > notice_time_max) {
                    notice_time = notice_time_max;
                } else if (notice_time < notice_time_min && notice_time > 0)
                    notice_time = notice_time_min;
                TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
                notice_time_info.setText("" + notice_time);

                //data change?
                detectMeetingDataIsDifferent();
                req.dismiss();
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        req.show();
        //req.getWindow().setAttributes(lp);
        req.getWindow().setLayout((7 * width) / 7, (5 * height) / 5);

    }

    HomeActivity.SendUserLocationResponseToOtherClass mSendUserLocationResponseToOtherClass = new HomeActivity.SendUserLocationResponseToOtherClass() {
        @Override
        public void success(SendUserLocationResponse response) {
            groupData = null;
            List<GroupData> groupDatas = response.getData();
            for (GroupData t : groupDatas) {
                if (t.getGroupInfos()[0].getKey().equals(groupKey)) {
                    groupData = t;
 /*                   GroupMembersAdapter adapter = new GroupMembersAdapter(groupData.getGroupMembers());
                    mRV.setLayoutManager(new LinearLayoutManager(mContext));
                    mRV.setAdapter(adapter);*/
/*                    GroupMembersAdapter adapter = (GroupMembersAdapter) mRV.getAdapter();
                    adapter.mData = t.getGroupMembers();
                    adapter.notifyDataSetChanged();*/
                    refreshView();
                }
            }

            //group may disband
            if (groupData == null) {
                showGroupDisbandDialog();
            }
        }

        @Override
        public void fail(VolleyError error, boolean shouldRetry) {

        }
    };

    POI targetPOI;

    public void updateCollectionPlaceData(POI target) {
        Log.e("OKOK", "updateCollectionPlaceData");
        if (!isCreater) return;
        if (targetPOI == null) {
            targetPOI = target;
        } else {
            if (!String.valueOf(targetPOI.getId()).equals(target)) {
                targetPOI = target;
            } else {
                //don't need to update
                return;
            }
        }
        groupCollectionPOI = target;
        groupCollectionPID = String.valueOf(targetPOI.getId());
        final String tmp = /*getString(R.string.dialog_hint_create_group_collection_place) + */targetPOI.getName();
        Log.e("OKOK", "tmp" + tmp);
        collectionPlace.setText(tmp);
//        final TextView collectionPlaceFloor = mView.findViewById(R.id.collection_place_more_info);
        //detectCollectionChanged();
        final TextView edit_place_btn = editLayout.findViewById(R.id.edit_meeting_place_setting_btn);
//        final TextView edit_place_floor = editLayout.findViewById(R.id.edit_meeting_place_more_info);
        edit_place_btn.setText(targetPOI.getName());
//        edit_place_floor.setVisibility(View.VISIBLE);

        //does the setting change?
        detectMeetingDataIsDifferent();

    }

    private void detectMeetingDataIsDifferent() {
        if (groupData == null) return;
        if (groupData.getGroupInfos() == null) return;
        if (groupData.getGroupInfos().length == 0) return;

        //does the setting change?
        final String meetingTime_ = groupData.getGroupInfos()[0].getMeetingTime();
        String endTime = groupData.getGroupInfos()[0].getEndTimestamp();
        final TextView meetingTime = editLayout.findViewById(R.id.edit_meeting_time_info);
        final String noticeTime_ = groupData.getGroupInfos()[0].getNoticeTime();
        MaterialButton edit = editLayout.findViewById(R.id.edit_meeting_function);
        boolean change = false;
        if (groupCollectionPID == null || groupCollectionPID.length() == 0) {
            if (targetPOI != null)
                change = true;
        } else {
            TextView edit_place_btn = editLayout.findViewById(R.id.edit_meeting_place_setting_btn);
            if (edit_place_btn.getText().toString().equals(getString(R.string.string_select_please)))
                change = true;
            if (groupCollectionPOI != null)
                if (targetPOI != null) if (!groupCollectionPOI.equals(targetPOI))
                    change = true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Does the meeting time changed?
        if (!change)
            if (!meetingTime.getText().equals(select_hint)) {
                if (meetingTime_ != null) {
                    if (meetingTime_.length() > 0) {
                        try {
                            Date d = sdf.parse(meetingTime_);
                            String mTime = d.getHours() + ":" + String.format("%1$02d", d.getMinutes());
                            String[] tmp1 = meetingTime.getText().toString().split(":");
                            Log.e(TAG, "detectMeetingDataIsDifferent:" + mTime + " " + meetingTime.getText().toString());
                            if (!mTime.equals(meetingTime.getText().toString())) {
                                change = true;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        change = true;
                    }
                } else {
                    change = true;
                }
            } else {
                if (meetingTime_ != null) if (meetingTime_.length() > 0)
                    change = true;
            }

        //Does the notice time changed?
        if (!change) {
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            try {
                Date d1 = sdf.parse(meetingTime_);
                c1.setTime(d1);
                Date d2 = sdf.parse(noticeTime_);
                c2.setTime(d2);
                long difference = c1.getTimeInMillis() - c2.getTimeInMillis();
                int notice_time_ = (int) (difference / (60 * 1000));
                Log.e(TAG, "detectMeetingDataIsDifferent:" + notice_time_ + " " + noticeTime);
                if (notice_time != notice_time_) {
                    change = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        edit.setEnabled(change);
    }

    private void detectCollectionChanged() {
        Log.e(TAG, groupCollectionPID + " " + (targetPOI != null));
        boolean changed = false;
        if (groupCollectionPID == null) if (targetPOI != null)
            changed = true;
        if (groupCollectionPID.length() == 0) if (targetPOI != null)
            changed = true;
        if (!changed)
            if (targetPOI != null && groupCollectionPOI != null)
                if (!String.valueOf(targetPOI.getId()).equals(groupCollectionPOI.getId())) {
                    changed = true;
                }
        if (!changed) {
            String collectionTime_ = groupData.getGroupInfos()[0].getMeetingTime();
            String endTime = groupData.getGroupInfos()[0].getEndTimestamp();
            //end time date format should be yyyy-MM-dd HH:mm:ss
            String[] tmp = endTime.split(" ");
            String[] tmp1 = tmp[1].split(":");
            final String h_e = tmp1[0];
            final String m_e = tmp1[1];
            //collection time date format should be yyyy-MM-dd HH:mm:ss
            if (collectionTime_ != null) {
                if (collectionTime_.length() > 0) {
                    tmp = collectionTime_.split(" ");
                    tmp1 = tmp[1].split(":");
                    final String h_c = tmp1[0];
                    final String m_c = tmp1[1];
                    //is reset button need to enable?
                    if (!("" + collectionTimeH.getValue()).equals(h_c) || !("" + collectionTimeM.getValue()).equals(m_c)) {
                        changed = true;
                    }
                } else {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }
        if (changed) {
            reset.post(new Runnable() {
                @Override
                public void run() {
                    reset.setEnabled(true);
                }
            });
        } else {
            reset.post(new Runnable() {
                @Override
                public void run() {
                    reset.setEnabled(false);
                }
            });
        }
    }

    class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.GroupMembersViewHolder> {

        private GroupMember[] mData;

        public GroupMembersAdapter(GroupMember[] groupMembers) {
            //set the device to top
            ArrayList<GroupMember> tmp = new ArrayList<>();
            if (groupMembers.length > 0) {
                //find the device
                GroupMember user = null;
                String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                for (GroupMember gm : groupMembers) {
                    String device_ = gm.getDeviceId();
                    if (device_.equals(groupCreaterDeviceId))
                        continue;
                    if (device_.equals(myDeviceId)) {
                        user = gm;
                    } else {
                        tmp.add(gm);
                    }
                }
                if (user != null)
                    tmp.add(0, user);
            }
            mData = tmp.toArray(new GroupMember[0]);
        }

        public void updateList(GroupMember[] groupMembers) {
            //set the device to top
            ArrayList<GroupMember> tmp = new ArrayList<>();
            if (groupMembers.length > 0) {
                //find the device
                GroupMember user = null;
                String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                for (GroupMember gm : groupMembers) {
                    String device_ = gm.getDeviceId();
                    if (device_.equals(groupCreaterDeviceId))
                        continue;
                    if (device_.equals(myDeviceId)) {
                        user = gm;
                    } else {
                        tmp.add(gm);
                    }
                }
                if (user != null)
                    tmp.add(0, user);
            }
            mData = tmp.toArray(new GroupMember[0]);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mData.length;
        }

        @Override
        public GroupMembersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.group_members_list_item2, null);

            GroupMembersViewHolder viewHolder = new GroupMembersViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(GroupMembersViewHolder holder, int position) {
            GroupMember groupMember = mData[position];
            holder.itemView.setTag(position);

            if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                boolean you = mData[position].getDeviceId().equals(myDeviceId);
                if (you) {
                    holder.tel.setTag(null);
//                    holder.tel.setImageResource(R.mipmap.button_phone_edit);
                    //holder.tel.setVisibility(View.INVISIBLE);
                    //modify phone
                    holder.tel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showModifyPhoneDialog();
                        }
                    });
                    //add hint
                    holder.nameTV.setText(groupMember.getName() + " (" + getString(R.string.string_you) + ")");
                } else {
                    if (mData[position].getTel() != null) {
                        if (mData[position].getTel().length() > 0) {
                            holder.tel.setTag(mData[position].getTel());
                            holder.tel.setImageResource(R.mipmap.button_phone);
                            holder.tel.setVisibility(View.VISIBLE);
                        } else {
                            holder.tel.setTag(null);
                            holder.tel.setVisibility(View.GONE);
                        }
                    } else {
                        holder.tel.setTag(null);
                        holder.tel.setVisibility(View.GONE);
                    }
                    holder.nameTV.setText(groupMember.getName());
                }
//                holder.nameTV.setTextColor(groupMember.getRole().equals(GroupMember.ADMIN) ? ContextCompat.getColor(mContext, R.color.blue_00) :
//                        ContextCompat.getColor(mContext, R.color.gray_99));
            } else {
                holder.tel.setVisibility(View.GONE);
            }

        }

        class GroupMembersViewHolder extends RecyclerView.ViewHolder {

            private TextView nameTV;
            private TextView floorNumberTV;
            private ImageView tel;

            private GroupMembersViewHolder(final View itemView) {
                super(itemView);

                nameTV = itemView.findViewById(R.id.group_members_list_item_tv_name);
                floorNumberTV = itemView.findViewById(R.id.group_members_list_item_tv_floor_number);
                tel = itemView.findViewById(R.id.group_members_list_item_iv_tel);
                View.OnClickListener mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getId() == itemView.getId()) {

                            //send to HomeActivity
                            //EventBus.getDefault().post(new OmniEvent(OmniEvent.OPEN_GROUP_MAP, ""));

                            //default floor plan, null mean user's floor and auto change floor
                            //non null means target floor plan and disable auto change floor
                            int position = (int) itemView.getTag();
/*                            String deviceID = mData[position].getDeviceId();
                            String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                            if (myDeviceId == null)
                                myDeviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                            if (deviceID.equals(myDeviceId)) {
                                openGroupMapPage(null, null);
                            } else {
                                openGroupMapPage(mData[position], null);
                            }*/
                            openGroupMapPage(mData, position, null);

                            //GroupMembersFragment.this.dismiss();
                            //getChildFragmentManager().beginTransaction().detach(GroupMembersFragment.this).commit();

                            //switchTab();
                        } else if (v.getId() == tel.getId()) {
                            //phone number should be saved in tag
                            if (v.getTag() != null) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", (String) v.getTag(), null));
                                startActivity(intent);
                            }
                        }
                    }
                };

                tel.setOnClickListener(mOnClickListener);
                itemView.setOnClickListener(mOnClickListener);
            }
        }
    }


    private void openGroupMapPage(GroupMember target, String collection_place) {
//        GroupMap fragment = new GroupMap();
//        Bundle bundle = new Bundle();
//        GroupData groupData = (GroupData) getArguments().getSerializable(ARG_KEY_GROUP_DATA);
//        bundle.putSerializable(ARG_KEY_GROUP_DATA, groupData);
//        if (collection_place != null) {
//            bundle.putString(COLLECTION_PLACE, collection_place);
//            bundle.putSerializable(COLLECTION_PLACE_POI, groupCollectionPOI);
//        }
//        if (target != null)
//            bundle.putSerializable(TARGET_MEMBER, target);
//        fragment.setArguments(bundle);
//        if (getActivity() != null) {
//            ((HomeActivity) getActivity()).openFragmentPage(fragment);
//        }
    }

    private void openGroupMapPage(GroupMember[] target, int position, String collection_place) {

//        GroupMap fragment = new GroupMap();
//        Bundle bundle = new Bundle();
//        GroupData groupData = (GroupData) getArguments().getSerializable(ARG_KEY_GROUP_DATA);
//        bundle.putSerializable(ARG_KEY_GROUP_DATA, groupData);
//        if (collection_place != null) {
//            bundle.putString(COLLECTION_PLACE, collection_place);
//            bundle.putSerializable(COLLECTION_PLACE_POI, groupCollectionPOI);
//        }
//        if (target != null) {
//            bundle.putSerializable(TARGET_MEMBER_ALL, target);
//            bundle.putSerializable(TARGET_MEMBER_POSITION, position);
//        }
//        fragment.setArguments(bundle);
//        if (getActivity() != null) {
//            ((HomeActivity) getActivity()).openFragmentPage(fragment);
//        }
    }

    public void showModifyPhoneDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final Dialog req = builder.setView(new View(getActivity())).create();

        req.show();
        /*
         * make sure soft keyboard showing in AlertDialog Builder
         */
        req.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        req.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //for make custom dialog with rounded corners in android
        //set the background of your dialog to transparent
        req.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        req.setContentView(R.layout.modify_phone_dialog_view);

        final TextInputLayout inputEditTextLayout = req.findViewById(R.id.modify_phone_til);
        final TextInputEditText inputEditText = req.findViewById(R.id.modify_phone_tiet);
        final MaterialButton modfiy = req.findViewById(R.id.modify_phone);
        final MaterialButton cancel = req.findViewById(R.id.cancel);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e(TAG, s + " start:" + start + " before:" + before + " count:" + count);
                if ((start == 4 || start == 8) && before == 1 && count == 0) {

                } else if ((start == 3 || start == 7) && before == 0 && count == 1) {
                    inputEditText.setText((inputEditText.getText().toString() + "-"));
                    int pos = inputEditText.getText().length();
                    inputEditText.setSelection(pos);
                } else if ((start == 4 || start == 8) && before == 0 && count == 1) {
                    String tmp = s.toString();
                    if (start == 4) {
                        int cnt = s.toString().split("-", -1).length - 1;
                        if (cnt == 0) {
                            tmp = s.toString().substring(0, 4) + "-" + s.toString().substring(4);
                        }
                    } else if (start == 8) {
                        int cnt = s.toString().split("-", -1).length - 1;
                        if (cnt == 0) {
                            tmp = s.toString().substring(0, 4) + "-" + s.toString().substring(4, 8) + "-" + s.toString().substring(8);
                        } else if (cnt == 1) {
                            tmp = s.toString().substring(0, 8) + "-" + s.toString().substring(8);
                        }
                    }
                    inputEditText.setText(tmp);
                    int pos = inputEditText.getText().length();
                    inputEditText.setSelection(pos);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (userInfo != null) if (userInfo.getTel() != null)
                        inputEditText.setHint(userInfo.getTel());
                } else {
                    ((HomeActivity) getActivity()).hideKeyboard(v.getWindowToken());
                }
            }
        });

        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == modfiy.getId()) {
                    final String phoneStr = inputEditText.getText().toString().trim();
                    boolean valid = Tools.getInstance().isValidMobileNumber(phoneStr, "TW");
                    Log.e(TAG, phoneStr + "\t" + valid);
                    if (TextUtils.isEmpty(phoneStr)) {
                        inputEditTextLayout.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.red));
                        inputEditTextLayout.setError(getString(R.string.input_error_required));
                    } else if (!valid) {
                        inputEditTextLayout.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.red));
                        inputEditTextLayout.setError(getString(R.string.input_error_wrong_phone_number_format));
                    } else {
                        ((HomeActivity) getActivity()).hideKeyboard(inputEditText.getWindowToken());
                        mView.findViewById(R.id.group_member_view_loading).setVisibility(View.VISIBLE);
                        TpApi.getInstance().updateGroup(mContext, groupKey, "Y", false, null, null, phoneStr, null, null, null, null, "",
                                new NetworkManager.NetworkManagerListener<UpdateGroupResponse>() {
                                    @Override
                                    public void onSucceed(UpdateGroupResponse response) {
                                        if (getActivity() != null) {
                                            DialogTools.Companion.getInstance().showHintDialog(getActivity(),
                                                    getString(R.string.string_phone_updated), phoneStr);
                                        }
                                        req.dismiss();
                                        mView.findViewById(R.id.group_member_view_loading).setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                        DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.error_code_timeout);
                                        req.dismiss();
                                        mView.findViewById(R.id.group_member_view_loading).setVisibility(View.GONE);
                                    }

                                });
                    }
                } else if (v.getId() == cancel.getId()) {
                    ((HomeActivity) getActivity()).hideKeyboard(inputEditText.getWindowToken());
                    req.dismiss();
                }
            }
        };
        modfiy.setOnClickListener(mOnClickListener);
        cancel.setOnClickListener(mOnClickListener);
        req.show();
    }

    public void showCustomLeaveGroupDialog() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        final Dialog req = adb.setView(new View(getActivity())).create();

        req.show();
        /*
         * make sure soft keyboard showing in AlertDialog Builder
         */
        req.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        req.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //for make custom dialog with rounded corners in android
        //set the background of your dialog to transparent
        req.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        req.setContentView(R.layout.custom_leave_group_warning_dialog);

        TextView titleTv = req.findViewById(R.id.rec_dialog_title);
        TextView contentTv = req.findViewById(R.id.rec_dialog_content);


        titleTv.setText(R.string.warning);
        contentTv.setVisibility(View.VISIBLE);
        if (isCreater) {
            contentTv.setText(R.string.leave_group_disband_text);
        } else {
            contentTv.setText(R.string.leave_group_warning_text);
        }


        TextView cancelBtn = req.findViewById(R.id.rw_collect_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req.dismiss();
            }
        });

        TextView okBtn = req.findViewById(R.id.rw_collect_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                //req.dismiss();
                if (isCreater) {
                    //delete the group
                    TpApi.getInstance().updateGroup(mContext, groupKey, "Y", true, null, null, null, null, null, null, null, "",
                            new NetworkManager.NetworkManagerListener<UpdateGroupResponse>() {
                                @Override
                                public void onSucceed(UpdateGroupResponse response) {
                                    //cancel notification of this group
                                    NotificationManager notificationManager = DataCacheManager.getInstance().getNotificationManager();
                                    notificationManager.cancel(groupId, Integer.parseInt(groupId));//MeetingFunctionChanged
                                    notificationManager.cancel(groupKey, Integer.parseInt(groupKey));//Notice
                                    //send group data be a reference to delete the group item in listview
                                    GroupData gd = (GroupData) getArguments().getSerializable(ARG_KEY_GROUP_DATA);
                                    EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_LEFT_GROUP, groupData));
                                    req.dismiss();


                                    //cancel alarm
                                    String noticeTime = groupData.getGroupInfos()[0].getNoticeTime();
                                    if (noticeTime != null) if (noticeTime.length() > 0) {
                                        String key = groupData.getGroupInfos()[0].getKey();
                                        GroupInfo gi = gd.getGroupInfos()[0];
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date nt = null;
                                        try {
                                            nt = dateFormatter.parse(noticeTime);
                                            //to cancel alarm, consider group key and notice time only
                                            CreateGroupCollectionJsonObject c = new CreateGroupCollectionJsonObject();
                                            c.setGroupKey(key);
                                            c.setCollectionNoticeTime(nt.getTime());

                                            Calendar cal = Calendar.getInstance();
                                            cal.setTimeInMillis(c.getCollectionNoticeTime());
                                            MyAlarmManager mAM = new MyAlarmManager(getContext());
                                            mAM.cancelAlarm(c, MyAlarmManager.FIND_FRIEND_NOTICE);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    //save leave group data, the data will delete while the available time is expired
                                    SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(getContext());
                                    String oldLeftGroupData = s.getString(KEY_LEFT_GROUP_ENDTIME, "");
                                    GroupData[] oldLeftGroups = null;
                                    if (oldLeftGroupData.length() > 0)
                                        oldLeftGroups = NetworkManager.Companion.getInstance().getGson().fromJson(oldLeftGroupData, GroupData[].class);
                                    ArrayList<GroupData> tmps = new ArrayList<>();
                                    if (oldLeftGroups != null)
                                        for (GroupData tmp : oldLeftGroups) {
                                            if (tmp == null) continue;
                                            tmps.add(tmp);
                                        }
                                    tmps.add(groupData);
                                    String newLeftGroupData = NetworkManager.Companion.getInstance().getGson().toJson(tmps.toArray(new GroupData[0]), GroupData[].class);
                                    Log.e(TAG, "newLeftGroupData:" + newLeftGroupData);
                                    s.edit().putString(KEY_LEFT_GROUP_ENDTIME, newLeftGroupData).apply();

                                    //GroupMembersFragment.this.dismiss();
                                    //getChildFragmentManager().beginTransaction().detach(GroupMembersFragment.this).commit();
                                    //getActivity().getSupportFragmentManager().popBackStack();
                                    //getActivity().getSupportFragmentManager().popBackStack (GroupMainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    getFragmentManager().popBackStackImmediate();
                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                    DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.error_code_timeout);
                                }

                            });
                } else {
                    TpApi.getInstance().leaveGroup(mContext, groupKey, "",
                            new NetworkManager.NetworkManagerListener<LeaveGroupResponse>() {
                                @Override
                                public void onSucceed(LeaveGroupResponse object) {
                                    //cancel notification of this group
                                    NotificationManager notificationManager = DataCacheManager.getInstance().getNotificationManager();
                                    notificationManager.cancel(groupId, Integer.parseInt(groupId));//MeetingFunctionChanged
                                    notificationManager.cancel(groupKey, Integer.parseInt(groupKey));//Notice
                                    //remove this group data in old data
                                    SendUserLocationResponse oldResponse = DataCacheManager.getInstance().getmSendUserLocationResponse();
                                    if (oldResponse != null) if (oldResponse.getData() != null) {
                                        List<GroupData> newData = new ArrayList<>();
                                        for (GroupData gd : oldResponse.getData()) {
                                            if (gd.getGroupInfos() == null) continue;
                                            GroupInfo oGI = gd.getGroupInfos()[0];
                                            String oKey = oGI.getKey();
                                            if (oKey == null) continue;
                                            if (!gd.getGroupInfos()[0].getKey().equals(groupKey)) {
                                                newData.add(gd);
                                                Log.e(TAG, "findFriendGroupDisbandHandler:" + oKey);
                                            }

                                        }
                                        oldResponse.setData(newData);
                                        DataCacheManager.getInstance().setmSendUserLocationResponse(oldResponse);
                                    }

                                    //send group data be a reference to delete the group item in listview
                                    GroupData gd = (GroupData) getArguments().getSerializable(ARG_KEY_GROUP_DATA);
                                    EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_LEFT_GROUP, groupData));
                                    req.dismiss();


                                    //cancel alarm
                                    String noticeTime = groupData.getGroupInfos()[0].getNoticeTime();
                                    if (noticeTime != null) if (noticeTime.length() > 0) {
                                        String key = groupData.getGroupInfos()[0].getKey();
                                        GroupInfo gi = gd.getGroupInfos()[0];
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date nt = null;
                                        try {
                                            nt = dateFormatter.parse(noticeTime);
                                            //to cancel alarm, consider group key and notice time only
                                            CreateGroupCollectionJsonObject c = new CreateGroupCollectionJsonObject();
                                            c.setGroupKey(key);
                                            c.setCollectionNoticeTime(nt.getTime());

                                            Calendar cal = Calendar.getInstance();
                                            cal.setTimeInMillis(c.getCollectionNoticeTime());
                                            MyAlarmManager mAM = new MyAlarmManager(getContext());
                                            mAM.cancelAlarm(c, MyAlarmManager.FIND_FRIEND_NOTICE);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    //save leave group data, the data will delete while the available time is expired
                                    SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(getContext());
                                    String oldLeftGroupData = s.getString(KEY_LEFT_GROUP_ENDTIME, "");
                                    GroupData[] oldLeftGroups = null;
                                    if (oldLeftGroupData.length() > 0)
                                        oldLeftGroups = NetworkManager.Companion.getInstance().getGson().fromJson(oldLeftGroupData, GroupData[].class);
                                    ArrayList<GroupData> tmps = new ArrayList<>();
                                    if (oldLeftGroups != null)
                                        for (GroupData tmp : oldLeftGroups) {
                                            if (tmp == null) continue;
                                            tmps.add(tmp);
                                        }
                                    tmps.add(groupData);
                                    String newLeftGroupData = NetworkManager.Companion.getInstance().getGson().toJson(tmps.toArray(new GroupData[0]), GroupData[].class);
                                    Log.e(TAG, "newLeftGroupData:" + newLeftGroupData);
                                    s.edit().putString(KEY_LEFT_GROUP_ENDTIME, newLeftGroupData).apply();

                                    //GroupMembersFragment.this.dismiss();
                                    //getChildFragmentManager().beginTransaction().detach(GroupMembersFragment.this).commit();
                                    //getActivity().getSupportFragmentManager().popBackStack();
                                    //getActivity().getSupportFragmentManager().popBackStack (GroupMainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    getFragmentManager().popBackStackImmediate();

                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                    req.dismiss();
                                    DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.error_code_timeout);
                                }
                            });
                }

                //getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        req.show();
        //req.getWindow().setAttributes(lp);
        req.getWindow().setLayout((7 * width) / 7, (5 * height) / 5);

    }

    boolean groupDisbandIsShown = false;

    public void showGroupDisbandDialog() {
        if (groupDisbandIsShown) return;
        groupDisbandIsShown = true;
        //leave the group
        TpApi.getInstance().leaveGroup(mContext, groupKey, "",
                new NetworkManager.NetworkManagerListener<LeaveGroupResponse>() {
                    @Override
                    public void onFail(@NotNull String errorMsg, boolean shouldRetry) {

                    }

                    @Override
                    public void onSucceed(LeaveGroupResponse object) {
                    }

                });


        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        final Dialog req = adb.setView(new View(getActivity())).create();

        req.show();
        /*
         * make sure soft keyboard showing in AlertDialog Builder
         */
        req.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        req.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //for make custom dialog with rounded corners in android
        //set the background of your dialog to transparent
        req.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        req.setContentView(R.layout.title_content_dialog);

        TextView titleTv = req.findViewById(R.id.title);
        TextView contentTv = req.findViewById(R.id.content);


        titleTv.setText(R.string.warning);
        contentTv.setVisibility(View.VISIBLE);
        contentTv.setText(R.string.leave_group_disband_by_creator_text);

        TextView okBtn = req.findViewById(R.id.ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req.dismiss();
                //cancel notification of this group
                NotificationManager notificationManager = DataCacheManager.getInstance().getNotificationManager();
                notificationManager.cancel(groupId, Integer.parseInt(groupId));//MeetingFunctionChanged
                notificationManager.cancel(groupKey, Integer.parseInt(groupKey));//Notice
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_fl);
                if (f != null) {
                    String className = f.getClass().getSimpleName();
//                    if (className.equals(GroupMap.class.getSimpleName()))
//                        getFragmentManager().popBackStackImmediate();
                }
                getFragmentManager().popBackStackImmediate();

            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        req.show();
        //req.getWindow().setAttributes(lp);
        req.getWindow().setLayout((7 * width) / 7, (5 * height) / 5);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (editViewIsShown) {
                final FrameLayout editFL = mView.findViewById(R.id.detail_action_bar_fl_edit);
                editLayout.setVisibility(View.GONE);
                editViewIsShown = false;
                editFL.setVisibility(View.VISIBLE);
                return true;
            }

            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                targetPOI = (POI) data.getExtras().get(NaviSDKText.INTENT_EXTRAS_GATHER_POI);
                Log.e(LOG_TAG, "targetPOI" + targetPOI.getName());

                collectionPlace.setText(targetPOI.getName());
            }
        }
    }
}
