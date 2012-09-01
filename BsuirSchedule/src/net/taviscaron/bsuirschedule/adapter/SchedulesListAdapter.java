package net.taviscaron.bsuirschedule.adapter;

import java.util.Date;

import net.taviscaron.bsuirschedule.R;
import net.taviscaron.bsuirschedule.core.Constants;
import net.taviscaron.bsuirschedule.core.DateUtil;
import net.taviscaron.bsuirschedule.model.Schedule;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class SchedulesListAdapter extends CursorAdapter {
    public SchedulesListAdapter(Context context, Cursor c) {
        super(context, c);
    }
    
    public String getGroupNameAtPos(int pos) {
        Cursor cursor = (Cursor) getItem(pos);
        String group = cursor.getString(cursor.getColumnIndex(Schedule.GROUP_ATTR));
        return group;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String currentGroup = sp.getString(Constants.CURRENT_GROUP_PREF_KEY, null);
        
        String faculty = cursor.getString(cursor.getColumnIndex(Schedule.FACULTY_ATTR));
        String group = cursor.getString(cursor.getColumnIndex(Schedule.GROUP_ATTR));
        long updateDate = cursor.getLong(cursor.getColumnIndex(Schedule.UPDATE_DATE_ATTR));
        Date date = new Date(updateDate);
        
        TextView checkedAsDefault = (TextView) view.findViewById(R.id.item_checked_as_default);
        checkedAsDefault.setVisibility((currentGroup != null && currentGroup.equals(group)) ? View.VISIBLE : View.INVISIBLE);
        
        TextView groupName = (TextView) view.findViewById(R.id.item_group_name);
        groupName.setText(String.format("%s (%s)", group, faculty));
        
        TextView updateDateLabel = (TextView) view.findViewById(R.id.item_update_date_value);
        updateDateLabel.setText(DateUtil.simpleDateFormat(date));
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.schedules_list_item, null);
    }
}
