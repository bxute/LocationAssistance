package hack.galert.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.GPS.GPSTracker;
import hack.galert.R;
import hack.galert.TaskAgents.SettingsInvalidatorTask;
import hack.galert.connnections.VolleyUtils;
import hack.galert.models.SettingsModel;
import hack.galert.sharedpref.SharedPreferenceManager;

public class CreateSettings extends AppCompatActivity {

    private Toolbar toolbar;
    private SeekBar volumeLevel;
    private Switch bluetooth;
    private Switch wifi;
    private Switch mobileData;
    private Switch vibration;
    private EditText title;
    Button saveBtn;
    private Handler mSettingsHandler;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_settings);


        Intent intent = getIntent();
        if (intent == null) {
            // new to create settings
            setToolbar("Create New Settings");

        } else {
            // it is a edit settings activity
            setToolbar("Edit Setting");

        }

        initViews();
    }

    private void initViews() {
        title = (EditText) findViewById(R.id.createSettingsTitle);
        bluetooth = (Switch) findViewById(R.id.bluetoothSwitch);
        wifi = (Switch) findViewById(R.id.wifiSwitch);
        mobileData = (Switch) findViewById(R.id.mobileDataSwitch);
        vibration = (Switch) findViewById(R.id.vibrationSwitch);
        saveBtn = (Button) findViewById(R.id.saveSettingButton);
        volumeLevel = (SeekBar) findViewById(R.id.volumeControl);
        progressDialog =  new ProgressDialog(this);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Settings", "Save Btn clicked");
                progressDialog.setMessage("Saving Settings");
                progressDialog.show();
                allowSettingsRequest();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,AutoSettings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void allowSettingsRequest() {

        Log.d(".SettingsTask","Allowing Settings");

        final SettingsModel settings = new SettingsModel(
                title.getText().toString(),
                volumeLevel.getProgress(),
                bluetooth.isChecked(),
                wifi.isChecked(),
                mobileData.isChecked(),
                vibration.isChecked(),
                "",
                ""
        );

        final String url = URLS.URL_SETTINGS_SET;
//        final String url  = "www.google.co.in";
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        progressDialog.dismiss();
                        Log.d("SettingsTask", " response " + s);

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
                String user = SharedPreferenceManager.getInstance(CreateSettings.this).getUserEmail();
                String lat = String.valueOf(new GPSTracker(CreateSettings.this).getLatitude());
                String lon = String.valueOf(new GPSTracker(CreateSettings.this).getLatitude());

                map.put("longitude", lat);
                map.put("latitude", lon);
                map.put("volumeLevel","4");
                map.put("vibrationMode",settings.vibrationMode+"");
                map.put("brightness","200");
                map.put("mobileData",settings.mobileDataState+"");
                map.put("wifi",settings.wifiState+"");
                map.put("bluetooth",settings.bluetoothState+"");
                map.put("username",user);
                map.put("activity",settings.title);
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "settingsSave", this);

    }

}
