package com.abk.distance.services;

import android.util.Log;

import java.io.Serializable;

public class StepDataCalculator implements Serializable {
    public int steps;
    public int cadence;
    public float strideLength;
    public long timeTook;
    public int totalTime;

    public StepDataCalculator(int steps, long timeTook, float distance, int totalTime){

        this.steps = steps;
        this.timeTook = timeTook;
        this.cadence = calculateCadence(steps, timeTook);
        this.strideLength = calculateStrideLength(distance, steps);
        this.totalTime = totalTime;


    }

    public int calculateCadence(int stepCount, long currentTime) {
        float steps = stepCount;
        float currentTimeMinutes = (currentTime/1000L) / 60.0f;

        int cadence = (int) (steps / currentTimeMinutes);

        return cadence;
    }


    public float calculateStrideLength(float distance, int stepCount) {
        if (stepCount == 0) {
            return 0;
        }

        float strideLength = distance / stepCount;
        return strideLength;
    }
}
