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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.connnections.VolleyUtils;
import hack.galert.models.FriendsModel;
import hack.galert.sharedpref.SharedPreferenceManager;

/**
 * Created by Ankit on 10/14/2016.
 */
public class FriendsInvalidatorTask extends Thread {

    private Context mContext;
    private Handler mHandler;
    private int mTaskType;
    private String mAllowedFriends;
    public static final int TASK_TYPE_SET = 0;
    public static final int TASK_TYPE_GET = 1;
    private String friendsID;
    private boolean isAllowed;

    public FriendsInvalidatorTask(Context context, Handler handler) {

        this.mContext = context;
        this.mHandler = handler;
    }

    public void setmAllowedFriends(String mAllowedFriends) {
        this.mAllowedFriends = mAllowedFriends;
    }

    public void setTaskType(int type) {
        this.mTaskType = type;
    }

    @Override
    public void run() {
        if (mTaskType == TASK_TYPE_SET) {
            allowFriendsRequest();
        } else {
            getFriendsStatusRequest();
        }
    }

    private void getFriendsStatusRequest() {
        final String url = URLS.URL_FRIENDS_GET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("FriendsTest", "resp " + s);
                        sendDataToHandler(s);
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
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "navItemsReq", mContext);

    }

    private void allowFriendsRequest() {

        final String url = URLS.URL_FRIENDS_SET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("Friends"," after delete resp "+s);
                        sendDataToHandler(s);
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
                map.put("username",user);
                map.put("friendUsername", friendsID);
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "navItemsReq", mContext);

    }

    private void sendDataToHandler(String s) {

        Message locationMsg = Message.obtain();
        try {
            JSONArray array = new JSONArray(s);
            ArrayList<FriendsModel> frnds = new ArrayList<>();

            for(int i=0;i<array.length();i++){
                frnds.add(new FriendsModel(array.getString(i),array.getString(i),true));
            }

            locationMsg.obj = frnds;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mHandler.sendMessage(locationMsg);

    }

    public void setFriendState(String friendsID, boolean isAllowed) {

        this.friendsID = friendsID;
        this.isAllowed = isAllowed;
    }
}
