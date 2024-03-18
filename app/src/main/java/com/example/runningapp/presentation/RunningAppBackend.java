/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.runningapp.presentation;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;


import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.time.Duration;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.os.Vibrator;


import com.example.runningapp.shared.Activity;
import com.example.runningapp.shared.ActivityRecord;
import com.example.runningapp.shared.AppDatabase;

//class DataSample {
//    public Date timestamp = new Date();
//    public float heartRate = 0;
//
//    public Location location = null;
//}

class Segment {
//    public int lapNumber = 1;
//    public Date startTime = null;
//    public Date stopTime = null;

    public com.example.runningapp.shared.Segment dbData;

    public Segment previousSegment = null;

    public Vector<ActivityRecord> samples = new Vector<ActivityRecord>();

    private Duration finalizedTotalElapsedTime = null;
    private Duration finalizedLapElapsedTime = null;
    private Context context;
    private Activity activity;

    private static Timer dbSaver = new Timer();
    private static Vector<Segment> segmentsToSave = new Vector<Segment>();

    private static boolean _staticInit = false;


    public Segment(Context context, Activity act){
        this.context = context;
        this.activity = act;
        com.example.runningapp.shared.Segment s = new com.example.runningapp.shared.Segment();
        this.dbData = act.createSegment(AppDatabase.getInstance(context), s);
        this.dbData.lapNumber = (long)(1);

        Segment self = this;
        Segment.segmentsToSave.add(this);

        if(!Segment._staticInit){
            Segment.dbSaver.schedule(new TimerTask() {
                long skips = 0;
                @Override
                public void run() {
                    try {

                        for (Segment s : Segment.segmentsToSave) {
                            s.activity.commit(AppDatabase.getInstance(context));
                            s.dbData.commit(AppDatabase.getInstance(context));
                        }
                        skips = 0;
                    }
                    catch (java.util.ConcurrentModificationException e) {
                        skips++;
                        System.out.println(("Segment commit skipped " + skips) + " times");
                    }
                }
            }, 10, 100);
            Segment._staticInit = true;
        }
    }

    public Segment pressStartButton() {
        if (this.dbData.startTime == null) {
            this.dbData.startTime = new Date();
            return this;
        } else if (this.dbData.stopTime == null) {
            this.dbData.stopTime = new Date();
            Segment newSegment = new Segment(this.context,this.activity);
            newSegment.previousSegment = this;
            newSegment.dbData.lapNumber = this.dbData.lapNumber;
            return newSegment;
        } else {
            throw new Error("Start and Stop times are both already set!");
        }
    }

    public Segment pressLapButton() {
        Date lapPressMoment = new Date();

        if (this.dbData.startTime == null) {
            return this;
        } else if (this.dbData.stopTime == null) {
            this.dbData.stopTime = lapPressMoment;
            Segment newSegment = new Segment(this.context, this.activity);
            newSegment.previousSegment = this;
            newSegment.dbData.startTime = lapPressMoment;
            newSegment.dbData.lapNumber = this.dbData.lapNumber + 1;
            return newSegment;
        } else {
            throw new Error("Cannot lap an finalized segment!");
        }
    }

    public boolean isRunning() {
        return this.dbData.startTime != null && this.dbData.stopTime == null;
    }

    public Duration getTotalDuration() {
        if (finalizedTotalElapsedTime != null) {
            return finalizedTotalElapsedTime;
        }

        Duration result = null;

        if (this.previousSegment == null) {
            result = this.getSegmentTime();
        } else {
            result = this.getSegmentTime().plus(this.previousSegment.getTotalDuration());
        }

        if (this.dbData.startTime != null && this.dbData.stopTime != null) {
            this.finalizedTotalElapsedTime = result;
        }

        return result;
    }

    public Duration getLapDuration() {
        if (finalizedLapElapsedTime != null) {
            return finalizedLapElapsedTime;
        }

        Duration result = null;

        if(this.previousSegment != null) {
            System.out.println("Lap numbers: " + this.previousSegment.dbData.lapNumber
                    + " => " + this.dbData.lapNumber);
        }
        if (this.previousSegment == null || !(this.previousSegment.dbData.lapNumber.equals(this.dbData.lapNumber))) {
            result = this.getSegmentTime();
        } else {
            result = this.getSegmentTime().plus(this.previousSegment.getLapDuration());
        }

        if (this.dbData.startTime != null && this.dbData.stopTime != null) {
            this.finalizedLapElapsedTime = result;
        }

        return result;
    }

    public ActivityRecord takeSample() {
        if (!this.isRunning()) {
            return null;
        }
        ActivityRecord result = new ActivityRecord();
        result.segmentID = this.dbData.id;
        this.samples.add(result);
        return result;
    }

    private Duration getSegmentTime() {
        if (this.dbData.startTime == null) {
            return Duration.ZERO;
        }
        if (this.dbData.stopTime == null) {
            return Duration.ofMillis(new Date().getTime() - this.dbData.startTime.getTime());
        }
        return Duration.ofMillis(this.dbData.stopTime.getTime() - this.dbData.startTime.getTime());
    }


}

interface Callback {
    void run();
}

public class RunningAppBackend implements SensorEventListener {
    private Timer processTask = new Timer();
    private com.example.runningapp.shared.Activity activity = new com.example.runningapp.shared.Activity();


    private SensorManager mSensorManager;
    private Sensor mHeartSensor;
    private LocationManager locManager;
    private Context context;
    private long gpsRefreshMs = 100;

    float lastHeartRate = 0;
    Location lastLocation = null;

    public Segment currentSegment;

    public Callback onRefresh = ()->{};

