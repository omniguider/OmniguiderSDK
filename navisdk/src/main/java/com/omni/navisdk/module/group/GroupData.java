package com.omni.navisdk.module.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GroupData implements Serializable {

    @SerializedName("info")
    private GroupInfo[] groupInfos;
    @SerializedName("members")
    private GroupMember[] groupMembers;

    public GroupInfo[] getGroupInfos() {
        return groupInfos;
    }

    public void setGroupInfos(GroupInfo[] groupInfos) {
        this.groupInfos = groupInfos;
    }

    public GroupMember[] getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(GroupMember[] groupMembers) {
        this.groupMembers = groupMembers;
    }
}
