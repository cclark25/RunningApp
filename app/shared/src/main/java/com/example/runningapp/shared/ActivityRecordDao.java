package com.example.runningapp.shared;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ActivityRecordDao {

    @Insert
    public long[] insert(ActivityRecord... acts);

    @Delete
    public void delete(ActivityRecord... acts);

    @Update
    public void update(ActivityRecord... acts);

    @Query("SELECT * FROM ActivityRecord WHERE ActivityRecord.id IN (:ids)")
    public List<ActivityRecord> get(long... ids);


    @Query("SELECT * FROM ActivityRecord WHERE ActivityRecord.segmentID in (:segIds)")
    public List<ActivityRecord> getForSegment(long... segIds);

    @Query("SELECT * FROM ActivityRecord")
    public List<ActivityRecord> getAll();
}
