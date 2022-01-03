package com.omni.navisdk.view.group_location;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.omni.navisdk.BuildConfig;
import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.module.POI;
import com.omni.navisdk.module.group.CreateGroupResponse;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.network.TpApi;
import com.omni.navisdk.tool.DialogTools;
import com.omni.navisdk.tool.Tools;
import com.omni.navisdk.view.FragmentOnKeyEvent;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreateGroupTestFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "fragment_add_group";

    private Context mContext;
    private View mView;
    private LayoutInflater mInflater;
    private TextView collectPlace;
    private boolean createResult = false;
    private GroupMainFragment.DialogFragmentCloseListener listener;
    private final int collect_interval = 10;
    private final int notice_interval = 5;
    int height, width;

    private TextInputEditText nameOTIET;
    private TextInputEditText titleOTIET;

    public static CreateGroupTestFragment newInstance() {
        CreateGroupTestFragment fragment = new CreateGroupTestFragment();

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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.create_group_view, null, false);

            mInflater = inflater;


            Toolbar toolbar = mView.findViewById(R.id.hint_action_bar);
            TextView titleTV = toolbar.findViewById(R.id.hint_action_bar_tv_title);
            titleTV.setText(R.string.group_page_create_group);
            final FrameLayout homeFL = toolbar.findViewById(R.id.only_back_action_bar_fl_home);
            final ImageView clean_meeting_setting = mView.findViewById(R.id.clean_setting);
            final TextView meeting_setting = mView.findViewById(R.id.meeting_time_info);
            collectPlace = mView.findViewById(R.id.collection_place_setting_btn);
            final ImageView notice_time_add = mView.findViewById(R.id.notice_increase_time);
            final ImageView notice_time_reduce = mView.findViewById(R.id.notice_decrease_time);
            notice_time_add.setEnabled(false);
            notice_time_reduce.setEnabled(false);

            final MaterialButton create = mView.findViewById(R.id.create_group_dialog_tv_create);

            nameOTIET = mView.findViewById(R.id.create_user_name);
            String language = Locale.getDefault().getLanguage();
            if (language.equals("en")) {
                int maxLength = 20;
                nameOTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
            if (!BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                nameOTIET.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }

            titleOTIET = mView.findViewById(R.id.create_group_name);
            if (language.equals("en")) {
                int maxLength = 20;
                titleOTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
            final TextInputEditText telTI = mView.findViewById(R.id.create_group_phone);

            TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (v.getId() == titleOTIET.getId()) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            nameOTIET.requestFocus();
                        }
                    } else if (v.getId() == nameOTIET.getId()) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                telTI.requestFocus();
                            }
                        } else {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                telTI.clearFocus();
                                if (getActivity() != null)
                                    ((HomeActivity) getActivity()).hideKeyboard(v.getWindowToken());
                            }
                        }
                    } else if (v.getId() == telTI.getId()) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                //hide soft keyboard only, v.clearFocus(); cause the view move up then move down
                                if (getActivity() != null)
                                    ((HomeActivity) getActivity()).hideKeyboard(v.getWindowToken());
                            }
                        }
                    }
                    return false;
                }
            };
            titleOTIET.setOnEditorActionListener(mOnEditorActionListener);
            nameOTIET.setOnEditorActionListener(mOnEditorActionListener);
            telTI.setOnEditorActionListener(mOnEditorActionListener);
            telTI.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.e(TAG, s + " start:" + start + " before:" + before + " count:" + count);
                    if ((start == 4 || start == 8) && before == 1 && count == 0) {

                    } else if ((start == 3 || start == 7) && before == 0 && count == 1) {
                        telTI.setText((telTI.getText().toString() + "-"));
                        int pos = telTI.getText().length();
                        telTI.setSelection(pos);
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
                        telTI.setText(tmp);
                        int pos = telTI.getText().length();
                        telTI.setSelection(pos);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

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

            View.OnClickListener mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == homeFL.getId()) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        if (titleOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                            titleOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
                    } else if (v.getId() == clean_meeting_setting.getId()) {
                        cleanMeetingSetting();
                    } else if (v.getId() == collectPlace.getId()) {
                        //hide keyboard
                        if (titleOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                            titleOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
//                        if (isVisible()) if (getActivity() != null)
//                            ((HomeActivity) getActivity()).openFragmentPage(
//                                    GroupSelectCollectionPlaceMapFragment.newInstance(CreateGroupTestFragment.this),
//                                    GroupSelectCollectionPlaceMapFragment.TAG);
                    } else if (v.getId() == meeting_setting.getId()) {
                        //hide keyboard
                        if (titleOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                            titleOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
                        showMeetingTimeSettingDialog();
                    } else if (v.getId() == notice_time_add.getId()) {
                        //hide keyboard
                        if (titleOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                            titleOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
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
                        TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
                        notice_time_info.setText("" + notice_time);
                    } else if (v.getId() == notice_time_reduce.getId()) {
                        //hide keyboard
                        if (titleOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                            titleOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
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
                        TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
                        notice_time_info.setText("" + notice_time);
                    } else if (v.getId() == create.getId()) {
                        //                    if (!mSRL.isRefreshing()) {
//                        mSRL.setRefreshing(true);
//                    }

                        ////////////for test////////////
/*                    if (collectionNoticeTime.isEnabled()) {
                        Calendar tmp = Calendar.getInstance();
                        tmp.setTime(new Date());
                        tmp.set(Calendar.HOUR_OF_DAY, collectionTimeH.getValue());
                        tmp.set(Calendar.MINUTE, collectionTimeM.getValue());
                        //notice data
                        CreateGroupCollectionJsonObject c = new CreateGroupCollectionJsonObject();
                        c.setGroupKey("1234");
                        c.setGroupName("testGroup");
                        c.setCreater("testName");
                        c.setEndTime(mNumberPicker.getValue());
                        c.setCollectionTime(tmp.getTimeInMillis());
                        if (collectionPlacePOI != null) {
                            c.setCollectionPlace(NetworkManager.getInstance().getGson().toJson(collectionPlacePOI, POI.class));
                        }


                        int before = Integer.parseInt(collectionNoticeTimes.get(collectionNoticeTime.getValue()));
                        MyAlarmManager mAM = new MyAlarmManager(getActivity());
                        //for test, set the minute to now, and trigger alarm after 'before' second
                        tmp.set(Calendar.MINUTE, new Date().getMinutes());
                        tmp.add(Calendar.SECOND, before);
                        //datas.put(GROUP_COLLECTION_NOTICE_TIME, ""+tmp.getTimeInMillis());
                        c.setCollectionNoticeTime(tmp.getTimeInMillis());
                        mAM.addAlarm(tmp, c, MyAlarmManager.FIND_FRIEND_NOTICE);
                        mAM.cancelAlarm(c, MyAlarmManager.FIND_FRIEND_NOTICE);
                    }
                    if(true) return;*/
                        ////////////for test////////////

                        String nameStr = nameOTIET.getText().toString().trim();
                        String mCreateTitleStr = titleOTIET.getText().toString().trim();
                        String tel = telTI.getText().toString();
                        int wordLimit = 10;
                        String language = Locale.getDefault().getLanguage();
                        if (language.equals("en"))
                            wordLimit = 20;
                        if (TextUtils.isEmpty(nameStr)) {
                            //nameTIL.setError(getString(R.string.input_error_required));
                            DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_name);
                        } else if (nameStr.length() > wordLimit) {
                            //titleTIL.setError(getString(R.string.eng_text_limit));
                            DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_name);
                        } else if (TextUtils.isEmpty(mCreateTitleStr)) {
                            //titleTIL.setError(getString(R.string.input_error_required));
                            DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_title);
                        } /* else if (Tools.isChineseText(mCreateTitleStr) && mCreateTitleStr.length() > 10) {
                        //titleTIL.setError(getString(R.string.zh_text_limit));
                        DialogTools.getInstance().showHintDialog(mContext, R.string.dialog_hint_create_group_title);
                    }*/ else if (mCreateTitleStr.length() > wordLimit) {
                            //titleTIL.setError(getString(R.string.eng_text_limit));
                            DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_title);
                        } else {
                            if (nameOTIET.isFocused()) {
                                ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                                nameOTIET.clearFocus();
                            } else if (titleOTIET.isFocused()) {
                                ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                                titleOTIET.clearFocus();
                            }
                            if (tel.length() > 0) {
                                boolean isPhone = Tools.getInstance().isValidMobileNumber(tel, "TW");
                                if (!isPhone) {
                                    //telTIL.setError(getString(R.string.input_error_wrong_phone_number_format));
                                    DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_tel);
                                    return;
                                }
                            }

                            String meeting_time = "";
                            String notice_time_ = "";
                            String p_id = "";
                            String p_type = "";
                            Calendar tmp = Calendar.getInstance();
                            tmp.setTime(new Date());
                            TextView meeting_setting = mView.findViewById(R.id.meeting_time_info);
                            if (!meeting_setting.getText().equals(getString(R.string.string_select_please))) {
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                DateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
                                String[] meeting_time_string = meeting_setting.getText().toString().split(":");

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

                                if (notice_time > 0) {
                                    int before = notice_time;
                                    tmp.add(Calendar.MINUTE, -before);
                                    try {
                                        String date_ = "" + String.format("%04d", tmp.get(Calendar.YEAR)) + String.format("%2d", tmp.get(Calendar.MONTH) + 1) + String.format("%02d", tmp.get(Calendar.DAY_OF_MONTH)) +
                                                " " + String.format("%02d", tmp.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", tmp.get(Calendar.MINUTE)) + "00";
                                        notice_time_ = dateFormatter.format(df.parse(date_));
                                        Log.e(TAG, "meeting_time:" + meeting_time + " meeting_timeH:" + meeting_time_string[0] + " meeting_timeM:" + meeting_time_string[1] + "\n" + date_);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (collectionPlacePOI != null) {
                                p_id = String.valueOf(collectionPlacePOI.getId());
                                switch (collectionPlacePOI.getType()) {
                                    default:
                                        p_type = "poi";
                                        break;
                                }
                            }


                            mView.findViewById(R.id.create_group_loading).setVisibility(View.VISIBLE);
                            Calendar c = Calendar.getInstance();
                            int h = 19 - c.get(Calendar.HOUR_OF_DAY);
                            if (h < 1) h = 1;
                            TpApi.getInstance().createGroup(mContext, mCreateTitleStr, nameStr, tel, "" + h,
                                    notice_time_, meeting_time, p_type, p_id, "",
                                    new NetworkManager.NetworkManagerListener<CreateGroupResponse>() {
                                        @Override
                                        public void onSucceed(CreateGroupResponse response) {
                                            //dialog.dismiss();
                                            if (response != null) {
                                                if (response.getResult().equals("true")) {
                                                    //set notice to alarm manager if the group was created


                                                    showCustomCreateGroupSuccessDialog(response);
                                                    createResult = true;
                                                    if (getActivity() != null)
                                                        ((HomeActivity) getActivity()).findFriendMode(true);
                                                } else {
                                                    showCustomDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown));
                                                    createResult = false;
                                                }
                                            } else {
                                                showCustomDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown));
                                                createResult = false;
                                            }
                                            mView.findViewById(R.id.create_group_loading).setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                            showCustomDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown));
                                            createResult = false;
                                            mView.findViewById(R.id.create_group_loading).setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                }
            };

            homeFL.setOnClickListener(mOnClickListener);
            clean_meeting_setting.setOnClickListener(mOnClickListener);
            collectPlace.setOnClickListener(mOnClickListener);
            meeting_setting.setOnClickListener(mOnClickListener);
            notice_time_add.setOnClickListener(mOnClickListener);
            notice_time_reduce.setOnClickListener(mOnClickListener);
            create.setOnClickListener(mOnClickListener);

            View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        if (view.getY() < height / 2) return;
                        final ScrollView s = mView.findViewById(R.id.scrollview);
                        final int from = 0;
                        final int to = (int) (view.getMeasuredHeight() - view.getY());
                        s.setTag(0);
                        ValueAnimator mAnimator = ValueAnimator.ofInt(0, 100);
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int animatorValue = (int) animation.getAnimatedValue();
                                float fraction = animatorValue / 100f;
                                IntEvaluator mEvaluator = new IntEvaluator();
                                int tmp = mEvaluator.evaluate(fraction, from, to);
                                s.scrollBy(0, tmp - (int) s.getTag());
                            }
                        });
                        mAnimator.setInterpolator(new AccelerateInterpolator());
                        mAnimator.setDuration(getResources().getInteger(R.integer.config_ExpandAnimTime));
                        mAnimator.setTarget(s);
                        mAnimator.start();
                    }
                }
            };

            //titleOTIET.setOnFocusChangeListener(mOnFocusChangeListener);
            //nameOTIET.setOnFocusChangeListener(mOnFocusChangeListener);
            telTI.setOnFocusChangeListener(mOnFocusChangeListener);
        }
        return mView;
    }

    public void onStart() {
        super.onStart();

    }

    POI collectionPlacePOI;

    public void updateCollectionPlaceData(POI target) {
        collectionPlacePOI = target;
        String name = target.getName();
        collectPlace.setText(name);
//        TextView collectPlaceMoreInfo = mView.findViewById(R.id.collection_place_more_info);
//        collectPlaceMoreInfo.setVisibility(View.VISIBLE);
    }

    public void showCustomDialog(String title, String cause) {

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
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                //finish();
                req.dismiss();
                if (getActivity() != null) {
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                }
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        req.show();
        //req.getWindow().setAttributes(lp);
        req.getWindow().setLayout((7 * width) / 7, (5 * height) / 5);

    }

    int notice_time = 0;
    int notice_time_min = 0;
    int notice_time_max = 0;

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
                TextView meeting_setting = mView.findViewById(R.id.meeting_time_info);
                String info = collectionTimeH.getValue() + ":" + String.format("%1$02d", Integer.parseInt(collectionTimeM.getDisplayedValues()[collectionTimeM.getValue()]));
                meeting_setting.setText(info);

                //update notice time range
                ImageView notice_time_add = mView.findViewById(R.id.notice_increase_time);
                ImageView notice_time_reduce = mView.findViewById(R.id.notice_decrease_time);

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

    public void cleanMeetingSetting() {
        notice_time = 0;
        notice_time_min = 0;
        notice_time_max = 0;
        collectPlace.setText(R.string.string_select_please);
        TextView collectPlaceMoreInfo = mView.findViewById(R.id.collection_place_more_info);
        collectPlaceMoreInfo.setText(null);
        collectPlaceMoreInfo.setVisibility(View.GONE);
        collectionPlacePOI = null;
        TextView meeting_setting = mView.findViewById(R.id.meeting_time_info);
        meeting_setting.setText(R.string.string_select_please);
        TextView notice_time_info = mView.findViewById(R.id.notice_time_info);
        notice_time_info.setText("0");
        final ImageView notice_time_add = mView.findViewById(R.id.notice_increase_time);
        final ImageView notice_time_reduce = mView.findViewById(R.id.notice_decrease_time);
        notice_time_add.setEnabled(false);
        notice_time_reduce.setEnabled(false);

    }

    public void showCustomCreateGroupSuccessDialog(final CreateGroupResponse response) {

        if (!isVisible()) return;
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

        req.setContentView(R.layout.custom_create_group_success_dialog);

        TextView titleTv = req.findViewById(R.id.rec_dialog_title);
//        titleTv.setText(getString(R.string.group_key_is) + response.getData()[0].getKey());

        TextView keyTv = req.findViewById(R.id.rec_dialog_key);
        keyTv.setText(response.getData()[0].getKey());

        TextView copyBtn = req.findViewById(R.id.rw_collect_copy_btn);
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                req.dismiss();
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label name", response.getData()[0].getKey());
                clipboard.setPrimaryClip(clip);
                if (getActivity() != null) {
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                }
                DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.string_copy_key_done);

            }
        });

        TextView okBtn = req.findViewById(R.id.rw_collect_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                req.dismiss();
                if (getActivity() != null) {
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                }
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
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            return true;
        }
        return false;
    }
}