    public RunningAppBackend(Context context) {
        RunningAppBackend self = this;
        this.context = context;
        this.activity.createdAt = new Date();

//        (new Timer()).schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("Activity ID before insert: " + self.activity.id);
////                AppDatabase.getInstance(context).getActivityDao()
////                        .insert(self.activity);
//                self.activity.commit(AppDatabase.getInstance(context));
//
//                System.out.println("Activity ID after insert: " + self.activity.id);
//
//                List<Activity> dbActivities = AppDatabase.getInstance(context).getActivityDao()
//                        .getAll();
//
//                System.out.println("Activities in the database: "
//                        + String.join(", ",  (dbActivities.stream().map(
//                            (Activity a) -> Long.toString(a.id)
//                        )).collect(Collectors.toList()))
//
//                );
//
//            }
//        }, 10);


        this.processTask.schedule(new TimerTask() {
            public void run() {
                if(self.currentSegment == null){
                    return;
                }

                ActivityRecord s = self.currentSegment.takeSample();

                if (s != null && self.lastLocation != null) {
                    s.timestamp = new Date();
                    s.heartRate = new Double(self.lastHeartRate);
                    s.latitude = self.lastLocation.getLatitude();
                    s.longitude = self.lastLocation.getLongitude();
                    s.altitude = self.lastLocation.getAltitude();
                    System.out.println("GPS added to sample.");
                }
                else {
                    System.out.println("s: " + s);
                    System.out.println("self.lastLocation: " + self.lastLocation);
                }
                self.onRefresh.run();
            }
        }, 0, this.gpsRefreshMs);

        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        locManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        if(
                self.locManager != null
                        && self.locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        && (context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION")
                        == PackageManager.PERMISSION_GRANTED)
        ) {
            this.locManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    100,
                    1,
                    new GPSListener(self),
                    context.getMainLooper()
            );

        }
        else {
            System.out.println("GPS 1:" + (self.locManager != null));
            System.out.println("GPS 2:" + self.locManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            System.out.println("GPS 3:" + (context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION")
                    == PackageManager.PERMISSION_GRANTED));
        }

        this.startMeasure();
    }

    public Duration getTotalDuration(){
        return this.currentSegment.getTotalDuration();
    }

    public Duration getLapDuration(){
        return this.currentSegment.getLapDuration();
    }

    public long getCurrentLapNumber(){
        return this.currentSegment.dbData.lapNumber != null
                ? this.currentSegment.dbData.lapNumber : 0;
    }

    public boolean startButton(){
        if(this.currentSegment == null){
            this.currentSegment = new Segment(context, this.activity);
        }
        this.currentSegment = this.currentSegment.pressStartButton();
        return this.currentSegment.isRunning();
//        if(this.currentSegment.isRunning()){
//            this.startMeasure();
//        }
//        else {
//            this.stopMeasure();
//        }
    }

    public boolean lapButton(){
        Long l = this.currentSegment.dbData.lapNumber;
        this.currentSegment = this.currentSegment.pressLapButton();
        boolean result = !(
                l != null ? l : Long.valueOf(0)
            ).equals(this.currentSegment.dbData.lapNumber);

        if(result){
            Vibrator vibrator = (Vibrator) this.context.getSystemService(VIBRATOR_SERVICE);
            long[] vibrationPattern = new long[2];
            vibrationPattern[0] = 0;
            vibrationPattern[1] = 500;
            vibrator.vibrate(vibrationPattern, -1);
        }

        return result;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.lastHeartRate = event.values[0];

//        System.out.println("Heart Rate: " + this.lastHeartRate );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean heartRateMeasuring = false;

    private void startMeasure() {
        if(!this.heartRateMeasuring){
            this.heartRateMeasuring = mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

//        System.out.println("Sensor registered: " + (this.heartRateMeasuring ? "yes" : "no"));
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
        this.heartRateMeasuring = false;
//        System.out.println("Sensor stopped: " + (this.heartRateMeasuring ? "yes" : "no"));
    }

    public float getHeartRate(){
        return this.lastHeartRate;
    }

    public double getDistance(boolean lapDistance){
        Segment curseg = this.currentSegment;
        double totalDistance = 0;
        while(curseg != null){
            if(lapDistance && curseg.dbData.lapNumber != this.currentSegment.dbData.lapNumber){
                break;
            }
            totalDistance += GPSDataSummarizer.summarizeDistance((Vector<ActivityRecord>) curseg.samples.clone());
            curseg = curseg.previousSegment;
        }
        return totalDistance;
    }

    private boolean lastCheckActive = false;
    public boolean getGPSStatus(){
        boolean providerEnabled = this.locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean hasPermission = (context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED);

        if (!(this.locManager != null && providerEnabled && hasPermission)){
            this.lastCheckActive = false;
            return false;
        }

        Location last = this.locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(last == null){
            this.lastCheckActive = false;
            return false;
        }
        double elapsedTime = (SystemClock.elapsedRealtimeNanos() - (last.getElapsedRealtimeNanos())) / 1000000.00;
        boolean elapsedTimeRecent = elapsedTime < this.gpsRefreshMs * 10;

        boolean connected = last != null && elapsedTimeRecent;

        if(!this.lastCheckActive && connected && !this.currentSegment.isRunning()){
            Vibrator vibrator = (Vibrator) this.context.getSystemService(VIBRATOR_SERVICE);
            long[] vibrationPattern = new long[6];
            vibrationPattern[0] = 0;
            vibrationPattern[1] = 500;
            vibrationPattern[2] = 10;
            vibrationPattern[3] = 500;
            vibrationPattern[4] = 10;
            vibrationPattern[5] = 500;
            vibrator.vibrate(vibrationPattern, -1);
        }

        this.lastCheckActive = connected;
        return connected;
    }
}

