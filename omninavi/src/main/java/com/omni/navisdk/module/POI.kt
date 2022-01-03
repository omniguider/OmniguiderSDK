package com.omni.navisdk.module

import com.google.gson.annotations.SerializedName
import com.omni.navisdk.R
import java.io.Serializable

class POI : Serializable {
    @SerializedName("id")
    var id = 0

    @SerializedName("store_id")
    val store_id = 0

    @SerializedName("name")
    var name: String? = null
        private set

    @SerializedName("desc")
    var desc: String? = null
        private set

    @SerializedName("logo")
    val logo: String? = null

    @SerializedName("type")
    var type: String? = null
        private set

    @SerializedName("type_zh")
    var typeZh: String? = null
        private set

    @SerializedName("color")
    val color: String? = null

    @SerializedName("min_level")
    val min_level = 0

    @SerializedName("max_level")
    val max_level = 0

    @SerializedName("lat")
    var latitude = 0.0
        private set

    @SerializedName("lng")
    var longitude = 0.0
        private set

    @SerializedName("distance")
    var distance : String? = null
        private set

    @SerializedName("is_entrance")
    var isEntrance: String? = null
        private set

    @SerializedName("is_door")
    var isDoor: String? = null
        private set

    @SerializedName("is_office")
    var is_office: Boolean? = null

    @SerializedName("office")
    val office: Array<OGPOIOffice>? = null

    @SerializedName("icon")
    val icon: Array<OGPOIIcon>? = null
    var poiType = -1
    private var poiIconResId = -1

    @SerializedName("number")
    var number = 1
        private set

    @SerializedName("aac_id")
    var aac_id = 0

    @SerializedName("ac_id")
    var ac_id = 0

    /**
     * Only for GuideExhibit method toPOI()
     */
    var guideOrder = 0
        private set
    var floorNumber: String? = null
        private set
    var exhibitContent: String? = null
        private set
    var exhibitImageURL: String? = null
        private set

    private fun setName(name: String) {
        this.name = name
    }

    private fun setDesc(desc: String) {
        this.desc = desc
    }

    private fun setType(type: String) {
        this.type = type
    }

    private fun setTypeZh(typeZh: String) {
        this.typeZh = typeZh
    }

    private fun setIsEntrance(isEntrance: String) {
        this.isEntrance = isEntrance
    }

    private fun setIsDoor(isDoor: String) {
        this.isDoor = isDoor
    }

    private fun setFloorNumber(floorNumber: String) {
        this.floorNumber = floorNumber
    }

    private fun setExhibitContent(exhibitContent: String) {
        this.exhibitContent = exhibitContent
    }

    private fun setExhibitImageURL(exhibitImageURL: String) {
        this.exhibitImageURL = exhibitImageURL
    }

