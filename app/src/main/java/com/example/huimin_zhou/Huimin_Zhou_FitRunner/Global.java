package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

/**
 * Created by Lucidity on 17/2/8.
 */

public class Global {
    // main
    public static final int POS_WELCOME = 0;
    public static final int POS_HISTORY = 1;
    public static final int POS_SETTING = 2;

    // dialog
    public static final int DIALOG_DATE_PICKER = 0;
    public static final int DIALOG_TIME_PICKER = 1;
    public static final int DIALOG_DURATION = 2;
    public static final int DIALOG_DISTANCE = 3;
    public static final int DIALOG_CALORIES = 4;
    public static final int DIALOG_HEART_RATE = 5;
    public static final int DIALOG_COMMENT = 6;
    public static final int DIALOG_ID_PHOTO_PICKER = 7;
    public static final String DIALOG_ID_KEY = "dialog_key";

    // profile
    public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
    public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;

    // Service
    public final static int MSG_REGISTER_CLIENT = 0;
    public final static int MSG_UNREGISTER_CLIENT = 1;
    public final static int MSG_DATA_INIT = 2;
    public final static int MSG_DATA_CHANGE = 3;

    public final static String KEY_ISMILE = "mile_pref";
    public final static String KEY_ISSERVICE = "is_service";

    // Notification
    public final static int NOTIFICATION_SERVICE_CODE = 3;
}
