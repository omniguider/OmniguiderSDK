package com.omni.navisdk.view.group_location;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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


public class CreateGroupFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "fragment_add_group";

    private Context mContext;
    private View mView;
    private String mCreateTitleStr;
    private NumberPicker mNumberPicker;

    private LayoutInflater mInflater;
    private RelativeLayout collectionLayout;
    private TextView collectPlace;
    private boolean createResult = false;
    private GroupMainFragment.DialogFragmentCloseListener listener;
    private final int notice_interval = 5;
    int height, width;

    public static CreateGroupFragment newInstance() {
        CreateGroupFragment fragment = new CreateGroupFragment();

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
            mView = inflater.inflate(R.layout.create_group_dialog, null, false);

            mInflater = inflater;


            Toolbar toolbar = mView.findViewById(R.id.hint_action_bar);
            TextView titleTV = toolbar.findViewById(R.id.hint_action_bar_tv_title);
            titleTV.setText(R.string.group_page_create_group);

            final TextInputLayout titleTIL = mView.findViewById(R.id.create_group_dialog_til_title);
            final TextInputEditText titleOTIET = mView.findViewById(R.id.create_group_dialog_otiet_title);
            final TextInputLayout nameTIL = mView.findViewById(R.id.create_group_dialog_til_name);
            final TextInputEditText nameOTIET = mView.findViewById(R.id.create_group_dialog_otiet_name);
            if (!BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                nameOTIET.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            final TextInputLayout telTIL = mView.findViewById(R.id.create_group_dialog_til_tel);
            final TextInputEditText telTI = mView.findViewById(R.id.create_group_dialog_til_tel_edt);

            TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (v.getId() == titleOTIET.getId()) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            nameTIL.requestFocus();
                        }
                    } else if (v.getId() == nameTIL.getId()) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                telTIL.requestFocus();
                            }
                        } else {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                telTIL.clearFocus();
                                if (getActivity() != null)
                                    ((HomeActivity) getActivity()).hideKeyboard(v.getWindowToken());
                            }
                        }
                    } else if (v.getId() == telTIL.getId()) {
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

            mNumberPicker = mView.findViewById(R.id.create_group_dialog_np);
            mNumberPicker.setMinValue(1);
            mNumberPicker.setMaxValue(8);

            collectionLayout = mView.findViewById(R.id.collection_layout);
            final View collectionBlocker = mView.findViewById(R.id.collection_time_blocker);
            final CheckBox enable_collection_finction = mView.findViewById(R.id.enable_collection_time);
            enable_collection_finction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isChecked()) {
                        collectionBlocker.animate().setDuration(getResources().getInteger(R.integer.config_CollapseAnimTime));
                        collectionBlocker.animate().setInterpolator(new FastOutSlowInInterpolator());
                        collectionBlocker.animate().scaleX(0);
                        collectionBlocker.animate().scaleY(0);
                        collectionBlocker.animate().setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                collectionBlocker.setVisibility(View.GONE);
                                collectionBlocker.animate().setListener(null);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    } else {
                        collectionBlocker.setVisibility(View.VISIBLE);
                        collectionBlocker.animate().setDuration(getResources().getInteger(R.integer.config_ExpandAnimTime));
                        collectionBlocker.animate().setInterpolator(new FastOutSlowInInterpolator());
                        collectionBlocker.animate().scaleX(1);
                        collectionBlocker.animate().scaleY(1);
                    }
                    collectionBlocker.animate().start();
                }
            });

            final Calendar c = Calendar.getInstance();
            final NumberPicker collectionTimeH = collectionLayout.findViewById(R.id.collection_time_hour);
            final NumberPicker collectionTimeM = collectionLayout.findViewById(R.id.collection_time_minute);
            collectionTimeH.setMinValue(c.get(Calendar.HOUR_OF_DAY));
            collectionTimeH.setMaxValue(c.get(Calendar.HOUR_OF_DAY) + mNumberPicker.getValue());
            collectionTimeM.setMinValue(c.get(Calendar.MINUTE) + 1);
            collectionTimeM.setMaxValue(59);

            collectPlace = collectionLayout.findViewById(R.id.collection_place_setting_btn);
            collectPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (isVisible()) if (getActivity() != null)
