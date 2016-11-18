package hack.galert.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.R;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.connnections.VolleyUtils;
import hack.galert.database.LocalDatabaseHelper;
import hack.galert.models.SMLReminderModel;
import hack.galert.models.SettingsModel;
import hack.galert.sharedpref.SharedPreferenceManager;

/**
 * Created by Ankit on 11/13/2016.
 */
public class AutoSettingsListAdapter extends ArrayAdapter<SettingsModel> {

    private static Context mContext;
    private static AutoSettingsListAdapter mInstance;
    private ArrayList<SettingsModel> settingsList;


    TextView deleteSettingsBtn;
    private String user;
    private TextView settingsTitle;
    private ProgressDialog progressDialog;

    public AutoSettingsListAdapter(Context context, int resource) {
        super(context, 0);
        settingsList = new ArrayList<>();
        mContext = context;
        user = SharedPreferenceManager.getInstance(mContext).getUserEmail();
        progressDialog = new ProgressDialog(context);

    }

    public static AutoSettingsListAdapter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AutoSettingsListAdapter(context, 0);
        }
        return mInstance;
    }

    public void loadInitials() {
        requestSettings();
    }

    public void setSettings(ArrayList<SettingsModel> friendList) {
        this.settingsList = friendList;
        notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.reminder_item_layout, null, false);
        }

        settingsTitle = (TextView) view.findViewById(R.id.reminder_title);
        deleteSettingsBtn = (TextView) view.findViewById(R.id.reminder_delete);
        final SettingsModel setting = settingsList.get(position);

        deleteSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (progressDialog == null) progressDialog = new ProgressDialog(mContext);

                progressDialog.setMessage("Deleting Settings");
                progressDialog.show();
                if (ConnectionUtils.getInstance(mContext).isConnected())
                    deleteSetting(setting.latitude, setting.longitude);

            }
        });

        // data binding
        settingsTitle.setText(setting.getTitle());

        return view;
    }

    private void deleteSetting(final String lat, final String lon) {

        final String url = URLS.URL_SETTINGS_DELETE;
        Log.d("Settings", "attempt delete settings");
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        requestSettings();
                        Log.d("Settings", " response " + s);
                        progressDialog.dismiss();
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

                Log.d("Settings ", " sending " + lat + " lon " + lon);
                map.put("username", user);
                map.put("latitude", lat);
                map.put("longitude", lon);
                // REMINDERS
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "remindersave", mContext);

    }

    private void requestSettings() {

        final String url = URLS.URL_SETTINGS_GET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        parseSettingsJson(s);
                        Log.d("Settings", " response " + s);
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
                String user = SharedPreferenceManager.getInstance(mContext).getUserEmail();
                map.put("username", user);
                // REMINDERS
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "remindersunc", mContext);


    }

    private void parseSettingsJson(String s) {

       LocalDatabaseHelper helper =  LocalDatabaseHelper.getInstance(mContext);
        helper.truncateSettingsTable();

        ArrayList<SettingsModel> settings = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(s);

            for (int i = 0; i < array.length(); i++) {

                JSONObject setting = array.getJSONObject(i);

                SettingsModel model = new SettingsModel(
                        setting.getJSONObject("fields").getString("title"),
                        setting.getJSONObject("fields").getDouble("volumeLevel"),
                        setting.getJSONObject("fields").getBoolean("bluetooth"),
                        setting.getJSONObject("fields").getBoolean("wifi"),
                        setting.getJSONObject("fields").getBoolean("mobileData"),
                        setting.getJSONObject("fields").getBoolean("vibrationMode"),
                        setting.getJSONObject("fields").getString("latitude"),
                        setting.getJSONObject("fields").getString("longitude"));

                helper.writeSetting(model);
                settings.add(model);
            }

            setSettings(settings);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return settingsList.size();
    }

}
