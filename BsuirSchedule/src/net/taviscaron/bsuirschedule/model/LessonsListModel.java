package net.taviscaron.bsuirschedule.model;

import android.database.Cursor;

public class LessonsListModel {
    public String title;
    public Cursor cursor;
    
    public LessonsListModel() {
    }
    
    public LessonsListModel(String title, Cursor cursor) {
        this.title = title;
        this.cursor = cursor;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Cursor getCursor() {
        return cursor;
    }
    
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
