package hack.galert.TaskAgents;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.connnections.VolleyUtils;
import hack.galert.sharedpref.SharedPreferenceManager;

/**
 * Created by Ankit on 10/14/2016.
 */
public class LocationRequesterTask extends Thread {

    private Context mContext;
    private Handler mHandler;
    private String mFriend;

    public LocationRequesterTask(Context context, Handler handler , String user) {

        this.mContext = context;
        this.mHandler = handler;
        this.mFriend = user;
    }

    @Override
    public void run() {
            placeRequest();
    }

    private void placeRequest(){

        final String url = URLS.URL_LOCATION_REQUEST;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("LocationRequest"," res "+s);
                        sendDataToHandler(s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String userName = SharedPreferenceManager.getInstance(mContext).getUserEmail();
                Log.d("LocationTrack"," request from "+userName+" for "+mFriend);
                params.put("selfUsername", userName);
                params.put("friendUsername",mFriend);
                return params;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "locatingFrnd", mContext);

    }

    private void sendDataToHandler(String s) {

        Message locationMsg = Message.obtain();
        locationMsg.obj = s;
        mHandler.sendMessage(locationMsg);

    }
}
