package net.taviscaron.bsuirschedule.test.core;

import java.util.Date;
import java.util.GregorianCalendar;

import net.taviscaron.bsuirschedule.core.DateUtil;
import junit.framework.TestCase;

public class DateUtilTests extends TestCase {
    public void testWorkWeekFromDate() {
        Date sept1 = new GregorianCalendar(2012, 8, 1).getTime();
        assertEquals(1, DateUtil.workWeekFromDate(sept1));
        
        Date sept2 = new GregorianCalendar(2012, 8, 2).getTime();
        assertEquals(1, DateUtil.workWeekFromDate(sept2));
        
        Date sept3 = new GregorianCalendar(2012, 8, 3).getTime();
        assertEquals(2, DateUtil.workWeekFromDate(sept3));
        
        Date sept9 = new GregorianCalendar(2012, 8, 9).getTime();
        assertEquals(2, DateUtil.workWeekFromDate(sept9));
        
        Date sept10 = new GregorianCalendar(2012, 8, 10).getTime();
        assertEquals(3, DateUtil.workWeekFromDate(sept10));
        
        Date sept16 = new GregorianCalendar(2012, 8, 16).getTime();
        assertEquals(3, DateUtil.workWeekFromDate(sept16));
        
        Date sept17 = new GregorianCalendar(2012, 8, 17).getTime();
        assertEquals(4, DateUtil.workWeekFromDate(sept17));
        
        Date sept23 = new GregorianCalendar(2012, 8, 23).getTime();
        assertEquals(4, DateUtil.workWeekFromDate(sept23));
        
        Date sept24 = new GregorianCalendar(2012, 8, 24).getTime();
        assertEquals(1, DateUtil.workWeekFromDate(sept24));
        
        Date sept30 = new GregorianCalendar(2012, 8, 30).getTime();
        assertEquals(1, DateUtil.workWeekFromDate(sept30));
        
        Date aug23 = new GregorianCalendar(2012, 7, 23).getTime();
        assertEquals(4, DateUtil.workWeekFromDate(aug23));
        
        Date aug26 = new GregorianCalendar(2012, 7, 26).getTime();
        assertEquals(4, DateUtil.workWeekFromDate(aug26));
        
        Date aug27 = new GregorianCalendar(2012, 7, 27).getTime();
        assertEquals(1, DateUtil.workWeekFromDate(aug27));
    }
}
