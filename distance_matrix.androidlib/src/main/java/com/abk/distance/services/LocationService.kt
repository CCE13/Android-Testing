package com.abk.distance.services


import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.concurrent.fixedRateTimer


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
    var feedbackVolume = 0f;
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

    @RequiresApi(Build.VERSION_CODES.S)
    private val locationRequest: LocationRequest = LocationRequest.Builder(100)
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
        feedbackVolume = i.getFloatExtra("feedbackVolume", 0f)
        System.out.println("Sound Values" + " " + voiceVolume + " h" +footstepVolume + " s" + feedbackVolume)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            this, NOTIFICATION_CHANNEL_ID
        )
        val yourRequestCode = 66
        val resultIntent = Intent(this, getMainActivityClass(this))
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                yourRequestCode,
                resultIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(
                this, yourRequestCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val collapsedView = RemoteViews(packageName, R.layout.notification_collapsed)
        val expandedView = RemoteViews(packageName, R.layout.notification_expanded)

        expandedView.setTextViewText(R.id.DistanceRan, dataTypes?.get(R.id.DistanceRan.toString()))
        expandedView.setTextViewText(R.id.TimeTaken, dataTypes?.get(R.id.TimeTaken.toString()))

        //assign intent to the button
        val pauseIntent = Intent("service.PauseRequest")
        val pausePendingIntent =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        expandedView.setOnClickPendingIntent(R.id.Pause, pausePendingIntent)
        expandedView.setOnClickPendingIntent(R.id.Resume, pausePendingIntent)

        if (_paused) {
            expandedView.setTextViewText(R.id.Notification_Text_Header, "Run Paused")
            expandedView.setViewVisibility(R.id.Resume, View.VISIBLE)
            expandedView.setViewVisibility(R.id.Pause, View.GONE);
        } else {
            expandedView.setTextViewText(
                R.id.Notification_Text_Header, "RunAI is tracking Your Run"
            )
            expandedView.setViewVisibility(R.id.Resume, View.GONE)
            expandedView.setViewVisibility(R.id.Pause, View.VISIBLE);
        }

        return builder
            //.setContentTitle("RunAI is tracking your run")
            .setSmallIcon(R.drawable.logo).setContentIntent(pendingIntent).setOnlyAlertOnce(true)
            .setAutoCancel(false).setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setFullScreenIntent(pendingIntent, true)

            .build()
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


    private fun initialiseLibGDX()
    {
        LappingManager = LappingManager(LapInterval, DistanceToTravel)
        AITextToSpeech = AITextToSpeech(applicationContext)

        SoundController = SoundController(applicationContext, AITextToSpeech)
        StepsTracker = StepsTracker(applicationContext)
        HeartRateTracker = HeartRateTracker(applicationContext)
        StepsTracker!!.startCounting()
        HeartRateTracker!!.trackHeartRate()
        LibGDX = LibGDX(
            SoundController,
            PlayerDataInterfaceClass(RankID.toFloat(), 0f, 0f, DistanceToTravel),
            modeValue
        )


        LibGDX!!.paceMinute = targetPaceMinuteAI;
        LibGDX!!.paceSecond = targetPaceSecondsAI;

        LibGDX!!.voiceVolumeValue = voiceVolume
        LibGDX!!.footstepVolumeValue = footstepVolume
        System.out.println("Volume " + footstepVolume + " " + LibGDX!!.footstepVolumeValue);
        LibGDX!!.feedbackVolumeValue = feedbackVolume

        AIHandler = AIHandler(LibGDX, SoundController);
        AIHandler!!.StartTheAI();
    }

    private fun updateLibgdxData(){

        // Update RunAI values
        //test.testUpdate();
        if (!distance.toFloat().isNaN()) {
            LibGDX!!.playerDistanceMeters = distance.toFloat()
        } else {
            LibGDX!!.playerDistanceMeters = 1f;
        }
        LibGDX!!.modeValue = modeValue;
        LibGDX!!.voiceVolumeValue = voiceVolume
        LibGDX!!.footstepVolumeValue = footstepVolume
        LibGDX!!.feedbackVolumeValue = feedbackVolume
        AIHandler!!.RunTheAI()
        AIHandler!!.PingTheAI()
    }
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

                initialiseLibGDX();
                stepTimer = 0;
                distance = 0.0;

                var StepDataCalculator = StepDataCalculator(StepsTracker!!.getStepCount(), 0, StepsTracker!!.getDistance(distance.toFloat()),0)
                stepsData?.add(StepDataCalculator)


                timer = fixedRateTimer("timer", initialDelay = 0, period = 1000) {
                    if (!_paused) {
                        if (distance < DistanceToTravel) {
                            _timer++
                        }
                            stepTimer++;
                            if(_stepTimeInteval  == stepTimer){
                                var StepDataCalculator = StepDataCalculator(StepsTracker!!.getStepCount(), System.currentTimeMillis() - lastTimeMillisecond, StepsTracker!!.getDistance(distance.toFloat()),_timer)
                                lastTimeMillisecond = System.currentTimeMillis()
                                stepsData?.add(StepDataCalculator)
                                stepTimer = 0;
                            }

                    }
                    updateLibgdxData();
                    FormatText(distance, _timer)
                    val notification = createNotificationChanel()
                    manager.notify(NOTI_ID, notification)
                }



            } catch (e: RuntimeException) {
                Log.e(LOG_TAG, e.localizedMessage.toString())
            }

        }
    }

    private var distance: Double = 0.0;


    private fun FormatText(distance: Double, rawSeconds: Int) {
        val milliseconds = rawSeconds * 1000;

        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

        val secondsRaw = (milliseconds / 1000).toFloat()
        SaveValues(100f, secondsRaw);

        distanceIntent.putExtra("distance", distance)
        sendBroadcast(distanceIntent)
//        val d = distance * 1000
        val meters = (distance % 1000).toInt()
        val kilometers: Int = ((distance.toInt() - meters) / 1000)

        val durationText = "Duration: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}"
        val distanceStr =
            "Distance: ${if (kilometers > 0) "$kilometers km $meters m" else "$meters m"}"
//        val distance = "$currentNumberOfStepCount : ${getDistanceRun(currentNumberOfStepCount)} km"


        dataTypes = mapOf<String, String>(
            R.id.DistanceRan.toString() to distanceStr, R.id.TimeTaken.toString() to durationText
        )
        LappingManager!!.lapChecker(distance, rawSeconds)

        if(!_inBackground || distance >= DistanceToTravel)
        {
            SimpleSendValues();
        }
    }


    private fun CalculateCurrentPace() {
        // Convert speed from m/s to km/min
        println("Speed :: " + currentSpeed)
        val speedInKmPerMin = currentSpeed * 0.06 // this is correct

        // Calculate pace in minutes per kilometer
        val paceInMinutesPerKm = if (speedInKmPerMin > 0) 1.00 / speedInKmPerMin else 0.00

        // If the pace is above 20 minutes, return (0, 0)
        if (currentSpeed <= 0.8) {
            currentPlayerPaceMin = 0;
            currentPlayerPaceSeconds= 0;
            return;
        }
        currentPlayerPaceMin = paceInMinutesPerKm.toInt()
        currentPlayerPaceSeconds= ((paceInMinutesPerKm - currentPlayerPaceMin ) * 60.0).toInt()
        println("Pace:: " + currentPlayerPaceMin + " : " + currentPlayerPaceSeconds)

        val paces =  paceChecker(serviceData!!.fastestPaceMin, serviceData!!.fastestPaceSeconds, serviceData!!.slowestPaceMin, serviceData!!.slowestPaceSeconds)
        fastestPaceMin = paces.first.first
        fastestPaceSeconds = paces.first.second
        slowestPaceMin = paces.second.first
        slowestPaceSeconds = paces.second.second
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
    private fun SendValues(){
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
        var slowestPaceMin = Int.MAX_VALUE
        var slowestPaceSeconds = Int.MAX_VALUE
// Initialize these to zero for comparison
        var fastestPaceMin = Int.MAX_VALUE
        var fastestPaceSeconds = Int.MAX_VALUE
        //do some checks between paces
        if(serviceData != null){
            val paces =  paceChecker(serviceData!!.fastestPaceMin, serviceData!!.fastestPaceSeconds, serviceData!!.slowestPaceMin, serviceData!!.slowestPaceSeconds)
            fastestPaceMin = paces.first.first
            fastestPaceSeconds = paces.first.second
            slowestPaceMin = paces.second.first
            slowestPaceSeconds = paces.second.second
        }
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
    fun paceChecker(
        fastestPaceMins: Int, fastestPaceSeconds: Int,
        slowestPaceMins: Int, slowestPaceSeconds: Int
    ): Pair<Pair<Int, Int>, Pair<Int, Int>> {

        // Convert the paces to seconds for comparison
        val fastestPaceInSeconds = fastestPaceMins * 60 + fastestPaceSeconds
        val slowestPaceInSeconds = slowestPaceMins * 60 + slowestPaceSeconds
        val currentPlayerPaceInSeconds = currentPlayerPaceMin * 60 + currentPlayerPaceSeconds

        // Initialize new fastest and slowest paces
        var newFastestPaceInSeconds = fastestPaceInSeconds
        var newSlowestPaceInSeconds = slowestPaceInSeconds

        // Check and update the new fastest pace if it's not 0 and is faster than the current fastest pace
        if (fastestPaceInSeconds == 0 || (fastestPaceInSeconds != 0 && currentPlayerPaceInSeconds < fastestPaceInSeconds)) {
            newFastestPaceInSeconds = currentPlayerPaceInSeconds
        }

        // Check and update the new slowest pace if it's not more than 20 minutes and is slower than the current slowest pace
        if (slowestPaceInSeconds == 0 ||slowestPaceInSeconds > 1200 ||currentPlayerPaceInSeconds <= 1200 && currentPlayerPaceInSeconds > slowestPaceInSeconds) {
            newSlowestPaceInSeconds = currentPlayerPaceInSeconds
        }

        // Convert the new fastest and slowest paces back to minutes and seconds
        val newFastestPaceMins = newFastestPaceInSeconds / 60
        val newFastestPaceSeconds = newFastestPaceInSeconds % 60
        val newSlowestPaceMins = newSlowestPaceInSeconds / 60
        val newSlowestPaceSeconds = newSlowestPaceInSeconds % 60

        return Pair(Pair(newFastestPaceMins, newFastestPaceSeconds), Pair(newSlowestPaceMins, newSlowestPaceSeconds))
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


    private fun SaveValues(meters: Float, time: Float) {

        SaveValueInFile("dist_m", meters);
        SaveValueInFile("time_s", time);

    }

    private fun SaveValueInFile(name: String, value: Float) {
        try {
            val file = File(filesDir, name + ".txt")

            //System.out.println(file);
            val writer = BufferedWriter(FileWriter(file))
            writer.write("Speed: $value")
            writer.close()
            // println("Variables saved to file.")

        } catch (e: IOException) {
            println("An error occurred saving the file.")
            e.printStackTrace()
        }

    }
    fun stopUpdatingLocation() {
        if (isLocationManagerUpdatingLocation) {
            try {
                wakeLock?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//            locationManager.removeUpdates(this)
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
            AIHandler!!.PingTheAI()
        }
        locationDistanceData!!.add(
            MapSimulationPointData(
                _timer, distance, AIHandler!!.aiDistanceTravelled,currentPlayerPaceMin,currentPlayerPaceSeconds
            )
        );
        SendValues();
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
        val filtered = filterLocation(newLocation)
        if (isLogging) {
            if (filtered != null) {
                //currentSpeed = filtered.speed
                filterAndAddLocation(newLocation);
            }
        } else {
            // if newLocation passed the filter, count up goodLocationCount.
//            if (filtered != null) {
//                goodGpsCount++
//                if (goodGpsCount > 2) {
//                    val intent = Intent("GotEnoughLocations")
//                    intent.putExtra("goodLocationCount", goodGpsCount)Z
//                    LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)
//                }
//            }
        }
        val intent = Intent("LocationUpdated")
        intent.putExtra("location", newLocation)
        LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)
    }

    var lastLocation: Location? = null

    @SuppressLint("NewApi")
    private fun getLocationAge(newLocation: Location): Long {
        val locationAge: Long
        locationAge =
            (SystemClock.elapsedRealtimeNanos() / 1000000) - (newLocation.elapsedRealtimeNanos / 1000000)
        return locationAge
    }

    private fun filterAndAddLocation(location: Location): Boolean {
        val age = getLocationAge(location)
        if (age > 2 * 1000) { // Change from 10 to 5 seconds, to increase location update frequency
            Log.d(TAG, "Location is old")
            oldLocationList!!.add(location)
            return false
        }
        if (location.accuracy <= 0) {
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
            1.0f // Adjust Q value for Kalman filter when speed is 0
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
        if (predictedDeltaInMeters > 90) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter!!.consecutiveRejectCount += 1
            if (kalmanFilter!!.consecutiveRejectCount > 4) {
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
    private fun filterLocation(location: Location): Location? {
        val age = getLocationAge(location)
        if (age > 5 * 1000) { // Change from 10 to 5 seconds, to increase location update frequency
            Log.d(TAG, "Location is old")
            oldLocationList!!.add(location)
            return null
        }
        if (location.accuracy <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.")
            noAccuracyLocationList!!.add(location)
            return null
        }
        //setAccuracy(newLocation.getAccuracy());
        val horizontalAccuracy = location.accuracy
        if (horizontalAccuracy > 100) { // Change from 200 to 100 meters, to increase accuracy
            Log.d(TAG, "Accuracy is too low.")
            inaccurateLocationList!!.add(location)
            return null
        }
        Log.d(TAG, "Location quality is good enough.")
        return location
    }

    private val pauseStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            _paused = intent.getBooleanExtra("isPaused", false)
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
        }
    }

    private val volumeStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            voiceVolume = intent.getFloatExtra("voiceVolume", 0f)
            footstepVolume = intent.getFloatExtra("footstepVolume", 0f)
            feedbackVolume = intent.getFloatExtra("feedbackVolume", 0f)
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