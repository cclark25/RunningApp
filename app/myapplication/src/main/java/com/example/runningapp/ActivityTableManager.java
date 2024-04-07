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
import com.example.runningapp.shared.ActivityRecord;
import com.example.runningapp.shared.ActivitySet;
import com.example.runningapp.shared.AppDatabase;
import com.example.runningapp.shared.GPSDataSummarizer;
import com.example.runningapp.shared.GPXInOut;
import com.example.runningapp.shared.OnIngest;
import com.example.runningapp.shared.Segment;

class ActivityRow {
    Context context;
    public TableRow row;
    public List<Segment> segments;
    public Vector<ActivityRecord> dataRecords = new Vector<>();

    public ActivityRow(Context context, Activity act){
        this.context = context;
//        GPSDataSummarizer.summarizeDistance();
        this.segments = act.getSegments(AppDatabase.getInstance(context));
        for(Segment s : this.segments){
            List<ActivityRecord> newRecords = s.getRecords(AppDatabase.getInstance(context));
            System.out.println("Records in segment: " + newRecords.size());
            this.dataRecords.addAll(newRecords);
        }
        this.dataRecords.sort((ActivityRecord a, ActivityRecord b)->{
            if(a.timestamp == null || b.timestamp == null){
                return -1;
            }
            return Long.valueOf(a.timestamp.getTime() - b.timestamp.getTime()).intValue();
        });
        this.row = new TableRow(context);

        double distance = GPSDataSummarizer.summarizeDistance(this.dataRecords);

        TextView txt = new TextView(context);
        StringBuilder activityText = new StringBuilder("Activity: " + act.id);
        for(int i = 0; i < act.id; i++){
            activityText.append(" - ");
        }
        txt.setText(activityText.toString());
        TextView txt2 = new TextView(context);
        txt2.setText((distance / 1609.344) + " mi");
//        Button btn = new Button(context);
//        btn.setText("Test button");

        this.row.addView(txt);
        this.row.addView(txt2);
//        this.row.addView(btn);
    }
}

public class ActivityTableManager {
    TableLayout table;
    Context context;
    Vector<ActivityRow> rows = new Vector<ActivityRow>();
    public int count = 0;

    public ActivityTableManager(AndroidCompanion context, TableLayout table){
        this.table = table;
        this.context = context;

        ActivityTableManager self = this;
        if(false){
            new GPXInOut().ingest(context, new OnIngest() {
                @Override
                public void run(Vector<ActivitySet> activities) {
                    for (ActivitySet act : activities) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                AppDatabase db = AppDatabase.getInstance(context);
                                act.activity.commit(db);
//                            System.out.println("aaa act: " + act.activity.id);
                                for (ActivitySet.SegmentSet s : act.segments) {
                                    act.activity.createSegment(db, s.segment);
                                    s.segment.commit(db);
//                                System.out.println("aaa seg: " + s.segment.id + ":" + s.segment.activityID);
                                    for (ActivityRecord r : s.activityRecords) {
                                        s.segment.createRecord(db, r);
//                                    r.commit(AppDatabase.getInstance(context));
//                                    System.out.println("aaa record: " + r.id + ":" + r.segmentID);
                                    }
                                    ActivityRecord.commitAll(db, s.activityRecords);
                                }
                                ActivityRow newRow = new ActivityRow(context, act.activity);


                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        self.table.addView(newRow.row);

                                        count++;
                                    }
                                });
                            }
                        }, 0);


                    }
                }
            });
        }

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
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            table.addView(r.row);
                        }
                    });

                    System.out.println("Activity loaded: " + act.id);
                }
            }
        }, 0);


    }
}
