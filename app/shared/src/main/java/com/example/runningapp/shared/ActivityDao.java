package com.example.runningapp.shared;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ActivityDao {
    @Insert
    public long[] insert(Activity... acts);

    @Delete
    public void delete(Activity... acts);

    @Update
    public void update(Activity... acts);

    @Query("SELECT * FROM Activity WHERE Activity.id IN (:ids)")
    public List<Activity> get(int... ids);

    @Query("SELECT * FROM Activity")
    public List<Activity> getAll();
}