    val pOIType: Int
        get() {
            if (type != null) {
                when (type) {
                    "Elevator" -> {
                        poiType = TYPE_ELEVATOR
                        poiIconResId = R.mipmap.poi_elevator
                    }
                    "Side elevator" -> {
                        poiType = TYPE_ELEVATOR
                        poiIconResId = R.mipmap.poi_elevator
                    }
                    "STAIRS" -> {
                        poiType = TYPE_STAIRS
                        poiIconResId = R.mipmap.poi_stair
                    }
                    "Stair" -> {
                        poiType = TYPE_STAIRS
                        poiIconResId = R.mipmap.poi_stair
                    }
                    "store" -> {
                        poiType = TYPE_STORE
                        poiIconResId = R.mipmap.syn_poi_store
                    }
                    "Event space" -> {
                        poiType = TYPE_EVENT_SPACE
                        poiIconResId = R.mipmap.syn_poi_event_speace
                    }
                    "Clapper Theater" -> {
                        poiType = TYPE_CLAPPER_THEATER
                        poiIconResId = R.mipmap.syn_poi_clapper_theater
                    }
                    "Clapper studio" -> {
                        poiType = TYPE_CLAPPER_STUDIO
                        poiIconResId = R.mipmap.syn_poi_clapper_studio
                    }
                    "Food court" -> {
                        poiType = TYPE_FOOD_COURT
                        poiIconResId = R.mipmap.syn_poi_food_court
                    }
                    "Accessibility" -> {
                        poiType = TYPE_ACCESSIBILITY
                        poiIconResId = R.mipmap.syn_poi_accesibility
                    }
                    "AED" -> {
                        poiType = TYPE_AED
                        poiIconResId = R.mipmap.syn_poi_aed
                    }
                    "packing area" -> {
                        poiType = TYPE_PACKING_AREA
                        poiIconResId = R.mipmap.syn_poi_packingarea
                    }
                    "ATM" -> {
                        poiType = TYPE_ATM
                        poiIconResId = R.mipmap.poi_atm
                    }
                    "cafe" -> {
                        poiType = TYPE_CAFE
                        poiIconResId = R.mipmap.syn_poi_cafe
                    }
                    "GARDEN" -> {
                        poiType = TYPE_GARDEN
                        poiIconResId = R.mipmap.syn_poi_garden
                    }
                    "CASHIER" -> {
                        poiType = TYPE_CASHIER
                        poiIconResId = R.mipmap.syn_poi_cashier
                    }
                    "FAMILY RESTROOM" -> {
                        poiType = TYPE_FAMILY_RESTROOM
                        poiIconResId = R.mipmap.syn_poi_family_restroom
                    }
                    "ELEVATOR FOR DISABLED" -> {
                        poiType = TYPE_ELEVATOR_FOR_DISABLED
                        poiIconResId = R.mipmap.syn_poi_elevator_for_disabled
                    }
                    "Diaper Changing" -> {
                        poiType = TYPE_DIAPER_CHANGING
                        poiIconResId = R.mipmap.syn_poi_diaper_changing
                    }
                    "vending machine" -> {
                        poiType = TYPE_VENDING_MACHINE
                        poiIconResId = R.mipmap.syn_poi_vending_machine
                    }
                    "PUBLIC PHONE" -> {
                        poiType = TYPE_PUBLIC_PHONE
                        poiIconResId = R.mipmap.syn_poi_public_phone
                    }
                    "TRASH Recycle" -> {
                        poiType = TYPE_TRASH_RECYCLE
                        poiIconResId = R.mipmap.syn_poi_trash_and_recycle
                    }
                    "Fire Hydrant" -> {
                        poiType = TYPE_FIRE_HYDRANT
                        poiIconResId = R.mipmap.poi_hydrant
                    }
                    "Hydrant" -> {
                        poiType = TYPE_FIRE_HYDRANT
                        poiIconResId = R.mipmap.poi_hydrant
                    }
                    "emergency_exit" -> {
                        poiType = TYPE_EMERGENCY_EXIT
                        poiIconResId = R.mipmap.syn_poi_emergency_exit
                    }
                    "Entrance" -> {
                        poiType = TYPE_ENTRANCE
                        poiIconResId = R.mipmap.poi_entrance
                    }
                    "Entrance/Exit" -> {
                        poiType = TYPE_ENTRANCE
                        poiIconResId = R.mipmap.poi_exit
                    }
                    "Fire Extinguisher" -> {
                        poiType = TYPE_EXTINGUISHER
                        poiIconResId = R.mipmap.syn_poi_fire_extinguisher
                    }
                    "escalator_up" -> {
                        poiType = TYPE_ESCALATOR_UP
                        poiIconResId = R.mipmap.syn_poi_escalator_up
                    }
                    "escalator_down" -> {
                        poiType = TYPE_ESCALATOR_DOWN
                        poiIconResId = R.mipmap.syn_poi_escalatord_down
                    }
                    "Escalator" -> {
                        poiType = TYPE_ESCALATOR_UP
                        poiIconResId = R.mipmap.poi_escalator
                    }
                    "Restroom" -> {
                        poiType = TYPE_RESTROOM
                        poiIconResId = R.mipmap.poi_restroom
                    }
                    "SERVICE CENTER" -> {
                        poiType = TYPE_SERVICE_CENTER
                        poiIconResId = R.mipmap.syn_poi_service_center
                    }
                    "Information" -> {
                        poiType = TYPE_INFORMATION
                        poiIconResId = R.mipmap.poi_information
                    }
                    "Lockers" -> {
                        poiType = TYPE_LOCKERS
                        poiIconResId = R.mipmap.syn_poi_lockers
                    }
                    "Restaurant", "restaurant" -> {
                        poiType = TYPE_RESTAURANT
                        poiIconResId = R.mipmap.poi_restaurant
                    }
                    "Breastfeeding room" -> {
                        poiType = TYPE_BREAST_FEEDING_ROOM
                        poiIconResId = R.mipmap.poi_breastfeeding_room
                    }
                    "Breastfeeding Room" -> {
                        poiType = TYPE_BREAST_FEEDING_ROOM
                        poiIconResId = R.mipmap.poi_breastfeeding_room
                    }
                    "map_text" -> poiType = TYPE_MAP_TEXT
                    "Parking space" -> {
                        poiType = TYPE_PARKING_SPACE
                        poiIconResId = R.mipmap.poi_parking
                    }
                    "Car Entrance" -> {
                        poiType = TYPE_CAR_ENTRANCE
                        poiIconResId = R.mipmap.poi_car_entrance
                    }
                    "Male toilet" -> {
                        poiType = TYPE_MALE_TOILET
                        poiIconResId = R.mipmap.poi_male_toilet
                    }
                    "Female toilet" -> {
                        poiType = TYPE_FEMALE_TOILET
                        poiIconResId = R.mipmap.poi_female_toilet
                    }
                    "Accessible toilets" -> {
                        poiType = TYPE_ACCESSIBLE_TOILET
                        poiIconResId = R.mipmap.poi_accessible_toilets
                    }
                    "Rest station" -> {
                        poiType = TYPE_REST_STATION
                        poiIconResId = R.mipmap.poi_rest_station
                    }
                    "Accessible parking" -> {
                        poiType = TYPE_ACCESSIBLE_PARKING
                        poiIconResId = R.mipmap.poi_accessible_parking
                    }
                    "Children's toilet" -> {
                        poiType = TYPE_CHILD_TOILET
                        poiIconResId = R.mipmap.poi_children_s_toilet
                    }
                    "Books dedicated elevator" -> {
                        poiType = TYPE_BOOKS_DEDICATED_ELEVATOR
                        poiIconResId = R.mipmap.poi_books_dedicated_elevator
                    }
                    "Fire hydrant" -> {
                        poiType = TYPE_HYDRANT
                        poiIconResId = R.mipmap.poi_hydrant
                    }
                    "Accessible ramp" -> {
                        poiType = TYPE_ACCESSIBLE_RAMP
                        poiIconResId = R.mipmap.poi_accessible_ramp
                    }
                    "Accessible special seats" -> {
                        poiType = TYPE_ACCESSIBLE_SPECIAL_SEATS
                        poiIconResId = R.mipmap.poi_accessible_special_seats
                    }
                    "Accessible wheelchair charging station" -> {
                        poiType = TYPE_WHEELCHAIR_CHARGING_STATION
                        poiIconResId = R.mipmap.poi_accessible_wheelchair_charging_station
                    }
                    "Breast feeding room" -> {
                        poiType = TYPE_BREAST_FEEDING_ROOM
                        poiIconResId = R.mipmap.poi_breastfeeding_room
                    }
                    "Escape Sling" -> {
                        poiType = TYPE_ESCAPE_SLING
                        poiIconResId = R.mipmap.icon_escape_sling
                    }
                    "Parent-child toilet" -> {
                        poiType = TYPE_PARENT_CHILD_TOILET
                        poiIconResId = R.mipmap.poi_diaper_changing
                    }
                    "Reception desk" -> {
                        poiType = TYPE_RECEPTION_DESK
                        poiIconResId = R.mipmap.poi_information
                    }
                    "Search area" -> {
                        poiType = TYPE_SEARCH_AREA
                        poiIconResId = R.mipmap.poi_search_area
                    }
                    "Bookshelf" -> {
                        poiType = TYPE_BOOKSHELF
                        poiIconResId = R.mipmap.poi_bookshelf
                    }
                    "Copy Room" -> {
                        poiType = TYPE_COPY_ROOM
                        poiIconResId = R.mipmap.poi_copy_room
                    }
                    "Conference room" -> {
                        poiType = TYPE_CONFERENCE_ROOM
                        poiIconResId = R.mipmap.poi_conference_room
                    }
                    "Reading machine" -> {
                        poiType = TYPE_READING_MACHINE
                        poiIconResId = R.mipmap.poi_reading_machine
                    }
                    "KIOSK" -> {
                        poiType = TYPE_KIOSK
                        poiIconResId = R.mipmap.poi_kiosk
                    }
                    "Digital leisure platform" -> {
                        poiType = TYPE_DIGITAL_LEISURE_PLATFORM
                        poiIconResId = R.mipmap.poi_digital_leisure_platform
                    }
                    "Auto Return Book" -> {
                        poiType = TYPE_AUTO_RETURN_BOOK
                        poiIconResId = R.mipmap.poi_auto_return_book
                    }
                    "Booking Machine" -> {
                        poiType = TYPE_BOOKING_MACHINE
                        poiIconResId = R.mipmap.poi_booking_machine
                    }
                    "Story House" -> {
                        poiType = TYPE_STORY_HOUSE
                        poiIconResId = R.mipmap.poi_story_house
                    }
                    "Study Room" -> {
                        poiType = TYPE_STUDY_ROOM
                        poiIconResId = R.mipmap.poi_study_room
                    }
                    "Administration Entrance" -> {
                        poiType = TYPE_ADMINISTRATION_ENTRANCE
                        poiIconResId = R.mipmap.icon_administration_entrance
                    }
                    "Shopping Entrance" -> {
                        poiType = TYPE_SHOPPING_ENTRANCE
                        poiIconResId = R.mipmap.icon_shopping_entrance
                    }
                    "Large Artwork" -> {
                        poiType = TYPE_LARGE_ARTWORK
                        poiIconResId = R.mipmap.icon_art
                    }
                    "Small Artwork" -> {
                        poiType = TYPE_SMALL_ARTWORK
                        poiIconResId = R.mipmap.poi_small_artwork
                    }
                    "Automatic Pay Station" -> {
                        poiType = TYPE_AUTOMATIC_PAY_STATION
                        poiIconResId = R.mipmap.poi_automatic_pay_station
                    }
                    "Bus Stop" -> {
                        poiType = TYPE_BUS_STOP
                        poiIconResId = R.mipmap.poi_bus_stop
                    }
                    "Pantry" -> {
                        poiType = TYPE_PANTRY
                        poiIconResId = R.mipmap.poi_pantry
                    }
                    "Self borrow" -> {
                        poiType = TYPE_SELF_BORROW
                        poiIconResId = R.mipmap.poi_self_borrow
                    }
                    "AV Room" -> {
                        poiType = TYPE_AV_ROOM
                        poiIconResId = R.mipmap.poi_theater
                    }
                    "Friendly seat" -> {
                        poiType = TYPE_FRIENDLY_SEAT
                        poiIconResId = R.mipmap.poi_accessible_special_seats
                    }
                    "Extinguisher" -> {
                        poiType = TYPE_EXTINGUISHER
                        poiIconResId = R.mipmap.icon_extinguisher
                    }
                    "Visual Guider" -> {
                        poiType = TYPE_VISUAL_GUIDER
                        poiIconResId = R.mipmap.poi_guider
                    }
                    "Special Area" -> {
                        poiType = TYPE_SPECIAL_AREA
                        poiIconResId = R.mipmap.poi_bookshelf
                    }
                    "Wall" -> {
                        poiType = TYPE_WALL
                        poiIconResId = R.mipmap.poi_wall
                    }
                    "Meeting Room" -> {
                        poiType = TYPE_MEETING_ROOM
                        poiIconResId = R.mipmap.poi_meeting_room
                    }
                    "Convenience Store" -> {
                        poiType = TYPE_CONVENIENCE
                        poiIconResId = R.mipmap.poi_convenience
                    }
                    "Post Office" -> {
                        poiType = TYPE_POST_OFFICE
                        poiIconResId = R.mipmap.poi_post_office
                    }
                    "Bank" -> {
                        poiType = TYPE_BANK
                        poiIconResId = R.mipmap.poi_bank
                    }
                    "Drinking fountains" -> {
                        poiType = TYPE_DRINKING
                        poiIconResId = R.mipmap.poi_drinking
                    }
                    "Division" -> {
                        poiType = TYPE_DIVISION
                        poiIconResId = R.mipmap.poi_division
                    }
                    else -> {
                        poiType = TYPE_UNKNOWN
                        poiIconResId = R.mipmap.syn_poi_information
                    }
                }
            }
            return poiType
        }

