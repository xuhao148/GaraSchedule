package org.aya.garaschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import java.security.Key;
import java.util.ArrayList;

public class NewPeriodActivity extends Activity {
    SchDBManager dbm;
    private ArrayList<CourseItem> courses;
    private ArrayList<String> courseNames;
    private AlertDialog.Builder timePick;
    private int timePickTarget = 0;
    private int startTime = 1;
    private int endTime = 1;

    public static int[] weekdays = {R.string.sunday,R.string.monday,R.string.tuesday,R.string.wednesday,R.string.thursday,R.string.friday,R.string.saturday};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_period);
        setTitle(getString(R.string.new_schedule));
        getActionBar().setDisplayShowHomeEnabled(false);

        try {
            dbm = SchDBManager.getInstance();

            // Populate course list
            courses = dbm.getCourses();
            courseNames = new ArrayList<>();
            for (CourseItem ci : courses) {
                courseNames.add(ci.name+" / "+ci.teacher);
            }
            String aCname[] = new String[courseNames.size()];
            courseNames.toArray(aCname);
            ArrayAdapter<CharSequence> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,aCname);
            Spinner sp = findViewById(R.id.p_course);
            sp.setAdapter(aa);

            String[] weekday_loc = new String[7];
            for (int ii = 0; ii < 7; ii++) {
                weekday_loc[ii] = getString(weekdays[ii]);
            }

            // Populate weekday list
            ArrayAdapter<CharSequence> aa2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, weekday_loc);
            Spinner spWeek = findViewById(R.id.p_weekday);
            spWeek.setAdapter(aa2);

            // Make a dialog builder for time

            Button bStart = findViewById(R.id.p_start);
            bStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timePickTarget = 0;
                    pickNumber(startTime);
                }
            });
            Button bEnd = findViewById(R.id.p_end);
            bEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timePickTarget = 1;
                    pickNumber(endTime);
                }
            });

            Button bCreate = findViewById(R.id.p_add_btn);
            Button bCancel = findViewById(R.id.p_cancel_btn);
            CheckBox c1 = findViewById(R.id.p_c1);
            CheckBox c2 = findViewById(R.id.p_c2);
            EditText loc = findViewById(R.id.p_loc);

            bCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NewPeriodActivity.this.finish();
                }
            });

            bCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sp.getSelectedItemPosition() < 0) {
                        Toast.makeText(NewPeriodActivity.this, R.string.no_courses_yet, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (startTime > endTime) {
                        Toast.makeText(NewPeriodActivity.this, R.string.start_gt_end, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int week = (c1.isChecked()?1:0) + (c2.isChecked()?2:0);
                    int flg = calculateFlag(spWeek.getSelectedItemPosition(),week);
                    try {
                        dbm.addSchedule(courses.get(sp.getSelectedItemPosition()).id,startTime,endTime,flg,
                                loc.getText().toString());
                        Toast.makeText(NewPeriodActivity.this, R.string.inserted, Toast.LENGTH_SHORT).show();
                        NewPeriodActivity.this.finish();
                    } catch (Exception ex) {
                        new AlertDialog.Builder(NewPeriodActivity.this)
                                .setTitle(R.string.error_title)
                                .setMessage(ex.getLocalizedMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            });

        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.runtime_error)
                    .setMessage(ex.getLocalizedMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NewPeriodActivity.this.finish();
                        }
                    });
        }
    }

    private void pickNumber(int initial) {
        NumberPicker np = new NumberPicker(this);
        np.setMinValue(1);
        np.setMaxValue(13);
        np.setValue(initial);
        np.setFocusable(true);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(R.string.set_time)
                .setView(np)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (timePickTarget == 0) {
                            startTime = np.getValue();
                            Button bs = findViewById(R.id.p_start);
                            bs.setText(String.format(getString(R.string.start_time_format),startTime));
                        } else if (timePickTarget == 1) {
                            endTime = np.getValue();
                            Button bs = findViewById(R.id.p_end);
                            bs.setText(String.format(getString(R.string.start_time_format),endTime));
                        }
                    }
                }).show();
        np.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int kc = keyEvent.getKeyCode();
                if (kc == KeyEvent.KEYCODE_DPAD_CENTER || kc == KeyEvent.KEYCODE_ENTER) {
                    // Confirm
                    dlg.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                } else if (kc == KeyEvent.KEYCODE_F1) {
                    np.requestFocus();
                }
                return true;
            }
        });
    }

    public static int calculateFlag(int weekday, int weekflag) {
        return weekday * 4 + weekflag;
    }
}
