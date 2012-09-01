package net.taviscaron.bsuirschedule.model;

import net.taviscaron.bsuirschedule.core.BitUtil;

public class Lesson {
    public static final String TABLE_NAME = "Lesson";
    // attrs
    public static final String DAY_ATTR = "day";
    public static final String SCHEDULE_ATTR = "schedule";
    public static final String TIME_ATTR = "time";
    public static final String WEEKS_ATTR = "weeks";
    public static final String SUBGROUP_ATTR = "subgroup";
    public static final String AUDITORIUM_ATTR = "auditorium";
    public static final String TEACHER_ATTR = "teacher";
    public static final String TYPE_ATTR = "type";
    public static final String SUBJ_ATTR = "subject";
    public static final int ALL_SUBGROUPS = BitUtil.encode(new int[] { 1, 2 });
    public static final int ALL_WEEKS = BitUtil.encode(new int[] { 1, 2, 3, 4 });
    
    public enum Type {
        UNKNOWN, PRACTICE, LECTURE, LAB,
    }
}
