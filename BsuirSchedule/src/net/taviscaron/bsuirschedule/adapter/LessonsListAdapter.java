package net.taviscaron.bsuirschedule.adapter;

import java.util.LinkedList;
import java.util.List;

import net.taviscaron.bsuirschedule.R;
import net.taviscaron.bsuirschedule.core.BitUtil;
import net.taviscaron.bsuirschedule.model.Lesson;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class LessonsListAdapter extends CursorAdapter {
    public LessonsListAdapter(Context context, Cursor c) {
        super(context, c);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String teacher = cursor.getString(cursor.getColumnIndex(Lesson.TEACHER_ATTR));
        TextView teacherView = (TextView) view.findViewById(R.id.lesson_teacher_label);
        teacherView.setText(teacher);
        
        String title = cursor.getString(cursor.getColumnIndex(Lesson.SUBJ_ATTR));
        TextView titleView = (TextView) view.findViewById(R.id.lesson_title_label);
        titleView.setText(title);
        
        int timeIndex = cursor.getInt(cursor.getColumnIndex(Lesson.TIME_ATTR));
        String time = context.getResources().getStringArray(R.array.lessons_time)[timeIndex];
        TextView timeView = (TextView) view.findViewById(R.id.lesson_time_label);
        timeView.setText(time);
        
        List<String> params = new LinkedList<String>();
        
        String auditorium = cursor.getString(cursor.getColumnIndex(Lesson.AUDITORIUM_ATTR));
        if (!TextUtils.isEmpty(auditorium)) {
            params.add(String.format("%s %s", auditorium, context.getString(R.string.lesson_param_auditorium)));
        }
        
        int subgroupsBits = cursor.getInt(cursor.getColumnIndex(Lesson.SUBGROUP_ATTR));
        if (subgroupsBits != Lesson.ALL_SUBGROUPS) {
            String subgroups = TextUtils.join(",", BitUtil.decode(subgroupsBits));
            params.add(String.format("%s %s", subgroups, context.getString(R.string.lesson_param_subgroups)));
        }
        
        int weeksBits = cursor.getInt(cursor.getColumnIndex(Lesson.WEEKS_ATTR));
        if (weeksBits != Lesson.ALL_WEEKS) {
            String weeks = TextUtils.join(",", BitUtil.decode(weeksBits));
            params.add(String.format("%s %s", weeks, context.getString(R.string.lesson_param_weeks)));
        }
        
        String stringParams = TextUtils.join(" / ", params);
        TextView paramView = (TextView) view.findViewById(R.id.lesson_param_label);
        paramView.setText(stringParams);
        
        int lessonType = cursor.getInt(cursor.getColumnIndex(Lesson.TYPE_ATTR));
        View backgroundView = view.findViewById(R.id.lesson_item_background);
        String backgroundColor = context.getResources().getStringArray(R.array.lesson_type_colors)[lessonType];
        backgroundView.setBackgroundColor(Color.parseColor(backgroundColor));
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.lesson_list_item, null);
    }
}
