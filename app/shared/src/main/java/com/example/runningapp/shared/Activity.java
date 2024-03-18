package com.example.runningapp.shared;

import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

@Entity(indices = {@Index("createdAt")})
public class Activity {
    @PrimaryKey(autoGenerate = true)
    public Long id = null;

    public Date createdAt;

    @Ignore
    public void commit(AppDatabase db){
        if(this.id == null) {
            long[] ids = db.getActivityDao().insert(this);
            this.id = ids[0];
        }
        else {
            db.getActivityDao().update(this);
        }
    }

    public List<Segment> getSegments(AppDatabase db){
        return db.getSegmentDao().getForActivity(this.id);
    }

    public Segment createSegment(AppDatabase db, Segment s){
        if(s.activityID == null){
            s.activityID = this.id;
        }
        return s;
    }
}