    fun getPOIIconRes(isSelected: Boolean): Int {
        pOIType
        return if (isSelected) R.mipmap.icon_select else poiIconResId
    }

    val mapTextImageUrl: String
        get() = "http://nlpi.omniguider.com/upload/map_text/$id.png"

    val urlToPoisImage: String
        get() = ""

    class Builder {
        private val mPOI: POI
        fun setId(id: Int): Builder {
            mPOI.id = id
            return this
        }

        fun setName(name: String): Builder {
            mPOI.setName(name)
            return this
        }

        fun setDesc(desc: String): Builder {
            mPOI.setDesc(desc)
            return this
        }

        fun setLat(lat: Double): Builder {
            mPOI.latitude = lat
            return this
        }

        fun setLng(lng: Double): Builder {
            mPOI.longitude = lng
            return this
        }

        fun setType(type: String): Builder {
            mPOI.setType(type)
            return this
        }

        fun setIsEntrance(isEntrance: Boolean): Builder {
            mPOI.setIsEntrance(if (isEntrance) "Y" else "N")
            return this
        }

        fun setIsDoor(isDoor: Boolean): Builder {
            mPOI.setIsDoor(if (isDoor) "Y" else "N")
            return this
        }

        fun setExhibitContent(content: String): Builder {
            mPOI.setExhibitContent(content)
            return this
        }

