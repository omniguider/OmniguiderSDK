package com.omni.omninavi.omninavi.model.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wiliiamwang on 03/06/2017.
 */

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
