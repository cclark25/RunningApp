package com.example.runningapp.shared;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SegmentDao {
    @Insert
    public long[] insert(Segment... acts);

    @Delete
    public void delete(Segment... acts);

    @Update
    public void update(Segment... acts);

    @Query("SELECT * FROM Segment WHERE Segment.id IN (:ids)")
    public List<Activity> get(int... ids);


    @Query("SELECT * FROM Segment WHERE Segment.activityID = :actId")
    public List<Segment> getForActivity(long actId);
}
