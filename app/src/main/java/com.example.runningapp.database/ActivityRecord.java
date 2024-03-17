package com.example.runningapp.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(
                entity = Segment.class,
                parentColumns = "id",
                childColumns = "segmentID"
        )
}, indices = {@Index("segmentID"),@Index("timestamp")})
public class ActivityRecord {
    @PrimaryKey(autoGenerate = true)
    public Long id = null;

    public Long segmentID = null;

    public Date timestamp;

    public Double latitude = null;
    public Double longitude = null;
    public Double altitude = null;

    public Double heartRate = null;
}
