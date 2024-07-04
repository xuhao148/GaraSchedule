package org.aya.garaschedule;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class JsonSchParser {
    enum ErrorType {
        NO_ERROR,
        WARNING,
        NOT_INITIALZED,
        INPUT_STREAM_BAD,
        IO_EXCEPTION,
        JSON_EXCEPTION
    };
    private ArrayList<CourseObjectSQLite> courses = null;
    private ArrayList<SchedsObjectSQLite> scheds = null;
    private ErrorType err = ErrorType.NOT_INITIALZED;
    private Exception ex;
    private String term_name;
    private String year_name;

    public ArrayList<CourseObjectSQLite> getCourses() {
        return courses;
    }

    public ArrayList<SchedsObjectSQLite> getScheds() {
        return scheds;
    }

    public ErrorType getErr() {
        return err;
    }

    public Exception getEx() {
        return ex;
    }

    public String getTerm_name() {
        return term_name;
    }

    public String getYear_name() {
        return year_name;
    }

    private StringBuilder err_out = new StringBuilder();

    public String getErrorMessage() {
        return err_out.toString();
    }
    public JsonSchParser(InputStream istJson) {
        try {
            if (istJson == null) {
                err = ErrorType.INPUT_STREAM_BAD;
                return;
            }
            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            StringBuilder out = new StringBuilder();
            InputStreamReader in = new InputStreamReader(istJson, StandardCharsets.UTF_8);
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
            String json = out.toString();
            JSONObject jso = new JSONObject(json);
            int current_cid = 1;
            int current_sid = 1;
            HashMap<String,CourseObjectSQLite> courses = new HashMap<>();
            ArrayList<SchedsObjectSQLite> scheds = new ArrayList<>();
            term_name = jso.getString("xqm");
            year_name = jso.getString("xnm");
            JSONArray kbList = jso.getJSONArray("kbList");
            int l = kbList.length();
            int parse_failure_counter = 0;
            for (int i=0; i<l; i++) {
                try {
                    JSONObject encounter = kbList.getJSONObject(i);
                    String item = encounter.getString("kcb");
                    String termspan = encounter.getString("xxq");
                    int start = encounter.getInt("djj");
                    int len = encounter.getInt("skcd");
                    int end = start + len - 1;
                    int weekocc = encounter.getInt("dsz");
                    int weekday = encounter.getInt("xqj");
                    if (weekday == 7) weekday = 0;
                    String course_name = "???";
                    String course_teacher = "";
                    String course_loc = "";
                    String course_occ = "";
                    String course_exam = "";
                    String[] elements = item.split("(<br>|zwf)");
                    int elem_len = elements.length;
                    if (elem_len >= 1) {
                        course_name = elements[0];
                        if (elem_len >= 2) {
                            course_occ = elements[1];
                            if (elem_len >= 3) {
                                course_teacher = elements[2];
                                if (elem_len >= 4) {
                                    course_loc = elements[3];
                                    if (elem_len >= 5) {
                                        course_exam = elements[4];
                                    }
                                }
                            }
                        }
                    } else {
                        err_out.append("A no-name course was found.\n");
                    }

                    // Find if it exists
                    CourseObjectSQLite cos;
                    if (courses.containsKey(course_name)) {
                        cos = courses.get(course_name);
                    } else {
                        cos = new CourseObjectSQLite();
                        cos.id = current_cid; current_cid++;
                        cos.description = String.format("%s\n%s\n%s",course_occ,course_exam,termspan);
                        cos.name = course_name;
                        cos.teacher = course_teacher;
                        courses.put(course_name,cos);
                    }
                    SchedsObjectSQLite sos = new SchedsObjectSQLite();
                    sos.cid = cos.id;
                    sos.course_from = start;
                    sos.course_to = end;
                    sos.loc = course_loc;
                    sos.id = current_sid++;
                    sos.week = weekday * 4 + weekocc + 1;
                    scheds.add(sos);
                } catch (JSONException jsex) {
                    parse_failure_counter++;
                }

                this.courses = new ArrayList<>();
                this.courses.addAll(courses.values());
                this.scheds = scheds;

            }
            if (parse_failure_counter > 0) {
                err_out.append(String.format("%d items failed to parse.\n",parse_failure_counter));
            }
            if (err_out.toString().isEmpty()) {
                err = ErrorType.NO_ERROR;
            } else {
                err = ErrorType.WARNING;
            }
        } catch (IOException ex) {
            err = ErrorType.IO_EXCEPTION;
            this.ex = ex;
        } catch (JSONException ex) {
            err = ErrorType.JSON_EXCEPTION;
            this.ex = ex;
        }

    }
}

class CourseObjectSQLite {
    public int id;
    public String name;
    public String teacher;
    public String description;
    public boolean execInsertIntoDb(SQLiteDatabase sqldb) {
        try {
            Object[] args = {id,name,teacher,description};
            sqldb.execSQL("INSERT INTO courses VALUES (?,?,?,?)",args);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

class SchedsObjectSQLite {
    public int id;
    public int cid;
    public int course_from;
    public int course_to;
    public int week;
    public String loc;
    public void execInsertIntoDb(SQLiteDatabase sqldb) throws SQLException {
            Object[] args = {id,cid,course_from,course_to,week,loc};
            sqldb.execSQL("INSERT INTO scheds VALUES (?,?,?,?,?,?)",args);
    }
}
