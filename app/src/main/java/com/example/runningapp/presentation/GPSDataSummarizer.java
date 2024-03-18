package com.example.runningapp.presentation;

import com.example.runningapp.shared.ActivityRecord;

import java.util.Vector;


public class GPSDataSummarizer {
    public static double earthRadiusM = 6_371_000;
    public static double summarizeDistance(Vector<ActivityRecord> samples){
        double totalDirectDistance = 0;

        Object[] validLocations = samples.stream().filter((ActivityRecord s)->{
            return s.latitude != null && s.longitude != null && s.altitude != null;
        }).toArray();

        for(int i = 0; i < validLocations.length - 2; i++){
            ActivityRecord a = (ActivityRecord) validLocations[i];
            ActivityRecord b = (ActivityRecord) validLocations[i + 1];

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
            System.out.println("Sample distance: " + sampleDist);

            totalDirectDistance += sampleDist;
        }

        System.out.println("Distance calculated: " + totalDirectDistance + ", sample count: " + validLocations.length);
        return totalDirectDistance;
    }
}
