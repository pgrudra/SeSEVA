package com.example.us0.foregroundnnotifications

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.us0.*
import com.example.us0.data.AllDatabase
import com.example.us0.home.HomeActivity
import kotlinx.android.synthetic.main.blocking_screen.view.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.sql.Timestamp
import java.util.*


class TestService : Service() {
    private val NOTIFICATION_ID = 1
    private val REQUEST_CODE = 0
    private val FLAGS = 0
    private val ONE_MINUTE_IN_SECONDS = 60
    private var isServiceStarted = false
    var gS:Job?=null
    private val pkgAndCat =
            Transformations.map(AllDatabase.getInstance(this).AppDatabaseDao.getEntireList()) { it ->
            it?.map { it.packageName to it.appCategory }?.toMap() ?: emptyMap<String,String>()
        }
    private val timeRules: HashMap<String, Int> = HashMap<String, Int>()
    private val launchRules: HashMap<String, Int> = HashMap<String, Int>()

    //wakeLock not implemented
    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        createUsageAlertChannel()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createUsageAlertChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val usageAlertNotificationChannel = NotificationChannel(
                getString(R.string.usage_alert_notification_channel_id),
                getString(R.string.usage_alert_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            usageAlertNotificationChannel.enableVibration(true)
            usageAlertNotificationChannel.description =
                getString(R.string.usage_alert_notification_channel_description)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(usageAlertNotificationChannel)
        }
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
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> Timber.i("never_happens")
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, TestService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setServiceState(this, com.example.us0.ServiceState.STARTED)
        //Try CoroutineScope instead of GlobalScope
        val sharedPref = applicationContext.getSharedPreferences(
            (R.string.shared_pref).toString(),
            Context.MODE_PRIVATE
        )
        timeRules["SOCIAL"] =
            sharedPref.getInt((R.string.social_max_time).toString(), 0) * ONE_MINUTE_IN_SECONDS
        timeRules["COMMUNICATION"] = sharedPref.getInt(
            (R.string.communication_max_time).toString(),
            0
        ) * ONE_MINUTE_IN_SECONDS
        timeRules["GAMES"] =
            sharedPref.getInt((R.string.games_max_time).toString(), 0) * ONE_MINUTE_IN_SECONDS
        timeRules["ENTERTAINMENT"] = sharedPref.getInt(
            (R.string.entertainment_max_time).toString(),
            0
        ) * ONE_MINUTE_IN_SECONDS
        timeRules["OTHERS"] =
            sharedPref.getInt((R.string.others_max_time).toString(), 0) * ONE_MINUTE_IN_SECONDS
        timeRules["MSNBS"] =
            sharedPref.getInt((R.string.msnbs_max_time).toString(), 0) * ONE_MINUTE_IN_SECONDS
        timeRules["VIDEO"] =
            sharedPref.getInt((R.string.video_max_time).toString(), 0) * ONE_MINUTE_IN_SECONDS
        launchRules["SOCIAL"] = sharedPref.getInt((R.string.social_max_launches).toString(), 0)
        launchRules["COMMUNICATION"] =
            sharedPref.getInt((R.string.communication_max_launches).toString(), 0)
        launchRules["GAMES"] = sharedPref.getInt((R.string.games_max_launches).toString(), 0)
        launchRules["ENTERTAINMENT"] =
            sharedPref.getInt((R.string.entertainment_max_launches).toString(), 0)
        launchRules["OTHERS"] = sharedPref.getInt((R.string.others_max_launches).toString(), 0)
        launchRules["MSNBS"] = sharedPref.getInt((R.string.msnbs_max_launches).toString(), 0)
        launchRules["VIDEO"] = sharedPref.getInt((R.string.video_max_launches).toString(), 0)
        pkgAndCat.observeForever {

                if(gS?.isActive == true){
                    gS?.cancel()
                }

            gS=GlobalScope.launch(Dispatchers.IO) {
                val main = Intent(Intent.ACTION_MAIN, null)
                main.addCategory(Intent.CATEGORY_LAUNCHER)
                val pm = requireNotNull(applicationContext.packageManager)
                val launchables = pm.queryIntentActivities(main, 0)
                val appPackageListR = ArrayList<String>()
                for (item in launchables) {
                    try {
                        val nameOfPackage: String = item.activityInfo.packageName
                        appPackageListR.add(nameOfPackage)
                    } catch (e: Exception) {
                    }
                }
                val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
                val appPackageList = appPackageListR.distinct()
                for (l in appPackageList) {
                    sortedEvents[l] = mutableListOf()
                }

                while (isServiceStarted) {
                    delay(10000)
                    launch(Dispatchers.IO) {
                        getStats(applicationContext, sortedEvents)
                    }


                    Log.i("TSC", "POPO")
                }

            }
        }
    }

