package hack.galert.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import hack.galert.GPS.GPSTracker;
import hack.galert.GPS.LocalLocationManager;
import hack.galert.R;
import hack.galert.TaskAgents.FriendsInvalidatorTask;
import hack.galert.TaskAgents.LocationRequesterTask;
import hack.galert.models.FriendsModel;
import hack.galert.services.LocationUpdaterService;
import hack.galert.sharedpref.SharedPreferenceManager;

public class LocationTrack extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Toolbar toolbar;
    private GPSTracker tracker;
    private Handler mFriendsLocatorDataHandler;
    private ProgressDialog mapProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);
        setToolbar();
        tracker = new GPSTracker(this);
        setUpMapIfNeeded();

        handleFriendIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPreferenceManager.getInstance(this).setLoginStatus(false);

            finish();
            return true;
        }

        if (id == R.id.action_friends) {
            startActivity(new Intent(LocationTrack.this, friendsActivity.class));
            return true;
        }

        if (id == R.id.action_reminders) {
            startActivity(new Intent(LocationTrack.this, CreateReminders.class));
            return true;
        }

        if (id == R.id.action_autosetting) {
            startActivity(new Intent(LocationTrack.this, CreateSettings.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tracker.stopUsingGPS();
    }

    @Override
    protected void onResume() {

        super.onResume();


        Log.d("LocationTrack", " checking Permission");
        startService(new Intent(this, LocationUpdaterService.class));

        if (tracker.canGetLocation()) {
            setUpMapIfNeeded();
        } else {
            tracker.showSettingsAlert();
        }


    }

    public LatLng getMyLocation() {
        return new LatLng(tracker.getLatitude(), tracker.getLongitude());
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            if (mMap != null) {
                setUpMap(getMyLocation());
            }
        }
    }

    private void setUpMap(LatLng initialLocation) {

        mMap.addMarker(new MarkerOptions().position(initialLocation).title("Me"));
        pointMapTo(initialLocation);
    }

    public void pointMapTo(LatLng newLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
    }

    public void locateFriend(String frnd,LatLng frndLoc){
        mMap.addMarker(new MarkerOptions().position(frndLoc).title(frnd));
        pointMapTo(frndLoc);
        mapProgressDialog.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setUpMap(getMyLocation());
    }

    private void handleFriendIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mapProgressDialog = new ProgressDialog(this);

            String username = intent.getExtras().getString("username");
            mapProgressDialog.setMessage("Locating "+username);
            mapProgressDialog.show();
            requestFriendLocation(username);
        }
    }

    private void requestFriendLocation(final String frnd) {

        mFriendsLocatorDataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                String json = msg.obj.toString();
                Log.d("LocationTrack"," handler respose "+json);
                try {
                    JSONObject object = new JSONObject(json);
                    String isFrnd = object.getString("friend");
                    if (isFrnd.equals("True")) {
                        String isLive = object.getString("live_status");

                        if (isLive.equals("True")) {
                            String lat = object.getString("latitude");
                            String lon = object.getString("longitude");
                            locateFriend(frnd,new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));

                        } else {
                            Toast.makeText(LocationTrack.this,"No Location Recorded",Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(LocationTrack.this, "You Are Not Allowed To Track", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        LocationRequesterTask locationRequesterTask = new LocationRequesterTask(this, mFriendsLocatorDataHandler, frnd);
        locationRequesterTask.start();

    }

    public void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Location Assistant");

    }

}
