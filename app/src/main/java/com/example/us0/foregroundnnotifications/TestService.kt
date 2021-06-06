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
    private val notificationId = 1
    private val requestCode = 0
    private val flagS = 0
    private val tenSeconds:Long=10000
    private val threeSeconds:Long=3000
    private val oneMinuteInSeconds = 60
    private var isServiceStarted = false
    var gS:Job?=null
    private lateinit var pkgAndCat:LiveData<Map<String,String>>
    /*private val pkgAndCat =
            Transformations.map(AllDatabase.getInstance(this).AppDatabaseDao.getEntireList()) { it ->
            it?.map { it.packageName to it.appCategory }?.toMap() ?: emptyMap<String,String>()
        }*/
    private val timeRules: HashMap<String, Int> = HashMap<String, Int>()
    private val launchRules: HashMap<String, Int> = HashMap<String, Int>()

    //wakeLock not implemented
    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        createUsageAlertChannel()
        startForeground(notificationId, notification)
        Log.i("TSS","4")
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
                PendingIntent.getActivity(this, requestCode, notificationIntent, flagS)
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
        Log.i("TS","2")
        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> startService()
            }
        }
        else{//check if allowed
            Log.i("TS","1")
            startService()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, TestService::class.java).also {
            it.setPackage(packageName)
        }
        Log.i("TS","3")
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        //applicationContext.getSystemService(Context.ALARM_SERVICE)
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
        Log.i("TSS","destroyed")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startService() {
        Log.i("TSS","21")
        val sharedPref = applicationContext.getSharedPreferences(
            (R.string.shared_pref).toString(),
            Context.MODE_PRIVATE
        )
        val modeChanged=sharedPref.getBoolean((R.string.mode_changed).toString(), true)
        if (isServiceStarted && !modeChanged) return
        with (sharedPref.edit()) {
            this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), false)
            this?.apply()
        }
        isServiceStarted = true
        Log.i("TSS","22")
        setServiceState(this, com.example.us0.ServiceState.STARTED)
        pkgAndCat=Transformations.map(AllDatabase.getInstance(this).AppDatabaseDao.getEntireList()) { it ->
            it?.map { it.packageName to it.appCategory }?.toMap() ?: emptyMap<String,String>()
        }
        //Try CoroutineScope instead of GlobalScope

        val serviceMode= sharedPref.getInt((R.string.service_mode).toString(), 1)
        timeRules["SOCIAL"] =
            sharedPref.getInt((R.string.social_max_time).toString(), 0) * oneMinuteInSeconds
        timeRules["COMMUNICATION"] = sharedPref.getInt(
            (R.string.communication_max_time).toString(),
            0
        ) * oneMinuteInSeconds
        timeRules["GAMES"] =
            sharedPref.getInt((R.string.games_max_time).toString(), 0) * oneMinuteInSeconds
        timeRules["ENTERTAINMENT"] = sharedPref.getInt(
            (R.string.entertainment_max_time).toString(),
            0
        ) * oneMinuteInSeconds
        timeRules["OTHERS"] =
            sharedPref.getInt((R.string.others_max_time).toString(), 0) * oneMinuteInSeconds
        timeRules["MSNBS"] =
            sharedPref.getInt((R.string.msnbs_max_time).toString(), 0) * oneMinuteInSeconds
        timeRules["VIDEO"] =
            sharedPref.getInt((R.string.video_max_time).toString(), 0) * oneMinuteInSeconds
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

            Log.i("TS","24")
                if(gS?.isActive == true){
                    gS?.cancel()
                }
            gS=GlobalScope.launch(Dispatchers.IO) {
               /* val main = Intent(Intent.ACTION_MAIN, null)
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
                val appPackageList = appPackageListR.distinct()*/

                Log.i("TS78","25")
                val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
                for (l in it.keys) {
                    sortedEvents[l] = mutableListOf()
                }
                when (serviceMode) {
                    3 -> {
                        Log.i("TS78","3")
                        while (isServiceStarted) {
                            delay(threeSeconds)
                            launch(Dispatchers.IO) {
                                getStatsStrict(applicationContext, sortedEvents)
                            }
                        }
                    }
                    2 -> {
                        Log.i("TS78","2")
                        while (isServiceStarted) {
                            delay(tenSeconds)
                            launch(Dispatchers.IO) {
                                getStatsMedium(applicationContext, sortedEvents)
                            }
                        }
                    }
                    else -> {
                        Log.i("TS78","else")
                        while (isServiceStarted) {
                            delay(tenSeconds)
                            launch(Dispatchers.IO) {
                                getStatsLight(applicationContext, sortedEvents)
                            }
                        }
                    }
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

    private fun getStatsLight(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {
        Log.i("TS","27")
        sortedEvents.forEach { (_, events) -> events.clear() }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = Calendar.getInstance()
        val begin: Calendar = Calendar.getInstance()
        begin.set(Calendar.HOUR_OF_DAY, 0)
        begin.set(Calendar.MINUTE, 0)
        begin.set(Calendar.SECOND, 0)
        begin.set(Calendar.MILLISECOND, 0)

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
            Log.i("TS","28")
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
                    Log.i("TS","29")
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
                    catTime += totalTime
                    catLaunches += launches
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            if (maxTime > 0) {
                Log.i("TS","30")
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
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                } else {
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

    private fun getStatsMedium(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {
        Log.i("TS","27")
        sortedEvents.forEach { (_, events) -> events.clear() }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = Calendar.getInstance()
        val begin: Calendar = Calendar.getInstance()
        begin.set(Calendar.HOUR_OF_DAY, 0)
        begin.set(Calendar.MINUTE, 0)
        begin.set(Calendar.SECOND, 0)
        begin.set(Calendar.MILLISECOND, 0)

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
            Log.i("TS","28")
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
                    Log.i("TS","29")
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
                    catTime += totalTime
                    catLaunches += launches
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            if (maxTime > 0) {
                Log.i("TS","30")
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
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                } else {
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

    private fun getStatsStrict(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {
        Log.i("TS","27")
        sortedEvents.forEach { (_, events) -> events.clear() }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = Calendar.getInstance()
        val begin: Calendar = Calendar.getInstance()
        begin.set(Calendar.HOUR_OF_DAY, 0)
        begin.set(Calendar.MINUTE, 0)
        begin.set(Calendar.SECOND, 0)
        begin.set(Calendar.MILLISECOND, 0)

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
            Log.i("TS","28")
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
                    Log.i("TS","29")
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
                    catTime += totalTime
                    catLaunches += launches
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            if (maxTime > 0) {
                Log.i("TS","30")
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
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                } else {
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

