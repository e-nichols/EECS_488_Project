package net.nichnologist.hotspot;

import java.sql.Date;

/**
 * Created by dhorvath on 11/10/2015.
 */
public class TimePair {
    java.sql.Timestamp time1;
    java.sql.Timestamp time2;
    TimePair(java.sql.Timestamp t1, java.sql.Timestamp t2){
        time1 = t1;
        time2 = t2;
    }
}
