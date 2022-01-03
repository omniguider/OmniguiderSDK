package com.omni.navisdk.view.group_location;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
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
import com.omni.navisdk.module.group.GroupData;
import com.omni.navisdk.module.group.GroupInfo;
import com.omni.navisdk.module.group.JoinGroupResponse;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.network.TpApi;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.tool.Tools;
import com.omni.navisdk.view.FragmentOnKeyEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.omni.navisdk.tool.PreferencesTools.KEY_LEFT_GROUP_ENDTIME;

public class JoinGroupFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "fragment_add_group";

    private Context mContext;
    private View mView;
    private boolean joinResult = false;
    private TextInputLayout keyTIL;
    private TextInputEditText keyOTIET;
    private TextInputLayout nameTIL;
    private TextInputEditText nameOTIET;

    int height, width;
    private GroupMainFragment.DialogFragmentCloseListener listener;
    private static GroupMainFragment groupMainFragment;

    public static JoinGroupFragment newInstance(GroupMainFragment f) {
        JoinGroupFragment fragment = new JoinGroupFragment();
        groupMainFragment = f;
        return fragment;
    }

    public static JoinGroupFragment newInstance() {
        JoinGroupFragment fragment = new JoinGroupFragment();
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

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.join_group_dialog, null, false);


            Toolbar toolbar = mView.findViewById(R.id.hint_action_bar);
            TextView titleTV = toolbar.findViewById(R.id.hint_action_bar_tv_title);
            titleTV.setText(R.string.group_page_join_group);

            keyTIL = mView.findViewById(R.id.join_group_dialog_til_key);
            keyOTIET = mView.findViewById(R.id.join_group_dialog_otiet_key);
            nameTIL = mView.findViewById(R.id.join_group_dialog_til_name);
            nameOTIET = mView.findViewById(R.id.join_group_dialog_otiet_name);

            FrameLayout homeFL = toolbar.findViewById(R.id.only_back_action_bar_fl_home);
            homeFL.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    //hide keyboard
                    if (keyOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(keyOTIET.getWindowToken());
                        keyOTIET.clearFocus();
                    } else if (nameOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                        nameOTIET.clearFocus();
                    }
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
                }
            });


            if (!BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                nameOTIET.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            final TextInputLayout telTIL = mView.findViewById(R.id.join_group_dialog_til_tel);
            final TextInputEditText telTI = mView.findViewById(R.id.join_group_dialog_til_tel_edt);
            TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (v.getId() == keyOTIET.getId()) {
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
            keyOTIET.setOnEditorActionListener(mOnEditorActionListener);
            nameOTIET.setOnEditorActionListener(mOnEditorActionListener);
            telTI.setOnEditorActionListener(mOnEditorActionListener);

            keyOTIET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (keyTIL.getError() != null) {
                        keyTIL.setBoxStrokeColor(ContextCompat.getColor(getContext(), R.color.tab_text_unselected_light_gray));
                        keyTIL.setError(null);
                        keyTIL.setHelperTextEnabled(true);
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

            mView.findViewById(R.id.join_group_dialog_tv_join).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String keyStr = keyOTIET.getText().toString().trim();
                    String nameStr = nameOTIET.getText().toString().trim();
                    String tel = telTI.getText().toString();
                    if (TextUtils.isEmpty(nameStr)) {
                        nameTIL.setError(getString(R.string.input_error_required));
                    } else if (TextUtils.isEmpty(keyStr)) {
                        keyTIL.setError(getString(R.string.input_error_required));
                    } else {
                        if (keyTIL.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(keyTIL.getWindowToken());
                            keyTIL.clearFocus();
                        } else if (nameTIL.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameTIL.getWindowToken());
                            nameTIL.clearFocus();
                        } else if (tel.length() > 0) {
                            boolean isPhone = Tools.getInstance().isValidMobileNumber(tel, "TW");
                            if (!isPhone) {
                                telTIL.setError(getString(R.string.input_error_wrong_phone_number_format));
                                return;
                            }
                        }

                        TpApi.getInstance().joinGroup(mContext, keyStr, nameStr, tel, "",
                                new NetworkManager.NetworkManagerListener<JoinGroupResponse>() {
                                    @Override
                                    public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                        joinResult = false;
                                        showJoinInfoDialog(getString(R.string.join_group_fail), getString(R.string.error_dialog_title_text_unknown), false);
                                    }

                                    @Override
                                    public void onSucceed(JoinGroupResponse response) {
                                        Log.e(TAG, "response result : " + response.getResult() + ", response err msg : " + response.getErrorMessage() +
                                                ", msg : " + response.getMessage());

                                        //GroupJoinFragment.this.dismiss();
                                        if (response.getResult().equals("true")) {
                                            joinResult = true;
                                            showJoinInfoDialog(getString(R.string.join_group_sucessfully), "", true);

                                        } else if (response.getResult().equals("false")) {
                                            if (response.getErrorMessage().equals("DUPLICATE DEVICE_ID")) {
                                                joinResult = true;
                                                showJoinInfoDialog(getString(R.string.join_group_sucessfully), getString(R.string.dialog_error_msg_duplicate_device_id), true);
                                            } else {
                                                showJoinInfoDialog(getString(R.string.join_group_fail), getString(R.string.dialog_error_msg_invalid_group), false);
                                            }
//                                        DialogTools.getInstance().showErrorMessage(mContext,
//                                                R.string.error_dialog_title_text_normal,
//                                                getString(R.string.dialog_error_msg_invalid_group));
                                        }
                                    }
                                });
                    }
                }
            });


            //enable function of tnmns spread one
            if (!BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                mView.findViewById(R.id.join_group_dialog_til_tel).setVisibility(View.GONE);
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


    public void showJoinInfoDialog(String title, String cause, final boolean closePage) {

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
        if (title != null) {
            if (title.length() > 0) {
                titleTv.setText(title);
                titleTv.setVisibility(View.VISIBLE);
            } else {
                titleTv.setVisibility(View.GONE);
            }
        } else {
            titleTv.setVisibility(View.GONE);
        }
        if (cause != null) {
            if (cause.length() > 0) {
                contentTv.setText(cause);
                contentTv.setVisibility(View.VISIBLE);
            } else {
                contentTv.setVisibility(View.GONE);
            }
        } else {
            contentTv.setVisibility(View.GONE);
        }


        TextView okBtn = req.findViewById(R.id.rw_collect_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                //finish();
                req.dismiss();
                if (joinResult) {
                    if (getActivity() != null) {
                        ((HomeActivity) getActivity()).findFriendMode(true);
                    }
                    //remove old left group data if the key same as the key
                    TextInputEditText keyOTIET = mView.findViewById(R.id.join_group_dialog_otiet_key);
                    String keyStr = keyOTIET.getText().toString().trim();
                    SharedPreferences s = PreferencesTools.Companion.getInstance().getPreferences(getContext());
                    String oldLeftGroupData = s.getString(KEY_LEFT_GROUP_ENDTIME, "");
                    GroupData[] oldLeftGroups = null;

                    if (oldLeftGroupData.length() > 0) {
                        oldLeftGroups = NetworkManager.Companion.getInstance().getGson().fromJson(oldLeftGroupData, GroupData[].class);
                        List<GroupData> usefulData = new ArrayList<>();
                        for (GroupData gd : oldLeftGroups) {
                            if (gd == null) continue;
                            GroupInfo gi = gd.getGroupInfos()[0];
                            String key = gi.getKey();
                            if (key != null) if (key.length() > 0) if (!key.equals(keyStr)) {
                                usefulData.add(gd);
                            }
                        }
                        //save useful data
                        String newLeftGroupData = NetworkManager.Companion.getInstance().getGson().toJson(usefulData.toArray(new GroupData[0]), GroupData[].class);
                        Log.e(TAG, "newLeftGroupData:" + newLeftGroupData);
                        s.edit().putString(KEY_LEFT_GROUP_ENDTIME, newLeftGroupData).apply();
                    }
                }
                if (closePage)
                    collapseToBottom();
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
    private void collapseToBottom() {
        //hide keyboard
        if (keyOTIET.isFocused()) {
            ((HomeActivity) getActivity()).hideKeyboard(keyOTIET.getWindowToken());
            keyOTIET.clearFocus();
        } else if (nameOTIET.isFocused()) {
            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
            nameOTIET.clearFocus();
        }
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
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            collapseToBottom();
            return true;
        }
        return false;
    }
}
