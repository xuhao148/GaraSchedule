package org.aya.garaschedule;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class WeekdayScheduleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekday_schedule);

        getActionBar().setDisplayShowHomeEnabled(false);

        if (!getIntent().hasExtra("weekday")) {
            Toast.makeText(this, R.string.err_no_intent, Toast.LENGTH_SHORT).show();
            this.finish();
        }
        int weekNum = getIntent().getIntExtra("weekday",0);
        ArrayList<ScheduleItem> si = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            int week_no = TimeDataUtil.getWeekNumber();
            int week_day = TimeDataUtil.getWeekday();
            if (getIntent().hasExtra("weekno")) {
                week_no = getIntent().getIntExtra("weekno",week_no);
            }
            SchDBManager sm = SchDBManager.getInstance();
            ArrayList<Calendar> todayStarts = TimeDataUtil.getTodayBegins();
            ArrayList<Calendar> todayEnds = TimeDataUtil.getTodayEnds();
            ArrayList<PeriodItem> allPeriods = sm.getPeriods();
            Calendar now = Calendar.getInstance();
            setTitle(String.format(getString(R.string.weekday_format),getString(ManPeriodActivity.weekString[week_no]),getString(NewPeriodActivity.weekdays[weekNum])));
            for (PeriodItem pi : allPeriods) {
                if (week_no == 2 && pi.week2 || week_no == 1 && pi.week1 || week_no == 3) {
                    if (pi.weekday == weekNum || weekNum == 7) {
                        ScheduleItem sci = new ScheduleItem();
                        sci.item_name = pi.course;
                        sci.on = false;
                        sci.item_info = String.format(getString(R.string.item_info_format),sdf.format(todayStarts.get(pi.start).getTime()),
                                sdf.format(todayEnds.get(pi.end).getTime()),pi.loc);
                        if (todayStarts.get(pi.start).before(now) && todayEnds.get(pi.end).after(now)) {
                            sci.on = true;
                        }
                        si.add(sci);
                    }
                }
            }

            ListView lv = findViewById(R.id.weekday_list);
            lv.setAdapter(new ScheduleListAdapter(this,si));
        } catch (Exception ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }
}
