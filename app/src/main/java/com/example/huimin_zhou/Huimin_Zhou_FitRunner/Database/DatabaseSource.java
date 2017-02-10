package com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Parser;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Lucidity on 17/1/29.
 */

public class DatabaseSource {
    private String[] allColumns = {
            MySQLOpenHelper.ID, MySQLOpenHelper.INPUT_TYPE, MySQLOpenHelper.ACTIVITY_TYPE,
            MySQLOpenHelper.DATE, MySQLOpenHelper.DURATION, MySQLOpenHelper.DISTANCE,
            MySQLOpenHelper.CLIMB, MySQLOpenHelper.AVG_SPEED, MySQLOpenHelper.CALORIES,
            MySQLOpenHelper.HEART_RATE, MySQLOpenHelper.LOCATION};

    private SQLiteDatabase mDatabase;
    private MySQLOpenHelper mSQLOpenHelper;
    private Context context;
    public DatabaseSource(Context context) {
        mSQLOpenHelper = new MySQLOpenHelper(context);
        this.context = context;
    }

    public void open() throws SQLException {
        if (mSQLOpenHelper == null) {
            mSQLOpenHelper = new MySQLOpenHelper(context);
        }
        mDatabase = mSQLOpenHelper.getReadableDatabase();
    }

    public void close() {
        mDatabase.close();
    }

    public long insertEntry(ExerciseEntry entry) {
        ContentValues values = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        values.put(MySQLOpenHelper.INPUT_TYPE,      entry.getInputType());
        values.put(MySQLOpenHelper.ACTIVITY_TYPE,   entry.getActivityType());
        values.put(MySQLOpenHelper.DATE,            dateFormat.format(entry.getDate().getTime()));
        values.put(MySQLOpenHelper.DURATION,        entry.getDuration());
        values.put(MySQLOpenHelper.DISTANCE,        entry.getDistance());
        values.put(MySQLOpenHelper.CLIMB,           entry.getClimb());
        values.put(MySQLOpenHelper.AVG_SPEED,       entry.getAvgSpeed());
        values.put(MySQLOpenHelper.CALORIES,        entry.getCalories());
        values.put(MySQLOpenHelper.HEART_RATE,      entry.getHeart());
        byte[] bytes = Parser.parseLatLng2Byte(entry.getLocationList());
        values.put(MySQLOpenHelper.LOCATION,        bytes);

        return mDatabase.insert(MySQLOpenHelper.TABLE_NAME, null, values);
    }

    // query all the columns
    public ArrayList<ExerciseEntry> queryAll() {
        ArrayList<ExerciseEntry> arrayList = new ArrayList<ExerciseEntry>();
        Cursor cursor = mDatabase.query(
                MySQLOpenHelper.TABLE_NAME,
                allColumns,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ExerciseEntry entry = cursorToEntry(cursor);
            arrayList.add(entry);
            cursor.moveToNext();
        }
        return arrayList;
    }

    public ExerciseEntry queryOne(long ID) {
        Cursor cursor = mDatabase.query(
                MySQLOpenHelper.TABLE_NAME,
                allColumns,
                MySQLOpenHelper.ID + "=?",
                new String[] {String.valueOf(ID)}, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            return cursorToEntry(cursor);
        } else {
            return null;
        }
    }

    public void delete(ExerciseEntry entry) {
        mDatabase.delete(MySQLOpenHelper.TABLE_NAME,
                MySQLOpenHelper.ID + " = " + entry.getId(), null);
    }

    public void deleteAll() {
        mDatabase.delete(MySQLOpenHelper.TABLE_NAME, null, null);
    }

    private ExerciseEntry cursorToEntry(Cursor cursor) {
        ExerciseEntry entry = new ExerciseEntry();
        entry.setId(cursor.getLong(0));
        entry.setInputType(cursor.getString(1));
        entry.setActivityType(cursor.getString(2));
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String date = cursor.getString(3);
            entry.setDate(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        entry.setDuration(cursor.getInt(4));
        entry.setDistance(cursor.getFloat(5));
        entry.setClimb(cursor.getFloat(6));
        entry.setAvgSpeed(cursor.getFloat(7));
        entry.setCalories(cursor.getInt(8));
        entry.setHeart(cursor.getInt(9));
        entry.setLocation(Parser.parseByte2Latlng(cursor.getBlob(10)));
        return entry;
    }
}
