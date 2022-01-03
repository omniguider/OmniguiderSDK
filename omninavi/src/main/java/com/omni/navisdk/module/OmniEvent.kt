package com.omni.navisdk.module

import android.os.Bundle

class OmniEvent {
    var type: Int
        private set
    var content: String? = null
        private set
    var obj: Any? = null
        private set
    var arguments: Bundle? = null
        private set

    constructor(type: Int, content: String?) {
        this.type = type
        this.content = content
    }

    constructor(type: Int, obj: Any?) {
        this.type = type
        this.obj = obj
    }

    constructor(type: Int, args: Bundle?) {
        this.type = type
        arguments = args
    }

    companion object {
        const val TYPE_CLICK_HOME = 19
        const val TYPE_BOOK_DETAIL_INFO_PAGE_DESTROYED = 20
        const val TYPE_REQUEST_LAST_LOCATION = 21
        const val TYPE_USER_OUTDOOR_LOCATION = 22
        const val TYPE_USER_INDOOR_LOCATION = 23
        const val TYPE_FLOOR_PLAN_CHANGED = 24
        const val TYPE_POIS_ADDED = 25
        const val TYPE_NAVIGATION_MODE_CHANGED = 26
        const val TYPE_NOTIFICATION_HISTORY_STATUS_CHANGED = 27
        const val TYPE_REQUEST_CURRENT_FLOOR = 28
        const val TYPE_FIREBASE_TOKEN_CHANGED = 29
        const val TYPE_RECEIVED_FIREBASE_MESSAGE = 30
        const val TYPE_REQUEST_USER_LOCATION_INDOOR_ONLY = 31
        const val TYPE_REQUEST_USER_LOCATION_OUTDOOR_ONLY = 32
        const val TYPE_REQUEST_USER_LOCATION_AUTOMATICALLY = 33
        const val TYPE_USER_LOCATION_REQUEST_TYPE = 34
        const val TYPE_CHECK_USER_LOCATION_REQUEST_TYPE = 35
        const val TYPE_PARKING_SPACE_CHANGED = 36
        const val TYPE_SEND_TRACE_ID = 37
        const val TYPE_SELECT_OTHER_LEVEL_MISSION_ENTER = 38
        const val TYPE_SELECT_OTHER_LEVEL_MISSION_COMPLETE = 39
        const val TYPE_LOGIN_STATUS_CHANGED = 40
        const val TYPE_MISSION_COMPLETE = 41
        const val TYPE_REWARD_COMPLETE = 42
        const val TYPE_CHECKOUT_SCAN = 43
        const val TYPE_CHECKOUT_HOLD = 44
        const val TYPE_CLICK_BORROWED_LIST = 45
        const val TYPE_SELECT_OTHER_LEVEL_MISSION = 46
        const val TYPE_BEACON_LEAVE_NOTICE = 99
        const val EVENT_CONTENT_USER_IN_NAVIGATION = "event_content_user_in_navigation"
        const val EVENT_CONTENT_NOT_NAVIGATION = "event_content_not_navigation"

        const val TYPE_GROUP_RESPONSE = 47
        const val TYPE_LEFT_GROUP = 48
        const val TYPE_LEFT_GROUP_MAP = 49
    }
}