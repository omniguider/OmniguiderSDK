package com.omni.navisdk.view.group_location;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.omni.navisdk.BuildConfig;
import com.omni.navisdk.HomeActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.module.group.GroupData;
import com.omni.navisdk.module.group.GroupInfo;
import com.omni.navisdk.module.group.JoinGroupResponse;
import com.omni.navisdk.network.NetworkManager;
import com.omni.navisdk.network.TpApi;
import com.omni.navisdk.tool.DialogTools;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.tool.Tools;
import com.omni.navisdk.view.FragmentOnKeyEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.omni.navisdk.tool.PreferencesTools.KEY_LEFT_GROUP_ENDTIME;

public class JoinGroupTestFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "fragment_add_group";

    private Context mContext;
    private View mView;
    private boolean joinResult = false;

    private TextInputEditText nameOTIET;
    private TextInputEditText keyOTIET;
    private TextInputEditText telTI;
    int height, width;
    private GroupMainFragment.DialogFragmentCloseListener listener;
    private static GroupMainFragment groupMainFragment;

    public static JoinGroupTestFragment newInstance(GroupMainFragment f) {
        JoinGroupTestFragment fragment = new JoinGroupTestFragment();
        groupMainFragment = f;
        return fragment;
    }

    public static JoinGroupTestFragment newInstance() {
        JoinGroupTestFragment fragment = new JoinGroupTestFragment();
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
            mView = inflater.inflate(R.layout.join_group_view, null, false);


            Toolbar toolbar = mView.findViewById(R.id.hint_action_bar);
            TextView titleTV = toolbar.findViewById(R.id.hint_action_bar_tv_title);
            titleTV.setText(R.string.group_page_join_group);
            nameOTIET = mView.findViewById(R.id.join_user_name);
            keyOTIET = mView.findViewById(R.id.join_group_key);
            telTI = mView.findViewById(R.id.join_group_phone);

            FrameLayout homeFL = toolbar.findViewById(R.id.only_back_action_bar_fl_home);
            homeFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //hide keyboard
                    if (keyOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(keyOTIET.getWindowToken());
                        keyOTIET.clearFocus();
                    } else if (nameOTIET.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                        nameOTIET.clearFocus();
                    } else if (telTI.isFocused()) {
                        ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                        telTI.clearFocus();
                    }
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            String language = Locale.getDefault().getLanguage();
            if (language.equals("en")) {
                int maxLength = 20;
                nameOTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }

            TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (v.getId() == keyOTIET.getId()) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            telTI.requestFocus();
                        }
                    } else if (v.getId() == nameOTIET.getId()) {
                        if (BuildConfig.SHOW_TNMNS_SPREAD_ONE) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                keyOTIET.requestFocus();
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
            keyOTIET.setOnEditorActionListener(mOnEditorActionListener);
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

            mView.findViewById(R.id.join_group_dialog_tv_join).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String keyStr = keyOTIET.getText().toString().trim();
                    String nameStr = nameOTIET.getText().toString().trim();
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
                    } else if (TextUtils.isEmpty(keyStr)) {
                        //keyTIL.setError(getString(R.string.input_error_required));
                        DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_join_group_input_key);
                    } else if (keyStr.length() != 4) {
                        //keyTIL.setError(getString(R.string.input_error_required));
                        DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_join_group_input_key);
                    } else {
                        //hide keyboard
                        if (keyOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(keyOTIET.getWindowToken());
                            keyOTIET.clearFocus();
                        } else if (nameOTIET.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
                            nameOTIET.clearFocus();
                        } else if (telTI.isFocused()) {
                            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
                            telTI.clearFocus();
                        }
                        if (tel.length() > 0) {
                            boolean isPhone = Tools.getInstance().isValidMobileNumber(tel, "TW");
                            if (!isPhone) {
                                //telTI.setError(getString(R.string.input_error_wrong_phone_number_format));
                                DialogTools.Companion.getInstance().showHintDialog(getActivity(), R.string.dialog_hint_create_group_tel);
                                return;
                            }
                        }
                        TpApi.getInstance().joinGroup(mContext, keyStr, nameStr, tel, "",
                                new NetworkManager.NetworkManagerListener<JoinGroupResponse>() {
                                    @Override
                                    public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                                        joinResult = false;
                                    }

                                    @Override
                                    public void onSucceed(JoinGroupResponse response) {
                                        Log.e(TAG, "response result : " + response.getResult() + ", response err msg : " + response.getErrorMessage() +
                                                ", msg : " + response.getMessage());
                                        if (getActivity() == null) return;
                                        //GroupJoinFragment.this.dismiss();
                                        getActivity().runOnUiThread(() -> {
                                            if (response.getResult().equals("true")) {
                                                joinResult = true;
                                                showJoinInfoDialog(getString(R.string.join_group_sucessfully), "");

                                            } else if (response.getResult().equals("false")) {
                                                if (response.getErrorMessage().equals("DUPLICATE DEVICE_ID")) {
                                                    joinResult = true;
                                                    showJoinInfoDialog(getString(R.string.join_group_sucessfully), getString(R.string.dialog_error_msg_duplicate_device_id));
                                                } else if (response.getErrorMessage().equals("DUPLICATE NAME")) {
                                                    showJoinInfoDialog(getString(R.string.join_group_fail), getString(R.string.dialog_error_msg_invalid_name));
                                                } else {
                                                    showJoinInfoDialog(getString(R.string.join_group_fail), getString(R.string.dialog_error_msg_invalid_group));
                                                }
//                                        DialogTools.getInstance().showErrorMessage(mContext,
//                                                R.string.error_dialog_title_text_normal,
//                                                getString(R.string.dialog_error_msg_invalid_group));
                                            }
                                        });
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

    public void onStart() {
        super.onStart();
    }


    public void showJoinInfoDialog(String title, String cause) {
        if (getActivity() == null) return;
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
            @Override
            public void onClick(View v) {
                //finish();
                req.dismiss();
                if (joinResult) {
                    if (getActivity() != null) {
                        ((HomeActivity) getActivity()).findFriendMode(true);
                    }
                    //remove old left group data if the key same as the key
                    TextInputEditText keyOTIET = mView.findViewById(R.id.join_group_key);
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
                collapseToBottom();
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        req.show();
        //req.getWindow().setAttributes(lp);
        req.getWindow().setLayout((7 * width) / 7, (5 * height) / 5);
        mView.findViewById(R.id.joingroup_loading).setVisibility(View.GONE);
    }

    private void collapseToBottom() {
        //hide keyboard
        if (keyOTIET.isFocused()) {
            ((HomeActivity) getActivity()).hideKeyboard(keyOTIET.getWindowToken());
            keyOTIET.clearFocus();
        } else if (nameOTIET.isFocused()) {
            ((HomeActivity) getActivity()).hideKeyboard(nameOTIET.getWindowToken());
            nameOTIET.clearFocus();
        } else if (telTI.isFocused()) {
            ((HomeActivity) getActivity()).hideKeyboard(telTI.getWindowToken());
            telTI.clearFocus();
        }
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            collapseToBottom();
            return true;
        }
        return false;
    }
}
