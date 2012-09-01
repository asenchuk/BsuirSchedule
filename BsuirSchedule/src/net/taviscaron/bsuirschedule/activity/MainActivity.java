package net.taviscaron.bsuirschedule.activity;

import net.taviscaron.bsuirschedule.core.Constants;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String currentGroup = sp.getString(Constants.CURRENT_GROUP_PREF_KEY, null);
        
        Intent intent = null;
        if (currentGroup != null) {
            intent = new Intent(this, LessonsListActivity.class);
            intent.putExtra(LessonsListActivity.GROUP_NAME_EXTRA, currentGroup);
        } else {
            intent = new Intent(this, ManageSchedulesActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
