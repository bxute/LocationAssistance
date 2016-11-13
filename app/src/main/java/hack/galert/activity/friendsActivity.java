package hack.galert.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.Constants;
import hack.galert.Configs.URLS;
import hack.galert.DeviceSettingsConfiguration.SettingsConfigurer;
import hack.galert.R;
import hack.galert.TaskAgents.FriendsInvalidatorTask;
import hack.galert.adapter.FriendsListAdapter;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.connnections.VolleyUtils;
import hack.galert.database.LocalDatabaseHelper;
import hack.galert.models.FriendsModel;
import hack.galert.models.SMLReminderModel;
import hack.galert.models.SettingsModel;
import hack.galert.sharedpref.SharedPreferenceManager;

public class friendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView friendsList;
    private FriendsListAdapter adapter;
    private Handler mFriendsDataHandler;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setToolbar();
        progressDialog = new ProgressDialog(this);
        setList();
//        plugData();
        if (ConnectionUtils.getInstance(this).isConnected()) {
            loadFriends();
        }
    }

    private void testSettings(){
        SettingsConfigurer configurer = SettingsConfigurer.getInstance(this);
        configurer.putBluetooth(true);
        configurer.putOnSilent();
        configurer.putOnVibration();
        configurer.setBrightness(200);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Search Friend");
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                progressDialog.setMessage("Searching For Friend");
                progressDialog.show();

                searchFriend(s);
                return true;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFriends(){

        mFriendsDataHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ArrayList<FriendsModel> friends = (ArrayList<FriendsModel>) msg.obj;
                Log.d("Friends"," list size "+friends.size());
                adapter.setFriendList(friends);

             }
        };

        FriendsInvalidatorTask friendsInvalidatorTask = new FriendsInvalidatorTask(this,mFriendsDataHandler);
        friendsInvalidatorTask.setTaskType(FriendsInvalidatorTask.TASK_TYPE_GET);
        friendsInvalidatorTask.start();

    }

    public void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void setList(){

        friendsList = (ListView) findViewById(R.id.friendsListView);
        adapter =  FriendsListAdapter.getInstance(this);
        friendsList.setAdapter(adapter);
    }

    public void plugData(){

        ArrayList<FriendsModel> list = new ArrayList<>();

        list.add(new FriendsModel("Ankit Kumar","2012",true));
        list.add(new FriendsModel("Sumit Kumar","2012",true));
        list.add(new FriendsModel("Saman","2012",true));
        list.add(new FriendsModel("Saketnt","2012",false));
        list.add(new FriendsModel("Senate", "2012", true));
        list.add(new FriendsModel("suman", "2012", false));
        list.add(new FriendsModel("Rahul", "2012", true));
        list.add(new FriendsModel("Rae Singh", "2012", false));
        list.add(new FriendsModel("Rupal singh", "2012", false));

        adapter.setFriendList(list);
    }

    public void testDb(){

        LocalDatabaseHelper helper = LocalDatabaseHelper.getInstance(this);

        // write Test
        SettingsModel settingsModel = new SettingsModel("Class Setting",1,false,false,true,true,"-12.2","123.32");
        SMLReminderModel smlReminderModel = new SMLReminderModel(1,
                "Reminder for medicince",
                "remind me about medicine",
                "",
                "");

        ArrayList<FriendsModel> friendsModels = new ArrayList<>();

        friendsModels.add(new FriendsModel("Ankit",
                "12021",
                true));

        friendsModels.add(new FriendsModel("Ramesh",
                "324",
                false));

        friendsModels.add(new FriendsModel("Smith",
                "334",
                false));


        helper.writeSetting(settingsModel);
        helper.writeReminder(smlReminderModel);
        helper.writeFriends(friendsModels);

        helper.getSettings();
        helper.getReminders();
        helper.getFriends();

    }

    private void searchFriend(final String frnd_name){

        final String url = URLS.URL_FRIENDS_SEARCH;

        StringRequest frndSearchReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("SearchResponse" ," res "+response );
                        try {
                            JSONObject object = new JSONObject(response);
                            if(object.getString("flag").equals("True")){
                                showAddFriendDialog(object.getString("userName"));
                            }else{
                                Toast.makeText(friendsActivity.this,"User Doesn`t Exist",Toast.LENGTH_LONG).show();

                            }

                            progressDialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("username",frnd_name);
                return map;
            }
        };

        frndSearchReq.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(frndSearchReq, "frndSearch", this);
    }

    private void showAddFriendDialog(final String frnd) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Friend");

        // Setting Dialog Message
        alertDialog.setMessage("Add '" + frnd + "' As Friend");

        // On pressing Settings button
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                progressDialog.setMessage("Adding To Your Friend List");
                progressDialog.show();
                addFriend(frnd);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void addFriend(final String frndToAdd){

        final String url = URLS.URL_FRIENDS_ADD;

        StringRequest frndSearchReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Friend","[add] resp "+response);
                        try {
                            JSONObject object = new JSONObject(response);
                            if(object.getString("status").equals("200")){
                                progressDialog.hide();
                                loadFriends();
                            }else{
                                Toast.makeText(friendsActivity.this,"Cannot Add As Friend ! ",Toast.LENGTH_LONG).show();
                            }

                            progressDialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                String prefName = SharedPreferenceManager.getInstance(friendsActivity.this).getUserEmail();
                map.put("username",prefName);
                map.put("friendList","[\""+frndToAdd+"\"]");
                return map;
            }
        };

        frndSearchReq.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(frndSearchReq, "frndSearch", this);

    }

}
