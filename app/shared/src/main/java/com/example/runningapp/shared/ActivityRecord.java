package com.example.runningapp.shared;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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

    public ActivityRecord(){
        this.timestamp = new Date();
    }

    @Ignore
    public void commit(AppDatabase db){
        if(this.id == null) {
            long[] ids = db.getActivityRecordDao().insert(this);
            this.id = ids[0];
        }
        else {
            db.getActivityRecordDao().update(this);
        }

        System.out.println("Total ActivityRecords: " + db.getActivityRecordDao().getAll().size());
    }

    public String describe(){
        return String.format(
                Locale.ENGLISH,
                "id: %d, segmentID: %d, timestamp: %d, heartRate: %f, latitude: %f, longitude: %f, altitude: %f",
                this.id,
                this.segmentID,
                this.timestamp.getTime(),
                this.heartRate,
                this.latitude,
                this.longitude,
                this.altitude
        );
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof ActivityRecord)){
            return false;
        }

        ActivityRecord ot = (ActivityRecord) other;
        System.out.printf(".equals overridden\n");


        boolean matches = true;
        for( java.lang.reflect.Field f : this.getClass().getFields()){
//            System.out.printf("comparing %s values.\n", f.getName());
            try {
                if(f.getType() == double.class || f.getType() == Double.class ){
                    boolean closeEnough = Math.abs(((Double) f.get(this)) - ((Double) f.get(ot))) <= 0.00001;
                    matches = matches && closeEnough;
//                    System.out.printf(
//                            "original.%s%s~=newRec.%s\n%s%s~=%s\n",
//                            f.getName(), closeEnough ? "":"!" , f.getName(),
//                            f.get(this), closeEnough ? "":"!" , f.get(ot));
                }
                else if(f.getType() == Date.class){
                    boolean timeMatch = ((Date) f.get(this)).getTime() == ((Date) f.get(ot)).getTime();
                    matches = matches && timeMatch;
//                    System.out.printf(
//                            "original.%s%s==newRec.%s\n%s%s==%s\n",
//                            f.getName(), timeMatch ? "":"!" , f.getName(),
//                            f.get(this), timeMatch ? "":"!" , f.get(ot));
                }
                else if(f.get(this) != f.get(ot)){
//                    System.out.printf(
//                            "original.%s!=newRec.%s\n%s!==%s\n",
//                            f.getName(), f.getName(),
//                            f.get(this), f.get(ot));
                    matches = false;
                }
                else {
//                    System.out.printf("original.%s==newRec.%s\n", f.getName(), f.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return matches;
    }

    public static void commitAll(AppDatabase db, Iterable<ActivityRecord> records){
        ArrayList<ActivityRecord> l = new ArrayList<>();

        for(ActivityRecord r : records){
            l.add(r);
        }

        ActivityRecord[] tmpArry = {};
        ActivityRecord[] asArray = l.toArray(tmpArry);
        System.out.printf(
                "Activities to commit: %d, from empty with %d\n",
                asArray.length,
                tmpArry.length);
        long[] newIDs = db.getActivityRecordDao().insert(
                asArray
        );

//        List<ActivityRecord> newRecords = db.getActivityRecordDao().get(newIDs);

//        if(newIDs.length != l.size()){
//            throw new Error("Sizes don't match!");
//        }
//
//        for(int i = 0; i < newIDs.length; i++){
//            ActivityRecord original = l.get(i);
//            ActivityRecord newRec = db.getActivityRecordDao()
//                    .get(newIDs[i]).get(0);
//            original.id = newRec.id;
//            if(!original.equals(newRec)){
//                System.out.printf(
//                        "Mismatch: original=(%s)\nnewRec=(%s)\n",
//                        original.describe(),
//                        newRec.describe()
//                );
//                throw new Error("Committed activities don't match! " + original + newRec);
//            }
//        }
    }
}
