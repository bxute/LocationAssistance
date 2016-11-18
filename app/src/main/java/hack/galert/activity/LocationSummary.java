package hack.galert.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.DataParser;
import hack.galert.GPS.GPSTracker;
import hack.galert.R;
import hack.galert.connnections.VolleyUtils;
import hack.galert.sharedpref.SharedPreferenceManager;

public class LocationSummary extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker tracker;
    private PolylineOptions polylineOptions;
    private ArrayList<LatLng> points;
    private ArrayList<SummaryModel> summaryModelArrayList;
    private Object user;
    private TextView userNametv;
    private TextView summaryDate;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_summary);
        userNametv = (TextView) findViewById(R.id.userName);
        userNametv.setText(SharedPreferenceManager.getInstance(LocationSummary.this).getUserEmail());
        summaryDate = (TextView) findViewById(R.id.summaryDate);
        summaryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateFilterView();
            }
        });
        final Calendar myCalendar = Calendar.getInstance();
        summaryDate.setText(myCalendar.get(Calendar.YEAR)+"-"+myCalendar.get(Calendar.MONTH)+"-"+myCalendar.get(Calendar.DAY_OF_MONTH));


        setUpMapWithSummary();

    }


    private void setUpMapWithSummary() {

        polylineOptions = new PolylineOptions();

        if (mMap == null) {
            ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    private void parseLocationData(String response) {

        summaryModelArrayList = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(response);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                summaryModelArrayList.add(new SummaryModel(obj.getString("latitude"), obj.getString("longitude"), obj.getString("time")));
            }

            prepareSummary(summaryModelArrayList);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void drawVisitedLocations(ArrayList<LatLng> points) {

        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(14);
        lineOptions.color(Color.RED);

        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
        }

    }

    private void prepareSummary(ArrayList<SummaryModel> summaryModelArrayList) {

        int STEP = 1;
        int start = 0;
        int end = STEP;

        points = new ArrayList<LatLng>();

        for (int i = 0; i <= summaryModelArrayList.size(); i = i + STEP) {

            if (start < summaryModelArrayList.size()) {
                double lat_org = Double.parseDouble(summaryModelArrayList.get(start).Latitude);
                double lon_org = Double.parseDouble(summaryModelArrayList.get(start).Longitude);
                // draw
                points.add(new LatLng(lat_org, lon_org));
                // increment after drawing on map
                start += STEP;
                end += STEP;
            }

        }

        drawVisitedLocations(points);

        LatLng center = new LatLng(Double.parseDouble(summaryModelArrayList.get(0).Latitude),
                Double.parseDouble(summaryModelArrayList.get(0).Longitude));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

    private void requestSummary(final String date) {

        final String url = URLS.URL_LOCATION_SUMMARY;

        StringRequest frndSearchReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        parseLocationData(response);
                        if(progressDialog!=null) progressDialog.dismiss();
                        Log.d("LocationSummary", " res " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                String t_user = SharedPreferenceManager.getInstance(LocationSummary.this).getUserEmail();
                map.put("username", t_user);
                map.put("date", date);
                return map;
            }
        };

        frndSearchReq.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(frndSearchReq, "frndSearch", this);
    }

    private void addMarker(LatLng hotspot, float descriptor, String text) {

        mMap.addMarker(new MarkerOptions()
                .position(hotspot)
                .title(text)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(descriptor)));

    }

    private void activateFilterView() {

        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(year + "-" + monthOfYear + "-" + dayOfMonth);
            }

        };


        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void updateLabel(String date) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Requesting Summary For : "+date);
        progressDialog.show();
        summaryDate.setText(date);
        requestSummary(date);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final String datetime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        requestSummary(datetime);
    }
}
