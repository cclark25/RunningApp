package com.example.runningapp.shared;

import java.util.Vector;

public class ActivitySet {
    public Activity activity;
    public ActivitySet(Activity a){
        this.activity = a;
    }
    public Vector<SegmentSet> segments = new Vector<>();

    public static class SegmentSet {
        public SegmentSet(Segment s){
            this.segment = s;
        }
        public Segment segment;
        public Vector<ActivityRecord> activityRecords = new Vector<>();
    }
}
