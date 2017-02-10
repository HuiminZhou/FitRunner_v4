package com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lucidity on 17/1/29.
 */

public class ExerciseEntry {
    private long Id;
    private String mInputType = "";
    private String mActivityType = "";
    private Calendar mDate;
    private int mDuration; // seconds
    private int mCalories;
    private int mHeart;
    private float mDistance; // miles
    private float mClimb;
    private float mAvgSpeed;     // Average speed
    private String mComment;
    private ArrayList<LatLng> mLocationList; // Location list

    public ExerciseEntry() {
        mDate = Calendar.getInstance();
        mLocationList = new ArrayList<>();
    }

    public long getId() {return Id;}

    public String getInputType() {return mInputType;}

    public String getActivityType() {return mActivityType;}

    public Calendar getDate() {return mDate;}

    public int getDuration() {return mDuration;}

    public float getDistance() {return mDistance;}

    public int getCalories() {return mCalories;}

    public int getHeart() {return mHeart;}

    public float getClimb() {return mClimb;}

    public float getAvgSpeed() {return mAvgSpeed;}

    public ArrayList<LatLng> getLocationList() {return mLocationList;}

    public void setId(long Id) {this.Id = Id;}

    public void setInputType(String inputType) {this.mInputType = inputType;}

    public void setActivityType(String activityType) {this.mActivityType = activityType;}

    public void setDate(int year, int month, int day) {
        this.mDate.set(Calendar.YEAR, year);
        this.mDate.set(Calendar.MONTH, month);
        this.mDate.set(Calendar.DAY_OF_MONTH, day);
    }

    public void setDate(Date date) {
        this.mDate.setTime(date);
    }

    public void setTime(int hour, int minute, int second) {
        this.mDate.set(Calendar.HOUR_OF_DAY, hour);
        this.mDate.set(Calendar.MINUTE, minute);
        this.mDate.set(Calendar.SECOND, second);
    }

    public void setDuration(int duration) {this.mDuration = duration;}

    public void setDistance(float distance) {this.mDistance = distance;}

    public void setCalories(int calories) {this.mCalories = calories;}

    public void setHeart(int heart) {this.mHeart = heart;}

    public void setClimb(float climb) {this.mClimb = climb;}

    public void setAvgSpeed(float speed) {this.mAvgSpeed = speed;}

    public void addLocation(LatLng latLng) {this.mLocationList.add(latLng);}

    public void setLocation(ArrayList<LatLng> latLng) {
        for (LatLng i : latLng) {
            this.mLocationList.add(i);
        }
    }
}
