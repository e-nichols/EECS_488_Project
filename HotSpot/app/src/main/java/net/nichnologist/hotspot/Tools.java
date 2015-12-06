package net.nichnologist.hotspot;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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

    public static java.sql.Timestamp getTimeStampRelative(int minutes){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes * (-1));
        java.util.Date d1 = cal.getTime();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        String time1 = sdf.format(d1);

        return Timestamp.valueOf(time1);
    }

    protected static ArrayList<LocationObject> getDemoList(){
        ArrayList<LocationObject> demoList = new ArrayList();
        //free state
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.971769 + .000020, -95.235589 - .000035), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(35), new LatLng(38.971769 + .000030, -95.235589 + .000070), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(10), new LatLng(38.971769 + .000015, -95.235589 - .000025), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.971769 - .000023, -95.235589 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.971769 - .000017, -95.235589 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.971769 - .000030, -95.235589 + .000060), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.971769 + .000027, -95.235589 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.971769 + .000057, -95.235589 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(40), new LatLng(38.971769 + .000043, -95.235589 - .000037), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.971769 + .000028, -95.235589 + .000042), " ", false));

        //715 restaurant
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.970639 + .000028, -95.236184 + .000028), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.970639 - .000023, -95.236184 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.970639 - .000017, -95.236184 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.970639 - .000030, -95.236184 + .000060), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.970639 + .000027, -95.236184 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.970639 + .000057, -95.236184 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(40), new LatLng(38.970639 + .000043, -95.236184 - .000037), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.970639 + .000028, -95.236184 + .000042), " ", false));

        //la parilla
        demoList.add(new LocationObject(Tools.getTimeStampRelative(60), new LatLng(38.970322 + .000028, -95.235607 + .000042), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(53), new LatLng(38.970322 + .000027, -95.235607 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.970322 + .000057, -95.235607 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(60), new LatLng(38.970322 + .000043, -95.235607 - .000037), " ", false));

        // tonic
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.970159 - .000053, -95.235646 + .000049), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.970159 - .000023, -95.235646 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.970159 - .000017, -95.235646 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.970159 - .000030, -95.235646 + .000060), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.970159 + .000027, -95.235646 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.970159 + .000057, -95.235646 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.970159 - .000023, -95.235646 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.970159 - .000059, -95.235646 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.970159 - .000065, -95.235646 + .000064), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.970159 + .000047, -95.235646 + .000039), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.970159 + .000057, -95.235646 + .000020), " ", false));

        // merchants
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.969562 + .000057, -95.235643 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(53), new LatLng(38.969562 + .000027, -95.235643 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.969562 + .000057, -95.235643 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(90), new LatLng(38.969562 - .000053, -95.235643 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.969562 - .000089, -95.235643 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(35), new LatLng(38.969562 - .000065, -95.235643 + .000064), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.969562 + .000047, -95.235643 + .000039), " ", false));

        // burger stand
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.969201 - .000030, -95.236187 + .000060), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.969201 + .000013, -95.236187 + .000051), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.969201 + .000027, -95.236187 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.969201 + .000057, -95.236187 + .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.969201 - .000023, -95.236187 + .000049), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.969201 - .000059, -95.236187 - .000080), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(25), new LatLng(38.969201 - .000030, -95.236187 + .000060), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(33), new LatLng(38.969201 + .000027, -95.236187 - .000030), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(15), new LatLng(38.969201 - .000047, -95.236187 + .000020), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(5),  new LatLng(38.969201 - .000023, -95.236187 + .000029), " ", false));
        demoList.add(new LocationObject(Tools.getTimeStampRelative(45), new LatLng(38.969201 - .000059, -95.236187 - .000080), " ", false));

        // picklemans
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.968625, -95.235651), " ", false));

        // pig
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.967674, -95.235369), " ", false));

        // jimmy johns
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.966615, -95.235562), " ", false));

        // replay
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.965831, -95.235524), " ", true));

        // encore
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.965312, -95.236157), " ", false));

        // pita pit
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.965170, -95.236200), " ", false));

        // zen zero
        demoList.add(new LocationObject(Tools.getTimeStampRelative(30), new LatLng(38.968926, -95.236203), " ", false));

        return demoList;
    }
}
