package net.nichnologist.hotspot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public LocationService() {
    }

    private static final String TAG =
            "LocationService";

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand " + startId);

        final int currentId = startId;
        prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();
        final SqlSender sender = new SqlSender();

        Runnable r = new Runnable() {
            public void run() {

                while(prefs.getBoolean(getString(R.string.share_location), false)){
                    long endTime = System.currentTimeMillis() +
                            600*1000;

                    Log.i(TAG, "Service running " + currentId);
                    sender.addLoc(38.971669, -95.23525, false);

                    while (System.currentTimeMillis() < endTime) {
                        synchronized (this) {
                            try {
                                wait(endTime -
                                        System.currentTimeMillis());
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                stopSelf();
            }
        };

        Thread t = new Thread(r);
        t.start();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
    }

}