package hack.galert.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import hack.galert.R;
import hack.galert.activity.Reminders;

/**
 * Created by Ankit on 9/10/2016.
 */
public class LocalNotificationManager {

    public static LocalNotificationManager mInstance;
    private static Context context;
    private int mNotificationId = 0;

    public LocalNotificationManager() {
    }

    public LocalNotificationManager(Context context) {
        this.context = context;
    }

    public static LocalNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocalNotificationManager(context);
        }
        return mInstance;
    }

    public void launchNotification(String msg,int id) {

        //TODO: change icon and add pendingIntent , which navigates user to downloads activity

        Intent intent = new Intent(context, Reminders.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(android.R.drawable.ic_popup_reminder);
        mBuilder.setContentTitle("Location Assistant");
        mBuilder.setContentText(msg);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification));
        this.mNotificationId = id;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(mNotificationId, mBuilder.build());
    }

}
