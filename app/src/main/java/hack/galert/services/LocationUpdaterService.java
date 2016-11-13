package hack.galert.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import hack.galert.Configs.URLS;
import hack.galert.GPS.GPSTracker;
import hack.galert.log.L;
import hack.galert.connnections.VolleyUtils;
import hack.galert.sharedpref.SharedPreferenceManager;

/**
 * Created by Ankit on 10/14/2016.
 */
public class LocationUpdaterService extends Service {
    private static final long POST_UPDATE_INTERVAL = 20 * 1000;      // 5 seconds
    private static final int SERVER_TIMEOUT_LIMIT = 5 * 1000;       // 5 seconds
    private final String url = URLS.URL_UPDATE_LOCATION;
    private Timer mTimer;
    private GPSTracker tracker;
    private double mCurrentLat;
    private double mCurrentLon;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {


        if (mTimer != null) {
            //mTimer.cancel();
        } else {
            mTimer = new Timer();
        }

        tracker = new GPSTracker(getApplicationContext());
        mTimer.scheduleAtFixedRate(new RegularUpdateTimerTask(), 0, POST_UPDATE_INTERVAL);

    }

    public void retrieveLocation(){
        mCurrentLat = tracker.getLatitude();
        mCurrentLon = tracker.getLongitude();

    }


    class RegularUpdateTimerTask extends TimerTask {

        @Override
        public void run() {

            retrieveLocation();
           final String username  = SharedPreferenceManager.getInstance(getApplicationContext()).getUserEmail();
           final String datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

            StringRequest updateCheckReq = new StringRequest(
                    Request.Method.POST,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            L.m("LocationUpdateService", " response :" + s);
                            // TODO: CHECK RESPONSE AND LOG IT
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map =  new HashMap<>();
                    map.put("username",username);
                    map.put("latitude",""+mCurrentLat);
                    map.put("longitude",""+mCurrentLon);
                    Log.d("LocationUpdateService", username + " Sending " + "lat " + mCurrentLat + " lon " + mCurrentLon + " time " + datetime);
                    map.put("time", datetime);

                    return map;
                }
            };

            updateCheckReq.setRetryPolicy(new DefaultRetryPolicy(
                    SERVER_TIMEOUT_LIMIT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleyUtils.getInstance().addToRequestQueue(updateCheckReq, "postUpdatedLocationReq", getApplicationContext());

        }
    }

}
