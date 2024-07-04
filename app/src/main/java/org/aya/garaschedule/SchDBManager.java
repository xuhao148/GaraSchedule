package org.aya.garaschedule;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

import java.util.ArrayList;

public class SchDBManager {
    private static SchDBManager dbm = null;
    public SQLiteDatabase db;
    private final static String create_courses = "CREATE TABLE IF NOT EXISTS courses (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, teacher TEXT, description TEXT);";
    private final static String create_scheds = "CREATE TABLE IF NOT EXISTS scheds (id INTEGER PRIMARY KEY AUTOINCREMENT, cid INT NOT NULL, course_from INT, course_to INT, week INT, loc TEXT, FOREIGN KEY (cid) REFERENCES courses(id) ON DELETE CASCADE ON UPDATE CASCADE)";
    private final static String create_prefs = "CREATE TABLE IF NOT EXISTS prefs (k TEXT PRIMARY KEY NOT NULL, v INTEGER)";
    private final static String db_name = "/data/data/org.aya.garaschedule/data.db";
    private SchDBManager() {
        db = SQLiteDatabase.openOrCreateDatabase(db_name,null);
        db.execSQL(create_courses);
        db.execSQL(create_scheds);
        db.execSQL(create_prefs);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
    }

    public static SchDBManager getInstance() throws SQLException {
        if (dbm == null) {
            dbm = new SchDBManager();
        }
        return dbm;
    }

    public int getScheduleCount() {
        String sql = "SELECT count(*) from scheds;";
        int ret;
        Cursor crsr = db.rawQuery(sql,null);
        if (crsr.moveToFirst()) {
            int n = crsr.getInt(0);
            ret = n;
        } else {
            ret = -1;
        }
        crsr.close();
        return ret;
    }

    public int getCourseCount() {
        String sql = "SELECT count(*) from courses;";
        int ret;
        Cursor crsr = db.rawQuery(sql,null);
        if (crsr.moveToFirst()) {
            int n = crsr.getInt(0);
            ret = n;
        } else {
            ret = -1;
        }
        crsr.close();
        return ret;
    }

    public void addCourse(String cname, String tname, String desc) {
        String sql = "INSERT INTO courses (name,teacher,description) VALUES (?,?,?);";
        String args[] = {cname,tname,desc};
        db.execSQL(sql,args);
    }

    public ArrayList<CourseItem> getCourses() {
        ArrayList<CourseItem> courseItems = new ArrayList<>();
        String sql = "SELECT * FROM courses";
        Cursor crsr = db.rawQuery(sql,null);
        if (crsr.moveToFirst()) {
            do {
                CourseItem ci = new CourseItem();
                ci.id = crsr.getInt(0);
                ci.name = crsr.getString(1);
                ci.teacher = crsr.getString(2);
                ci.desc = crsr.getString(3);
                courseItems.add(ci);
            } while (crsr.moveToNext());
        }
        return courseItems;
    }

    public ArrayList<PeriodItem> getPeriods() {
        ArrayList<PeriodItem> periodItems = new ArrayList<>();
        String sql = "SELECT scheds.id,scheds.course_from,scheds.course_to,scheds.week,scheds.loc,courses.name FROM scheds,courses WHERE scheds.cid=courses.id ORDER BY scheds.course_from ASC";
        Cursor crsr = db.rawQuery(sql,null);
        if (crsr.moveToFirst()) {
            do {
                PeriodItem pi = new PeriodItem();
                pi.id = crsr.getInt(0);
                pi.start = crsr.getInt(1);
                pi.end = crsr.getInt(2);
                int weekflag = crsr.getInt(3);
                pi.weekday = weekflag / 4;
                int wnflag = weekflag % 4;
                pi.week1 = ((wnflag & 1) != 0);
                pi.week2 = ((wnflag & 2) != 0);
                pi.loc = crsr.getString(4);
                pi.course = crsr.getString(5);
                periodItems.add(pi);
            } while (crsr.moveToNext());
        }
        return periodItems;
    }

