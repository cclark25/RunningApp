package com.example.runningapp.presentation;

import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Stream;



public class GPSDataSummarizer {
    public static double earthRadiusM = 6_371_000;
    public static double summarizeDistance(Vector<DataSample> samples){
        double totalDirectDistance = 0;

        Object[] validLocations = samples.stream().filter((DataSample s)->{
            return s.location != null;
        }).toArray();

        for(int i = 0; i < validLocations.length - 2; i++){
            DataSample a = (DataSample) validLocations[i];
            DataSample b = (DataSample) validLocations[i + 1];

            double dAltMeters = b.location.getAltitude() - a.location.getAltitude();

            double dLat = Math.toRadians(b.location.getLatitude()-a.location.getLatitude());
            double dLon = Math.toRadians(b.location.getLongitude()-a.location.getLongitude());

            double lat1 = Math.toRadians(a.location.getLatitude());
            double lat2 = Math.toRadians(b.location.getLatitude());

            double ab = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(ab), Math.sqrt(1-ab));

            double sampleDist = Math.sqrt(Math.pow(c * GPSDataSummarizer.earthRadiusM, 2)
                    + Math.pow(dAltMeters, 2));
//            System.out.println("Sample distance: " + sampleDist);

            totalDirectDistance += sampleDist;
        }

//        System.out.println("Distance calculated: " + totalDirectDistance + ", sample count: " + samples.size());
        return totalDirectDistance;
    }
}
