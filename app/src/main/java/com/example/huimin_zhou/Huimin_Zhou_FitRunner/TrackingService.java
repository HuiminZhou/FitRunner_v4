package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.ExerciseEntry;


/**
 * Created by Lucidity on 17/2/8.
 */

public class TrackingService extends Service implements LocationListener  {
    private static ExerciseEntry mEntry = new ExerciseEntry();
    private boolean mIsBound = false;
    public LocationManager mLocationManager;
    private Messenger mClient; // track current client
    private Messenger mReceiveMessenger = new Messenger(new IncomingMessengerHandler());

    private long preTime;
    private double iniAltitude;
    private static float curSpeed = 0;
    private Location preLocation;
    private Bundle initBundle;

    @Override
    public void onCreate() {
        super.onCreate();
        initLocationManager();
    }

    /***************************** Service *****************************/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mReceiveMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /***************************** Location Listener *****************************/
    private void initLocationManager() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(criteria, true);
        try {
            Location location = mLocationManager.getLastKnownLocation(provider);
            mEntry.addLocation(Parser.Location2LatLng(location)); // initialize start location
            preTime = System.currentTimeMillis(); // initialize start time
            mLocationManager.requestLocationUpdates(provider, 400, (float) 0.5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
        sendMsgToUI();
    }

    private void updateLocation(Location location) {
        if (iniAltitude == 0) iniAltitude = location.getAltitude();
        if (preLocation == null) preLocation = Parser.Location2Location(location);

        // duration
        long curTime = System.currentTimeMillis();
        long curDuration = curTime - preTime; // millisecond
        preTime = curTime;
        int totDuration = mEntry.getDuration() + (int) (curDuration / 1000);
        mEntry.setDuration(totDuration);

        // distance
        Location curLocation = Parser.Location2Location(location);
        double curDistance = Parser.parseLatLng2Distance(preLocation, curLocation); // miles
        float totDistance = mEntry.getDistance() + (float) curDistance;
        mEntry.setDistance(totDistance);

        // latitude, longitude, altitude
        mEntry.addLocation(Parser.Location2LatLng(location));
        mEntry.setClimb((float)(location.getAltitude() - iniAltitude));

        // speed
        curSpeed = (float) (curDistance * 1000 * 3600 / curDuration);
        float avgSpeed = (float) (1.0 * totDistance * 3600 / totDuration);
        mEntry.setAvgSpeed(avgSpeed);

        // calories
        mEntry.setCalories((int)(totDistance * 150));
    }

    private void sendMsgToUI() {
        Message toSend = Message.obtain(null, Global.MSG_DATA_CHANGE);
        try {
            if (mIsBound && mClient != null) {
                mClient.send(toSend);
            }
        } catch (RemoteException e) {}
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    /***************************** Set & Get *****************************/
    public static float getCurSpeed() {return curSpeed;}

    public static ExerciseEntry getEntry() {return mEntry;}

    public static void setEntry(String inputType, String activType) {
        mEntry.setInputType(inputType);
        mEntry.setActivityType(activType);
    }

    public static void resetEntry() {
        mEntry = new ExerciseEntry();
    }

    /***************************** Incoming handler *****************************/
    private class IncomingMessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Global.MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    mIsBound = true;
                    break;
                case Global.MSG_UNREGISTER_CLIENT:
                    mClient = null;
                    mIsBound = false;
                    break;
                case Global.MSG_DATA_INIT:
                    initBundle = msg.getData();
                    onNotification();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /***************************** Notification *****************************/
    private void onNotification() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Dummy");
        Intent redirectIntent = new Intent(this, MapActivity.class);
        redirectIntent.putExtras(initBundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getBaseContext(),
                0,
                redirectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification))
                .setSmallIcon(R.drawable.dart_icon)
                .setContentIntent(pendingIntent).build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Global.NOTIFICATION_SERVICE_CODE, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .cancel(Global.NOTIFICATION_SERVICE_CODE);
    }
}
