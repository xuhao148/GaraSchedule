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
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ManCourseActivity extends Activity {
    private ArrayList<GaraItem> items;
    private ArrayList<CourseItem> courses;
    private int currentIndex;
    private AlertDialog.Builder opDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_course);

        setTitle(getString(R.string.course_man));
        getActionBar().setDisplayShowHomeEnabled(false);

        opDialog = new AlertDialog.Builder(ManCourseActivity.this)
                .setTitle(R.string.operations)
                .setItems(new String[]{getString(R.string.detail), getString(R.string.rename), getString(R.string.set_teacher), getString(R.string.set_desc), getString(R.string.remove)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Toast.makeText(ManCourseActivity.this, "You chose: "+ManCourseActivity.this.currentIndex, Toast.LENGTH_SHORT).show();
                                switch (i) {
                                    case 0:
                                    {
                                        // 見る
                                        CourseItem ci = courses.get(currentIndex);
                                        new AlertDialog.Builder(ManCourseActivity.this)
                                                .setTitle(ci.name)
                                                .setMessage(String.format(getString(R.string.detail_format),ci.teacher,ci.desc))
                                                .setPositiveButton("OK",null)
                                                .show();
                                        break;
                                    }
                                    case 1:
                                    {
                                        EditText txt = new EditText(ManCourseActivity.this);
                                        txt.setHint(R.string.new_name);
                                        txt.setSingleLine();
                                        // 名前変更
                                        new AlertDialog.Builder(ManCourseActivity.this)
                                                .setTitle(R.string.rename)
                                                .setView(txt)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.updateCourseName(courses.get(currentIndex).id,txt.getText().toString());
                                                            Toast.makeText(ManCourseActivity.this, R.string.changed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManCourseActivity.this)
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
                                        EditText txt = new EditText(ManCourseActivity.this);
                                        txt.setHint(R.string.new_teacher);
                                        txt.setSingleLine();
                                        // 教師変更
                                        new AlertDialog.Builder(ManCourseActivity.this)
                                                .setTitle(R.string.set_teacher)
                                                .setView(txt)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.updateTeacherName(courses.get(currentIndex).id,txt.getText().toString());
                                                            Toast.makeText(ManCourseActivity.this,R.string.changed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManCourseActivity.this)
                                                                    .setTitle(R.string.runtime_error)
                                                                    .setMessage(ex.getLocalizedMessage())
                                                                    .setPositiveButton("OK",null).show();
                                                        }
                                                    }
                                                }).show();
                                        break;
                                    }
                                    case 3:
                                    {
                                        EditText txt = new EditText(ManCourseActivity.this);
                                        txt.setHint(R.string.description);
                                        // 備考変更
                                        new AlertDialog.Builder(ManCourseActivity.this)
                                                .setTitle(R.string.set_desc)
                                                .setView(txt)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.updateDesc(courses.get(currentIndex).id,txt.getText().toString());
                                                            Toast.makeText(ManCourseActivity.this,R.string.changed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManCourseActivity.this)
                                                                    .setTitle(R.string.runtime_error)
                                                                    .setMessage(ex.getLocalizedMessage())
                                                                    .setPositiveButton("OK",null).show();
                                                        }
                                                    }
                                                }).show();
                                        break;
                                    }
                                    case 4:
                                    {
                                        new AlertDialog.Builder(ManCourseActivity.this)
                                                .setTitle(R.string.remove)
                                                .setMessage(R.string.confirm_remove)
                                                .setPositiveButton(R.string.no,null)
                                                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            SchDBManager dbm = SchDBManager.getInstance();
                                                            dbm.removeCourse(courses.get(currentIndex).id);
                                                            Toast.makeText(ManCourseActivity.this, R.string.removed_toast,Toast.LENGTH_SHORT).show();
                                                            refreshItems();
                                                        } catch (Exception ex) {
                                                            new AlertDialog.Builder(ManCourseActivity.this)
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
        ListView lv = findViewById(R.id.man_course_list);
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
                new AlertDialog.Builder(ManCourseActivity.this)
                        .setTitle(R.string.remove_all)
                        .setMessage(R.string.remove_all_prompt)
                        .setPositiveButton(R.string.no,null)
                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    SchDBManager dbm = SchDBManager.getInstance();
                                    dbm.removeAllCourses();
                                    Toast.makeText(ManCourseActivity.this,R.string.removed_toast,Toast.LENGTH_SHORT).show();
                                    ManCourseActivity.this.finish();
                                } catch (Exception ex) {
                                    new AlertDialog.Builder(ManCourseActivity.this)
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
            courses = dbm.getCourses();
            items.clear();
            for (CourseItem ci : courses) {
                items.add(new GaraItem(String.format(getString(R.string.course_format),ci.name,ci.teacher)));
            }
            ListView lv = findViewById(R.id.man_course_list);
            lv.invalidateViews();
        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setMessage(ex.getLocalizedMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ManCourseActivity.this.finish();
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
