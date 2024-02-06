package com.abk.distance;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class HeartRateTracker {

    private SensorManager sensorManager;
    private Sensor hearRateDetector;
    public float heartRate;

    public HeartRateTracker(Context context){

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        hearRateDetector = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

    }

    public void trackHeartRate(){
        SensorEventListener heartRateSensor = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){

                    heartRate = event.values[0];

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(heartRateSensor, hearRateDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
