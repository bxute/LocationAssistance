package hack.galert.converter;

import android.content.Context;

public class GeoDistanceCalculator {

    private double mLat1;
    private double mLat2;
    private double mLon1;
    private double mLon2;

    private double theta = 0;

    private static Context context;
    private static GeoDistanceCalculator mInstance;

    public GeoDistanceCalculator(Context context) {
        this.context = context;
    }

    public static GeoDistanceCalculator getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GeoDistanceCalculator(context);
        }
        return mInstance;
    }

    public GeoDistanceCalculator setLatitudes(double origin , double destination){

        this.mLat1 = origin;
        this.mLat2 = destination;
        return mInstance;
    }

    public GeoDistanceCalculator setLongitude(double origin , double destination){
        this.mLon1 = origin;
        this.mLon2 = destination;
        return mInstance;
    }

    public double getDistanceInMeter(){

        double distance = 0;
        theta = mLon1 - mLon2;

        distance = Math.sin((Math.PI/180.0)*mLat1) * Math.sin((Math.PI/180.0)*mLat2)
                    +
                   Math.cos((Math.PI/180.0)*mLat1) * Math.cos((Math.PI/180.0)*mLat2) * Math.cos((Math.PI/180.0)*theta);

        distance = Math.acos(distance);
        distance = (distance * 180.0) / Math.PI;
        distance = distance * 111189.57696;

        // return distance in meter

        return distance;
    }

}