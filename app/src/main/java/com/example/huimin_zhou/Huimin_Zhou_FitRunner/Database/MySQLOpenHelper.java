package com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lucidity on 17/1/29.
 */

public class MySQLOpenHelper extends SQLiteOpenHelper {
    public static final int DATEBASE_VERSION = 1;
    public static final String DATABASE_NAME = "fitrunner.db";
    public static final String TABLE_NAME = "excercise";
    public static final String ID = "_id";

    public static final String INPUT_TYPE = "inputType";
    public static final String ACTIVITY_TYPE = "activityType";
    public static final String DATE = "date";
    public static final String DURATION = "duration";
    public static final String DISTANCE = "distance";
    public static final String CLIMB = "climb";
    public static final String AVG_SPEED = "avgSpeed";
    public static final String CALORIES = "calories";
    public static final String HEART_RATE = "heartRate";
    public static final String LOCATION = "location";
    public static final String COMMENT = "comment";

    private static final String DB_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME +      " ("
            + ID +              " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + INPUT_TYPE +      " STRING NOT NULL, "
            + ACTIVITY_TYPE +   " STRING NOT NULL, "
            + DATE +            " DATETIME NOT NULL, "
            + DURATION +        " INTEGER NOT NULL, "
            + DISTANCE +        " FLOAT, "
            + CLIMB +           " FLOAT, "
            + AVG_SPEED +       " FLOAT, "
            + CALORIES +        " INTEGER, "
            + HEART_RATE +      " INTEGER, "
            + LOCATION +        " BLOB);";

    private static final String DB_UPDATE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    public MySQLOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATEBASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.execSQL("DROP TABLE " + TABLE_NAME);
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DB_UPDATE);
        onCreate(db);
    }
}
