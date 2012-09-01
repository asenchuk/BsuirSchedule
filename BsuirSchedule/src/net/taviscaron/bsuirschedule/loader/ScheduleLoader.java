package net.taviscaron.bsuirschedule.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.taviscaron.bsuirschedule.core.BitUtil;
import net.taviscaron.bsuirschedule.model.Lesson;
import net.taviscaron.bsuirschedule.model.Schedule;
import net.taviscaron.bsuirschedule.storage.DBHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class ScheduleLoader {
    public static final String TAG = "ScheduleLoader";
    
    public static final int SCHEDULE_LOADED_SUCCESSFULLY = 1;
    public static final int SCHEDULE_NOT_FOUND = 2;
    public static final int SCHEDULE_NOT_UPDATED = 3;
    public static final int SCHEDULE_LOAD_FAILED = 4;
    
    private static final String SCHEDULE_URL = "http://www.bsuir.by/psched/rest/%s";
    
    private static final String[] TERMS_LITERALS = new String[] {
            "\u043E\u0441\u0435\u043D\u043D\u0438\u0439",
            "\u0432\u0435\u0441\u0435\u043D\u043D\u0438\u0439",
    };
    
    private static final String[] DAYS_LITERALS = new String[] {
            "\u043F\u043D",
            "\u0432\u0442",
            "\u0441\u0440",
            "\u0447\u0442",
            "\u043F\u0442",
            "\u0441\u0431",
            "\u0432\u0441",
    };
    
    private static final String[] LESSON_TYPES_LITERALS = new String[] {
            "unknown",
            "\u043F\u0437",
            "\u043B\u043A",
            "\u043B\u0440",
    };
    
    private static final String[] TIME_PERIOD_LITERALS = new String[] {
            "8:00-9:35",
            "9:45-11:20",
            "11:40-13:15",
            "13:25-15:00",
            "15:20-16:55",
            "17:05-18:40",
            "18:45-20:20",
            "20:25-22:00",
    };
    
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
    
    private Handler handler;
    private Context context;
    private Thread thread;
    
    public ScheduleLoader(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public Handler getHander() {
        return handler;
    }
    
    public void setHander(Handler hander) {
        this.handler = hander;
    }
    
    public boolean isWorking() {
        return (thread != null);
    }
    
    private void loadScheduleForGroupInternal(String group) {
        String url = String.format(SCHEDULE_URL, group);
        Document document = loadXmlDocumentFromUrl(url);
        
        if (document != null) {
            ScheduleValuesWrapper scheduleValues = parseScheduleDocument(document, group);
            if (scheduleValues != null) {
                if (insertScheduleIntoDatabase(scheduleValues)) {
                    sendLoadedSuccessfullyNotification();
                } else {
                    sendNotUpdatedNotification();
                }
            } else {
                sendScheduleLoadFailedNotification();
            }
        } else {
            sendNotFoundNotification();
        }
        
        thread = null;
    }
    
    private Document loadXmlDocumentFromUrl(String stringUrl) {
        Document document = null;
        InputStream is = null;
        try {
            URL url = new URL(stringUrl);
            is = url.openStream();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = db.parse(is);
        } catch (SAXException e) {
            Log.i(TAG, "retrieved xml is broken", e);
        } catch (MalformedURLException e) {
            Log.i(TAG, "bad url", e);
        } catch (IOException e) {
            Log.i(TAG, "exception occurred during the reading data from bsuir's service", e);
        } catch (Exception e) {
            Log.e(TAG, "xml parsing exception", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return document;
    }
    
    private ScheduleValuesWrapper parseScheduleDocument(Document document, String group) {
        ScheduleValuesWrapper scheduleValues = null;
        
        Element root = document.getDocumentElement();
        if ("collection".equals(root.getTagName())) {
            ContentValues schedule = new ContentValues();
            List<ContentValues> lessons = new LinkedList<ContentValues>();
            
            NodeList rows = root.getElementsByTagName("ROW");
            for (int i = 0; i < rows.getLength(); i++) {
                Element row = (Element) rows.item(i);
                
                // schedule attributes
                if (i == 0) {
                    String faculty = row.getAttribute("faculty");
                    schedule.put(Schedule.FACULTY_ATTR, faculty);
                    
                    int year = parseIntegerValue(row.getAttribute("year"), -1);
                    schedule.put(Schedule.YEAR_ATTR, year);
                    
                    int course = parseIntegerValue(row.getAttribute("course"), -1);
                    schedule.put(Schedule.COURSE_ATTR, course);
                    
                    String stringTerm = row.getAttribute("term");
                    int term = findInArray(TERMS_LITERALS, stringTerm, 0);
                    schedule.put(Schedule.TERM_ATTR, term);
                    
                    String stream = row.getAttribute("stream");
                    schedule.put(Schedule.STREAM_ATTR, stream);
                    
                    schedule.put(Schedule.GROUP_ATTR, group);
                    
                    String stringDate = row.getAttribute("date");
                    Date date = null;
                    try {
                        if (!TextUtils.isEmpty(stringDate)) {
                            date = dateFormatter.parse(stringDate);
                        } else {
                            date = new Date();
                        }
                    } catch (Exception e) {
                        date = new Date();
                    }
                    schedule.put(Schedule.UPDATE_DATE_ATTR, date.getTime());
                }
                
                ContentValues lesson = new ContentValues();
                
                // lesson attrs
                String stringSubgroup = row.getAttribute("subgroup");
                int[] subgroupsArray = commaSeparatedIntegerList(stringSubgroup, new int[] {1, 2});
                int subgroups = BitUtil.encode(subgroupsArray);
                lesson.put(Lesson.SUBGROUP_ATTR, subgroups);
                
                String stringWeekDay = row.getAttribute("weekDay");
                int weekDay = findInArray(DAYS_LITERALS, stringWeekDay, 0);
                lesson.put(Lesson.DAY_ATTR, weekDay);
                
                String stringTimePeriod = row.getAttribute("timePeriod");
                int timePeriod = findInArray(TIME_PERIOD_LITERALS, stringTimePeriod, 0);
                lesson.put(Lesson.TIME_ATTR, timePeriod);
                
                String stringWeekList = row.getAttribute("weekList");
                int[] weeksArray = commaSeparatedIntegerList(stringWeekList, new int[] {1, 2, 3, 4});
                int weeks = BitUtil.encode(weeksArray);
                lesson.put(Lesson.WEEKS_ATTR, weeks);
                
                String stringSubjectType = row.getAttribute("subjectType");
                int subjectTypeIndex = findInArray(LESSON_TYPES_LITERALS, stringSubjectType, 0);
                lesson.put(Lesson.TYPE_ATTR, subjectTypeIndex);
                
                lesson.put(Lesson.SUBJ_ATTR, row.getAttribute("subject"));
                lesson.put(Lesson.AUDITORIUM_ATTR, row.getAttribute("auditorium"));
                lesson.put(Lesson.TEACHER_ATTR, row.getAttribute("teacher"));
                
                lessons.add(lesson);
            }
            
            scheduleValues = new ScheduleValuesWrapper();
            scheduleValues.schedule = schedule;
            scheduleValues.lessons = lessons;
        }
        
        return scheduleValues;
    }
    
    private int parseIntegerValue(String value, int defaultValue) {
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return result;
    }
    
    private int findInArray(Object[] array, Object object, int defaultIndex) {
        int index = defaultIndex;
        for (int i = 0; i < array.length; i++) {
            if (object.equals(array[i])) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    private int[] commaSeparatedIntegerList(String source, int[] defaultList) {
        int[] result = defaultList;
        String[] parts = source.split(",");
        try {
            int[] parsed = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                parsed[i] = Integer.parseInt(parts[i]);
            }
            result = parsed;
        } catch (NumberFormatException e) {
        }
        return result;
    }
    
    private boolean insertScheduleIntoDatabase(ScheduleValuesWrapper scheduleValues) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        
        try {
            database.beginTransaction();
            
            String group = scheduleValues.schedule.getAsString(Schedule.GROUP_ATTR);
            Date updateDate = new Date(scheduleValues.schedule.getAsLong(Schedule.UPDATE_DATE_ATTR));
            
            Cursor existingGroupCursor = database.query(Schedule.TABLE_NAME, null, Schedule.GROUP_ATTR + " = ?", new String[] {
                group
            }, null, null, null);
            if (existingGroupCursor.moveToNext()) {
                // storage already contains group
                Date date = new Date(existingGroupCursor.getLong(existingGroupCursor.getColumnIndex(Schedule.UPDATE_DATE_ATTR)));
                if (date.compareTo(updateDate) >= 0) {
                    throw new Exception("not updated"); // stored schedule is
                                                        // not expired
                }
                
                // remove existing schedule
                String oldScheduleId = existingGroupCursor.getString(existingGroupCursor.getColumnIndex(BaseColumns._ID));
                database.delete(Schedule.TABLE_NAME, BaseColumns._ID + " = ?", new String[] {
                    oldScheduleId
                });
                database.delete(Lesson.TABLE_NAME, Lesson.SCHEDULE_ATTR + " = ?", new String[] {
                    oldScheduleId
                });
            }
            
            long scheduleId = database.insert(Schedule.TABLE_NAME, null, scheduleValues.schedule);
            if (scheduleId == -1) {
                throw new Exception("can't add new schedule");
            }
            
            for (ContentValues lesson : scheduleValues.lessons) {
                lesson.put(Lesson.SCHEDULE_ATTR, scheduleId);
                if (database.insert(Lesson.TABLE_NAME, null, lesson) == -1) {
                    throw new Exception("can't add lesson");
                }
            }
            
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            database.endTransaction();
            database.close();
        }
    }
    
    public void loadScheduleForGroup(final String group) {
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loadScheduleForGroupInternal(group);
                }
            });
            thread.start();
        }
    }
    
    private void sendLoadedSuccessfullyNotification() {
        if (handler != null) {
            Message.obtain(handler, SCHEDULE_LOADED_SUCCESSFULLY).sendToTarget();
        }
    }
    
    private void sendNotFoundNotification() {
        if (handler != null) {
            Message.obtain(handler, SCHEDULE_NOT_FOUND).sendToTarget();
        }
    }
    
    private void sendNotUpdatedNotification() {
        if (handler != null) {
            Message.obtain(handler, SCHEDULE_NOT_UPDATED).sendToTarget();
        }
    }
    
    private void sendScheduleLoadFailedNotification() {
        if (handler != null) {
            Message.obtain(handler, SCHEDULE_NOT_UPDATED).sendToTarget();
        }
    }
    
    private class ScheduleValuesWrapper {
        ContentValues schedule;
        List<ContentValues> lessons;
    }
}