    public void updateCourseName(int id, String new_name) {
        Object[] args = {new_name, id};
        String sql = "UPDATE courses SET name=? WHERE id=?";
        db.execSQL(sql,args);
    }

    public void updateLoc(int id, String new_loc) {
        Object[] args = {new_loc, id};
        String sql = "UPDATE scheds SET loc=? WHERE id=?";
        db.execSQL(sql,args);
    }

    public void removePeriod(int id) {
        Object[] args = {id};
        String sql = "DELETE FROM scheds WHERE id = ?";
        db.execSQL(sql,args);
    }

    public void removeAllPeriods() {
        String sql = "DELETE FROM scheds;";
        db.execSQL(sql);
    }

    public void updateTeacherName(int id, String new_name) {
        Object[] args = {new_name, id};
        String sql = "UPDATE courses SET teacher=? WHERE id=?";
        db.execSQL(sql,args);
    }

    public void updateDesc(int id, String new_name) {
        Object[] args = {new_name, id};
        String sql = "UPDATE courses SET description=? WHERE id=?";
        db.execSQL(sql,args);
    }

    public void removeCourse(int id) {
        Object[] args = {id};
        String sql = "DELETE FROM courses WHERE id = ?";
        db.execSQL(sql,args);
    }

    public void removeAllCourses() {
        String sql = "DELETE FROM courses";
        db.execSQL(sql);
    }

    public void addSchedule(int cid, int from, int to, int week, String loc) {
        String sql = "INSERT INTO scheds (cid,course_from,course_to,week,loc) VALUES (?,?,?,?,?)";
        Object[] args = {cid,from,to,week,loc};
        db.execSQL(sql,args);
    }

    public int getPref(String key) {
        String[] args = {key};
        String sql = "SELECT v FROM prefs WHERE k=?";
        Cursor crsr = db.rawQuery(sql,args);
        if (crsr.moveToFirst()) {
            int ret =  crsr.getInt(0);
            crsr.close();
            return ret;
        } else {
            crsr.close();
            String sql_insert = "INSERT INTO prefs VALUES (?,0);";
            db.execSQL(sql_insert,args);
            return 0;
        }
    }

    public int getPref(String key, int on_create) {
        String[] args = {key};
        String sql = "SELECT v FROM prefs WHERE k=?";
        Cursor crsr = db.rawQuery(sql,args);
        if (crsr.moveToFirst()) {
            int ret =  crsr.getInt(0);
            crsr.close();
            return ret;
        } else {
            Object[] arg2 = {key,on_create};
            crsr.close();
            String sql_insert = "INSERT INTO prefs VALUES (?,?);";
            db.execSQL(sql_insert,arg2);
            return on_create;
        }
    }

    public void setPref(String key, int value) {
        String[] args = {key};
        Object[] args2 = {value,key};
        Object[] args3 = {key,value};
        String sql = "SELECT v FROM prefs WHERE k=?";
        Cursor crsr = db.rawQuery(sql,args);
        if (crsr.moveToFirst()) {
            crsr.close();
            String sql_update = "UPDATE prefs SET v=? WHERE k=?";
            db.execSQL(sql_update,args2);
        } else {
            crsr.close();
            String sql_insert = "INSERT INTO prefs VALUES (?,?);";
            db.execSQL(sql_insert,args3);
        }
    }

    public void closeDb() {
        db.close();
    }
    public void reopenDb() {
        db = SQLiteDatabase.openOrCreateDatabase(db_name,null);
        db.execSQL(create_courses);
        db.execSQL(create_scheds);
        db.execSQL(create_prefs);
    }

}

class CourseItem {
    public int id;
    public String name;
    public String teacher;
    public String desc;
}

class PeriodItem {
    public int id;
    public String course;
    public String teacher;
    public String loc;
    public int start;
    public int end;
    public int weekday;
    public boolean week1;
    public boolean week2;
}