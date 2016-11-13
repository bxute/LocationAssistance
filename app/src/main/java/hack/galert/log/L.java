package hack.galert.log;

import android.app.Application;
import android.util.Log;

/**
 * Created by Ankit on 10/15/2016.
 */
public class L extends Application {

    public static void m(String TAG, String m){
        Log.d(TAG, m);
    }

}