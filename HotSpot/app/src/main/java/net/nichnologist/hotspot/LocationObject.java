package net.nichnologist.hotspot;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dhorvath on 12/5/2015.
 */
public class LocationObject {
    private java.sql.Timestamp time;
    private LatLng latlon;
    private String GoogleID;
    private boolean checkIn;

    public LocationObject(java.sql.Timestamp time, LatLng latlon, String GoogleID, boolean checkIn){
        this.time = time;
        this.latlon = latlon;
        this.GoogleID = GoogleID;
        this.checkIn = checkIn;
    }

    public java.sql.Timestamp getTime(){
        return time;
    }

    public LatLng getLatLng(){
        return latlon;
    }

    public String getGoogleID(){
        return GoogleID;
    }

    public boolean getCheckIn(){
        return checkIn;
    }


}