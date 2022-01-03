package com.omni.navisdk.view.group_location;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.omni.navisdk.R;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.view.FragmentOnKeyEvent;


public class GroupIntroFragment extends Fragment implements FragmentOnKeyEvent {

    public static final String TAG = "GroupMainFragment";
    private static final String ARG_KEY_OPEN_FROM = "arg_key_open_from";

    public class OpenFrom {
        public static final int OPEN_FROM_HOME_DIALOG_FRAGMENT = 20;
        public static final int OPEN_FROM_MAIN_TAB_ACTIVITY = 21;
    }

    public static GroupIntroFragment newInstance(String p) {
        GroupIntroFragment fragment = new GroupIntroFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TOUCH_POINT, p);
        fragment.setArguments(bundle);
        return fragment;
    }

    private Context mContext;
    private View mView;
    int height,width;


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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {

            mView = inflater.inflate(R.layout.group_intro_fragment_view, container, false);
        }

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.group_intro_fragment_view_action_bar);
        TextView titleTV = toolbar.findViewById(R.id.hint_action_bar_tv_title);
        titleTV.setText(getString(R.string.home_page_option_group_location_intro));
        FrameLayout homeFL = toolbar.findViewById(R.id.only_back_action_bar_fl_home);
        homeFL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyUp(KeyEvent.KEYCODE_BACK, null);
            }
        });
        ((TextView)view.findViewById(R.id.rec_dialog_title)).setText(getString(R.string.group_key_is) + "9876");

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showIntro = sharedPreferences.getBoolean(PreferencesTools.KEY_SHOW_INTRO_FIND_FRIEND, true);
        CheckBox dont_show_intro = mView.findViewById(R.id.intro_do_not_show_again);
        dont_show_intro.setChecked(!showIntro);
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume(){
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            getActivity().getSupportFragmentManager().popBackStack();
            //show intro again?
            CheckBox dont_show_intro = mView.findViewById(R.id.intro_do_not_show_again);
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putBoolean(PreferencesTools.KEY_SHOW_INTRO_FIND_FRIEND, !dont_show_intro.isChecked()).apply();

            return true;
        }
        return false;
    }

}
