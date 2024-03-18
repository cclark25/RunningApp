package com.example.runningapp;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.example.runningapp.shared.Activity;
import com.example.runningapp.shared.ActivityDao;
import com.example.runningapp.shared.AppDatabase;

class ActivityRow {
    Context context;
    public TableRow row;
    public ActivityRow(Context context, Activity act){
        this.context = context;
        this.row = new TableRow(context);
        TextView txt = new TextView(context);
        txt.setText("Activity: " + act.id);
        this.row.addView(txt);
    }
}

public class ActivityTableManager {
    TableLayout table;
    Context context;
    Vector<ActivityRow> rows = new Vector<ActivityRow>();

    public ActivityTableManager(Context context, TableLayout table){
        this.table = table;
        this.context = context;

        ActivityTableManager self = this;

        new Timer().schedule(new TimerTask(){
            /**
             * The action to be performed by this timer task.
             */
            @Override
            public void run() {
                ActivityDao dao = AppDatabase.getInstance(context).getActivityDao();
                List<Activity> activities = dao.getAll();

                System.out.println("Activities pulled: " + activities.size());

                for(Activity act : activities) {
                    ActivityRow r = new ActivityRow(context, act);
                    self.rows.add(r);
                    table.addView(r.row);

                    System.out.println("Activity loaded: " + act.id);
                }
            }
        }, 0);


    }
}
