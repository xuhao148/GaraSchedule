package org.aya.garaschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewSchedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sched);

        setTitle(getString(R.string.new_course));
        getActionBar().setDisplayShowHomeEnabled(false);

        EditText etCname = findViewById(R.id.coursename);
        EditText etTname = findViewById(R.id.teachername);
        EditText etDesc  = findViewById(R.id.course_desc);
        Button bOK = findViewById(R.id.course_add_btn);
        Button bCancel = findViewById(R.id.course_cancel_button);

        bOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etCname.getText().toString().isEmpty()) {
                    new AlertDialog.Builder(NewSchedActivity.this)
                            .setTitle(R.string.warning)
                            .setMessage(R.string.please_enter_course_name)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK",null)
                            .show();
                    return;
                }
                String cName = etCname.getText().toString();
                String tName = etTname.getText().toString();
                String desc = etDesc.getText().toString();
                try {
                    SchDBManager dbm = SchDBManager.getInstance();
                    dbm.addCourse(cName,tName,desc);
                    Toast.makeText(NewSchedActivity.this,R.string.inserted,Toast.LENGTH_SHORT).show();
                    NewSchedActivity.this.finish();
                } catch (Exception sqlex) {
                    new AlertDialog.Builder(NewSchedActivity.this)
                            .setTitle(R.string.sql_error)
                            .setMessage(sqlex.getLocalizedMessage())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NewSchedActivity.this.finish();
                                }
                            })
                            .show();
                }
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewSchedActivity.this.finish();
            }
        });

    }
}