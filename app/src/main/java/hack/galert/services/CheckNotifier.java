package hack.galert.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import hack.galert.GPS.GPSTracker;
import hack.galert.converter.GeoDistanceCalculator;
import hack.galert.database.LocalDatabaseHelper;
import hack.galert.models.SMLReminderModel;
import hack.galert.models.SettingsModel;

/**
 * Created by Ankit on 10/24/2016.
 */
public class CheckNotifier extends Service {

    public static final int NOTIFICATION_TYPE_SETTINGS = 0;
    public static final int NOTIFICATION_TYPE_REMINDER = 1;
    public static final int MIN_DISTANCE = 25; // area coverage
    private double mCurrentLat = 0;
    private double mCurrentLon = 0;
    private GPSTracker tracker;
    private Timer mTimer;
    private long CHECK_UPDATE_INTERVAL = 1 * 60 * 1000;     // 1 min
    private LocalDatabaseHelper dbHelper;
    private GeoDistanceCalculator distanceCalculator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        tracker = new GPSTracker(getApplicationContext());
        distanceCalculator = GeoDistanceCalculator.getInstance(getApplicationContext());

        if (mTimer != null) {
            //mTimer.cancel();
        } else {
            mTimer = new Timer();
        }

        mTimer.scheduleAtFixedRate(new RegularCheckupTask(), 0, CHECK_UPDATE_INTERVAL);


    }

    public void retrieveLocation(){
        mCurrentLat = tracker.getLatitude();
        mCurrentLon = tracker.getLongitude();

    }

    public void checkSettings(){
        //logic
        /*
            > get all settings

            > calculate distance between them and notify accordingly
            > or change settings
        * */
        dbHelper = LocalDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<SettingsModel> settings = dbHelper.getSettings();

        for(SettingsModel setting: settings){
            double distance = distanceCalculator.setLatitudes(setting.getLatitude(),mCurrentLat)
                                                .setLongitude(setting.getLongitude(),mCurrentLon)
                                                .getDistanceInMeter();

            if(distance <= MIN_DISTANCE){
                configureSettings(setting);
            }
        }

    }

    public void checkReminders(){
        //logic
        /*
            > get all reminders
            > calculate distance between them and notify accordingly
            > or remind with a notification

        * */

        dbHelper = LocalDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<SMLReminderModel> reminders = dbHelper.getReminders();

        for(SMLReminderModel reminder: reminders){
            double distance = distanceCalculator.setLatitudes(reminder.getLatitude(),mCurrentLat)
                    .setLongitude(reminder.getLongitude(),mCurrentLon)
                    .getDistanceInMeter();

            if(distance <= MIN_DISTANCE){
                    remind(reminder);
            }
        }

    }


    private void remind(SMLReminderModel reminder){          // notification for reminder
        Log.d("Checker","there is some reminder");
    }

    private void configureSettings(SettingsModel setting){   // change settings
        Log.d("Checker","there is some setting to set");
    }


    private class RegularCheckupTask extends TimerTask{

        @Override
        public void run() {
            // read all data base and match with current location

            retrieveLocation();
            checkReminders();
            checkSettings();

        }
    }


}
