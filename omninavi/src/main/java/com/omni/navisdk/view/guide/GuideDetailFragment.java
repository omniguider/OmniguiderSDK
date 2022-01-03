package com.omni.navisdk.view.guide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.omni.navisdk.NaviSDKActivity;
import com.omni.navisdk.R;
import com.omni.navisdk.module.OfficialEvent;
import com.omni.navisdk.module.POI;
import com.omni.navisdk.network.LocationApi;
import com.omni.navisdk.network.NetworkManager;

import org.jetbrains.annotations.NotNull;

import static com.omni.navisdk.tool.NaviSDKText.LOG_TAG;

public class GuideDetailFragment extends Fragment {

    public static final String TAG = "fragment_tag_guide_detail";
    private static final String ARG_KEY_DATA = "ARG_KEY_DATA";
    private static final String ARG_KEY_DOMAIN_NAME = "arg_key_domain_name";
    private static final String ARG_KEY_MAP_BEARING = "arg_key_map_bearing";
    private static final String ARG_KEY_AUTO_HEADING = "arg_key_auto_heading";
    private static final String ARG_KEY_ENCRYPT_KEY = "arg_key_encrypt_key";
    private static final String ARG_KEY_THEME_ID = "arg_key_theme_id";
    private static final String ARG_KEY_THEME_PLAN_ID = "arg_key_theme_plan_id";
    private static final String ARG_KEY_SEARCH_POI = "arg_key_search_poi";

    private View mView;
    private LinearLayout contentLL;
    private View itemGuideView;
    private OfficialEvent officialEvent;

    public static GuideDetailFragment newInstance(OfficialEvent officialEvent) {
        GuideDetailFragment fragment = new GuideDetailFragment();

        Bundle arg = new Bundle();
        arg.putParcelable(ARG_KEY_DATA, officialEvent);
        fragment.setArguments(arg);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        officialEvent = getArguments().getParcelable(ARG_KEY_DATA);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_guide_detail, null, false);

            mView.findViewById(R.id.fragment_guide_detail_fl_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            NetworkManager.Companion.getInstance().setNetworkImage(getContext(),
                    mView.findViewById(R.id.fragment_guide_detail_niv),
                    officialEvent.getImage());

            ((TextView) mView.findViewById(R.id.fragment_guide_detail_title)).setText(officialEvent.getTitle());
            ((TextView) mView.findViewById(R.id.fragment_guide_detail_point_number)).setText(
                    String.format(getString(R.string.activity_home_guide_point_number),
                            officialEvent.getResult().getExhibits().size()));

            contentLL = mView.findViewById(R.id.fragment_guide_detail_ll_contents);
            contentLL.removeAllViews();
            for (int i = 0; i < officialEvent.getResult().getExhibits().size(); i++) {
                itemGuideView = LayoutInflater.from(getContext()).inflate(R.layout.item_guide_detail, container, false);
                ((TextView) itemGuideView.findViewById(R.id.item_guide_detail_title)).setText(
                        officialEvent.getResult().getExhibits().get(i).getTitle_zh());
                int finalI = i;
                itemGuideView.findViewById(R.id.item_guide_detail_map).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), NaviSDKActivity.class);
                        intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/");
                        intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://");
                        intent.putExtra(ARG_KEY_MAP_BEARING, 0f);
                        intent.putExtra(ARG_KEY_AUTO_HEADING, true);
                        POI searchPOI = new POI();
                        searchPOI.setId(officialEvent.getResult().getE_ids().get(finalI));
                        intent.putExtra(ARG_KEY_SEARCH_POI, searchPOI);
                        startActivity(intent);
                    }
                });
                contentLL.addView(itemGuideView);
            }

//            mView.findViewById(R.id.naviBtn).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity(), NaviSDKActivity.class);
//                    intent.putExtra(ARG_KEY_DOMAIN_NAME, "https://navi.taipei/");
//                    intent.putExtra(ARG_KEY_ENCRYPT_KEY, "doitapp://");
//                    intent.putExtra(ARG_KEY_MAP_BEARING, 0f);
//                    intent.putExtra(ARG_KEY_AUTO_HEADING, true);
//                    intent.putExtra(ARG_KEY_THEME_ID, officialEvent.getResult().getE_ids());
//                    intent.putExtra(ARG_KEY_THEME_PLAN_ID, officialEvent.getResult().getExhibits().get(0).getPlan_id());
//                    startActivity(intent);
//                }
//            });
        }
        return mView;
    }

    private void openFragmentPage(Fragment fragment, String tag) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_home_fl, fragment, tag)
                .addToBackStack(null)
                .commit();
    }
}
