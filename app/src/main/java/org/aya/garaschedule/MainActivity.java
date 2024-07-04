package org.aya.garaschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayShowHomeEnabled(false);

        // Set title
        setTitle(R.string.app_name);

        // Initialize main menu
        ArrayList<GaraItem> items = new ArrayList<>();
        items.add(new GaraItem(getString(R.string.sched_today)));
        items.add(new GaraItem(getString(R.string.sched_by_weekday)));
        items.add(new GaraItem(getString(R.string.sched_man)));
        items.add(new GaraItem(getString(R.string.sched_preview)));
        items.add(new GaraItem(getString(R.string.end_activity)));
        GaraCommandAdapter mainMenuAdapter = new GaraCommandAdapter(this,items);
        ListView lv = (ListView) findViewById(R.id.mainmenu);
        lv.setAdapter(mainMenuAdapter);
        lv.setSelection(0);
        lv.requestFocus();
        lv.setOnItemClickListener(new MainMenuDealer());

        // Read / initialize data
        try {
            SchDBManager sm = SchDBManager.getInstance();
            db = sm.db;
            if (sm.getPref("initialized") == 0) {
                sm.setPref("initialized",1);
                sm.setPref("pdfWidth",598);
                sm.setPref("pdfHeight",842);
                sm.setPref("pdfMargin",30);
                sm.setPref("invertedWeek",0);
            }
        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setMessage(ex.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                        }
                    }).show();
        }

        Button bToggle = findViewById(R.id.toggle_week);
        bToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    SchDBManager sc = SchDBManager.getInstance();
                    sc.setPref("invertedWeek",1-sc.getPref("invertedWeek"));
                    updateWeekInfo();
                    updateNextCourseInfo();
                } catch (Exception ex) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.error_title)
                            .setMessage(ex.getMessage())
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.this.finish();
                                }
                            }).show();
                }
            }
        });

        updateWeekInfo();


        // TODO: Update the "Next Course" info
        updateNextCourseInfo();

    }

    void updateWeekInfo() {
        String week_str = getString(ManPeriodActivity.weekString[TimeDataUtil.getWeekNumber()]);
        TextView tv = findViewById(R.id.week_label);
        tv.setText(String.format(getString(R.string.today_is_week),week_str));
    }

    void updateNextCourseInfo() {
        try {
            int week_no = TimeDataUtil.getWeekNumber();
            int week_day = TimeDataUtil.getWeekday();
            SchDBManager sm = SchDBManager.getInstance();
            ArrayList<Calendar> todayStarts = TimeDataUtil.getTodayBegins();
            ArrayList<Calendar> todayEnds = TimeDataUtil.getTodayEnds();
            ArrayList<PeriodItem> allPeriods = sm.getPeriods();
            Calendar now = Calendar.getInstance();
            PeriodItem closest = null;
            long min_interv = Long.MAX_VALUE;
            for (PeriodItem pi : allPeriods) {
                if (week_no == 2 && pi.week2 || week_no == 1 && pi.week1) {
                    if (pi.weekday == week_day) {
                        Calendar begin = todayStarts.get(pi.start);
                        if (now.before(begin)) {
                            long dura = begin.getTimeInMillis() - now.getTimeInMillis();
                            if (dura < min_interv) {
                                min_interv = dura;
                                closest = pi;
                            }
                        }
                    }
                }
            }

            TextView tv1 = findViewById(R.id.next_name);
            TextView tv2 = findViewById(R.id.next_time_loc);

            if (closest == null) {
                tv1.setText(R.string.no_next);
                tv2.setText("");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                tv1.setText(closest.course);
                tv2.setText(String.format(getString(R.string.time_loc_format),sdf.format(todayStarts.get(closest.start).getTime()),
                        sdf.format(todayEnds.get(closest.end).getTime()),closest.loc));
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNextCourseInfo();
        updateWeekInfo();
    }

    class MainMenuDealer implements AdapterView.OnItemClickListener {
        int week_no_chosen;

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                case 0:
                {
                    Intent it = new Intent(MainActivity.this, WeekdayScheduleActivity.class);
                    it.putExtra("weekday",TimeDataUtil.getWeekday());
                    startActivity(it);
                    break;
                }
                case 1:
                {
                    RadioGroup rg = new RadioGroup(MainActivity.this);
                    rg.setOrientation(RadioGroup.HORIZONTAL);
                    RadioButton rb1 = new RadioButton(MainActivity.this);
                    rb1.setText(R.string.week_1);
                    rg.addView(rb1);
                    RadioButton rb2 = new RadioButton(MainActivity.this);
                    rb2.setText(R.string.week_2);
                    rg.addView(rb2);
                    rb1.setChecked(true);

                    String[] weekDays = new String[7];
                    for (int ii=0; ii<7; ii++)
                        weekDays[ii] = getString(NewPeriodActivity.weekdays[ii]);

                    AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.select_weekday)
                            .setSingleChoiceItems(weekDays, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    week_no_chosen = i;
                                }
                            })
                            .setView(rg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent it = new Intent(MainActivity.this, WeekdayScheduleActivity.class);
                                    it.putExtra("weekday",week_no_chosen);
                                    it.putExtra("weekno",rb1.isChecked()?1:2);
                                    startActivity(it);
                                }
                            })
                            .show();
                    break;
                }
                case 2:
                {
                    Intent it = new Intent(MainActivity.this,SchedManMenuActivity.class);
                    startActivity(it);
                    break;
                }
                case 3:
                {
                    Intent it = new Intent(MainActivity.this, DocActivity.class);
                    startActivity(it);
                    break;
                }
                case 4:
                    MainActivity.this.finish();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.about_this_app).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.about_this_app)
                        .setMessage(R.string.about_this_app_info)
                        .setPositiveButton("OK",null)
                        .setIcon(R.mipmap.ic_launcher)
                        .show();
                return true;
            }
        });
        return true;
    }
}