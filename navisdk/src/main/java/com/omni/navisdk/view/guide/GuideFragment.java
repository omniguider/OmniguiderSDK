package com.omni.navisdk.view.guide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.omni.navisdk.R;
import com.omni.navisdk.manager.AnimationFragmentManager;
import com.omni.navisdk.module.OfficialEvent;
import com.omni.navisdk.network.LocationApi;
import com.omni.navisdk.network.NetworkManager;

import org.jetbrains.annotations.NotNull;

public class GuideFragment extends Fragment {

    public static final String TAG = "fragment_tag_guide";

    private View mView;
    private LinearLayout contentLL;
    private View itemGuideView;

    public static GuideFragment newInstance() {
        GuideFragment fragment = new GuideFragment();

        Bundle arg = new Bundle();
        fragment.setArguments(arg);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_guide, null, false);

            mView.findViewById(R.id.fragment_guide_fl_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            contentLL = mView.findViewById(R.id.fragment_guide_ll_contents);

            LocationApi.Companion.getInstance().getThemeGuide(requireActivity(),
                    new NetworkManager.NetworkManagerListener<OfficialEvent[]>() {
                        @Override
                        public void onSucceed(OfficialEvent[] officialEvents) {
                            Log.e("LOG", "getThemeGuide" + officialEvents.length);
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                contentLL.removeAllViews();
                                for (int i = 0; i < officialEvents.length; i++) {
                                    itemGuideView = LayoutInflater.from(getContext()).inflate(R.layout.item_guide, container, false);

                                    NetworkManager.Companion.getInstance().setNetworkImage(getContext(),
                                            itemGuideView.findViewById(R.id.item_guide_niv),
                                            officialEvents[i].getImage());

                                    ((TextView) itemGuideView.findViewById(R.id.item_guide_title)).setText(officialEvents[i].getTitle());
                                    ((TextView) itemGuideView.findViewById(R.id.item_guide_point_number)).setText(
                                            String.format(getString(R.string.activity_home_guide_point_number),
                                                    officialEvents[i].getResult().getExhibits().size()));
                                    int finalI = i;
                                    itemGuideView.findViewById(R.id.item_guide_ll).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            openFragmentPage(GuideDetailFragment.newInstance(officialEvents[finalI]),
                                                    GuideDetailFragment.TAG);
                                        }
                                    });

                                    contentLL.addView(itemGuideView);
                                }
                            });
                        }

                        @Override
                        public void onFail(@NotNull String errorMsg, boolean shouldRetry) {
                            Log.e("LOG", "getThemeGuide" + errorMsg);
                        }
                    });
        }
        return mView;
    }

    private void openFragmentPage(Fragment fragment, String tag) {
        AnimationFragmentManager.getInstance().addFragmentPage(getActivity(), R.id.activity_home_fl, fragment, tag);
    }
}
