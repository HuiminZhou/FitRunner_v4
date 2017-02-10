package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.MySQLOpenHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.DatabaseSource;
import com.example.huimin_zhou.Huimin_Zhou_FitRunner.Database.ExerciseEntry;


/**
 * Created by Lucidity on 17/1/29.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection {
    private DatabaseSource mDBSource;
    private GoogleMap mMap;
    private Marker mMarker;
    private Marker mStarter;
    private PolylineOptions mRectOptions;
    private Polyline mPoly;
    private boolean isMile = true;
    private ExerciseEntry curEntry;
    private boolean isService;
    private Bundle initBundle;

    private ServiceConnection mConnection = this;
    private Messenger mServiceMessenger = null; // send
    private Messenger mReceiveMessenger = new Messenger(new IncomingMessageHandler()); // receive
    private boolean mIsBound = false;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_map);
        // add toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDBSource = new DatabaseSource(this);
        Intent intent = this.getIntent();
        initBundle = intent.getExtras();
        isService = initBundle.getBoolean(Global.KEY_ISSERVICE);
        isMile = initBundle.getBoolean(Global.KEY_ISMILE);
        if (isService) {
            // Log.d("MAP", "service");
            doStartService();
            doBindService();
        } else {
            // Log.d("DISPLAY", "service");
            displayDBEntry();
        }

        // google map
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
    }

    /***************************** Service *****************************/
    private void doStartService() {startService(new Intent(this, TrackingService.class));}

    private void doStopService() {
        TrackingService.resetEntry();
        stopService(new Intent(this, TrackingService.class));
    }

    private void doBindService() {
        bindService(new Intent(this, TrackingService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        mIsBound = true;
        String inputType = initBundle.getString(MySQLOpenHelper.INPUT_TYPE);
        String activType = initBundle.getString(MySQLOpenHelper.ACTIVITY_TYPE);
        TrackingService.setEntry(inputType, activType);
    }

    private void doUnbindService() {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                Message msg = Message.obtain(null, Global.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mReceiveMessenger;
                try {
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        Message msg = Message.obtain(null, Global.MSG_REGISTER_CLIENT);
        msg.replyTo = mReceiveMessenger;
        Message msgBundle = Message.obtain(null, Global.MSG_DATA_INIT);
        msgBundle.setData(initBundle);
        try {
            mServiceMessenger.send(msg);
            mServiceMessenger.send(msgBundle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {mServiceMessenger = null;}

    @Override
    public void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    /***************************** Draw Map *****************************/
    @Override
    public void onMapReady(GoogleMap googleMap) { // set map as new map
        mMap = googleMap;
        if (curEntry != null) {
            drawStats(false, curEntry);
        } else {
            drawStats(true, TrackingService.getEntry());
        }
    }

    private void drawMap(ExerciseEntry entry) {
        ArrayList<LatLng> arrayList = entry.getLocationList();
        if (mMarker != null) {
            mMarker.remove();
        }
        if (mPoly != null) {
            mPoly.remove();
        }
        int size = arrayList.size();
        mRectOptions = new PolylineOptions();
        for (int i = 0; i < size; ++i) {
            LatLng latLng = arrayList.get(i);
            if (i == 0 && mStarter == null){
                mStarter = mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));
            } else if (i == size - 1) {
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_ORANGE)));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
            mRectOptions.add(latLng);
        }
        mRectOptions.color(Color.BLUE);
        mPoly = mMap.addPolyline(mRectOptions);
    }

    private void drawStats(boolean isService, ExerciseEntry entry) {
        TextView textView = (TextView) findViewById(R.id.stats_type);
        textView.setText("Type: " + entry.getActivityType());
        textView = (TextView) findViewById(R.id.stats_avg_speed);
        textView.setText("Avg speed: " + Parser.parseSpeed(isMile, entry.getAvgSpeed()));
        textView = (TextView) findViewById(R.id.stats_cur_speed);
        if (isService) {
            textView.setText("Cur speed: " + Parser.parseSpeed(isMile, TrackingService.getCurSpeed()));
        } else {
            textView.setText("Cur speed: n/a");
        }
        textView = (TextView) findViewById(R.id.stats_climb);
        textView.setText("Climb: " + Parser.parseDistance(isMile, entry.getClimb()));
        textView = (TextView) findViewById(R.id.stats_calorie);
        textView.setText("Calorie: " + entry.getCalories());
        textView = (TextView) findViewById(R.id.stats_distance);
        textView.setText("Distance: " + Parser.parseDistance(isMile, entry.getDistance()));
        drawMap(entry);
    }

    /***************************** Draw Buttons *****************************/
    private void displayDBEntry() {
        Long id = initBundle.getLong(MySQLOpenHelper.ID);
        mDBSource.open();
        curEntry = mDBSource.queryOne(id);
        mDBSource.close();

        Button btn = (Button) findViewById(R.id.btn_save_map);
        btn.setVisibility(View.INVISIBLE);
        btn = (Button) findViewById(R.id.btn_cancel_map);
        btn.setVisibility(View.INVISIBLE);
    }

    public void onSaveClick(View view) {
        new SaveToDB().execute(TrackingService.getEntry());
        doUnbindService();
        doStopService();
        finish();
    }

    public void onCancelClick(View view) {
        doUnbindService();
        doStopService();
        finish();
    }

    // add delete button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isService) {
            return false;
        } else {
            getMenuInflater().inflate(R.menu.actionbar, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                mDBSource.open();
                mDBSource.delete(curEntry);
                mDBSource.close();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***************************** Incoming handler *****************************/
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Global.MSG_DATA_CHANGE:
                    drawStats(true, TrackingService.getEntry());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /***************************** Save to DB AsyncTask *****************************/
    public class SaveToDB extends AsyncTask<ExerciseEntry, Void, String> {
        @Override
        protected String doInBackground(ExerciseEntry... entries) {
            mDBSource.open();
            if (entries != null && entries.length > 0) {
                long id = mDBSource.insertEntry(entries[0]);
                mDBSource.close();
                return "" + id;
            }
            mDBSource.close();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Entry #" + result + " saved.", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
