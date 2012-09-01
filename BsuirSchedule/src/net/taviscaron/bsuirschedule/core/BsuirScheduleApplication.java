package net.taviscaron.bsuirschedule.core;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dFN0OUtrZENHMkxyNW0tbnlzbG1hT0E6MQ")
public class BsuirScheduleApplication extends Application {
    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }
}
