package com.example.runningapp.shared;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(foreignKeys = {
        @ForeignKey(
                entity = Activity.class,
                parentColumns = "id",
                childColumns = "activityID"
        )
}, indices = {@Index("activityID"), @Index("startTime"), @Index("stopTime")})
public class Segment {
    @PrimaryKey(autoGenerate = true)
    public Long id = null;
    public Long activityID = null;

    public Long lapNumber = null;

    public Date startTime;
    public Date stopTime;

    @Ignore
    public void commit(AppDatabase db){
        if(this.id == null){
            long[] ids = db.getSegmentDao().insert(this);
            this.id = ids[0];
        }
        else {
            db.getSegmentDao().update(this);
        }
    }
}
