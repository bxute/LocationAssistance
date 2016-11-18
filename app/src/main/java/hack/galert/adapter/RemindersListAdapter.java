package hack.galert.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import hack.galert.GPS.GPSTracker;
import hack.galert.R;
import hack.galert.TaskAgents.FriendsInvalidatorTask;
import hack.galert.activity.LocationTrack;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.connnections.VolleyUtils;
import hack.galert.database.LocalDatabaseHelper;
import hack.galert.log.L;
import hack.galert.models.FriendsModel;
import hack.galert.models.SMLReminderModel;
import hack.galert.sharedpref.SharedPreferenceManager;

/**
 * Created by Ankit on 11/13/2016.
 */
public class RemindersListAdapter extends ArrayAdapter<SMLReminderModel> {
    private static Context mContext;
    private static RemindersListAdapter mInstance;
    private ArrayList<SMLReminderModel> reminderModels;


    TextView deleteReminderBtn;
    private String user;
    private TextView reminderTitle;
    private TextView reminderNote;
    private ProgressDialog progressDialog;

    public RemindersListAdapter(Context context, int resource) {
        super(context, 0);
        reminderModels = new ArrayList<>();
        mContext = context;
        user = SharedPreferenceManager.getInstance(mContext).getUserEmail();
        progressDialog =  new ProgressDialog(context);

    }

    public static RemindersListAdapter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RemindersListAdapter(context, 0);
        }
        return mInstance;
    }

    public void loadInitials(){
        requestReminders();
    }

    public void setReminders(ArrayList<SMLReminderModel> friendList) {
        this.reminderModels = friendList;
        notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.reminder_item_layout, null, false);
        }

        reminderTitle = (TextView) view.findViewById(R.id.reminder_title);
        reminderNote = (TextView) view.findViewById(R.id.reminder_note);
        deleteReminderBtn = (TextView) view.findViewById(R.id.reminder_delete);
        final SMLReminderModel reminderModel = reminderModels.get(position);

        deleteReminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ConnectionUtils.getInstance(mContext).isConnected())
                    deleteReminder(reminderModel._id_reminder+"");

            }
        });

        // data binding
        reminderTitle.setText(reminderModel.getTitle());
        reminderNote.setText(reminderModel.content);

        return view;
    }

    private void deleteReminder(final String reminder_id) {

        final String url = URLS.URL_REMINDERS_DELETE;
        Log.d("Reminders", "attempt delete reminders");
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        requestReminders();
                        Log.d("Reminders", " response " + s);
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

                Log.d("Reminder"," sending "+user+" id-"+reminder_id);
                map.put("username", user);
                map.put("id", reminder_id);
                // REMINDERS
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "remindersave", mContext);


    }

    private void requestReminders() {

        final String url = URLS.URL_REMINDERS_GET;
        Log.d("Reminders", "attempt delete reminders");
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        parseReminderJson(s);
                        Log.d("Reminders", " response " + s);
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

    private void parseReminderJson(String s) {

        LocalDatabaseHelper helper = LocalDatabaseHelper.getInstance(mContext);
        helper.truncateRemindersTable();

        ArrayList<SMLReminderModel> reminders = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(s);

            for (int i = 0; i < array.length(); i++) {

                JSONObject reminder = array.getJSONObject(i);

                SMLReminderModel model =new SMLReminderModel(reminder.getInt("pk"),
                        reminder.getJSONObject("fields").getString("reminder_title"),
                        reminder.getJSONObject("fields").getString("reminder_text"),
                        reminder.getJSONObject("fields").getString("latitude"),
                        reminder.getJSONObject("fields").getString("longitude"));

                helper.writeReminder(model);
                reminders.add(model);

            }

            setReminders(reminders);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return reminderModels.size();
    }

}

