package net.taviscaron.bsuirschedule.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
    /**
     * Calculate BSUIR work week from date
     * 
     * @param date
     *            date of the day to calculating its work week
     * @return work week (1 - 4)
     */
    public static int workWeekFromDate(Date date) {
        GregorianCalendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(date);
        
        int year = endCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH);
        if (month < Calendar.SEPTEMBER) {
            year -= 1;
        }
        
        GregorianCalendar startCalendar = new GregorianCalendar(year, Calendar.SEPTEMBER, 1);
        startCalendar.get(Calendar.WEEK_OF_YEAR); // side effect: forced
                                                  // populating WEEK_OF_YEAR
                                                  // field
        startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date startDate = startCalendar.getTime();
        
        int days = (int) ((date.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000));
        int week = (int) ((days / 7) % 4 + 1);
        
        return week;
    }
    
    /**
     * Format passed date as dd.mm.yyyy
     * 
     * @param date
     *            data to format
     * @return formatted string
     */
    public static String simpleDateFormat(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String format = df.format(date);
        return format;
    }
    
    public static int weekDayOfDate(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (day < 0) {
            day = Calendar.SATURDAY + day;
        }
        return day;
    }
    
    public static Date today() {
        return new Date();
    }
    
    public static Date tomorrow() {
        Date today = today();
        Date tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
        return tomorrow;
    }
}
