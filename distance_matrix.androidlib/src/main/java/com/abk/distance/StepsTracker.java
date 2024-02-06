package com.abk.distance;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.mygdx.runai.states.DebugNode;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class StepsTracker {

    private SensorManager sensorManager;
    private Sensor stepDetector;
    public int stepCount;
    private float savedDistance;

    public StepsTracker(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCount = 0;

    }

    public void startCounting(){
        SensorEventListener stepListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                stepCount++;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d("StepsTracker", "Sensor Accuracy " + accuracy );
            }


        };

        sensorManager.registerListener(stepListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public int getStepCount(){
        int count = stepCount;
        stepCount = 0;
        return count;
    }


    public float getDistance(float distance){
        //distance it ran while the function was not running
        float distanceRan = distance - savedDistance;
        savedDistance = distance;
        return distanceRan;
    }





}
