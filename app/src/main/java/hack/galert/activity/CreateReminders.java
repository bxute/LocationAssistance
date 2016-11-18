package hack.galert.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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
import hack.galert.connnections.VolleyUtils;
import hack.galert.sharedpref.SharedPreferenceManager;

public class CreateReminders extends AppCompatActivity {

    private EditText mReminderTitle;
    private EditText mReminderContent;
    private Button mSaveReminderBtn;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminders);

        initViews();

    }

    public void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Reminder");

    }
    private void initViews(){

        progressDialog = new ProgressDialog(this);
        mReminderContent = (EditText) findViewById(R.id.reminders_content_edit);
        mReminderTitle = (EditText) findViewById(R.id.reminders_title_edit);
        mSaveReminderBtn = (Button) findViewById(R.id.save_reminder_btn);
        mSaveReminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Saving Reminder");
                progressDialog.show();

                uploadReminders();
            }
        });
        setToolbar();

    }

    private void uploadReminders(){

       final String lat = String.valueOf(getIntent().getExtras().getDouble("lat"));
       final String lon = String.valueOf(getIntent().getExtras().getDouble("lon"));

        final String url = URLS.URL_REMINDERS_SET;
        Log.d("Reminders","attempt Uploading reminders");
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("Reminders"," response "+s);
                        progressDialog.dismiss();
                        startActivity(new Intent(CreateReminders.this,Reminders.class));
                        finish();
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
                String user = SharedPreferenceManager.getInstance(CreateReminders.this).getUserEmail();
                map.put("longitude", lon);
                map.put("latitude",lat);
                map.put("username",user);
                map.put("reminder_title",mReminderTitle.getText().toString());
                map.put("reminder_text",mReminderContent.getText().toString());

                // REMINDERS
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "remindersave", this);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_reminders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reminders) {
            startActivity(new Intent(CreateReminders.this,Reminders.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
