package hack.galert.DeviceSettingsConfiguration;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Ankit on 11/13/2016.
 */
public class SettingsConfigurer {

    private static Context context;
    private static SettingsConfigurer mInstance;
    private AudioManager audioManager;

    public SettingsConfigurer(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static SettingsConfigurer getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SettingsConfigurer(context);
        }
        return mInstance;
    }

    public void putOnSilent(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void putOnVibration(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    private void putOnNormalMode(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void putWifi(boolean state){
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(state);
    }

    public void putBluetooth(boolean state){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();

        if(isEnabled && !state){
            bluetoothAdapter.disable();
        }
        if(!isEnabled && state){
            bluetoothAdapter.enable();
        }

    }

    public void setBrightness(int level){ // max brightness 255


    }

    public void setMobileData(boolean state){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyManager.getClass().getDeclaredMethod("setDataEnabled",boolean.class);
            if(setMobileDataEnabledMethod !=null){
                setMobileDataEnabledMethod.invoke(telephonyManager,state);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
