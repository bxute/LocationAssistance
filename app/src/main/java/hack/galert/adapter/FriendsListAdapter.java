package hack.galert.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import hack.galert.R;
import hack.galert.TaskAgents.FriendsInvalidatorTask;
import hack.galert.activity.LocationTrack;
import hack.galert.connnections.ConnectionUtils;
import hack.galert.log.L;
import hack.galert.models.FriendsModel;

/**
 * Created by Ankit on 10/15/2016.
 */
public class FriendsListAdapter extends ArrayAdapter<FriendsModel> {

    private static Context mContext;
    private static FriendsListAdapter mInstance;
    private ArrayList<FriendsModel> friends;

    TextView name;
    TextView allowanceBtn;
    private Handler mFriendsHandler;

    public static FriendsListAdapter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FriendsListAdapter(context, 0);
        }
        return mInstance;
    }

    public FriendsListAdapter(Context context, int resource) {
        super(context, 0);
        friends = new ArrayList<>();
        this.mContext = context;
    }

    public void setFriendList(ArrayList<FriendsModel> friendList) {
        this.friends = friendList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.friends_item_layout, null, false);
        }

        name = (TextView) view.findViewById(R.id.friendName);
        allowanceBtn = (TextView) view.findViewById(R.id.allowanceBtn);

        final FriendsModel friend = friends.get(position);

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocation(friend.friendsName);
            }
        });

        allowanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ConnectionUtils.getInstance(mContext).isConnected())
                    toggleAllowance(friend.friendsID, !friend.isAllowed);

            }
        });

        // data binding

        name.setText(friend.friendsName);
        if (friend.isAllowed) {
            allowanceBtn.setText("Block User");
            allowanceBtn.setBackgroundResource(R.drawable.disallowed_btn_bg);
        }
        return view;
    }

    private void toggleAllowance(String friendsID, boolean isAllowed) {

        mFriendsHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                setFriendList((ArrayList<FriendsModel>) msg.obj);

            }
        };

        FriendsInvalidatorTask friendsInvalidatorTask = new FriendsInvalidatorTask(mContext,mFriendsHandler);
        friendsInvalidatorTask.setTaskType(FriendsInvalidatorTask.TASK_TYPE_SET);
        friendsInvalidatorTask.setFriendState(friendsID, isAllowed);
        friendsInvalidatorTask.start();

    }

    @Override
    public int getCount() {
        L.m("FriendsAdapter", " friends " + friends.size());
        return friends.size();
    }

    private void requestLocation(String user){
        Intent i = new Intent(mContext, LocationTrack.class);
        i.putExtra("username",user);
        mContext.startActivity(i);
    }


}
