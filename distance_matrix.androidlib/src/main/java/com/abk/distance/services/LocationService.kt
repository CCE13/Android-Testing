package com.abk.distance.services


import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.location.*
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.abk.distance.HeartRateTracker
import com.abk.distance.LappingManager
import com.abk.distance.PlayerDataInterfaceClass
import com.abk.distance.StepsTracker
import com.abk.distance.utils.DataPoint
import com.abk.gps_forground.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mygdx.runai.LibGDX
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.fixedRateTimer
import kotlin.math.ceil


class LocationService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private val TAG: String = "LocationService"
    private val binder = LocationServiceBinder()

    private val NOTI_ID: Int = 2
    private var startTime: Long = 0
    val manager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    var isLocationManagerUpdatingLocation = false
    var locationList: ArrayList<Location>? = null
    var oldLocationList: ArrayList<Location>? = null
    var noAccuracyLocationList: ArrayList<Location>? = null
    var inaccurateLocationList: ArrayList<Location>? = null
    var kalmanNGLocationList: ArrayList<Location>? = null
    var latLongList: ArrayList<DataPoint>? = null
    var isLogging = true
    var currentSpeed = 0.0f // meters/second
    var kalmanFilter: KalmanLatLong? = null
    var runStartTimeInMillis: Long = 0
    var gpsCount = 0
    var goodGpsCount = 0
    val distanceIntent = Intent(SEND_DISTANCE_DATA)
    var _timer = 0;

    var _paused: Boolean = false
    var _inBackground : Boolean = false;
    var dataTypes: Map<String, String>? = null;

    var LapInterval = 0f;
    var DistanceToTravel = 0f;

    var AIHandler: AIHandler? = null;
    var LappingManager: LappingManager? = null
    var SoundController: SoundController? = null
    var StepsTracker: StepsTracker? = null
    var HeartRateTracker: HeartRateTracker? = null;
    var AITextToSpeech: AITextToSpeech? = null
    var LibGDX: LibGDX? = null;
    var steps: ArrayList<Int>? = null;
    var cadence: ArrayList<Int>? = null;
    var strideLength: ArrayList<Float>? = null;
    var voiceVolume = 0f;
    var footstepVolume = 0f;
    var stepsData: ArrayList<StepDataCalculator>? = null;

    var modeValue: Int = 0;

    var locationDistanceData: ArrayList<MapSimulationPointData>? = null

    var RankID = " ";
    var targetPaceMinuteAI: Int = 0;
    var targetPaceSecondsAI: Int = 0;

    var currentPlayerPaceMin : Int = 0;
    var currentPlayerPaceSeconds : Int = 0;

    // Initialize these to some high value for comparison
    var slowestPaceMin = Int.MAX_VALUE
    var slowestPaceSeconds = Int.MAX_VALUE
    // Initialize these to zero for comparison
    var fastestPaceMin = Int.MAX_VALUE
    var fastestPaceSeconds = Int.MAX_VALUE
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var playerName = " ";


    @RequiresApi(Build.VERSION_CODES.S)
    private val locationRequest: LocationRequest = LocationRequest.Builder(500)
        .setWaitForAccurateLocation(true)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            onLocationChanged(locationResult.lastLocation!!)
        }
    }

    override fun onCreate() {
        isLocationManagerUpdatingLocation = false
        locationList = ArrayList()
        noAccuracyLocationList = ArrayList()
        oldLocationList = ArrayList()
        inaccurateLocationList = ArrayList()
        kalmanNGLocationList = ArrayList()
        kalmanFilter = KalmanLatLong(1f)
        latLongList = ArrayList()
        isLogging = true
        locationDistanceData = ArrayList()
        steps = ArrayList()
        cadence = ArrayList()
        strideLength = ArrayList()
        stepsData = ArrayList()


        RegisterReceiver("service.PauseRequest",pauseRequestReceiver)
        RegisterReceiver("com.abk.distance.PAUSE_STATE_CHANGE",pauseStateReceiver)
        RegisterReceiver("com.abk.distance.Volume_State_Change",volumeStateReceiver)
        RegisterReceiver("com.abk.distance.BACKGROUND_STATE_CHANGE",backgroundStateReceiver)
        RegisterReceiver("com.abk.distance.PACER_STATE_CHANGE",paceChangeReceiver)


        val notification = createNotificationChanel()
        startForeground(NOTI_ID, notification)
        val file = File(getExternalFilesDir(null), "Running");
        file.writeText("running")


    }
    fun RegisterReceiver(intentFilter : String,broadcastReceiver: BroadcastReceiver) {
        val filter = IntentFilter(intentFilter);
        registerReceiver(broadcastReceiver, filter);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(i: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(i, flags, startId)
        RankID = i.getStringExtra("RankID").toString();
        modeValue = i.getIntExtra("Mode", 0);
        LapInterval = i.getFloatExtra("lapInterval", 0.0f)
        DistanceToTravel = i.getFloatExtra("distanceToTravel", 0.0f) * 1000
        LappingManager = LappingManager(LapInterval, DistanceToTravel);
        targetPaceMinuteAI = i.getIntExtra("PaceMinute", 0)
        targetPaceSecondsAI = i.getIntExtra("PaceSecond", 0)
        voiceVolume = i.getFloatExtra("voiceVolume", 0f)
        footstepVolume = i.getFloatExtra("footstepVolume", 0f)
        playerName = i.getStringExtra("Name").toString();
        System.out.println("Sound Values" + " " + voiceVolume + " h" +footstepVolume + " s")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startUpdatingLocation()
        return START_STICKY
    }


    override fun onDestroy() {
        stopUpdatingLocation()
        timer?.cancel()
        paceTimer?.cancel()
        AIHandler?.stopRunning()
        LibGDX?.dispose()
        timer = null;
        paceTimer = null
        AIHandler = null
        SoundController = null
        LibGDX = null
        latLongList = null;

        unregisterReceiver(pauseStateReceiver)
        unregisterReceiver(pauseRequestReceiver)
        unregisterReceiver(volumeStateReceiver)
        unregisterReceiver(backgroundStateReceiver)
        unregisterReceiver(paceChangeReceiver)


        manager.cancel(NOTI_ID)
        super.onDestroy()
    }

    private fun createNotificationChanel(): Notification {
        val NOTIFICATION_CHANNEL_ID = "com.getDistance"
        val channelName = "Foreground Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,

            )

        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        chan.description = "Foreground Service showing your activity and duration"
        manager.createNotificationChannel(chan)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, NOTIFICATION_CHANNEL_ID
        )
        val yourRequestCode = 66
        val resultIntent = Intent(this, getMainActivityClass(this))
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                yourRequestCode,
                resultIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val collapsedView = RemoteViews(packageName, R.layout.notification_collapsed)
        val expandedView = RemoteViews(packageName, R.layout.notification_expanded)

        val nightModeFlags: Int = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> setNotifcationDarkMode(expandedView)
            Configuration.UI_MODE_NIGHT_NO -> setNotifcationLightMode(expandedView)
        }

        expandedView.setTextViewText(R.id.Distance_ran, dataTypes?.get(R.id.Distance_ran.toString()))
        expandedView.setTextViewText(R.id.TimeTaken, dataTypes?.get(R.id.TimeTaken.toString()))
        expandedView.setTextViewText(R.id.Pace, dataTypes?.get(R.id.Pace.toString()))

        //assign intent to the button
        val pauseIntent = Intent("service.PauseRequest")
        val pausePendingIntent =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        expandedView.setOnClickPendingIntent(R.id.Pause, pausePendingIntent)
        expandedView.setOnClickPendingIntent(R.id.Resume, pausePendingIntent)

        if (_paused) {
            expandedView.setViewVisibility(R.id.Resume, View.VISIBLE)
            expandedView.setViewVisibility(R.id.Pause, View.GONE);
        } else {
            expandedView.setViewVisibility(R.id.Resume, View.GONE)
            expandedView.setViewVisibility(R.id.Pause, View.VISIBLE);
        }

        return builder
            //.setContentTitle("RunAI is tracking your run")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
    }

    private fun setNotifcationLightMode(view: RemoteViews){
        view.setTextColor(R.id.Distance_ran,Color.BLACK);
        view.setTextColor(R.id.distance_prefix,Color.BLACK);
        view.setTextColor(R.id.TimeTaken,Color.BLACK);
        view.setTextColor(R.id.time_prefix,Color.BLACK);
        view.setTextColor(R.id.Pace,Color.BLACK);
        view.setTextColor(R.id.pace_prefix,Color.BLACK);
    }
    private fun setNotifcationDarkMode(view: RemoteViews){
        view.setTextColor(R.id.Distance_ran,Color.WHITE);
        view.setTextColor(R.id.distance_prefix,Color.WHITE);
        view.setTextColor(R.id.TimeTaken,Color.WHITE);
        view.setTextColor(R.id.time_prefix,Color.WHITE);
        view.setTextColor(R.id.Pace,Color.WHITE);
        view.setTextColor(R.id.pace_prefix,Color.WHITE);
    }

    private fun getMainActivityClass(context: Context): Class<*>? {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        val className = launchIntent!!.component!!.className
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onRebind(intent: Intent) {
        Log.d(LOG_TAG, "onRebind ")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(LOG_TAG, "onUnbind ")
        return true
    }


    /**
     * Binder class
     *
     * @author Takamitsu Mizutori
     */
    inner class LocationServiceBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }


    var timer: Timer? = null;
    var paceTimer : Timer? = null;
    var _stepTimeInteval = 5;
    var stepTimer = 0;
    var lastTimeMillisecond: Long = System.currentTimeMillis()
    var playerDataClass: PlayerDataInterfaceClass? = null
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun startUpdatingLocation() {
        //FirebaseApp.initializeApp(this);
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::LocationWakelock").apply {
                acquire()
            }
        }
        if (!isLocationManagerUpdatingLocation) {
            startTime = System.currentTimeMillis()
            isLocationManagerUpdatingLocation = true
            runStartTimeInMillis = (SystemClock.elapsedRealtimeNanos() / 1000000)
            locationList!!.clear()
            oldLocationList!!.clear()
            noAccuracyLocationList!!.clear()
            inaccurateLocationList!!.clear()
            kalmanNGLocationList!!.clear()
            //System.out.println(paused)


            //Exception thrown when GPS or Network provider were not available on the user's device.
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null /* Looper */
                )
                gpsCount = 0
                goodGpsCount = 0
                InitialiseLibGDX()

                timer = fixedRateTimer("timer", initialDelay = 0, period = 1000) {
                    if (!_paused) {
                        if (distance < DistanceToTravel) {
                            _timer++
                            playerDataClass!!.SetPlayerData(_timer,currentPlayerPaceMin,currentPlayerPaceSeconds,distance)
                        }
                        RunLibGDX()
                    }

                    FormatText(distance, _timer)
                    val notification = createNotificationChanel()
                    manager.notify(NOTI_ID, notification)
                }

            } catch (e: RuntimeException) {
                Log.e(LOG_TAG, e.localizedMessage.toString())
            }

        }
    }

    private fun RunLibGDX() {

        //this to handle the steps
        stepTimer++;
        if (_stepTimeInteval == stepTimer) {
            var StepData = StepDataCalculator(
                StepsTracker!!.getStepCount(),
                System.currentTimeMillis() - lastTimeMillisecond,
                StepsTracker!!.getDistance(distance.toFloat()),
                _timer
            )
            lastTimeMillisecond = System.currentTimeMillis()
            stepsData?.add(StepData)
            stepTimer = 0;
        }

        // Update the LibGDX values
        if (!distance.toFloat().isNaN()) {
            LibGDX!!.playerDistanceMeters = distance.toFloat()
        } else {
            LibGDX!!.playerDistanceMeters = 1f;
        }
        LibGDX!!.modeValue = modeValue;
        LibGDX!!.voiceVolumeValue = voiceVolume
        LibGDX!!.footstepVolumeValue = footstepVolume
        AIHandler!!.RunTheAI()
    }

    private fun InitialiseLibGDX() {
        LappingManager = LappingManager(LapInterval, DistanceToTravel)
        AITextToSpeech = AITextToSpeech(applicationContext)

        SoundController = SoundController(applicationContext, AITextToSpeech)
        StepsTracker = StepsTracker(applicationContext)
        HeartRateTracker = HeartRateTracker(applicationContext)
        StepsTracker!!.startCounting()
        HeartRateTracker!!.trackHeartRate()

        Log.d(TAG, RankID.toFloat().toString() + " Rank ID")

        playerDataClass =
            PlayerDataInterfaceClass(RankID.toFloat(), 0, DistanceToTravel, playerName)
        LibGDX = LibGDX(
            SoundController,
            playerDataClass,
            modeValue,
        )


        LibGDX!!.paceMinute = targetPaceMinuteAI;
        LibGDX!!.paceSecond = targetPaceSecondsAI;

        LibGDX!!.voiceVolumeValue = voiceVolume
        LibGDX!!.footstepVolumeValue = footstepVolume

        AIHandler = AIHandler(LibGDX);
        AIHandler!!.StartTheAI();
        stepTimer = 0;
        distance = 0.0;

        var stepData = StepDataCalculator(StepsTracker!!.getStepCount(), 0, StepsTracker!!.getDistance(distance.toFloat()),0)
        stepsData?.add(stepData)
    }

    private var distance: Double = 0.0;


    private fun FormatText(distance: Double, rawSeconds: Int) {
        val milliseconds = rawSeconds * 1000;

        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()


        distanceIntent.putExtra("distance", distance)
        sendBroadcast(distanceIntent)
//        val d = distance * 1000
        val kilometers: Double = distance / 1000f
        val flooredKilometers = BigDecimal(kilometers.toDouble()).setScale(2, RoundingMode.FLOOR).toFloat()

        val durationText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        val distanceStr = flooredKilometers.toString();
        val paceText = "$currentPlayerPaceMin'$currentPlayerPaceSeconds''"


        dataTypes = mapOf<String, String>(
            R.id.Distance_ran.toString() to distanceStr, R.id.TimeTaken.toString() to durationText,
            R.id.Pace.toString() to paceText
        )
        LappingManager!!.lapChecker(distance, rawSeconds)

        if(!_inBackground || distance >= DistanceToTravel)
        {
            SimpleSendValues();
        }
    }


    private fun CalculateCurrentPace() {
        // Convert speed from m/s to km/min
        val speedInKmPerMin = currentSpeed * 0.06 // this is correct

        // Calculate pace in minutes per kilometer
        val paceInMinutesPerKm = if (speedInKmPerMin > 0) 1.00 / speedInKmPerMin else 0.00

        // If the pace is above 20 minutes, return (0, 0)
        if (currentSpeed <= 0.8) {
            currentPlayerPaceMin = 0;
            currentPlayerPaceSeconds= 0;
            return;
        }
        else{
            currentPlayerPaceMin = paceInMinutesPerKm.toInt()
            currentPlayerPaceSeconds= ceil((paceInMinutesPerKm - currentPlayerPaceMin ) * 60.0).toInt()
        }

        if((currentPlayerPaceMin < fastestPaceMin || (currentPlayerPaceMin == fastestPaceMin && currentPlayerPaceSeconds < fastestPaceSeconds))){
            fastestPaceMin = currentPlayerPaceMin;
            fastestPaceSeconds = currentPlayerPaceSeconds;
        }
        if(slowestPaceMin > 20 && currentPlayerPaceMin < 20||currentPlayerPaceMin > slowestPaceMin || (currentPlayerPaceMin == slowestPaceMin && currentPlayerPaceSeconds > slowestPaceSeconds)){
            slowestPaceMin = currentPlayerPaceMin;
            slowestPaceSeconds = currentPlayerPaceSeconds;
        }
    }

    var serviceData :ServiceData ?= null;

    private fun SimpleSendValues(){
        val distanceTravelled = distance
        val stepCounted = stepsData
        val totalTimeRan = _timer
        val aiDistanceTravelled = ArrayList(AIHandler!!.aiDistanceTravelled)
        val aiTime = ArrayList(AIHandler!!.aiTime)
        val lappingManager = LappingManager
        val heartRate = HeartRateTracker!!.heartRate.toInt()
        val locationDistanceDatas = null
        val locationDataPoints = null
// Initialize these to some high value for comparison
        var slowestPaceMin = Int.MAX_VALUE
        var slowestPaceSeconds = Int.MAX_VALUE
// Initialize these to zero for comparison
        var fastestPaceMin = Int.MAX_VALUE
        var fastestPaceSeconds = Int.MAX_VALUE
        //do some checks between paces
        var cadenceAtPoint = 0;
        if(stepsData!!.size > 0){
            cadenceAtPoint = stepsData!!.last().cadence;
        }
        serviceData = ServiceData(
            distanceTravelled = distanceTravelled,
            currentPlayerPaceMin,
            currentPlayerPaceSeconds,
            cadence = cadenceAtPoint,
            heartrate = heartRate,
            stepCounted = stepCounted,
            totalTimeRan = totalTimeRan,
            locationDataPoints = locationDataPoints,
            aiDistanceTravelled = aiDistanceTravelled,
            aiTime = aiTime,
            locationDistanceData = locationDistanceDatas,
            lappingManager = lappingManager,
            fastestPaceMin,
            fastestPaceSeconds,
            slowestPaceMin,
            slowestPaceSeconds,
        )
        val file = File(getExternalFilesDir(null), "Service Data")
        val fileOutputStream = FileOutputStream(file)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)
        objectOutputStream.writeObject(compressServiceData(serviceData!!))
        objectOutputStream.close()
        fileOutputStream.close()
        sendBroadcast(Intent().apply {
            action = "action.data_update"
            addCategory("action.category.distance")
            //putExtra("Service Data",compressServiceData(serviceData))
        })
    }
    private fun SendFinalValues(){
        val distanceTravelled = distance
        val stepCounted = stepsData
        val totalTimeRan = _timer
        val aiDistanceTravelled = ArrayList(AIHandler!!.aiDistanceTravelled)
        val aiTime = ArrayList(AIHandler!!.aiTime)
        val lappingManager = LappingManager
        val heartRate = HeartRateTracker!!.heartRate.toInt()
        val locationDistanceDatas = ArrayList(locationDistanceData!!)
        val locationDataPoints = ArrayList(ArrayList(latLongList))
// Initialize these to some high value for comparison
        var cadenceAtPoint = 0;
        if(stepsData!!.size > 0){
            cadenceAtPoint = stepsData!!.last().cadence;
        }

        serviceData = ServiceData(
            distanceTravelled = distanceTravelled,
            currentPlayerPaceMin,
            currentPlayerPaceSeconds,
            cadence = cadenceAtPoint,
            heartrate = heartRate,
            stepCounted = stepCounted,
            totalTimeRan = totalTimeRan,
            locationDataPoints = locationDataPoints,
            aiDistanceTravelled = aiDistanceTravelled,
            aiTime = aiTime,
            locationDistanceData = locationDistanceDatas,
            lappingManager = lappingManager,
            fastestPaceMin,
            fastestPaceSeconds,
            slowestPaceMin,
            slowestPaceSeconds,
        )
        val file = File(getExternalFilesDir(null), "Service Data")
        val fileOutputStream = FileOutputStream(file)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)
        objectOutputStream.writeObject(compressServiceData(serviceData!!))
        objectOutputStream.close()
        fileOutputStream.close()
        sendBroadcast(Intent().apply {
            action = "action.data_update"
            addCategory("action.category.distance")
            putExtra("CompetitionStopped",true)
        })
    }

    fun compressServiceData(data: ServiceData): String {
        // Convert object to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(data)
        objectOutputStream.flush()

        // Compress the byte array
        val compressedData = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(compressedData)
        gzipOutputStream.write(byteArrayOutputStream.toByteArray())
        gzipOutputStream.close()

        // Convert byte array to Base64 string to be sent in intent
        return Base64.encodeToString(compressedData.toByteArray(), Base64.DEFAULT)
    }

    fun stopUpdatingLocation() {
        if (isLocationManagerUpdatingLocation) {
            try {
                wakeLock?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isLocationManagerUpdatingLocation = false
        }
            if(stepTimer == 5){
                var StepDataCalculator = stepsData!!.last();
                stepsData?.add(StepDataCalculator)
            }
            else{
                var StepDataCalculator = StepDataCalculator(StepsTracker!!.getStepCount(), System.currentTimeMillis() - lastTimeMillisecond, StepsTracker!!.getDistance(distance.toFloat()),_timer)
                stepsData?.add(StepDataCalculator)
            }

        stepTimer = 0;
        if(lastLocation != null){
            latLongList!!.add(DataPoint(lastLocation!!.latitude, lastLocation!!.longitude))
        }
        locationDistanceData!!.add(
            MapSimulationPointData(
                _timer, distance, AIHandler!!.aiDistanceTravelled,currentPlayerPaceMin,currentPlayerPaceSeconds
            )
        );
        SendFinalValues();
        println("Sending NoN Compression data")
    }

    var pausedLocation : Location? = null;
    fun onLocationChanged(newLocation: Location) {
        if (distance >= DistanceToTravel) return;
        if(_paused){
            pausedLocation = newLocation;
            return;
        }
        if (gpsCount == 0) {
            locationDistanceData!!.add(
                MapSimulationPointData(
                    0, 0.0, AIHandler!!.aiDistanceTravelled,0,0
                )
            );
            latLongList!!.add(DataPoint(newLocation.latitude, newLocation.longitude))
        }
        gpsCount++
        if (isLogging) {
            filterAndAddLocation(newLocation);
        }
        val intent = Intent("LocationUpdated")
        intent.putExtra("location", newLocation)
        LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)
    }

    var lastLocation: Location? = null

    @SuppressLint("NewApi")
    private fun getLocationAge(newLocation: Location): Long {
        val locationAge: Long
        locationAge = System.currentTimeMillis() - newLocation.getTime();
        return locationAge
    }

    private fun filterAndAddLocation(location: Location): Boolean {
        val age = getLocationAge(location)
        if (age > 10 * 1000) { // Change from 10 to 5 seconds, to increase location update frequency
            Log.d(TAG, "Location is old")
            oldLocationList!!.add(location)
            return false
        }
        if (location.accuracy   <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.")
            noAccuracyLocationList!!.add(location)
            return false
        }
        //setAccuracy(newLocation.getAccuracy());
        if (location.accuracy > 100) { // Change from 200 to 100 meters, to increase accuracy
            Log.d(TAG, "Accuracy is too low.")
            inaccurateLocationList!!.add(location)
            return false
        }/* Kalman Filter */
        val Qvalue: Float
        val locationTimeInMillis = (location.getElapsedRealtimeNanos() / 1000000) as Long
        val elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis
        Qvalue = if (currentSpeed == 0.0f) {
            3.0f // Adjust Q value for Kalman filter when speed is 0
        } else {
            currentSpeed // meters per second
        }
        kalmanFilter!!.Process(
            location.getLatitude(), location.getLongitude(),

            location.getAccuracy(), elapsedTimeInMillis, Qvalue
        )
        val predictedLat: Double = kalmanFilter!!.get_lat()
        val predictedLng: Double = kalmanFilter!!.get_lng()
        val predictedLocation = Location("") //provider name is unecessary
        predictedLocation.setLatitude(predictedLat) //your coords of course
        predictedLocation.setLongitude(predictedLng)
        val predictedDeltaInMeters: Float = predictedLocation.distanceTo(location)
        if (predictedDeltaInMeters > 60) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter!!.consecutiveRejectCount += 1
            if (kalmanFilter!!.consecutiveRejectCount > 3) {
                kalmanFilter =
                    KalmanLatLong(3.0f) //reset Kalman Filter if it rejects more than 3 times in raw.
            }
            kalmanNGLocationList!!.add(location)
            return false
        } else {
            kalmanFilter!!.consecutiveRejectCount = 0
        }/* Notifiy predicted location to UI */
        val intent = Intent("PredictLocation")
        intent.putExtra("location", predictedLocation)
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent)
        Log.d(TAG, "Location quality is good enough.")
        currentSpeed = location.getSpeed()
        CalculateCurrentPace()
        if(pausedLocation != null){
            latLongList!!.add(DataPoint(pausedLocation!!.latitude, pausedLocation!!.longitude))
            locationList!!.add(pausedLocation!!)
            locationDistanceData!!.add(
                MapSimulationPointData(
                    _timer, distance, AIHandler!!.aiDistanceTravelled,currentPlayerPaceMin,currentPlayerPaceSeconds
                )
            );
            pausedLocation = null;
        }
            latLongList!!.add(DataPoint(location.latitude, location.longitude))

            if (locationList!!.isNotEmpty()) {
                distance += locationList!!.last().distanceTo(location)
            }
            locationList!!.add(location)
            locationDistanceData!!.add(
                MapSimulationPointData(
                    _timer, distance, AIHandler!!.aiDistanceTravelled,currentPlayerPaceMin,currentPlayerPaceSeconds
                )
            );

        lastLocation = location;
        return true
    }

    private fun stopTTS(){
        if(!_paused)
            return;
        AITextToSpeech!!.PauseTTSpeech();
    }

    private val pauseStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            _paused = intent.getBooleanExtra("isPaused", false)
            stopTTS();
        }
    }
    private val paceChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            targetPaceMinuteAI = intent.getIntExtra("PaceMinutes", 0)
            targetPaceSecondsAI = intent.getIntExtra("PaceSeconds", 0);
            LibGDX!!.UpdatePacerPace(targetPaceMinuteAI,targetPaceSecondsAI);
        }
    }

    private val pauseRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            _paused = !_paused;
            stopTTS();
        }
    }

    private val volumeStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            voiceVolume = intent.getFloatExtra("voiceVolume", 0f)
            footstepVolume = intent.getFloatExtra("footstepVolume", 0f)
        }
    }

    private val backgroundStateReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {

                _inBackground = intent.getBooleanExtra("isInBackground",false)
                println("$_inBackground is in background");
            };
        }
    }

    companion object {
        @kotlin.jvm.JvmField
        val LOG_TAG = LocationService::class.java.simpleName
        const val SEND_DISTANCE_DATA = "com.abk.distance.SEND_DISTANCE_DATA"
    }
}