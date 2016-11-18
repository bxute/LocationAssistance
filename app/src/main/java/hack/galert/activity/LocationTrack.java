package hack.galert.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hack.galert.DataParser;
import hack.galert.GPS.GPSTracker;
import hack.galert.R;
import hack.galert.TaskAgents.LocationRequesterTask;
import hack.galert.database.LocalDatabaseHelper;
import hack.galert.font.FontManager;
import hack.galert.models.SMLReminderModel;
import hack.galert.models.SettingsModel;
import hack.galert.services.CheckNotifier;
import hack.galert.services.LocationUpdaterService;
import hack.galert.sharedpref.SharedPreferenceManager;

import static hack.galert.R.id.chooserGroup;

public class LocationTrack extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener ,LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Toolbar toolbar;
    private GPSTracker tracker;
    private Handler mFriendsLocatorDataHandler;
    private ProgressDialog mapProgressDialog;
    private boolean mHotspotsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);
        setToolbar();
        tracker = new GPSTracker(this);
        setUpMapIfNeeded();
        setBottomBar();
        handleFriendIntent();
        setUpPlaceAutoComplete();

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
            startActivity(new Intent(LocationTrack.this, Login.class));
            finish();
            return true;
        }

        if (id == R.id.action_issues) {
            startActivity(new Intent(LocationTrack.this, Issues.class));
            return true;
        }

