package net.nichnologist.hotspot;

import android.content.Context;
import android.widget.Toast;

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
}
