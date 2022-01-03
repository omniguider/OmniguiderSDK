package com.omni.navisdk.view.group_location;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.omni.navisdk.BuildConfig;
import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.manager.DataCacheManager;
import com.omni.navisdk.manager.MyAlarmManager;
import com.omni.navisdk.module.OmniEvent;
import com.omni.navisdk.module.SendUserLocationResponse;
import com.omni.navisdk.module.group.CreateGroupCollectionJsonObject;
import com.omni.navisdk.module.group.CreateGroupResponse;
import com.omni.navisdk.module.group.GroupData;
import com.omni.navisdk.module.group.GroupInfo;
import com.omni.navisdk.module.group.GroupMember;
import com.omni.navisdk.module.group.JoinGroupResponse;
import com.omni.navisdk.module.group.LeaveGroupResponse;
import com.omni.navisdk.module.group.UpdateGroupResponse;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.network.TpApi;
import com.omni.navisdk.tool.DialogTools;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.view.FragmentOnKeyEvent;
import com.omni.navisdk.view.OmniTextInputEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.omni.navisdk.tool.PreferencesTools.KEY_LEFT_GROUP_ENDTIME;

public class GroupMainFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "GroupMainFragment";

    public static GroupMainFragment newInstance() {
        GroupMainFragment fragment = new GroupMainFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    private Context mContext;
    private View mView;
    private LayoutInflater mInflater;
    private RecyclerView mRV;
    private GroupInfoAdapter mAdapter;
    private FrameLayout mAddFL;
    private FrameLayout mJoinFL;
    private String mCreateTitleStr;
    private NumberPicker mNumberPicker;
    private EventBus mEventBus;
    private List<GroupData> mGroupDataList;
    int height, width;

    private TextView mNoGroupTv;
    private ImageView mNoGroupIv;
    private MaterialButton joinGroup;
    private MaterialButton createGroup;

    public interface DialogFragmentCloseListener {
        void onDialogFragmentClose(boolean result);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OmniEvent event) {
        switch (event.getType()) {
            case OmniEvent.TYPE_GROUP_RESPONSE:
                mGroupDataList = (List<GroupData>) event.getObj();

                GroupInfo currentGroupVisionInfo = PreferencesTools.Companion.getInstance().getProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, GroupInfo.class);
                if (currentGroupVisionInfo == null && mGroupDataList.size() == 1) {
                    PreferencesTools.Companion.getInstance().saveProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, mGroupDataList.get(0).getGroupInfos()[0]);
                }
                break;

            case OmniEvent.TYPE_LEFT_GROUP:
                GroupData groupData = (GroupData) event.getObj();
                for (GroupData g : mGroupDataList)
                    if (g.getGroupInfos()[0].getKey().equals(groupData.getGroupInfos()[0].getKey())) {
                        mGroupDataList.remove(g);
                        break;
                    }
                setRVData();
                break;
            case OmniEvent.TYPE_LEFT_GROUP_MAP:
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_fl);
                if (f != null) {
                    String className = f.getClass().getSimpleName();
//                    if (className.equals(GroupMap.class.getSimpleName()))
//                        getFragmentManager().popBackStackImmediate();
                }
                getFragmentManager().popBackStackImmediate();
                break;
        }
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
        Log.e(TAG, "onCreate");
        if (mEventBus == null) {
            mEventBus = EventBus.getDefault();
        }
        mEventBus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {

            mInflater = inflater;

            mView = inflater.inflate(R.layout.group_main_fragment_view, container, false);

            mNoGroupTv = mView.findViewById(R.id.group_main_fragment_view_tv_no_group);
            mNoGroupIv = mView.findViewById(R.id.group_main_fragment_view_iv_no_group);
            String language = Locale.getDefault().getLanguage();
            mNoGroupIv.setImageResource(R.mipmap.img_opps);
            createGroup = mView.findViewById(R.id.group_main_fragment_view_tv_create_group);
            createGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showCreateGroupDialog();
/*                    DialogFragmentCloseListener listener = new DialogFragmentCloseListener() {
                        @Override
                        public void onDialogFragmentClose(boolean result) {
                            if(result){
                                //send user location to server to update your location
                                //and get the members info
                                updateMemberList();

                            }
                        }
                    };
                    CreateGroupFragment fragment = CreateGroupFragment.newInstance();
                    fragment.setOnDialogFragmentCloseListener(listener);
                    fragment.show(getChildFragmentManager(), CreateGroupFragment.TAG);*/
                    if (isVisible()) if (getActivity() != null) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            ((HomeActivity) getActivity()).openFragmentPage(CreateGroupTestFragment.newInstance(),
                                    CreateGroupTestFragment.TAG);
                        } else {
                            ((HomeActivity) getActivity()).openFragmentPage(CreateGroupFragment.newInstance(),
                                    CreateGroupTestFragment.TAG);
                        }
                    }

                }
            });
            joinGroup = mView.findViewById(R.id.group_main_fragment_view_tv_join_group);
            joinGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //showJoinGroupDialog();
                    if (getActivity() != null) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            ((HomeActivity) getActivity()).openFragmentPage(JoinGroupTestFragment.newInstance(GroupMainFragment.this),
                                    JoinGroupTestFragment.TAG);
                        } else {
                            ((HomeActivity) getActivity()).openFragmentPage(JoinGroupFragment.newInstance(GroupMainFragment.this),
                                    JoinGroupTestFragment.TAG);
                        }
                    }
                }
            });

            mRV = mView.findViewById(R.id.group_main_fragment_view_rv);

            mView.findViewById(R.id.group_main_fragment_view_tv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (mGroupDataList != null) if (mGroupDataList.size() == 0)
//                        if (getActivity() != null)
//                            ((HomeActivity) getActivity()).findFriendMode(false);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            mAddFL = mView.findViewById(R.id.group_main_fragment_view_fl_add);
            mAddFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreateGroupDialog();
                }
            });

            mJoinFL = mView.findViewById(R.id.group_main_fragment_view_fl_join);
            mJoinFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showJoinGroupDialog();
                }
            });

        }

        return mView;
    }

    public void onStart() {
        super.onStart();
        //first time to opend find firend?
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showIntro = sharedPreferences.getBoolean(PreferencesTools.KEY_SHOW_INTRO_FIND_FRIEND, true);
//        if (showIntro) {
//            View groupIntro = mView.findViewById(R.id.group_main_fragment_intro);
//            groupIntro.callOnClick();
//        }
        //get member list
        updateMemberList();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mEventBus != null) {
            mEventBus.unregister(this);
        }

        if (getActivity() != null) {
            ((HomeActivity) getActivity()).findFriendMode(false);
            ((HomeActivity)getActivity()).removeSendUserLocationResponseToOtherClassListener(getClass());
        }
    }

    public void updateMemberList() {
        final HomeActivity.SendUserLocationResponseToOtherClass listener = new HomeActivity.SendUserLocationResponseToOtherClass() {
            @Override
            public void success(SendUserLocationResponse response) {
                if (response.getResult().equals("true")) {
                    mGroupDataList = response.getData();
                    GroupInfo currentGroupVisionInfo = PreferencesTools.Companion.getInstance()
                            .getProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, GroupInfo.class);
                    if (currentGroupVisionInfo == null && mGroupDataList.size() == 1) {
                        PreferencesTools.Companion.getInstance().saveProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, mGroupDataList.get(0).getGroupInfos()[0]);
                    }
                    setRVData();
/*
                    if(getActivity() != null)
                        ((HomeActivity)getActivity()).removeSendUserLocationResponseToOtherClassListener(getClass());
*/
                }
            }

            @Override
            public void fail(VolleyError error, boolean shouldRetry) {
                //try again later
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((HomeActivity) getActivity()).sendUserLocationToServer();
                    }
                }, 3000);
            }
        };

        SendUserLocationResponse lastGroupData = DataCacheManager.getInstance().getmSendUserLocationResponse();
        if (lastGroupData != null) {
            if (lastGroupData.getResult().equals("true")) {
                mGroupDataList = lastGroupData.getData();
                GroupInfo currentGroupVisionInfo = PreferencesTools.Companion.getInstance().getProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, GroupInfo.class);
                if (currentGroupVisionInfo == null && mGroupDataList.size() == 1) {
                    PreferencesTools.Companion.getInstance().saveProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, mGroupDataList.get(0).getGroupInfos()[0]);
                }
                setRVData();
            }
        }

        if (getActivity() != null) {
            ((HomeActivity) getActivity()).addSendUserLocationResponseToOtherClassListener(getClass(), listener);
            ((HomeActivity) getActivity()).sendUserLocationToServer();
            ((HomeActivity) getActivity()).findFriendMode(true);
        }
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        View view = mInflater.inflate(R.layout.create_group_dialog, null, false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        final TextInputLayout titleTIL = view.findViewById(R.id.create_group_dialog_til_title);
        final OmniTextInputEditText titleOTIET = view.findViewById(R.id.create_group_dialog_otiet_title);
        titleOTIET.setOnOmniEditTextActionListener(new OmniTextInputEditText.OnOmniEditTextActionListener() {
            @Override
            public void onSoftKeyboardDismiss() {
                setFocusOnFragment();
            }

            @Override
            public void onTouch(MotionEvent event) {

            }
        });

        final TextInputLayout nameTIL = view.findViewById(R.id.create_group_dialog_til_name);
        final OmniTextInputEditText nameOTIET = view.findViewById(R.id.create_group_dialog_otiet_name);
        nameOTIET.setOnOmniEditTextActionListener(new OmniTextInputEditText.OnOmniEditTextActionListener() {
            @Override
            public void onSoftKeyboardDismiss() {
                setFocusOnFragment();
            }

            @Override
            public void onTouch(MotionEvent event) {

            }
        });
        final TextInputEditText telTIL = mView.findViewById(R.id.create_group_dialog_til_tel_edt);
        mNumberPicker = view.findViewById(R.id.create_group_dialog_np);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(8);

        view.findViewById(R.id.create_group_dialog_tv_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.findViewById(R.id.group_main_loading).setVisibility(View.VISIBLE);
                mCreateTitleStr = titleOTIET.getText().toString().trim();
                String nameStr = nameOTIET.getText().toString().trim();
                String tel = telTIL.getText().toString();
                if (TextUtils.isEmpty(mCreateTitleStr)) {
                    titleTIL.setError(getString(R.string.input_error_required));
                } else if (TextUtils.isEmpty(nameStr)) {
                    nameTIL.setError(getString(R.string.input_error_required));
                } else {
                    TpApi.getInstance().createGroup(mContext, mCreateTitleStr, nameStr, tel, String.valueOf(mNumberPicker.getValue()), "", "", "", "", "",
                            new NetworkManager.NetworkManagerListener<CreateGroupResponse>() {
                                @Override
                                public void onSucceed(CreateGroupResponse response) {
                                    dialog.dismiss();
                                    showCreateGroupSuccessDialog(response);
                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                    dialog.dismiss();
                                    showRequestDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown));
                                }
                            });
                }

            }
        });

        dialog.show();
    }

    private void showCreateGroupSuccessDialog(CreateGroupResponse response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        View view = mInflater.inflate(R.layout.create_group_success_dialog, null, false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        OmniTextInputEditText titleOTIET = view.findViewById(R.id.create_group_success_dialog_otiet_title);
        titleOTIET.setText(mCreateTitleStr);
        titleOTIET.setClickable(false);
        titleOTIET.setFocusable(false);
        titleOTIET.setFocusableInTouchMode(false);
        titleOTIET.setKeyboardShow(false);

        OmniTextInputEditText keyOTIET = view.findViewById(R.id.create_group_success_dialog_otiet_key);
        keyOTIET.setText(response.getData()[0].getKey());
        keyOTIET.setClickable(false);
        keyOTIET.setFocusable(false);
        keyOTIET.setFocusableInTouchMode(false);
        keyOTIET.setKeyboardShow(false);

        view.findViewById(R.id.create_group_success_dialog_tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showJoinGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        View view = mInflater.inflate(R.layout.join_group_dialog, null, false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        final TextInputLayout keyTIL = view.findViewById(R.id.join_group_dialog_til_key);
        final OmniTextInputEditText keyOTIET = view.findViewById(R.id.join_group_dialog_otiet_key);
        keyOTIET.setOnOmniEditTextActionListener(new OmniTextInputEditText.OnOmniEditTextActionListener() {
            @Override
            public void onSoftKeyboardDismiss() {
                setFocusOnFragment();
            }

            @Override
            public void onTouch(MotionEvent event) {

            }
        });

        final TextInputLayout nameTIL = view.findViewById(R.id.join_group_dialog_til_name);
        final OmniTextInputEditText nameOTIET = view.findViewById(R.id.join_group_dialog_otiet_name);
        nameOTIET.setOnOmniEditTextActionListener(new OmniTextInputEditText.OnOmniEditTextActionListener() {
            @Override
            public void onSoftKeyboardDismiss() {
                setFocusOnFragment();
            }

            @Override
            public void onTouch(MotionEvent event) {

            }
        });

        view.findViewById(R.id.join_group_dialog_tv_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyStr = keyOTIET.getText().toString().trim();
                String nameStr = nameOTIET.getText().toString().trim();

                if (TextUtils.isEmpty(keyStr)) {
                    keyTIL.setError(getString(R.string.input_error_required));
                } else if (TextUtils.isEmpty(nameStr)) {
                    nameTIL.setError(getString(R.string.input_error_required));
                } else {

                    TpApi.getInstance().joinGroup(mContext, keyStr, nameStr, "", "",
                            new NetworkManager.NetworkManagerListener<JoinGroupResponse>() {
                                @Override
                                public void onSucceed(JoinGroupResponse response) {
                                    Log.e(TAG, "response result : " + response.getResult() + ", response err msg : " + response.getErrorMessage() +
                                            ", msg : " + response.getMessage());
                                    dialog.dismiss();

                                    if (response.getResult().equals("true")) {

                                        showRequestDialog(getString(R.string.join_group_sucessfully), "");

                                    } else {
                                        showRequestDialog(getString(R.string.join_group_fail), getString(R.string.dialog_error_msg_invalid_group));

                                    }
                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                    dialog.dismiss();
                                    showRequestDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown));
                                }
                            });
                }
            }
        });

        dialog.show();
    }

    private void setFocusOnFragment() {
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
    }

    private void setRVData() {
        Log.e("LOG","setRVData");
        mView.findViewById(R.id.group_main_loading).setVisibility(View.GONE);
        //remove the group was zero member, remove the group does not with creator
        ArrayList<GroupData> tmp = new ArrayList<>();
        for (GroupData gd : mGroupDataList) {
            if (gd.getGroupMembers() != null) if (gd.getGroupMembers().length > 0) {
                boolean hasCreator = false;
                for (GroupMember gm : gd.getGroupMembers()) {
                    if (gm.getRole().equals("admin")) {
                        hasCreator = true;
                        break;
                    }
                }
                if (hasCreator)
                    tmp.add(gd);
            }
        }

        mGroupDataList = tmp;

        if (mGroupDataList.size() == 0) {
            //mNoGroupRL.setVisibility(View.VISIBLE);
            mNoGroupTv.setVisibility(View.VISIBLE);
            mNoGroupIv.setVisibility(View.VISIBLE);
            mRV.setVisibility(View.GONE);
            return;
        } else if (mGroupDataList.size() > 0) {
            //mNoGroupRL.setVisibility(View.GONE);
            mNoGroupTv.setVisibility(View.GONE);
            mNoGroupIv.setVisibility(View.GONE);
            mRV.setVisibility(View.VISIBLE);
        }
        GroupInfo currentGroupVisionInfo = PreferencesTools.Companion.getInstance().getProperty(getActivity(), PreferencesTools.KEY_CURRENT_GROUP, GroupInfo.class);

        if (mAdapter == null) {

            mAdapter = new GroupInfoAdapter(getActivity(), mGroupDataList, currentGroupVisionInfo, new GroupInfoAdapter.AdapterListener() {
                @Override
                public void onItemClicked(GroupData groupData) {
                    openGroupMembersPage(groupData);
                    //EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_OPEN_GROUP_MEMBERS_PAGE, groupData));

                }

                @Override
                public void onGroupLeave(GroupData groupData, GroupInfoAdapter.GroupInfoViewHolder holder) {
                    showCustomLeaveGroupDialog(groupData, holder);
                }
            });

            mRV.setLayoutManager(new LinearLayoutManager(mContext));
            DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(mContext, R.color.gray_99));
            mRV.addItemDecoration(divider);

            mRV.setAdapter(mAdapter);
        } else {
            mAdapter.updataAdapter(mGroupDataList, currentGroupVisionInfo);
        }

        DialogTools.Companion.getInstance().dismissProgress(getActivity());
    }

    private void openGroupMembersPage(GroupData groupData) {
        if (getActivity() != null)
            if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                ((HomeActivity) getActivity()).openFragmentPage(GroupMembersTestFragment.newInstance(groupData),
                        GroupMembersTestFragment.TAG);
            }
