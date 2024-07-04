package org.aya.garaschedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeDataUtil {
    public static final int hours_begin[] = {0,8,8,10,10,11,13,14,15,16,17,18,19,20};
    public static final int hours_end[] = {0,8,9,10,11,12,14,15,15,17,17,19,20,21};
    public static final int minutes_begin[] = {0,0,50,0,50,40,25,15,5,15,5,50,40,30};
    public static final int minutes_end[] = {0,45,35,45,35,25,10,0,50,0,50,35,25,15};
    static ArrayList<Calendar> getTodayBegins() {
        ArrayList<Calendar> cal = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        for (int i=0; i<=13; i++) {
            Calendar it = (Calendar)now.clone();
            it.set(Calendar.HOUR_OF_DAY,hours_begin[i]);
            it.set(Calendar.MINUTE,minutes_begin[i]);
            it.set(Calendar.SECOND,0);
            it.set(Calendar.MILLISECOND,0);
            cal.add(it);
        }
        return cal;
    }

    static ArrayList<Calendar> getTodayEnds() {
        ArrayList<Calendar> cal = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        for (int i=0; i<=13; i++) {
            Calendar it = (Calendar)now.clone();
            it.set(Calendar.HOUR_OF_DAY,hours_end[i]);
            it.set(Calendar.MINUTE,minutes_end[i]);
            it.set(Calendar.SECOND,0);
            it.set(Calendar.MILLISECOND,0);
            cal.add(it);
        }
        return cal;
    }

    static int getWeekNumber() {
        int week_of_year = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int week_number = week_of_year % 2;
        try {
            SchDBManager sm = SchDBManager.getInstance();
            if (sm.getPref("invertedWeek") == 1) {
                if (week_number == 1) week_number = 0;
                else week_number = 1;
            }
            return week_number + 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    static int getWeekday() {
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        day--; // 0 - 7
        return day;
    }

}
