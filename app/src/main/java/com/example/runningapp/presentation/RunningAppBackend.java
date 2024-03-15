/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.runningapp.presentation;

import static android.content.Context.LOCALE_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;


import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import android.hardware.SensorEventListener;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import kotlin.UInt;

class DataSample {
    public Date timestamp = new Date();
    public float heartRate = 0;

    public Location location = null;
}

class Segment {
    public int lapNumber = 1;
    public Date startTime = null;
    public Date stopTime = null;

    public Segment previousSegment = null;

    public Vector<DataSample> samples = new Vector<DataSample>();

    private Duration finalizedTotalElapsedTime = null;
    private Duration finalizedLapElapsedTime = null;

    public Segment pressStartButton() {
        if (this.startTime == null) {
            this.startTime = new Date();
            return this;
        } else if (this.stopTime == null) {
            this.stopTime = new Date();
            Segment newSegment = new Segment();
            newSegment.previousSegment = this;
            newSegment.lapNumber = this.lapNumber;
            return newSegment;
        } else {
            throw new Error("Start and Stop times are both already set!");
        }
    }

    public Segment pressLapButton() {
        Date lapPressMoment = new Date();

        if (this.startTime == null) {
            return this;
        } else if (this.stopTime == null) {
            this.stopTime = lapPressMoment;
            Segment newSegment = new Segment();
            newSegment.previousSegment = this;
            newSegment.startTime = lapPressMoment;
            newSegment.lapNumber = this.lapNumber + 1;
            return newSegment;
        } else {
            throw new Error("Cannot lap an finalized segment!");
        }
    }

    public boolean isRunning() {
        return this.startTime != null && this.stopTime == null;
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

        if (this.startTime != null && this.stopTime != null) {
            this.finalizedTotalElapsedTime = result;
        }

        return result;
    }

    public Duration getLapDuration() {
        if (finalizedLapElapsedTime != null) {
            return finalizedLapElapsedTime;
        }

        Duration result = null;

        if (this.previousSegment == null || (this.previousSegment.lapNumber != this.lapNumber)) {
            result = this.getSegmentTime();
        } else {
            result = this.getSegmentTime().plus(this.previousSegment.getLapDuration());
        }

        if (this.startTime != null && this.stopTime != null) {
            this.finalizedLapElapsedTime = result;
        }

        return result;
    }

    public DataSample takeSample() {
        if (!this.isRunning()) {
            return null;
        }
        DataSample result = new DataSample();
        this.samples.add(result);
        return result;
    }

    private Duration getSegmentTime() {
        if (this.startTime == null) {
            return Duration.ZERO;
        }
        if (this.stopTime == null) {
            return Duration.ofMillis(new Date().getTime() - startTime.getTime());
        }
        return Duration.ofMillis(this.stopTime.getTime() - startTime.getTime());
    }


}

interface Callback {
    void run();
}

public class RunningAppBackend implements SensorEventListener {
    private Timer processTask = new Timer();


    private SensorManager mSensorManager;
    private Sensor mHeartSensor;
    private LocationManager locManager;
    private Context context;
    private long gpsRefreshMs = 100;

    float lastHeartRate = 0;
    Location lastLocation = null;

    public Segment currentSegment = new Segment();

    public Callback onRefresh = ()->{};

    public RunningAppBackend(Context context) {
        RunningAppBackend self = this;
        this.context = context;


        this.processTask.schedule(new TimerTask() {
            public void run() {
//                System.out.println("RunningAppBackend Timer ticked");
                DataSample s = self.currentSegment.takeSample();

                if (s != null) {
                    s.heartRate = self.lastHeartRate;
                    s.location = self.lastLocation;
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

        this.startMeasure();
    }

    public Duration getTotalDuration(){
        return this.currentSegment.getTotalDuration();
    }

    public Duration getLapDuration(){
        return this.currentSegment.getLapDuration();
    }

    public int getCurrentLapNumber(){
        return this.currentSegment.lapNumber;
    }

    public boolean startButton(){
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
        int l = this.currentSegment.lapNumber;
        this.currentSegment = this.currentSegment.pressLapButton();
        boolean result = l != this.currentSegment.lapNumber;

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
            if(lapDistance && curseg.lapNumber != this.currentSegment.lapNumber){
                break;
            }
            totalDistance += GPSDataSummarizer.summarizeDistance((Vector<DataSample>) curseg.samples.clone());
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

