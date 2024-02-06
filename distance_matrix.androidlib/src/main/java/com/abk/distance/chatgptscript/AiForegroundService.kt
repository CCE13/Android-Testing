package com.abk.distance.chatgptscript

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.abk.gps_forground.R
import com.theokanning.openai.completion.chat.ChatMessage
import org.greenrobot.eventbus.EventBus
import kotlin.concurrent.fixedRateTimer
import kotlinx.coroutines.*

class AiForegroundService : Service() {
    var chatGptAdvance: ChatGptAdvance? = null
    var runOnce: Boolean = false
    var responseMessage: ChatMessage? = null
    var waitingTime: Int = 10
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val yourRequestCode = 66
    var pendingIntent: PendingIntent? = null

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        val resultIntent = Intent(this, getMainActivityClass(this))
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    enum class Actions{
        START,STOP
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //When is sort of like a swtich statement
        //This code check if the action is started or stop
        when(intent?.action){
            Actions.START.toString() ->{
                startNotificationService()
                start();
                startGPTModel()
            }
            Actions.STOP.toString() -> stopSelf()
        }
        return START_NOT_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }


    private fun start(){
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Creates the notification that will be seen
        notificationBuilder = NotificationCompat.Builder(this, "aiAnalyser_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Running AI Analyser")
            .setContentText("Starting AI Analyser")
            .setFullScreenIntent(pendingIntent, true)

        val notification =notificationBuilder.build()


        //Starts the foreground service
        startForeground(3, notification)

    }

    private fun startNotificationService(){
        val channel = NotificationChannel(
            "aiAnalyser_channel",
            "Running AI Analyser",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.description = "Foreground service running a AI Analyser which is analysing your runs"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)  // Register the channel with NotificationManager
    }


    //Sends an event to send the output data
    fun sendData(data: String){
        EventBus.getDefault().post(MessageEvent(data))
    }

    private fun startGPTModel() {

        var timer = fixedRateTimer("timer", initialDelay = 0, period = 1000) {
            waitingTime -= 1
            EventBus.getDefault().post(MessageEvent(""" 
                Generation done in $waitingTime 
                You may exit the app if needed """));

            updateNotificationText("Time Left: $waitingTime")
        }

        coroutineScope.launch {

            chatGptAdvance = ChatGptAdvance()

            responseMessage = chatGptAdvance!!.responseMessage;

            if(responseMessage != null){
                updateNotificationText("Run Analyse Finished")
                sendData(responseMessage.toString())
                timer.cancel();
            }
        }




    }

    fun updateNotificationText(text: String){
        notificationBuilder.setContentText(text)
        notificationManager.notify(3, notificationBuilder.build())
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

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        coroutineScope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }




}