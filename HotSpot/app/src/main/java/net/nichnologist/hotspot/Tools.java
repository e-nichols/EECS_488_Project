package net.nichnologist.hotspot;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by dhorvath on 10/31/2015.
 *
 */
public class Tools {
    protected static void toastShort(String text, Context context){
        //Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    protected static void toastLong(String text, Context context){
        //Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /* Generates TimePair object based on time from past to current
    PRE: None.
    POST: None.
    RETURN: TimePair object with 2 timestamps. Uses parameters to find difference between current
             time and past time.
     */
    public static TimePair getTimePairFromNow(int hours, int minutes){

        Calendar cal = Calendar.getInstance();
        java.util.Date d1 = cal.getTime();
        cal.add(Calendar.HOUR, hours * (-1));
        cal.add(Calendar.MINUTE, minutes * (-1));
        java.util.Date d2 = cal.getTime();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String time1 = sdf.format(d1);
        String time2 = sdf.format(d2);

        return new TimePair(Timestamp.valueOf(time2), Timestamp.valueOf(time1));
    }
}
