package hack.galert.models;

/**
 * Created by Ankit on 10/15/2016.
 */
public class SettingsModel {
    public String title;
    public double volumeLevel;
    public boolean bluetoothState;
    public boolean wifiState;
    public boolean mobileDataState;
    public boolean vibrationMode;
    public String latitude;
    public String longitude;

    public SettingsModel(String title, double volumeLevel, boolean bluetoothState, boolean wifiState, boolean mobileDataState, boolean vibrationMode, String latitude, String longitude) {
        this.title = title;
        this.volumeLevel = volumeLevel;
        this.bluetoothState = bluetoothState;
        this.wifiState = wifiState;
        this.mobileDataState = mobileDataState;
        this.vibrationMode = vibrationMode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getVolumeLevel() {
        return volumeLevel;
    }

    public void setVolumeLevel(double volumeLevel) {
        this.volumeLevel = volumeLevel;
    }

    public boolean isBluetoothState() {
        return bluetoothState;
    }

    public void setBluetoothState(boolean bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

    public boolean isWifiState() {
        return wifiState;
    }

    public void setWifiState(boolean wifiState) {
        this.wifiState = wifiState;
    }

    public boolean isMobileDataState() {
        return mobileDataState;
    }

    public void setMobileDataState(boolean mobileDataState) {
        this.mobileDataState = mobileDataState;
    }

    public boolean isVibrationMode() {
        return vibrationMode;
    }

    public void setVibrationMode(boolean vibrationMode) {
        this.vibrationMode = vibrationMode;
    }

    public double getLatitude() {
        return Double.parseDouble(latitude);
//        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return Double.parseDouble(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}