//        else
//                ((HomeActivity) getActivity()).openFragmentPage(GroupMembersFragment.newInstance(groupData),
//                        GroupMembersTestFragment.TAG);
    }

    public void showRequestDialog(String title, String cause) {

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

        req.setContentView(R.layout.rw_collect_full_screen_dialog);

        TextView titleTv = req.findViewById(R.id.rec_dialog_title);
        TextView contentTv = req.findViewById(R.id.rec_dialog_content);
        titleTv.setText(title);
        contentTv.setVisibility(View.VISIBLE);
        contentTv.setText(cause);

        TextView okBtn = req.findViewById(R.id.rw_collect_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
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

    public void showCustomLeaveGroupDialog(final GroupData groupData, final GroupInfoAdapter.GroupInfoViewHolder holder) {

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

        final String groupId = groupData.getGroupInfos()[0].getId();
        final String groupKey = groupData.getGroupInfos()[0].getKey();

        boolean isCreater_ = false;
        for (GroupMember gm : groupData.getGroupMembers()) {
            Log.e(TAG, gm.getRole() + " " + gm.getName());
            if (gm.getRole().equals("admin")) {
                String myDeviceId = DataCacheManager.Companion.getInstance().getDeviceId(getActivity());
                if (myDeviceId.trim().equals(gm.getDeviceId().trim()))
                    isCreater_ = true;
                break;
            }
        }
        final boolean isCreater = isCreater_;
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
                                    NotificationManager notificationManager = DataCacheManager.Companion.
                                            getInstance().getNotificationManager();
                                    notificationManager.cancel(groupId, Integer.parseInt(groupId));//MeetingFunctionChanged
                                    notificationManager.cancel(groupKey, Integer.parseInt(groupKey));//Notice
                                    //send group data be a reference to delete the group item in listview
                                    EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_LEFT_GROUP, groupData));
                                    req.dismiss();


                                    //cancel alarm
                                    String noticeTime = groupData.getGroupInfos()[0].getNoticeTime();
                                    if (noticeTime != null) if (noticeTime.length() > 0) {
                                        String key = groupData.getGroupInfos()[0].getKey();
                                        GroupInfo gi = groupData.getGroupInfos()[0];
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

                                    //restore view x position to 0
                                    holder.infoLayout.setX(0);
                                    holder.touch_receiver.setX(0);
                                }

                                @Override
                                public void onFail(@NotNull String errorMsg, boolean shouldRetry) {

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
                                    EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_LEFT_GROUP, groupData));
                                    req.dismiss();


                                    //cancel alarm
                                    String noticeTime = groupData.getGroupInfos()[0].getNoticeTime();
                                    if (noticeTime != null) if (noticeTime.length() > 0) {
                                        String key = groupData.getGroupInfos()[0].getKey();
                                        GroupInfo gi = groupData.getGroupInfos()[0];
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

                                    //restore view x position to 0
                                    holder.infoLayout.setX(0);
                                    holder.touch_receiver.setX(0);
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mGroupDataList != null) if (mGroupDataList.size() == 0)
                if (getActivity() != null)
                    ((HomeActivity) getActivity()).findFriendMode(false);
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }
}
