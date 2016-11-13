package hack.galert.TaskAgents;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import hack.galert.Configs.URLS;
import hack.galert.models.SMLReminderModel;
import hack.galert.connnections.VolleyUtils;

/**
 * Created by Ankit on 10/15/2016.
 */
public class ReminderUpdateTask extends Thread {

    private Context mContext;
    private Handler mHandler;
    private int mTaskType;
    private SMLReminderModel mReminderModel;
    private static final int TASK_TYPE_SET = 0;
    private static final int TASK_TYPE_GET = 1;

    public ReminderUpdateTask(Context context, Handler handler, SMLReminderModel reminderModel) {

        this.mContext = context;
        this.mHandler = handler;
        this.mReminderModel = reminderModel;

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
        final String url = URLS.URL_REMINDERS_GET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        sendDataToHandler(s, TASK_TYPE_GET);
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

    private void allowFriendsRequest() {

        final String url = URLS.URL_REMINDERS_SET;

        StringRequest navItemsListRequest = new StringRequest(
                StringRequest.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        sendDataToHandler(s,TASK_TYPE_SET);
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
                // REMINDERS
                return map;
            }
        };

        navItemsListRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyUtils.getInstance().addToRequestQueue(navItemsListRequest, "navItemsReq", mContext);

    }

    private void sendDataToHandler(String s, int taskType) {

        Message locationMsg = Message.obtain();
        // TODO: get status and return to UI caller

        mHandler.sendMessage(locationMsg);

    }


}
