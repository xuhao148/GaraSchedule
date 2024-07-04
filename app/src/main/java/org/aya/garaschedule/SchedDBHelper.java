package org.aya.garaschedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SchedDBHelper extends SQLiteOpenHelper {
    public SchedDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE IF NOT EXISTS scheds (id INT PRIMARY KEY AUTOINCREMENT, name VARCHAR(128), course_from INT, course_to INT, week INT, desc VARCHAR(128));";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // We will NEVER upgrade this app.
    }
}
