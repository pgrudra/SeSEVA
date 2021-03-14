package com.example.us0.foregroundnnotifications

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.telephony.ServiceState
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.us0.Actions
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.installedapps.HomeActivity
import com.example.us0.setServiceState
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Runnable
import java.sql.Timestamp
import java.util.*

class TestService : Service() {
    private val NOTIFICATION_ID = 1
    private val REQUEST_CODE = 0
    private val FLAGS = 0
    private var isServiceStarted=false
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

//wakeLock not implemented
    override fun onCreate() {
        super.onCreate()
            val notification=createNotification()
            startForeground(NOTIFICATION_ID,notification)
    }

    private fun createNotification(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundServiceNotificationChannel = NotificationChannel(
                getString(R.string.foreground_service_notification_channel_id),
                getString(R.string.foreground_service_notification_channel_name),
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(foregroundServiceNotificationChannel)
        }
        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, FLAGS)
            }
        val notification = NotificationCompat.Builder(
            this,
            getString(R.string.foreground_service_notification_channel_id)
        )
                return notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_title))
            .setContentIntent(pendingIntent)
            .build()
        //.setContentText("not used")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null){
            when(intent.action){
                Actions.START.name->startService()
                Actions.STOP.name->stopService()
                else->Timber.i("never_happens")
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent=Intent(applicationContext,TestService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent:PendingIntent=PendingIntent.getService(this,1,restartServiceIntent,PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService:AlarmManager=applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime()+1000,restartServicePendingIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
    }
    private fun startService(){
        if(isServiceStarted)return
        isServiceStarted=true
        setServiceState(this, com.example.us0.ServiceState.STARTED)

        //Try CoroutineScope instead of GlobalScope
        GlobalScope.launch(Dispatchers.IO){
            val main = Intent(Intent.ACTION_MAIN, null)
            main.addCategory(Intent.CATEGORY_LAUNCHER)
            val pm = requireNotNull(applicationContext.packageManager)
            val launchables = pm.queryIntentActivities(main, 0)
            val appPackageListR = ArrayList<String>()
            for (item in launchables) {
                try {
                    val nameOfPackage: String = item.activityInfo.packageName
                    appPackageListR.add(nameOfPackage)
                } catch (e: Exception) {}
            }
            val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
            val appPackageList=appPackageListR.distinct()
            for (l in appPackageList) {
                sortedEvents[l] = mutableListOf()
            }
            while(isServiceStarted){
                launch(Dispatchers.IO){
                    getStats(applicationContext, sortedEvents)
                }
                delay(10000)
            }
        }
    }
    private fun stopService(){
try{
    stopForeground(true)
    stopSelf()
} catch (e:Exception){

}
        isServiceStarted=false
        setServiceState(this,com.example.us0.ServiceState.STOPPED)
    }

    private fun getStats(context: Context, sortedEvents: Map<String, MutableList<UsageEvents.Event>>) {
        sortedEvents.forEach { (packageName, events) -> events.clear() }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = Calendar.getInstance()
        Log.i("JK", Timestamp(now.timeInMillis).toString())
        val begin: Calendar = Calendar.getInstance()
        begin.set(Calendar.HOUR_OF_DAY, 0)
        begin.set(Calendar.MINUTE, 0)
        begin.set(Calendar.SECOND, 0)
        begin.set(Calendar.MILLISECOND, 0)
        Log.i("JK", Timestamp(begin.timeInMillis).toString())

        val systemEvents: UsageEvents = usm.queryEvents(
            begin.timeInMillis,
            now.timeInMillis
        )
        var eval = true
        while (systemEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            systemEvents.getNextEvent(event)
            val type = event.eventType
            if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (sortedEvents[event.packageName] != null) {
                    if (type == UsageEvents.Event.ACTIVITY_PAUSED) {
                        eval = false
                    } else {
                        eval = true
                    }
                }
                sortedEvents[event.packageName]?.add(event)
            }
        }
        val statss = mutableListOf<stat>()
        if (eval) {
            sortedEvents.forEach { (packageName, events) ->
                // Keep track of the current start and end times
                var startTime = 0L
                var endTime = 0L
                var launches = 0
                var totalTime = 0L
                var transt=0L
                events.forEach {
                    if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        // App was moved to the foreground: set the start time
                        startTime = it.timeStamp
                        launches += 1


                    } else if (it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        if(startTime!=0L || totalTime==0L){
                            endTime = it.timeStamp
                            transt=it.timeStamp}
                    }
                    if(startTime!=0L && transt!=0L && startTime>transt && (startTime-transt)<50){
                        launches-=1
                        transt=0L
                    }
                    if(it.eventType==UsageEvents.Event.ACTIVITY_RESUMED || it.eventType==UsageEvents.Event.ACTIVITY_PAUSED){

                        if(packageName=="com.google.android.youtube"){
                            Log.i("DWN",it.eventType.toString()+" "+Timestamp(it.timeStamp))
                        }
                    }
                    if (startTime == 0L && endTime != 0L) {
                        startTime = begin.timeInMillis
                    }
                    if (startTime != 0L && endTime != 0L) {
                        // Add the session time to the total time
                        totalTime += endTime - startTime
                        // Reset the start/end times to 0
                        startTime = 0L
                        endTime = 0L
                        if(packageName=="com.google.android.youtube"){
                            Log.i("DWN","${totalTime/60000}")
                        }
                    }
                }
                if (startTime != 0L && endTime == 0L) {
                    totalTime += now.timeInMillis - startTime
                }

                statss.add(stat(packageName, totalTime, launches.toString()))
                if (launches != 0) {
                    Log.i(
                        "WOW",
                        "package:" + packageName + "   TotalTime=" + (totalTime / 60000).toString() + "   Launches:" + launches.toString()
                    )
                }

            }
        }
    }

}

class stat(val packageName: String, val totalTime: Long, val launches: String)
