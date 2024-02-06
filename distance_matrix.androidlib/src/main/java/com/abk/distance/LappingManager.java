package com.abk.distance;

import android.util.Log;
import android.util.Pair;

import androidx.core.math.MathUtils;

import java.io.Serializable;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class  LappingManager implements Serializable
{
    private float previousTimingPerKM;
    private double distanceToNextLap;
    public List<Float> lapTimings = new ArrayList<>();
    public List<Float> lapDistance= new ArrayList<>();
    public float lapInterval;
    public float distanceToTravel;

    public LappingManager(float lapInterval,float distanceToTravel){
        this.lapInterval = lapInterval * 1000;
        this.distanceToTravel = distanceToTravel * 1000;
        distanceToNextLap = lapInterval * 1000;
    }
    public void lapChecker(double distanceTravelled, int timeTaken) {
        if (distanceTravelled >= distanceToNextLap && SumOfDistanceValues() != distanceToTravel) {
            float timeTakenThisKM = timeTaken;
            float timeToLog = timeTakenThisKM - previousTimingPerKM;
            float rounded = RoundToTwoDecimalPlaces(lapInterval/1000.0f,2);
            if(rounded != 0.00){
                lapTimings.add((float) Math.floor(timeToLog));
                lapDistance.add(rounded);
            }
            previousTimingPerKM = timeTakenThisKM;

            if ((distanceToTravel - distanceTravelled) != 0) {
                float newLapInterval  = (distanceToTravel - distanceTravelled) >= lapInterval ? lapInterval : (float) Math.floor(distanceToTravel - SumOfDistanceValues());
                lapInterval = newLapInterval;
                distanceToNextLap += lapInterval;
            }
        }
    }
    public void EndLapChecker(double distanceTravelled){
        lapInterval = (float) Math.floor(distanceTravelled - SumOfDistanceValues());
        if(lapInterval != 0){
            distanceToNextLap = distanceTravelled;
        }
    }

    private float SumOfDistanceValues(){
       float sum = 0f;
        for (int i = 0; i < lapDistance.size();i ++){
           sum += lapDistance.get(i);
        }
        return sum*1000;
    }

    public float RoundToTwoDecimalPlaces(double value,int digits) {
        float mult = (float) Math.pow(10.0, digits);
        return (float) (Math.floor(value * mult) / mult);
    }


}
