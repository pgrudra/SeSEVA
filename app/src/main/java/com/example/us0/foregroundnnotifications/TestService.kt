package com.example.us0.foregroundnnotifications

import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.installedapps.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.*

class TestService : Service() {
    private val NOTIFICATION_ID = 1
    private val REQUEST_CODE = 0
    private val FLAGS = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("inputExtra")
        val database = AllDatabase.getInstance(application).AppDatabaseDao
        var launchables = listOf<String>()
        val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, FLAGS)
            }

        val notification = NotificationCompat.Builder(
            this,
            getString(R.string.foreground_service_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(input)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        var handler: Handler? = Looper.myLooper()?.let { Handler(it) }
        //do heavy work on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            launchables = database.getLaunchablesList()

            for (l in launchables) {
                sortedEvents[l] = mutableListOf()
            }

            val runnableCode: Runnable = object : Runnable {
                override fun run() {
                    // Do something here on the main thread
                    getStats(applicationContext, sortedEvents)
                    Log.d("Handlers", "Called on main thread")
                    handler?.postDelayed(this, 60000)

                }
            }

            handler?.post(runnableCode)

        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun getStats(context: Context, sortedEvents: Map<String, MutableList<UsageEvents.Event>>) {
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

                events.forEach {
                    if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        // App was moved to the foreground: set the start time
                        startTime = it.timeStamp
                        launches += 1
                        Log.i("kk", packageName + "   " + Timestamp(startTime))
                    } else if (it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        endTime = it.timeStamp
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