//        if (id == R.id.action_reminders) {
//            startActivity(new Intent(LocationTrack.this, CreateReminders.class));
//            return true;
//        }
//
//        if (id == R.id.action_autosetting) {
//            startActivity(new Intent(LocationTrack.this, CreateSettings.class));
//            return true;
//        }
//
//        if (id == R.id.action_summary) {
//            startActivity(new Intent(LocationTrack.this, LocationSummary.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        super.onPause();


    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d("LocationTrack", " checking Permission");
        startService(new Intent(this, LocationUpdaterService.class));
        startService(new Intent(this, CheckNotifier.class));

        setUpMapIfNeeded();
        if (!tracker.canGetLocation()) {
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

        mMap.addMarker(new MarkerOptions()
                .position(initialLocation)
                .title("Me").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        pointMapTo(initialLocation);
    }

    public void pointMapTo(LatLng newLocation) {
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 16));

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                newLocation, 16);
        mMap.animateCamera(location);
    }

    public void locateFriend(String frnd, LatLng frndLoc) {

        LatLng myLocationAsSource = new LatLng(tracker.getLatitude(),tracker.getLongitude());
        String url = getUrl(myLocationAsSource , frndLoc);
        Log.d("LocationSummary"," Received Url: "+url);
        FetchUrl fetchUrl = new FetchUrl();
        fetchUrl.execute(url);

        mMap.addMarker(new MarkerOptions()
                .position(frndLoc)
                .title(frnd));
        pointMapTo(frndLoc);

        mapProgressDialog.dismiss();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showChooser(latLng);
            }
        });

        Log.d("LocationTrack", "MapReady");
        setUpMap(getMyLocation());
        setUpFab();


        if(!mHotspotsShown & mMap!=null){
            locateRemindersHotspots();
            locateSettingsHotspots();
            mHotspotsShown = true;
        }



    }

    private void locateRemindersHotspots() {

        //read from database and get the location and for each location mark with location.

        LocalDatabaseHelper dbHelper = LocalDatabaseHelper.getInstance(this);
        ArrayList<SMLReminderModel> reminders = dbHelper.getReminders();

        for (int i = 0; i < reminders.size(); i++) {

            LatLng location = new LatLng(Double.parseDouble(reminders.get(i).latitude),Double.parseDouble(reminders.get(i).longitude));

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.reminders_24);
            MarkerOptions markerOptions = new MarkerOptions().position(location)
                    .title(reminders.get(i).getTitle())
                    .snippet(reminders.get(i).content)
                    .icon(icon);
            Marker mMarker = mMap.addMarker(markerOptions);

        }
    }

    private void locateSettingsHotspots() {

        //read from database and get the location and for each location mark with location.

        LocalDatabaseHelper dbHelper = LocalDatabaseHelper.getInstance(this);
        ArrayList<SettingsModel> settings = dbHelper.getSettings();

        for (int i = 0; i < settings.size(); i++) {

            LatLng location = new LatLng(Double.parseDouble(settings.get(i).latitude),Double.parseDouble(settings.get(i).longitude));

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.settings_24);
            MarkerOptions markerOptions = new MarkerOptions().position(location)
                    .title("Settings")
                    .icon(icon);
            Marker mMarker = mMap.addMarker(markerOptions);

        }
    }

    private void showChooser(final LatLng latLng) {
        final Dialog dialog = new Dialog(this);
        int selected = 0;

        dialog.setTitle("Choose Action To Perform");
        dialog.setContentView(R.layout.action_chooser);
        RadioGroup grp = (RadioGroup) dialog.findViewById(chooserGroup);
        grp.check(selected);
        grp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                dialog.dismiss();

                if (i == R.id.remiderRadio) {
                    Intent intent = new Intent(LocationTrack.this, CreateReminders.class);
                    intent.putExtra("lat", latLng.latitude);
                    intent.putExtra("lon", latLng.longitude);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LocationTrack.this, CreateSettings.class);
                    intent.putExtra("lat", latLng.latitude);
                    intent.putExtra("lon", latLng.longitude);
                    startActivity(intent);

                }

            }
        });

        dialog.show();
    }

    private void setBottomBar() {

        TextView settings = (TextView) findViewById(R.id.settingHomeLink);
        TextView friends = (TextView) findViewById(R.id.friendsHomeLink);
        TextView reminders = (TextView) findViewById(R.id.remindersHomeLink);
        TextView summary = (TextView) findViewById(R.id.summaryHomeLink);
        Typeface tf = FontManager.getInstance(this).getTypeFace(FontManager.FONT_MATERIAL);
        settings.setTypeface(tf);
        friends.setTypeface(tf);
        reminders.setTypeface(tf);
        summary.setTypeface(tf);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LocationTrack.this, AutoSettings.class));
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LocationTrack.this, friendsActivity.class));
            }
        });

        reminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LocationTrack.this, Reminders.class));
            }
        });

        summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LocationTrack.this, LocationSummary.class));
            }
        });

    }

    private void handleFriendIntent() {

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mapProgressDialog = new ProgressDialog(this);

            final String username = intent.getExtras().getString("username");
            mapProgressDialog.setMessage("Locating " + username);
            mapProgressDialog.show();
            requestFriendLocation(username);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("LocationTrack", " re-locating friend");
                    requestFriendLocation(username);
                }
            }, 20000);

        }
    }

    private void requestFriendLocation(final String frnd) {

        mFriendsLocatorDataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                String json = msg.obj.toString();
                Log.d("LocationTrack", " handler respose " + json);
                try {
                    JSONObject object = new JSONObject(json);
                    String isFrnd = object.getString("friend");
                    if (isFrnd.equals("True")) {
                        String isLive = object.getString("live_status");

                        if (isLive.equals("True")) {
                            String lat = object.getString("latitude");
                            String lon = object.getString("longitude");
                            locateFriend(frnd, new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));

                        } else {
                            Toast.makeText(LocationTrack.this, "No Location Recorded", Toast.LENGTH_LONG).show();
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

    public void setUpFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng myLocc = new LatLng(new GPSTracker(LocationTrack.this).getLatitude(), new GPSTracker(LocationTrack.this).getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocc, 15));
            }
        });
    }

    private void setUpPlaceAutoComplete() {

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("LocationTrack", "Place: " + place.getName());
                pointMapTo(place.getLatLng());
                showChooser(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("LocationTrack", "An error occurred: " + status);
            }
        });

    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    // Fetches data from url    passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("LocationSummary", data.toString());
            } catch (Exception e) {
                Log.d("LocationSummary", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(14);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }


    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }
}
