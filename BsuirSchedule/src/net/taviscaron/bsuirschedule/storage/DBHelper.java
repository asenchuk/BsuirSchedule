package net.taviscaron.bsuirschedule.storage;

import net.taviscaron.bsuirschedule.model.Lesson;
import net.taviscaron.bsuirschedule.model.Schedule;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "schedules";
    
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "create the database version " + DB_VERSION);
        
        String schedTableCreate = String.format(
                "create table %s (%s integer primary key, %s text, %s integer, %s integer, %s integer, %s text, %s text, %s integer);", Schedule.TABLE_NAME,
                BaseColumns._ID, Schedule.FACULTY_ATTR, Schedule.YEAR_ATTR, Schedule.COURSE_ATTR, Schedule.TERM_ATTR, Schedule.STREAM_ATTR,
                Schedule.GROUP_ATTR, Schedule.UPDATE_DATE_ATTR);
        db.execSQL(schedTableCreate);
        
        String lessonTableCreate = String.format(
                "create table %s (%s integer primary key, %s integer, %s integer, %s integer, %s integer, %s integer, %s text, %s text, %s text, %s integer);",
                Lesson.TABLE_NAME, BaseColumns._ID, Lesson.SCHEDULE_ATTR, Lesson.DAY_ATTR, Lesson.TIME_ATTR, Lesson.WEEKS_ATTR, Lesson.SUBGROUP_ATTR,
                Lesson.AUDITORIUM_ATTR, Lesson.TEACHER_ATTR, Lesson.SUBJ_ATTR, Lesson.TYPE_ATTR);
        db.execSQL(lessonTableCreate);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "upgrade the database storage from " + oldVersion + " to " + newVersion);
        db.execSQL(String.format("drop table %s;", Schedule.TABLE_NAME));
        db.execSQL(String.format("drop table %s;", Lesson.TABLE_NAME));
    }
}
