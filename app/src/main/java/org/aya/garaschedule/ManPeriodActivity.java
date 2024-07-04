package org.aya.garaschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManPeriodActivity extends Activity {
    private ArrayList<GaraItem> items;
    private ArrayList<PeriodItem> periods;
    private int currentIndex;
    private AlertDialog.Builder opDialog;
    public final static int weekString[] = {R.string.week_zero,R.string.week_one,R.string.week_two,R.string.week_both};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_period);

        setTitle(R.string.sched_man);
        getActionBar().setDisplayShowHomeEnabled(false);

        opDialog = new AlertDialog.Builder(ManPeriodActivity.this)
                .setTitle(R.string.operations)
                .setItems(new String[]{getString(R.string.detail), getString(R.string.change_loc), getString(R.string.remove)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Toast.makeText(ManCourseActivity.this, "You chose: "+ManCourseActivity.this.currentIndex, Toast.LENGTH_SHORT).show();
                                switch (i) {
                                    case 0:
                                    {
                                        // 見る
                                        PeriodItem pi = periods.get(currentIndex);
                                        new AlertDialog.Builder(ManPeriodActivity.this)
                                                .setTitle(pi.course)
                                                .setMessage(String.format(getString(R.string.period_detail_format),pi.start,pi.end,pi.loc))
                                                .setPositiveButton("OK",null)
                                                .show();
                                        break;
                                    }
                                    case 1:
                                    {
                                        EditText txt = new EditText(ManPeriodActivity.this);
                                        txt.setHint(R.string.new_loc_title);
                                        txt.setSingleLine();
                                        // 名前変更
                                        new AlertDialog.Builder(ManPeriodActivity.this)
                                                .setTitle(R.string.change_loc)
                                                .setView(txt)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.updateLoc(periods.get(currentIndex).id,txt.getText().toString());
                                                            Toast.makeText(ManPeriodActivity.this,R.string.changed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManPeriodActivity.this)
                                                                    .setTitle(R.string.runtime_error)
                                                                    .setMessage(ex.getLocalizedMessage())
                                                                    .setPositiveButton("OK",null).show();
                                                        }
                                                    }
                                                }).show();
                                        break;
                                    }
                                    case 2:
                                    {
                                        new AlertDialog.Builder(ManPeriodActivity.this)
                                                .setTitle(R.string.remove)
                                                .setMessage(R.string.confirm_remove)
                                                .setPositiveButton(R.string.no,null)
                                                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.removePeriod(periods.get(currentIndex).id);
                                                            Toast.makeText(ManPeriodActivity.this,R.string.removed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManPeriodActivity.this)
                                                                    .setTitle(R.string.runtime_error)
                                                                    .setMessage(ex.getLocalizedMessage())
                                                                    .setPositiveButton("OK",null).show();
                                                        }
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                    break;
                                }
                            }
                        });

        // Get all available courses
        items = new ArrayList<>();
        refreshItems();
        ListView lv = findViewById(R.id.man_period_list);
        lv.setAdapter(new GaraCommandAdapter(this,items));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentIndex = i;
                opDialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.remove_all).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(ManPeriodActivity.this)
                        .setTitle(R.string.remove_all)
                        .setMessage(R.string.remove_all_prompt)
                        .setPositiveButton(R.string.no,null)
                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    SchDBManager dbm = SchDBManager.getInstance();
                                    dbm.removeAllPeriods();
                                    Toast.makeText(ManPeriodActivity.this,R.string.removed_toast,Toast.LENGTH_SHORT).show();
                                    ManPeriodActivity.this.finish();
                                } catch (Exception ex) {
                                    new AlertDialog.Builder(ManPeriodActivity.this)
                                            .setTitle(R.string.runtime_error)
                                            .setMessage(ex.getLocalizedMessage())
                                            .setPositiveButton("OK",null).show();
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });
        return true;
    }

    private void refreshItems() {
        // Get all available courses
        try {
            SchDBManager dbm = SchDBManager.getInstance();
            periods = dbm.getPeriods();
            items.clear();
            for (PeriodItem pi : periods) {
                items.add(new GaraItem(String.format(getString(R.string.period_format),pi.course,getString(weekString[(pi.week1?1:0)+(pi.week2?2:0)]),getString(NewPeriodActivity.weekdays[pi.weekday]),pi.start,pi.end)));
            }
            ListView lv = findViewById(R.id.man_period_list);
            lv.invalidateViews();
        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setMessage(ex.getLocalizedMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ManPeriodActivity.this.finish();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshItems();
    }
}
