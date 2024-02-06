package com.abk.distance;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.abk.distance.services.LocationService;
import com.abk.distance.services.MapSimulationPointData;
import com.abk.distance.services.ServiceData;
import com.abk.distance.services.StepDataCalculator;
import com.abk.distance.utils.DataPoint;
import com.abk.distance.utils.UnityCallbacks;
import com.asidik.jfmod.FmodAndroidInit;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import kotlin.Unit;

public class WalkingServiceBridge {
    public static final String TAG = "WalkingServiceBridge";
    Activity activity;
    private PlayerDataReceiver playerDataReceiver;
    private boolean vibratored;
    private double distance;

    private boolean competitionStopped;

    public String RankID;
    private int mode;
    private float distanceToTravel;

    private int paceMinutes;
    private int paceSeconds;

    private static float voiceVolume;
    private static float footstepVolume;
    private static float feedbackVolume;
    public WalkingServiceBridge(Activity activity)
    {
        this.activity = activity;
    }

    Gson gson = new Gson();

    public void PauseForegroundService(boolean _isPaused)
    {
        Intent intent = new Intent("com.abk.distance.PAUSE_STATE_CHANGE");
        intent.putExtra("isPaused",_isPaused);
        activity.sendBroadcast(intent);
    }
    public void UpdatePacerValues(int paceMinutes,int paceSeconds){
        Intent intent = new Intent("com.abk.distance.PACER_STATE_CHANGE");
        intent.putExtra("PaceMinutes",paceMinutes);
        intent.putExtra("PaceSeconds",paceSeconds);
        activity.sendBroadcast(intent);
    }
    public void startDistanceForegroundService(float lapInterval,float distanceToTravel,String rankID) {
        RankID = rankID;
        competitionStopped = false;
        vibratored = false;
        distance = 0;
        this.distanceToTravel = distanceToTravel;
        mode = 0;
        if (checkPermissions()) {
            startAhead(lapInterval,distanceToTravel);
        } else {
            UnityCallbacks.permissionDenied("ACTIVITY_RECOGNITION");
            takePermission(lapInterval,distanceToTravel);
        }
        // Get instance of Vibrator from current Context
    }

