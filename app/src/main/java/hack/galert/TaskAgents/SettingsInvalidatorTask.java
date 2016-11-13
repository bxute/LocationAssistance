package hack.galert.TaskAgents;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
//import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.connnections.VolleyUtils;
import hack.galert.models.SettingsModel;

/**
 * Created by Ankit on 10/14/2016.
 */
public class SettingsInvalidatorTask extends Thread {
    private Context mContext;
    private Handler mHandler;
    private int mTaskType;
    private SettingsModel mSettings;
    public static final int TASK_TYPE_SET = 0;
    public static final int TASK_TYPE_GET = 1;

    public SettingsInvalidatorTask(Context context, Handler handler) {

        this.mContext = context;
        this.mHandler = handler;

    }

    public void setTaskType(int type) {
        this.mTaskType = type;
    }

    public void setSettings(SettingsModel settings) {
        this.mSettings = settings;
    }

    @Override
    public void run() {
        if (mTaskType == TASK_TYPE_SET) {
            allowSettingsRequest();
        } else {
            getSettingsRequest();
        }
    }

    private void getSettingsRequest() {

        final String url = URLS.URL_SETTINGS_GET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        sendDataToHandler(s, mTaskType);
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
                map.put("owner", "");
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "navItemsReq", mContext);

    }

    private void allowSettingsRequest() {

        Log.d("SettingsTask","Allowing Settings");

        //final String url = URLS.URL_SETTINGS_SET;
        final String url  = "www.google.co.in";
        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        Log.d("SettingsTask"," response "+s);
                        sendDataToHandler(s, mTaskType);
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
                map.put("longitude", "12.1112");
                map.put("latitude","23.3433");
                map.put("volumeLevel","4.5");
                map.put("vibrationMode","True");
                map.put("brightness","6.7");
                map.put("mobileData","True");
                map.put("wifi","True");
                map.put("bluetooth","True");
                map.put("username","Ankit");
                map.put("activity","Buy Milk");
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "navItemsReq", mContext);

    }

    private void sendDataToHandler(String s, int type) {

        Message locationMsg = Message.obtain();

        if (type == TASK_TYPE_GET) {
         //   Gson gson = new Gson();
            SettingsModel settings = null;// = gson.fromJson(s, SettingsModel.class);  // todo: annotations needed in model class
            locationMsg.obj = settings;

        } else {

        }

        mHandler.sendMessage(locationMsg);

    }
}