//                        ((HomeActivity) getActivity()).openFragmentPage(GroupSelectCollectionPlaceMapFragment.newInstance(CreateGroupFragment.this),
//                                GroupSelectCollectionPlaceMapFragment.TAG);
                }
            });
            final NumberPicker collectionNoticeTime = collectionLayout.findViewById(R.id.collection_notice_time_minute);
            collectionNoticeTime.setEnabled(false);
            collectionNoticeTime.setAlpha(0.3f);
            final ArrayList<String> collectionNoticeTimes = new ArrayList<>();
            for (int i = 5; i <= 30; i += notice_interval) {
                collectionNoticeTimes.add("" + i);
            }
            final String[] tmp1 = collectionNoticeTimes.toArray(new String[0]);
            collectionNoticeTime.setDisplayedValues(tmp1);
            collectionNoticeTime.setMinValue(0);
            collectionNoticeTime.setMaxValue(0);
            collectionNoticeTime.setValue(0);

            NumberPicker.OnValueChangeListener mOnValueChangeListener = new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                        c.setTimeInMillis(System.currentTimeMillis());
                        if (picker.getId() == mNumberPicker.getId()) {
                            //change the max value of collectionTimeH
                            collectionTimeH.setMaxValue(c.get(Calendar.HOUR_OF_DAY) + mNumberPicker.getValue());
                            //update collection time range
                            if (collectionTimeH.getValue() == collectionTimeH.getMinValue()) {
                                //make sure the collection time can not smaller than current time
                                collectionTimeM.setMaxValue(59);
                                collectionTimeM.setMinValue(c.get(Calendar.MINUTE) + 1);
                            } else if (collectionTimeH.getValue() == collectionTimeH.getMaxValue()) {
                                //make sure the collection time can not bigger than group available time
                                collectionTimeM.setMinValue(0);
                                collectionTimeM.setMaxValue(c.get(Calendar.MINUTE) - 1);
                            } else {
                                collectionTimeM.setMinValue(0);
                                collectionTimeM.setMaxValue(59);
                            }
                        } else if (picker.getId() == collectionTimeH.getId()) {
                            //update collection time range
                            if (collectionTimeH.getValue() == collectionTimeH.getMinValue()) {
                                //make sure the collection time can not smaller than current time
                                collectionTimeM.setMaxValue(59);
                                collectionTimeM.setMinValue(c.get(Calendar.MINUTE) + 1);
                            } else if (collectionTimeH.getValue() == collectionTimeH.getMaxValue()) {
                                //make sure the collection time can not bigger than group available time
                                collectionTimeM.setMinValue(0);
                                if (c.get(Calendar.MINUTE) > 0) {
                                    collectionTimeM.setMaxValue(c.get(Calendar.MINUTE) - 1);
                                } else {
                                    collectionTimeM.setMaxValue(0);
                                }

                            } else {
                                collectionTimeM.setMinValue(0);
                                collectionTimeM.setMaxValue(59);
                            }
                        }

                        //update notice time range
                        int range = (collectionTimeH.getValue() - c.get(Calendar.HOUR_OF_DAY)) * 60 + collectionTimeM.getValue() - c.get(Calendar.MINUTE);
                        Log.e(TAG, "range:" + range);
                        if (range >= 30) {
                            collectionNoticeTime.setMinValue(0);
                            collectionNoticeTime.setMaxValue(tmp1.length - 1);
                            collectionNoticeTime.setEnabled(true);
                            collectionNoticeTime.animate().setDuration(getResources().getInteger(R.integer.config_CollapseAnimTime));
                            collectionNoticeTime.animate().alpha(1f);
                            collectionNoticeTime.animate().start();
                        } else if (range >= 5) {
                            int max = range / notice_interval - 1;
                            collectionNoticeTime.setMinValue(0);
                            collectionNoticeTime.setMaxValue(max);
                            //collectionNoticeTime.setValue(0);
                            collectionNoticeTime.setEnabled(true);
                            collectionNoticeTime.animate().setDuration(getResources().getInteger(R.integer.config_CollapseAnimTime));
                            collectionNoticeTime.animate().alpha(1f);
                            collectionNoticeTime.animate().start();
                        } else {
                            collectionNoticeTime.setEnabled(false);
                            collectionNoticeTime.animate().setDuration(getResources().getInteger(R.integer.config_CollapseAnimTime));
                            collectionNoticeTime.animate().alpha(0.3f);
                            collectionNoticeTime.animate().start();
                        }
                    }
                }
            };

            mNumberPicker.setOnValueChangedListener(mOnValueChangeListener);
            collectionTimeH.setOnValueChangedListener(mOnValueChangeListener);
            collectionTimeM.setOnValueChangedListener(mOnValueChangeListener);

            FrameLayout homeFL = toolbar.findViewById(R.id.only_back_action_bar_fl_home);
            homeFL.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(final View v) {
                    int x_p = (int) (v.getMeasuredWidth() / 2 + v.getX());
                    int y_p = (int) (v.getMeasuredHeight() / 2 + v.getY());
                    //find endRadius
                    Point a = new Point(width, height);
                    Point b = new Point(x_p, y_p);
                    double radius = Tools.getInstance().findDisBetweenTwoP(a, b);
                    //Log.e(TAG, "radius:" + radius + " dis:" + dis + " " + a + " " + b);
                    Log.e(TAG, "width:" + width + " height:" + height + " radius:" + radius);
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(mView, x_p, y_p, (float) radius, 0);
                    circularReveal.setDuration(getResources().getInteger(R.integer.config_CircleCollapseAnimTime));
                    circularReveal.setInterpolator(new FastOutSlowInInterpolator());
                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mView.setAlpha(0f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    //circularReveal.start();
                    mView.findViewById(R.id.mask).setVisibility(View.GONE);
                    mView.findViewById(R.id.mask).setAlpha(1f);
                    getActivity().getSupportFragmentManager().popBackStack();
                    if (titleOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(titleOTIET.getWindowToken());
                        titleOTIET.clearFocus();
                    } else if (nameOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                        nameOTIET.clearFocus();
                    } else if (telTIL.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(telTIL.getWindowToken());
                        telTIL.clearFocus();
                    }
                }
            });

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
            telTIL.setOnFocusChangeListener(mOnFocusChangeListener);
            titleOTIET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (titleTIL.getError() != null) {
                        titleTIL.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.tab_text_unselected_light_gray));
                        titleTIL.setError(null);
                        titleTIL.setHelperTextEnabled(true);
                        //passwordOETLayout.setHelperText();
                    }
                }
            });
            nameOTIET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (nameTIL.getError() != null) {
                        nameTIL.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.tab_text_unselected_light_gray));
                        nameTIL.setError(null);
                        nameTIL.setHelperTextEnabled(true);
                        //passwordOETLayout.setHelperText();
                    }
                }
            });
            telTI.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (telTIL.getError() != null) {
                        telTIL.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.tab_text_unselected_light_gray));
                        telTIL.setError(null);
                        telTIL.setHelperTextEnabled(true);
                        //passwordOETLayout.setHelperText();
                    }
                }
            });
            mView.findViewById(R.id.create_group_dialog_tv_create).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

                    mCreateTitleStr = titleOTIET.getText().toString().trim();
                    String nameStr = nameOTIET.getText().toString().trim();
                    String tel = telTI.getText().toString();
                    if (TextUtils.isEmpty(mCreateTitleStr)) {
                        titleTIL.setError(getString(R.string.input_error_required));
                    } else if (TextUtils.isEmpty(nameStr)) {
                        nameTIL.setError(getString(R.string.input_error_required));
                    } else if (isChineseText(mCreateTitleStr) && mCreateTitleStr.length() > 10) {
                        titleTIL.setError(getString(R.string.zh_text_limit));
                    } else if (mCreateTitleStr.length() > 20) {
                        titleTIL.setError(getString(R.string.eng_text_limit));
                    } else {
                        if (tel.length() > 0) {
                            boolean isPhone = Tools.getInstance().isValidMobileNumber(tel, "TW");
                            Log.e(TAG, "tel:" + tel + " " + isPhone);
                            if (!isPhone) {
                                telTIL.setError(getString(R.string.input_error_wrong_phone_number_format));
                                return;
                            }
                        }

                        String collection_time = "";
                        String notice_time = "";
                        String p_id = "";
                        String p_type = "";
                        if (enable_collection_finction.isChecked()) {


                            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            DateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
                            Calendar tmp = Calendar.getInstance();
                            tmp.setTime(new Date());
                            tmp.set(Calendar.HOUR_OF_DAY, collectionTimeH.getValue());
                            tmp.set(Calendar.MINUTE, collectionTimeM.getValue());
                            try {
                                String date_ = "" + String.format("%04d", tmp.get(Calendar.YEAR)) + String.format("%2d", tmp.get(Calendar.MONTH) + 1) + String.format("%02d", tmp.get(Calendar.DAY_OF_MONTH)) +
                                        " " + String.format("%02d", tmp.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", tmp.get(Calendar.MINUTE)) + "00";
                                collection_time = dateFormatter.format(df.parse(date_));
                                //Log.e(TAG, "collection_time:" + collection_time + " collectionTimeH:" + collectionTimeH.getValue() + " collectionTimeM:" + collectionTimeM.getValue() + "\n" + date_);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (collectionPlacePOI != null) {
                                p_id = String.valueOf(collectionPlacePOI.getId());
                                switch (collectionPlacePOI.getType()) {
                                    default:
                                        p_type = "poi";
                                        break;
                                }
                            }

                            if (collectionNoticeTime.isEnabled()) {
                                int before = Integer.parseInt(collectionNoticeTimes.get(collectionNoticeTime.getValue()));
                                tmp.add(Calendar.MINUTE, -before);
                                try {
                                    String date_ = "" + String.format("%04d", tmp.get(Calendar.YEAR)) + String.format("%2d", tmp.get(Calendar.MONTH) + 1) + String.format("%02d", tmp.get(Calendar.DAY_OF_MONTH)) +
                                            " " + String.format("%02d", tmp.get(Calendar.HOUR_OF_DAY)) + String.format("%02d", tmp.get(Calendar.MINUTE)) + "00";
                                    notice_time = dateFormatter.format(df.parse(date_));
                                    //Log.e(TAG, "collection_time:" + collection_time + " collectionTimeH:" + collectionTimeH.getValue() + " collectionTimeM:" + collectionTimeM.getValue() + "\n" + date_);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                            }

                        }

                        mView.findViewById(R.id.create_group_loading).setVisibility(View.VISIBLE);
                        TpApi.getInstance().createGroup(mContext, mCreateTitleStr, nameStr, tel, String.valueOf(mNumberPicker.getValue()),
                                notice_time, collection_time, p_type, p_id, "",
                                new NetworkManager.NetworkManagerListener<CreateGroupResponse>() {
                                    @Override
                                    public void onSucceed(CreateGroupResponse response) {
                                        //dialog.dismiss();
                                        if (response != null) {
                                            if (response.getResult().equals("true")) {
                                                //set notice to alarm manager if the group was created


                                                showCustomCreatGroupSucessDialog(response);
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
            });


            //enable function of tnmns spread one
            if (!BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                mView.findViewById(R.id.create_group_dialog_til_tel).setVisibility(View.GONE);
                mView.findViewById(R.id.collection_layout).setVisibility(View.GONE);
            }

        }
        return mView;
    }

    boolean shown = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onStart() {
        super.onStart();

        if (!shown) {
            Point a = new Point(0, 0);
            Point b = new Point(width / 2, height / 2);
            double radius = Tools.getInstance().findDisBetweenTwoP(a, b);
            //Log.e(TAG, "radius:" + radius + " dis:" + dis + " " + a + " " + b);
            Log.e(TAG, "width:" + width + " height:" + height + " radius:" + radius);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mView, width / 2, height / 2, 0, (float) radius);
            circularReveal.setDuration(getResources().getInteger(R.integer.config_CircleExpandAnimTime));
            circularReveal.setInterpolator(new FastOutSlowInInterpolator());
            //circularReveal.start();
            mView.findViewById(R.id.mask).setVisibility(View.GONE);
            mView.findViewById(R.id.mask).setAlpha(1f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mView.findViewById(R.id.mask),
                    "alpha",
                    1f,
                    0f);
            alphaAnimator.setDuration(getResources().getInteger(R.integer.config_CircleExpandAnimTime));
            alphaAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mView.findViewById(R.id.mask).setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            alphaAnimator.start();
            shown = true;
        }

    }

    POI collectionPlacePOI;

    public void updateCollectionPlaceData(POI target) {
        collectionPlacePOI = target;
        String tmp = target.getName();
        collectPlace.setText(tmp);
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


    public void showCustomCreatGroupSucessDialog(final CreateGroupResponse response) {

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

        titleTv.setText(getString(R.string.group_key_is) + response.getData()[0].getKey());

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int x_p = 0;
            int y_p = 0;
            //find endRadius
            Point a = new Point(width / 2, height);
            Point b = new Point(x_p, y_p);
            double radius = Tools.getInstance().findDisBetweenTwoP(a, b);
            //Log.e(TAG, "radius:" + radius + " dis:" + dis + " " + a + " " + b);
            Log.e(TAG, "width:" + width + " height:" + height + " radius:" + radius);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mView, width / 2, height, (float) radius, 0);
            circularReveal.setDuration(getResources().getInteger(R.integer.config_CircleCollapseAnimTime));
            circularReveal.setInterpolator(new FastOutSlowInInterpolator());
            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mView.setAlpha(0f);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            //circularReveal.start();
            mView.findViewById(R.id.mask).setVisibility(View.GONE);
            mView.findViewById(R.id.mask).setAlpha(1f);
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            return true;
        }
        return false;
    }

    public static boolean isChineseText(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block) ||
                    Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block) ||
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)) {
                return true;
            }
        }
        return false;
    }
}