    public void IsRunningInBackground(boolean isAppInBackground){
        Intent intent = new Intent("com.abk.distance.BACKGROUND_STATE_CHANGE");
        intent.putExtra("isInBackground",isAppInBackground);
        activity.sendBroadcast(intent);
    }
    public void startPacerForegroundService(float lapInterval, float distanceToTravel,int paceMin,int paceSeconds) {
        RankID = "1";
        competitionStopped = false;
        vibratored = false;
        distance = 0;
        this.distanceToTravel = distanceToTravel;
        mode = 1;

        this.paceMinutes = paceMin;
        this.paceSeconds = paceSeconds;
        Log.e(TAG, "startForegroundService: called process ::: " + Process.myPid());
        if (checkPermissions()) {
            startAhead(lapInterval,distanceToTravel);
        } else {
            UnityCallbacks.permissionDenied("ACTIVITY_RECOGNITION");
            takePermission(lapInterval,distanceToTravel);
        }

        // Get instance of Vibrator from current Context
    }
    public void stopForegroundService() {
        // Adding a 1-second delay
        competitionStopped = true;
        Intent mServiceIntent = new Intent(activity, LocationService.class);
        activity.stopService(mServiceIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                destroyListener();

            }
        }, 5000);
    }

    public void UpdateVolume(float voiceVolume,float footstepsVolume,float feedbackVolume){
        Intent intent = new Intent("com.abk.distance.Volume_State_Change");

        intent.putExtra("voiceVolume", voiceVolume);
        intent.putExtra("footstepVolume", footstepsVolume);
        intent.putExtra("feedbackVolume", feedbackVolume);


        activity.sendBroadcast(intent);
    }
    public void UpdateVolumeFirst(float voiceVolume,float footstepsVolume,float feedbackVolume){

        this.voiceVolume = voiceVolume;
        this.footstepVolume = footstepsVolume;
        this.feedbackVolume = feedbackVolume;

    }
    private void takePermission(float lapInterval, float distanceToTravel) {
        Dexter.withContext(activity)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            startAhead(lapInterval, distanceToTravel);
                        } else {
                            stopForegroundService();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        // Handle error, such as showing an error message
                        Log.e(TAG, "Permission request error: " + error.toString());
                    }
                })
                .check();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        boolean locationPermissionGranted = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();

            if (!locationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!coarseLocationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (!permissionsToRequest.isEmpty()) {
                activity.requestPermissions(
                        permissionsToRequest.toArray(new String[0]),
                        1
                );
            }
        }

        return locationPermissionGranted && coarseLocationPermissionGranted;
    }

    private void startAhead(float lapInterval,float distanceToTravel) {
        Intent mServiceIntent = new Intent(activity, LocationService.class);
        mServiceIntent.putExtra("lapInterval",lapInterval);
        mServiceIntent.putExtra("distanceToTravel",distanceToTravel);
        mServiceIntent.putExtra("RankID",RankID.toString());
        mServiceIntent.putExtra("Mode", mode);
        mServiceIntent.putExtra("PaceMinute", paceMinutes);
        mServiceIntent.putExtra("PaceSecond", paceSeconds);
        mServiceIntent.putExtra("voiceVolume", voiceVolume);
        mServiceIntent.putExtra("footstepVolume", footstepVolume);
        mServiceIntent.putExtra("feedbackVolume", feedbackVolume);

        System.out.println("TESTING VOLUME 2 " + this.voiceVolume + " " + this.footstepVolume + " " + this.feedbackVolume);
        RegisterReciever(distanceToTravel);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(mServiceIntent);
        } else {
            activity.startService(mServiceIntent);
        }

    }
    public void RegisterReciever(float distanceToTravel){
        competitionStopped = false;
        this.distanceToTravel = distanceToTravel;
        playerDataReceiver = new PlayerDataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.data_update");
        filter.addCategory("action.category.distance");
        activity.registerReceiver(playerDataReceiver, filter);
    }

    public void destroyListener() {
        if (playerDataReceiver != null) {
            activity.unregisterReceiver(playerDataReceiver);
            playerDataReceiver = null;
            System.out.println(playerDataReceiver);
            
            File file = new File(activity.getExternalFilesDir(null), "Service Data");
            if(file.exists()){
                file.delete();
            }
        }
    }

    public void NotifyThatRunHasStopped(){
        vibratored = true;
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 400 milliseconds
        v.vibrate(1300);

    }
    public  ServiceData decompressServiceData(String compressedDataString) {
        // Decode the Base64 string to get a byte array
        byte[] compressedData = Base64.decode(compressedDataString, Base64.DEFAULT);

        // Decompress the byte array using GZIP
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream)) {

            // Deserialize to get the ServiceData object
            return (ServiceData) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class PlayerDataReceiver extends BroadcastReceiver {
        private static final String TAG = "MyReceiver";


        @Override
        public void onReceive(Context context, Intent intent) {
//        Log.e(TAG, "onReceive: called process ::: " + Process.myPid());
            //ServiceData serviceData = decompressServiceData(intent.getStringExtra("Service Data"));
            ServiceData serviceData = null;
            File file = new File(activity.getExternalFilesDir(null), "Service Data");
            try
            {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                serviceData =  decompressServiceData((String) objectInputStream.readObject());
                objectInputStream.close();
                fileInputStream.close();
            }
            catch (Exception e){
                return;
            }
            distance = serviceData.getDistanceTravelled();
            int paceMin = serviceData.getPaceMin();
            int paceSecond = serviceData.getPaceSeconds();
            int timeSeconds = serviceData.getTotalTimeRan();
            ArrayList<Float> aiDistanceTravelled = serviceData.getAiDistanceTravelled();
            ArrayList<Integer> aiTime = serviceData.getAiTime();
            int fastestPaceMin = serviceData.getFastestPaceMin();
            int fastestPaceSeconds = serviceData.getFastestPaceSeconds();
            int slowestPaceMins = serviceData.getSlowestPaceMin();
            int slowestPaceSeconds = serviceData.getSlowestPaceSeconds();
            int cadence = serviceData.getCadence();
            int heartRate = serviceData.getHeartrate();

            LappingManager lappingManager = serviceData.getLappingManager();
            if(distance >= distanceToTravel*1000 && vibratored == false){
                NotifyThatRunHasStopped();
            }

            List<DataPoint> convertedData = new ArrayList<>();
            if(competitionStopped){
                if(lappingManager != null){
                    lappingManager.EndLapChecker(distance);
                    lappingManager.lapChecker(distance,timeSeconds);
                }
                ArrayList<DataPoint> gpsData = serviceData.getLocationDataPoints();
                if(gpsData != null){
                    convertedData.addAll(gpsData);
                }
            }

            List<Float> lapDistance = null;
            List<Float> lapTimings = null;
            if(lappingManager != null){
                lapDistance = lappingManager.lapDistance;
                lapTimings = lappingManager.lapTimings;
            }

            //if the competition stopped, send the location datas and mappoint datas
            // else send the simple data such as the player distance and player seconds

            if(competitionStopped)
            {
                ArrayList<MapSimulationPointData> locationDistanceData = serviceData.getLocationDistanceData();
                ArrayList<StepDataCalculator> stepDataCalculators = serviceData.getStepCounted();
                try {
                    String locationDistanceDataJson = gson.toJson(locationDistanceData);
                    String stepDataToJson = gson.toJson(stepDataCalculators);
                    JSONObject playerDataObj = new JSONObject();
                    playerDataObj.put("Distance", distance);
                    playerDataObj.put("CurrentCadence",cadence);
                    playerDataObj.put("HeartRate",heartRate);
                    playerDataObj.put("CurrentPaceMin",paceMin);
                    playerDataObj.put("CurrentPaceSeconds",paceSecond);
                    playerDataObj.put("Steps", stepDataToJson);
                    playerDataObj.put("TotalSeconds",timeSeconds);
                    playerDataObj.put("LocationDataJson",convertedData);
                    playerDataObj.put("LapDistances", lapDistance);
                    playerDataObj.put("LapTimings",lapTimings);
                    playerDataObj.put("MapPointDatas",locationDistanceDataJson);

                    playerDataObj.put("FastestPaceMin",fastestPaceMin);
                    playerDataObj.put("FastestPaceSeconds",fastestPaceSeconds);
                    playerDataObj.put("SlowestPaceMin",slowestPaceMins);
                    playerDataObj.put("SlowestPaceSeconds",slowestPaceSeconds);

                    JSONObject aiDataObj = new JSONObject();
                    aiDataObj.put("AIDistanceData",aiDistanceTravelled);
                    aiDataObj.put("AITimeData",aiTime);
                    UnityCallbacks.onUpdatePlayerData(playerDataObj.toString());
                    UnityCallbacks.onUpdateAIData(aiDataObj.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                try {
                    JSONObject playerDataObj = new JSONObject();
                    playerDataObj.put("Distance", distance);
                    playerDataObj.put("CurrentCadence",cadence);
                    playerDataObj.put("HeartRate",heartRate);
                    playerDataObj.put("CurrentPaceMin",paceMin);
                    playerDataObj.put("CurrentPaceSeconds",paceSecond);
                    playerDataObj.put("TotalSeconds",timeSeconds);
                    playerDataObj.put("LapDistances", lapDistance);
                    playerDataObj.put("LapTimings",lapTimings);

                    JSONObject aiDataObj = new JSONObject();
                    aiDataObj.put("AIDistanceData",aiDistanceTravelled);
                    aiDataObj.put("AITimeData",aiTime);
                    UnityCallbacks.onUpdatePlayerData(playerDataObj.toString());
                    UnityCallbacks.onUpdateAIData(aiDataObj.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
