package org.aya.garaschedule;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;

public class SchedManMenuActivity extends Activity {

    private ArrayList<GaraItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched_man_menu);

        setTitle(R.string.sched_man);
        getActionBar().setDisplayShowHomeEnabled(false);

        // Initialize main menu
        items = new ArrayList<>();
        items.add(new GaraItem(getString(R.string.smma_new_course)));
        items.add(new GaraItem(getString(R.string.smma_man_course)));
        items.add(new GaraItem(getString(R.string.smma_new_sched)));
        items.add(new GaraItem(getString(R.string.smma_man_sched)));
        items.add(new GaraItem(getString(R.string.smma_import_db)));
        items.add(new GaraItem(getString(R.string.smma_export_db)));
        items.add(new GaraItem(getString(R.string.extract_from_json)));
        items.add(new GaraItem(getString(R.string.back)));
        GaraCommandAdapter smMenuAdapter = new GaraCommandAdapter(this,items);
        ListView lv = (ListView) findViewById(R.id.smmenu);
        lv.setAdapter(smMenuAdapter);
        lv.setSelection(0);
        lv.requestFocus();
        lv.setOnItemClickListener(new SchedManMenuActivity.SMMenuDealer());

        updateDisplay();
    }

    class SMMenuDealer implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                case 0: {
                    Intent it = new Intent(SchedManMenuActivity.this, NewSchedActivity.class);
                    startActivity(it);
                    break;
                }
                case 1: {
                    if (!items.get(1).isEnabled()) break;
                    Intent it = new Intent(SchedManMenuActivity.this, ManCourseActivity.class);
                    startActivity(it);
                    break;
                }
                case 2: {
                    if (!items.get(2).isEnabled()) break;
                    Intent it = new Intent(SchedManMenuActivity.this, NewPeriodActivity.class);
                    startActivity(it);
                    break;
                }
                case 3: {
                    if (!items.get(3).isEnabled()) break;
                    Intent it = new Intent(SchedManMenuActivity.this, ManPeriodActivity.class);
                    startActivity(it);
                    break;
                }
                case 4: {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/vnd.sqlite3");
                    intent.putExtra(Intent.EXTRA_TITLE,"data.db");
                    startActivityForResult(intent,1145);
                    break;
                }
                case 5: {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent,11451);
                    break;
                }
                case 6:{
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent,114514);
                    break;
                }
                case 7:
                    SchedManMenuActivity.this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SchDBManager dbman = SchDBManager.getInstance();
        if (requestCode == 1145 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            File from = new File("/data/data/org.aya.garaschedule/data.db");
            if (from.canRead()) {
                try {
                    dbman.closeDb();
                    OutputStream ostream = getContentResolver().openOutputStream(uri);
                    InputStream istream = new FileInputStream(from.getPath());
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = istream.read(buf)) != -1) {
                        ostream.write(buf, 0, length);
                    }
                    istream.close();
                    ostream.flush();
                    ostream.close();
                    Toast.makeText(this, R.string.exported, Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.fs_error)
                            .setMessage(ex.getLocalizedMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }
                dbman.reopenDb();
                // Files.copy(from.toPath(), new File(uri.getPath()).toPath());
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.cannot_read_db)
                        .setPositiveButton("OK", null)
                        .show();
            }
        } else if (requestCode == 11451 && resultCode == RESULT_OK) {
            // Stub
            new AlertDialog.Builder(this)
                    .setTitle(R.string.strong_warning)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.warning_overwrite_db)
                    .setPositiveButton(R.string.no,null)
                    .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                dbman.closeDb();
                                File db = new File("/data/data/org.aya.garaschedule/data.db");
                                File tmp_db = new File("/data/data/org.aya.garaschedule/tmp.db");
                                if (tmp_db.exists()) {
                                    tmp_db.delete();
                                    tmp_db.createNewFile();
                                }
                                OutputStream ostream = new FileOutputStream(tmp_db.getPath());
                                InputStream istream = getContentResolver().openInputStream(data.getData());
                                byte[] buf = new byte[8192];
                                int length;
                                while ((length = istream.read(buf)) != -1) {
                                    ostream.write(buf, 0, length);
                                }
                                istream.close();
                                ostream.flush();
                                ostream.close();
                                if (db.exists()) {
                                    db.delete();
                                }
                                tmp_db.renameTo(db);
                                Toast.makeText(SchedManMenuActivity.this, R.string.imported, Toast.LENGTH_SHORT).show();
                                dbman.reopenDb();
                                updateDisplay();
                            } catch (Exception ex) {
                                new AlertDialog.Builder(SchedManMenuActivity.this)
                                        .setTitle(R.string.fs_error)
                                        .setMessage(ex.getMessage())
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        }
                    }).show();
        } else if (requestCode == 114514 && resultCode == RESULT_OK) {
            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                JsonSchParser jsp = new JsonSchParser(is);
                if (jsp.getErr() == JsonSchParser.ErrorType.NO_ERROR || jsp.getErr() == JsonSchParser.ErrorType.WARNING) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.json_input_title)
                            .setMessage(String.format(getString(R.string.json_import_prompt),
                                    jsp.getYear_name(),jsp.getTerm_name(),jsp.getCourses().size(),jsp.getScheds().size()))
                            .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dbman.removeAllPeriods();
                                    dbman.removeAllCourses();
                                    String sqlerrs = "";
                                    try {
                                        for (CourseObjectSQLite o : jsp.getCourses()) {
                                            o.execInsertIntoDb(dbman.db);
                                        }
                                        for (SchedsObjectSQLite o : jsp.getScheds()) {
                                            o.execInsertIntoDb(dbman.db);
                                        }
                                    } catch (SQLException sqlex) {
                                        sqlerrs = sqlerrs + sqlex.getLocalizedMessage() + "\n";
                                    }
                                    if (!sqlerrs.isEmpty()) {
                                        new AlertDialog.Builder(SchedManMenuActivity.this)
                                                .setTitle(R.string.error_title)
                                                .setMessage(sqlerrs)
                                                .setPositiveButton("OK",null)
                                                .show();
                                    }
                                    if (jsp.getErr() == JsonSchParser.ErrorType.WARNING) {
                                        new AlertDialog.Builder(SchedManMenuActivity.this)
                                                .setTitle(R.string.warning)
                                                .setMessage(String.format(getString(R.string.json_warning),jsp.getErrorMessage()))
                                                .setPositiveButton("OK",null)
                                                .show();
                                    }
                                    Toast.makeText(SchedManMenuActivity.this, R.string.inserted, Toast.LENGTH_SHORT).show();
                                    updateDisplay();
                                }
                            })
                            .setPositiveButton(R.string.no,null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.error_title)
                            .setMessage(R.string.json_io_error)
                            .setPositiveButton("OK",null)
                            .show();
                }
            } catch (Exception ex) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.runtime_error)
                        .setMessage(ex.toString() + "\n" + ex.getMessage())
                        .setPositiveButton("OK",null)
                        .show();
            }
        }
    }

    private void updateDisplay() {
        try {
            SchDBManager dbm = SchDBManager.getInstance();
            if (dbm.getCourseCount() == 0) {
                items.get(1).setEnabled(false);
                items.get(2).setEnabled(false);
                items.get(3).setEnabled(false);
            } else {
                items.get(1).setEnabled(true);
                items.get(2).setEnabled(true);
                items.get(3).setEnabled(true);
            }
            if (dbm.getScheduleCount() == 0) {
                items.get(3).setEnabled(false);
            } else {
                items.get(3).setEnabled(true);
            }
            String info = String.format(getString(R.string.course_sched_count_format),dbm.getCourseCount(),dbm.getScheduleCount());
            TextView tv = (TextView) findViewById(R.id.sm_itemcounter);
            tv.setText(info);
            ListView lv = findViewById(R.id.smmenu);
            lv.invalidateViews();
        } catch (Exception ex) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.runtime_error)
                    .setMessage(ex.getLocalizedMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SchedManMenuActivity.this.finish();
                        }
                    });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }
}