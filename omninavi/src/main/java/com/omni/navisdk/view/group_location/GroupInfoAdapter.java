package com.omni.navisdk.view.group_location;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omni.navisdk.R;
import com.omni.navisdk.module.group.GroupData;
import com.omni.navisdk.module.group.GroupInfo;
import com.omni.navisdk.module.group.GroupMember;
import com.omni.navisdk.tool.NameCircleViewMaker;
import com.omni.navisdk.tool.PreferencesTools;
import com.omni.navisdk.view.CircleNetworkImageView;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoAdapter extends RecyclerView.Adapter<GroupInfoAdapter.GroupInfoViewHolder> {

    private Activity mActivity;
    private List<GroupData> mGroupDataList;
    private GroupInfo mCurrentGroupVisionInfo;
    private AdapterListener mListener;

    public interface AdapterListener {
        void onItemClicked(GroupData groupData);

        void onGroupLeave(GroupData groupData, GroupInfoViewHolder holder);
    }


    public GroupInfoAdapter(Activity activity, List<GroupData> groupData, GroupInfo currentGroupInfo, AdapterListener listener) {
        mActivity = activity;
        //remove the group was zero member, remove the group does not with creator
        ArrayList<GroupData> tmp = new ArrayList<>();
        for (GroupData gd : groupData) {
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

        mCurrentGroupVisionInfo = currentGroupInfo;
        mListener = listener;
    }

    public void updataAdapter(List<GroupData> groupData, GroupInfo currentGroupInfo) {
        //remove the group was zero member
        ArrayList<GroupData> tmp = new ArrayList<>();
        for (GroupData gd : groupData)
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
        mGroupDataList = tmp;

        mCurrentGroupVisionInfo = currentGroupInfo;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mGroupDataList.size();
    }


    @Override
    public GroupInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.group_info_item_view1, null);

        GroupInfoViewHolder viewHolder = new GroupInfoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupInfoViewHolder holder, int position) {
        final GroupData groupData = mGroupDataList.get(position);
        final GroupInfo groupInfo = groupData.getGroupInfos()[0];

        GroupMember adminMember = null;
        for (GroupMember member : groupData.getGroupMembers()) {
            if (member.getRole().equals(GroupMember.ADMIN)) {
                adminMember = member;
                break;
            }
        }
        if (adminMember == null) {
            adminMember = groupData.getGroupMembers()[0];
        }
        TextView circleView = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.name_circle_view, null);
        circleView.setText(adminMember.getName().substring(0, 1));

        holder.iconCNIV.setImageBitmap(NameCircleViewMaker.getInstance().getBitmapFromView(circleView));

        String title = groupInfo.getTitle() + " (" + groupData.getGroupMembers().length + ")";
        holder.titleTV.setText(title);
        holder.keyTv.setText(mActivity.getString(R.string.group_key) + groupInfo.getKey());

        holder.visionRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesTools.Companion.getInstance().saveProperty(mActivity,
                        PreferencesTools.Companion.getInstance().KEY_CURRENT_GROUP, groupInfo);

                updataAdapter(mGroupDataList, groupInfo);
            }
        });
        final GroupInfoViewHolder holder_ = holder;
        holder.leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onGroupLeave(groupData, holder_);
            }
        });
        holder.touch_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e(TAG, "key : " + groupInfo.getKey());
                //EventBus.getDefault().post(new OmniEvent(OmniEvent.TYPE_OPEN_GROUP_MEMBERS_PAGE, groupData));

                if (mListener != null) {
                    mListener.onItemClicked(groupData);
                }

            }
        });
    }

    class GroupInfoViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout infoLayout;
        private CircleNetworkImageView iconCNIV;
        private TextView titleTV;
        private RelativeLayout visionRL;
        private ImageView visionIV;
        private TextView keyTv;
        private TextView leave;
        public View touch_receiver;
        private float startX, startY, endX, endY, x, y;
        private long downTime;

        public GroupInfoViewHolder(final View itemView) {
            super(itemView);
            infoLayout = itemView.findViewById(R.id.info_view);
            iconCNIV = itemView.findViewById(R.id.group_info_item_view_cniv);
            titleTV = itemView.findViewById(R.id.group_info_item_view_tv_title);
            visionRL = itemView.findViewById(R.id.group_info_item_view_rl_vision);
            keyTv = itemView.findViewById(R.id.group_info_itm_view_key);
            leave = itemView.findViewById(R.id.leave);
            touch_receiver = itemView.findViewById(R.id.touch_receiver);
            touch_receiver.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.e(getClass().getSimpleName(), "onTouch");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            downTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (System.currentTimeMillis() - downTime > 100) {

                            }
                            Log.e(getClass().getSimpleName(), "" + infoLayout.getX() + " " + itemView.getX() + " " + (itemView.getX() - leave.getMeasuredWidth() * 1.3));
                            if (infoLayout.getX() > -leave.getMeasuredWidth() * 1.3 && infoLayout.getX() <= 0)
                                infoLayout.setX(infoLayout.getX() + (event.getX() - x));
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            endX = event.getX();
                            endY = event.getY();
                            if (System.currentTimeMillis() - downTime <= 100) {
                                touch_receiver.performClick();
                                infoLayout.setX(0);
                                touch_receiver.setX(0);
                                return false;
                            } else {

                            }
                        default:
                            if (infoLayout.getX() < -leave.getMeasuredWidth() / 2) {
                                infoLayout.animate().x(itemView.getX() - leave.getMeasuredWidth());
                            } else if (infoLayout.getX() >= -leave.getMeasuredWidth() / 2) {
                                infoLayout.animate().x(itemView.getX());
                            }
                            touch_receiver.setX(infoLayout.getX());
                            infoLayout.animate().setDuration(mActivity.getResources().getInteger(R.integer.config_CollapseAnimTime));
                            infoLayout.animate().start();
                            break;
                    }
                    x = event.getX();
                    y = event.getY();
                    return true;
                }
            });
        }

    }
}