        fun setExhibitImageURL(imageURL: String): Builder {
            mPOI.setExhibitImageURL(imageURL)
            return this
        }

        /**
         * Only for GuideExhibit method toPOI()
         */
        fun setGuideOrder(guideOrder: Int): Builder {
            mPOI.guideOrder = guideOrder
            return this
        }

        fun setFloorNumber(floorNumber: String): Builder {
            mPOI.setFloorNumber(floorNumber)
            return this
        }

        fun build(): POI {
            return mPOI
        }

        init {
            mPOI = POI()
        }
    }

    companion object {
        const val TYPE_UNKNOWN = 0
        const val TYPE_EMERGENCY_EXIT = 1
        const val TYPE_ELEVATOR = 2
        const val TYPE_STAIRS = 3
        const val TYPE_EVENT_SPACE = 4
        const val TYPE_CLAPPER_THEATER = 5
        const val TYPE_CLAPPER_STUDIO = 6
        const val TYPE_FOOD_COURT = 7
        const val TYPE_ACCESSIBILITY = 8
        const val TYPE_AED = 9
        const val TYPE_PACKING_AREA = 10
        const val TYPE_ATM = 11
        const val TYPE_CAFE = 12
        const val TYPE_GARDEN = 13
        const val TYPE_DIAPER_CHANGING = 14
        const val TYPE_BREAST_FEEDING_ROOM = 15
        const val TYPE_CASHIER = 16
        const val TYPE_FAMILY_RESTROOM = 17
        const val TYPE_ELEVATOR_FOR_DISABLED = 18
        const val TYPE_VENDING_MACHINE = 19
        const val TYPE_PUBLIC_PHONE = 20
        const val TYPE_TRASH_RECYCLE = 21
        const val TYPE_FIRE_HYDRANT = 22
        const val TYPE_ENTRANCE = 23
        const val TYPE_EXTINGUISHER = 24
        const val TYPE_RESTAURANT = 25
        const val TYPE_MAP_TEXT = 26
        const val TYPE_STORE = 27
        const val TYPE_ESCALATOR_UP = 28
        const val TYPE_ESCALATOR_DOWN = 29
        const val TYPE_RESTROOM = 30
        const val TYPE_SERVICE_CENTER = 31
        const val TYPE_INFORMATION = 32
        const val TYPE_LOCKERS = 33
        const val TYPE_PARKING_SPACE = 34
        const val TYPE_CAR_ENTRANCE = 35
        const val TYPE_MALE_TOILET = 36
        const val TYPE_FEMALE_TOILET = 37
        const val TYPE_ACCESSIBLE_TOILET = 38
        const val TYPE_REST_STATION = 39
        const val TYPE_ACCESSIBLE_PARKING = 40
        const val TYPE_CHILD_TOILET = 41
        const val TYPE_BOOKS_DEDICATED_ELEVATOR = 42
        const val TYPE_HYDRANT = 43
        const val TYPE_ACCESSIBLE_RAMP = 44
        const val TYPE_ACCESSIBLE_SPECIAL_SEATS = 45
        const val TYPE_WHEELCHAIR_CHARGING_STATION = 46
        const val TYPE_ESCAPE_SLING = 47
        const val TYPE_PARENT_CHILD_TOILET = 48
        const val TYPE_RECEPTION_DESK = 49
        const val TYPE_SEARCH_AREA = 50
        const val TYPE_BOOKSHELF = 51
        const val TYPE_COPY_ROOM = 52
        const val TYPE_CONFERENCE_ROOM = 53
        const val TYPE_READING_MACHINE = 54
        const val TYPE_KIOSK = 55
        const val TYPE_DIGITAL_LEISURE_PLATFORM = 56
        const val TYPE_AUTO_RETURN_BOOK = 57
        const val TYPE_BOOKING_MACHINE = 58
        const val TYPE_STORY_HOUSE = 59
        const val TYPE_STUDY_ROOM = 60
        const val TYPE_ADMINISTRATION_ENTRANCE = 61
        const val TYPE_SHOPPING_ENTRANCE = 62
        const val TYPE_LARGE_ARTWORK = 63
        const val TYPE_SMALL_ARTWORK = 64
        const val TYPE_AUTOMATIC_PAY_STATION = 65
        const val TYPE_BUS_STOP = 66
        const val TYPE_PANTRY = 67
        const val TYPE_SELF_BORROW = 68
        const val TYPE_AV_ROOM = 69
        const val TYPE_FRIENDLY_SEAT = 70
        const val TYPE_VISUAL_GUIDER = 71
        const val TYPE_SPECIAL_AREA = 72
        const val TYPE_WALL = 73
        const val TYPE_MEETING_ROOM = 74
        const val TYPE_CONVENIENCE = 75
        const val TYPE_POST_OFFICE = 76
        const val TYPE_BANK = 77
        const val TYPE_DRINKING = 78
        const val TYPE_DIVISION = 79
    }
}