package net.taviscaron.bsuirschedule.activity;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

import net.taviscaron.bsuirschedule.R;
import net.taviscaron.bsuirschedule.adapter.SchedulesListAdapter;
import net.taviscaron.bsuirschedule.core.Constants;
import net.taviscaron.bsuirschedule.loader.ScheduleLoader;
import net.taviscaron.bsuirschedule.model.Schedule;
import net.taviscaron.bsuirschedule.storage.DBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ManageSchedulesActivity extends Activity {
    private static final String TAG = "ChooseScheduleActivity";
    private static final int INPUT_GROUP_NAME_DIALOG = 1;
    private static final int LOADING_PROGRESS_DIALOG = 2;
    
    private ListView schedulesList;
    private SchedulesListAdapter adapter;
    private SQLiteDatabase db;
    private ScheduleLoader loader;
    
    /** loader handler */
    private Handler loaderHandler = new LoaderHandler(this);
    private static class LoaderHandler extends Handler {
        private WeakReference<ManageSchedulesActivity> weakActivity;
        
        public LoaderHandler(ManageSchedulesActivity activity) {
            weakActivity = new WeakReference<ManageSchedulesActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg) {
            ManageSchedulesActivity activity = weakActivity.get();
            switch (msg.what) {
                case ScheduleLoader.SCHEDULE_LOADED_SUCCESSFULLY:
                    activity.scheduleSuccessfullyLoaded();
                    break;
                case ScheduleLoader.SCHEDULE_NOT_FOUND:
                    activity.scheduleNotFound();
                    break;
                case ScheduleLoader.SCHEDULE_NOT_UPDATED:
                    activity.scheduleNotUpdated();
                    break;
                case ScheduleLoader.SCHEDULE_LOAD_FAILED:
                    activity.scheduleLoadFailed();
                    break;
                default:
                    Log.w(TAG, "unknown message was received");
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.choose_schedule_screen);
        
        adapter = new SchedulesListAdapter(this, null);
        
        schedulesList = (ListView) findViewById(R.id.schedules_list);
        schedulesList.setAdapter(adapter);
        schedulesList.setEmptyView(findViewById(R.id.schedules_empty_list));
        schedulesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String selectedGroup = adapter.getGroupNameAtPos(pos);
                Log.i(TAG, "select group " + selectedGroup);
                
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ManageSchedulesActivity.this);
                sp.edit().putString(Constants.CURRENT_GROUP_PREF_KEY, selectedGroup).commit();
                
                Intent intent = new Intent(ManageSchedulesActivity.this, LessonsListActivity.class);
                intent.putExtra(LessonsListActivity.GROUP_NAME_EXTRA, selectedGroup);
                startActivity(intent);
                finish();
            }
        });
        
        registerForContextMenu(schedulesList);
        
        NonConfigurationWrapper config = (NonConfigurationWrapper) getLastNonConfigurationInstance();
        if (config != null) {
            loader = config.loader;
        } else {
            loader = new ScheduleLoader(this);
        }
        
        loader.setHander(loaderHandler);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        refreshList();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        if (view == schedulesList) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            String group = adapter.getGroupNameAtPos(info.position);
            menu.setHeaderTitle(getString(R.string.schedule_context_menu_title, group));
            inflater.inflate(R.menu.schedules_list_item_context_menu, menu);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.choose_schedule_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_schedule_menu_item:
                addNewSchedule();
                break;
            case R.id.settings_menu_item:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_schedule_menu:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                String group = adapter.getGroupNameAtPos(info.position);
                Log.i(TAG, "update sched for group " + group);
                loadScheduleForGroup(group);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        NonConfigurationWrapper config = new NonConfigurationWrapper();
        config.loader = loader;
        return config;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case INPUT_GROUP_NAME_DIALOG:
                View view = getLayoutInflater().inflate(R.layout.add_group_alert_text_edit, null);
                final EditText field = (EditText) view.findViewById(R.id.add_group_alert_text_edit);
                
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            String group = field.getText().toString();
                            if (Pattern.matches("\\d{6}", group)) {
                                loadScheduleForGroup(group);
                            } else {
                                Toast.makeText(ManageSchedulesActivity.this, R.string.group_name_is_invalid, Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(field.getWindowToken(), 0);
                    }
                };
                
                dialog = new AlertDialog.Builder(this).setTitle(R.string.add_schedule_dialog_title).setMessage(R.string.add_schedule_dialog_message)
                        .setView(view).setCancelable(true).setPositiveButton(R.string.add_schedule_dialog_go_button, listener)
                        .setNegativeButton(R.string.add_schedule_dialog_cancel_button, listener).create();
                break;
            case LOADING_PROGRESS_DIALOG:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getString(R.string.schedule_loading_dialog_message));
                dialog = progressDialog;
                break;
            default:
                break;
        }
        return dialog;
    }
    
    private void scheduleSuccessfullyLoaded() {
        dismissDialog(LOADING_PROGRESS_DIALOG);
        Toast.makeText(this, R.string.schedule_loaded_successfully, Toast.LENGTH_LONG).show();
        refreshList();
    }
    
    private void scheduleNotUpdated() {
        dismissDialog(LOADING_PROGRESS_DIALOG);
        Toast.makeText(this, R.string.schedule_not_updated, Toast.LENGTH_LONG).show();
    }
    
    private void scheduleNotFound() {
        dismissDialog(LOADING_PROGRESS_DIALOG);
        Toast.makeText(this, R.string.schedule_not_found, Toast.LENGTH_LONG).show();
    }
    
    private void scheduleLoadFailed() {
        dismissDialog(LOADING_PROGRESS_DIALOG);
        Toast.makeText(this, R.string.schedule_load_failed, Toast.LENGTH_LONG).show();
    }
    
    private void addNewSchedule() {
        showDialog(INPUT_GROUP_NAME_DIALOG);
    }
    
    private void loadScheduleForGroup(String group) {
        showDialog(LOADING_PROGRESS_DIALOG);
        loader.loadScheduleForGroup(group);
    }
    
    private void refreshList() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                Cursor cursor = null;
                try {
                    if (db != null && db.isOpen()) {
                        cursor = db.query(Schedule.TABLE_NAME, null, null, null, null, null, Schedule.UPDATE_DATE_ATTR);
                    }
                } catch (Exception e) {
                    // ignore all
                }
                return cursor;
            }
            
            @Override
            protected void onPostExecute(Cursor result) {
                adapter.changeCursor(result);
            }
        }.execute();
    }
    
    private class NonConfigurationWrapper {
        public ScheduleLoader loader;
    }
}
