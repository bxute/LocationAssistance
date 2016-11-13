package hack.galert.GPS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Created by Ankit on 11/7/2016.
 */
public class LocalLocationManager {

    private Context mContext;
    private LocationManager manager;

    public LocalLocationManager(Context context) {

        this.mContext = context;
    }

    public LocalLocationManager checkAndEnableLocation() {

        manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        }

        return this;
    }

    public void requestCurrentLocation(LocationListener listener) {

        if (manager != null) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500, 15, listener);

        }

    }

}
