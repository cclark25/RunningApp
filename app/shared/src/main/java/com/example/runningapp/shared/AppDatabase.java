package com.example.runningapp.shared;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import java.util.Date;

class Converters {
    @TypeConverter
    public Long dateToTimestamp(Date d){
        if(d == null){
            return null;
        }
        return d.getTime();
    }

    @TypeConverter
    public Date timestampToDate(Long d){
        if(d == null){
            return null;
        }
        return new Date(d);
    }

}

@Database(entities = {Activity.class, Segment.class, ActivityRecord.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    @Override
    public void clearAllTables() {

    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(@NonNull DatabaseConfiguration databaseConfiguration) {
        return null;
    }

    public abstract ActivityDao getActivityDao();
    public abstract SegmentDao getSegmentDao();
    public abstract ActivityRecordDao getActivityRecordDao();

    private static AppDatabase singleton = null;
    public static AppDatabase getInstance(Context context) {
        return AppDatabase.singleton != null ?
                AppDatabase.singleton
                : AppDatabase.buildDatabase(context);
    }
    private static AppDatabase buildDatabase(Context context){
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "AppDatabase")
                .addCallback(
                        new RoomDatabase.Callback() {
                            @Override
                            public void onCreate(SupportSQLiteDatabase db){
                                super.onCreate(db);
                            }
                        }
                ).build();
    }
}