    private fun stopService() {
        try {
            stopForeground(true)
            stopSelf()
            pkgAndCat.removeObserver { }
        } catch (e: Exception) {

        }
        isServiceStarted = false
        setServiceState(this, com.example.us0.ServiceState.STOPPED)
    }

    private fun getStats(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {

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
        var pkg: String? = null
        var lastResumeTimeStamp: Long = 0L
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
                        pkg = event.packageName
                        lastResumeTimeStamp = event.timeStamp
                    }
                }
                sortedEvents[event.packageName]?.add(event)
            }
        }
        if (eval) {
            var catTime = 0L
            var catLaunches = 0
            val cat = pkgAndCat.value?.get(pkg) ?: ""
            sortedEvents.forEach { (packageName, events) ->
                // Keep track of the current start and end times
                val cat1 = pkgAndCat.value?.get(packageName) ?: "."
                if (cat1 == cat) {
                    var startTime = 0L
                    var endTime = 0L
                    var launches = 0
                    var totalTime = 0L
                    var transt = 0L
                    events.forEach {
                        if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            // App was moved to the foreground: set the start time
                            startTime = it.timeStamp
                            launches += 1


                        } else if (it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                            if (startTime != 0L || totalTime == 0L) {
                                endTime = it.timeStamp
                                transt = it.timeStamp
                            }
                        }
                        if (startTime != 0L && transt != 0L && startTime > transt && (startTime - transt) < 50) {
                            launches -= 1
                            transt = 0L
                        }
                        if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED || it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {

                            Log.i(
                                "DWN",
                                it.eventType.toString() + " " + Timestamp(it.timeStamp)
                            )

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
                    Log.i("DWN", "${totalTime / 60000}")
                    catTime += totalTime
                    catLaunches += launches
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            Log.i("MXT", "$catLaunches")
            if (maxTime > 0) {
                if (catTimeInSeconds >= maxTime || catLaunches > maxLaunches) {
                    /*if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(context, "Rule Broken!!", Toast.LENGTH_LONG).show()
                        }
                    }*/
                    if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {

                            val blockingScreenParams: WindowManager.LayoutParams? =
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                                        PixelFormat.TRANSLUCENT
                                    )
                                } else {
                                    null
                                }
                            val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                            val blockingScreenView = LayoutInflater.from(context).inflate(
                                R.layout.blocking_screen,
                                null
                            )

                            blockingScreenView.i_understand.setOnClickListener {
                                wm.removeView(blockingScreenView)
                            }
                            blockingScreenView.text.text = "sdfgd"
                            if (blockingScreenParams != null) {
                                Log.i("TS87", "qqw")
                                wm.addView(blockingScreenView, blockingScreenParams)
                                Log.i("TS87", "dvfsd")
                            }
                        }
                    }
                } else {
                    Log.i("TS", "sww")
                    if (catTimeInSeconds >= maxTime - 25 && catTimeInSeconds < maxTime - 13) {

                        val notificationManager = ContextCompat.getSystemService(
                            context,
                            NotificationManager::class.java
                        ) as NotificationManager
                        notificationManager.cancelNotifications()
                        notificationManager.sendNotification(
                            "Less than 20 seconds remaining",
                            context
                        )
                    } else if (catLaunches == maxLaunches) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            val notificationManager = ContextCompat.getSystemService(
                                context,
                                NotificationManager::class.java
                            ) as NotificationManager
                            notificationManager.cancelNotifications()
                            notificationManager.sendNotification(
                                "Only 1 more launch remaining",
                                context
                            )
                        }
                    } else if (catTimeInSeconds >= maxTime - 60 && catTimeInSeconds < maxTime - 48) {
                        val notificationManager = ContextCompat.getSystemService(
                            context,
                            NotificationManager::class.java
                        ) as NotificationManager
                        notificationManager.cancelNotifications()
                        notificationManager.sendNotification("Less than a min remaining", context)
                    } else if (catLaunches == maxLaunches - 1) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            val notificationManager = ContextCompat.getSystemService(
                                context,
                                NotificationManager::class.java
                            ) as NotificationManager
                            notificationManager.cancelNotifications()
                            notificationManager.sendNotification(
                                "Only 2 more launch remaining",
                                context
                            )
                        }
                    } else if (catTimeInSeconds >= maxTime / 2 && catTimeInSeconds < (maxTime / 2) + 12) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(context, "Half time up", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else if(catTimeInSeconds >= maxTime - 13 && catTimeInSeconds < maxTime - 0){
                        /*val blockingScreenView=LayoutInflater.from(context).inflate(R.layout.blocking_screen,null)
                        blockingScreenView.i_understand.setOnClickListener {
                            wm.removeView(blockingScreenView) }
                        blockingScreenView.text.text="sdfgd"
                        wm.addView(blockingScreenView, blockingScreenParams)*/
                    }
                }

            }

        } else {
            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.cancelNotifications()
        }

}

}

