package com.example.runningapp.shared;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


public class GPSDataSummarizer {
    public static double earthRadiusM = 6_371_000;
    public static double summarizeDistance(Vector<ActivityRecord> samples){
        double totalDirectDistance = 0;

        AtomicInteger ind = new AtomicInteger();

        Object[] validLocations = samples.stream().filter((ActivityRecord s)->{
            return s.latitude != null && s.longitude != null && s.altitude != null;
        }).sorted((ActivityRecord a, ActivityRecord b)->{
            return Math.toIntExact(a.timestamp.getTime() - b.timestamp.getTime());
        }).toArray();
        // .filter((ActivityRecord)->ind.getAndAdd(10) % 265 == 0)

        for(int i = 0; i <= validLocations.length - 2; ){
            ActivityRecord first = (ActivityRecord) validLocations[i];
            ActivityRecord second = null;
            while(i + 1 <= validLocations.length - 1){
                i++;
                second = (ActivityRecord) validLocations[i];
                if(GPSDataSummarizer.checkThreshold(first, second)){
                    break;
                }
            }
            if(second == null){
                continue;
            }
            totalDirectDistance += GPSDataSummarizer.calculateDistance(first, second);
        }

        System.out.println("Distance calculated: " + totalDirectDistance + ", sample count: " + validLocations.length);
        return totalDirectDistance;
    }

    private static double calculateDistance(ActivityRecord a, ActivityRecord b){

        double dAltMeters = b.altitude - a.altitude;

        double dLat = Math.toRadians(b.latitude-a.latitude);
        double dLon = Math.toRadians(b.longitude-a.longitude);

        double lat1 = Math.toRadians(a.latitude);
        double lat2 = Math.toRadians(b.latitude);

        double ab = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(ab), Math.sqrt(1-ab));

        double sampleDist = Math.sqrt(Math.pow(c * GPSDataSummarizer.earthRadiusM, 2)
                + Math.pow(dAltMeters, 2));
        return  sampleDist;
    }

    private static boolean checkThreshold(ActivityRecord first, ActivityRecord second){
//        return GPSDataSummarizer.calculateDistance(first, second) >= 43.96643;
        return GPSDataSummarizer.calculateDistance(first, second) >= 30;
//        return second.timestamp.getTime() - first.timestamp.getTime() >= 20000;
//        return true;
    }
}
