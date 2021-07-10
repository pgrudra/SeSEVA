package com.spandverse.seseva.foregroundnnotifications

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.spandverse.seseva.*
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.home.HomeActivity
import kotlinx.android.synthetic.main.blocking_screen.view.*
import kotlinx.android.synthetic.main.blocking_screen.view.app_launches
import kotlinx.android.synthetic.main.blocking_screen.view.app_time
import kotlinx.android.synthetic.main.blocking_screen.view.cat_launches
import kotlinx.android.synthetic.main.blocking_screen.view.cat_time
import kotlinx.android.synthetic.main.blocking_screen.view.close_app_text
import kotlinx.android.synthetic.main.blocking_screen.view.button
import kotlinx.android.synthetic.main.blocking_screen.view.textView18
import kotlinx.android.synthetic.main.blocking_screen.view.textView44
import kotlinx.android.synthetic.main.blocking_screen_strict_mode.view.*
import kotlinx.coroutines.*
import java.util.*


class TestService : Service() {
    private val notificationId = 1
    private val requestCode = 0
    private val flagS = 0
    private val tenSeconds:Long=10000
    private val oneSecond:Long=1000
    private val oneMinuteInSeconds = 60
    private var isServiceStarted = false
    var gS:Job?=null
    private lateinit var pkgAndCat:LiveData<Map<String,String>>
    private lateinit var handler:Handler
    private lateinit var wm:WindowManager
    private lateinit var blockingScreenView: View
    private lateinit var blockingScreenViewStrict: View
    private var missionNumber:Int=0
    private var userName:String="User"
    private val cloudImagesReference = Firebase.storage
    /*private val pkgAndCat =
            Transformations.map(AllDatabase.getInstance(this).AppDatabaseDao.getEntireList()) { it ->
            it?.map { it.packageName to it.appCategory }?.toMap() ?: emptyMap<String,String>()
        }*/
    private val timeRules: HashMap<String, Int> = HashMap<String, Int>()
    private val launchRules: HashMap<String, Int> = HashMap<String, Int>()
    private val penalties: HashMap<String, Int> = HashMap<String, Int>()
    //wakeLock not implemented
    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        createUsageAlertChannel()
        startForeground(notificationId, notification)
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
            .setSmallIcon(R.drawable.ic_seseva_notification_icon)
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
                else -> startService()
            }
        }
        else{//check if allowed
            startService()
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
        //applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startService() {
        val sharedPref = applicationContext.getSharedPreferences(
            (R.string.shared_pref).toString(),
            Context.MODE_PRIVATE
        )
        handler = Handler(Looper.getMainLooper())
        wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        blockingScreenView = LayoutInflater.from(applicationContext).inflate(
            R.layout.blocking_screen,
            null
        )
        blockingScreenViewStrict = LayoutInflater.from(applicationContext).inflate(
            R.layout.blocking_screen_strict_mode,
            null
        )
        if(blockingScreenView.windowToken!=null){
            wm.removeView(blockingScreenView)
        }
        if(blockingScreenViewStrict.windowToken!=null){
            wm.removeView(blockingScreenViewStrict)
        }
        val modeChanged=sharedPref.getBoolean((R.string.mode_changed).toString(), true)
        if (isServiceStarted && !modeChanged) return
        with (sharedPref.edit()) {
            this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), false)
            this?.apply()
        }
        isServiceStarted = true
        setServiceState(this, com.spandverse.seseva.ServiceState.STARTED)

        /*val constraintNet= Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val minute:Int=(0..2).random()
        val currentTime= Calendar.getInstance()
        val twelveOne=Calendar.getInstance()
        twelveOne.set(Calendar.HOUR_OF_DAY,0)
        twelveOne.set(Calendar.MINUTE,minute)
        twelveOne.add(Calendar.DATE,1)
        val timeDiff=twelveOne.timeInMillis-currentTime.timeInMillis
        val updateStatsLocalRequest= OneTimeWorkRequestBuilder<LocalDatabaseUpdateWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("localDateBase")
            .build()
        val updateStatsCloudRequest= OneTimeWorkRequestBuilder<CloudDatabaseUpdateWorker>()
            .setConstraints(constraintNet)
            .addTag("cloudDateBase")
            .build()
        WorkManager.getInstance(applicationContext).beginUniqueWork("databaseUpdate",
            ExistingWorkPolicy.KEEP,updateStatsLocalRequest).then(updateStatsCloudRequest).enqueue()
*/


        pkgAndCat=Transformations.map(AllDatabase.getInstance(this).AppDatabaseDao.getEntireList()) { it ->
            it?.map { it.packageName to it.appCategory }?.toMap() ?: emptyMap<String,String>()
        }
        //Try CoroutineScope instead of GlobalScope
        userName=sharedPref.getString((R.string.user_name).toString(),"User")?:"User"
        val serviceMode= sharedPref.getInt((R.string.service_mode).toString(), 1)
        timeRules["SOCIAL"] =
            sharedPref.getInt((R.string.social_max_time).toString(), 0) * oneMinuteInSeconds
        timeRules["COMM. & BROWSING"] = sharedPref.getInt(
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
        timeRules["VIDEO & COMICS"] =
            sharedPref.getInt((R.string.video_max_time).toString(), 0) * oneMinuteInSeconds
        launchRules["SOCIAL"] = sharedPref.getInt((R.string.social_max_launches).toString(), 0)
        launchRules["COMM. & BROWSING"] =
            sharedPref.getInt((R.string.communication_max_launches).toString(), 0)
        launchRules["GAMES"] = sharedPref.getInt((R.string.games_max_launches).toString(), 0)
        launchRules["ENTERTAINMENT"] =
            sharedPref.getInt((R.string.entertainment_max_launches).toString(), 0)
        launchRules["OTHERS"] = sharedPref.getInt((R.string.others_max_launches).toString(), 0)
        launchRules["MSNBS"] = sharedPref.getInt((R.string.msnbs_max_launches).toString(), 0)
        launchRules["VIDEO & COMICS"] = sharedPref.getInt((R.string.video_max_launches).toString(), 0)


        penalties["SOCIAL"] = sharedPref.getInt((R.string.social_penalty).toString(), 0)
        penalties["COMM. & BROWSING"] =
            sharedPref.getInt((R.string.communication_penalty).toString(), 0)
        penalties["GAMES"] = sharedPref.getInt((R.string.games_penalty).toString(), 0)
        penalties["ENTERTAINMENT"] =
            sharedPref.getInt((R.string.entertainment_penalty).toString(), 0)
        penalties["OTHERS"] = sharedPref.getInt((R.string.others_penalty).toString(), 0)
        penalties["MSNBS"] = sharedPref.getInt((R.string.msnbs_penalty).toString(), 0)
        penalties["VIDEO & COMICS"] = sharedPref.getInt((R.string.video_penalty).toString(), 0)
        missionNumber=sharedPref.getInt((R.string.chosen_mission_number).toString(), 0)
        pkgAndCat.observeForever {

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

                val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
                for (l in it.keys) {
                    sortedEvents[l] = mutableListOf()
                }
                when (serviceMode) {
                    3 -> {
                        while (isServiceStarted) {
                            delay(oneSecond)
                            launch(Dispatchers.IO) {
                                getStatsStrict(applicationContext, sortedEvents)
                            }
                        }
                    }
                    2 -> {
                        while (isServiceStarted) {
                            delay(tenSeconds)
                            launch(Dispatchers.IO) {
                                getStatsMedium(applicationContext, sortedEvents)
                            }
                        }
                    }
                    else -> {
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
        setServiceState(this, com.spandverse.seseva.ServiceState.STOPPED)
    }

    private fun getStatsLight(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ){
        if(blockingScreenView.windowToken!=null){
            wm.removeView(blockingScreenView)
        }
        if(blockingScreenViewStrict.windowToken!=null){
            wm.removeView(blockingScreenViewStrict)
        }
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
        var lastResumeTimeStamp2: Long = 0L
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
                        if(event.packageName!=pkg){
                            lastResumeTimeStamp = event.timeStamp
                            pkg = event.packageName
                        }
                        lastResumeTimeStamp2=event.timeStamp
                    }
                }
                sortedEvents[event.packageName]?.add(event)
            }
        }
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()
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
                val handler = Handler(Looper.getMainLooper())
                if (catTimeInSeconds > maxTime || catLaunches > maxLaunches) {
                    if ((catTimeInSeconds-maxTime)%60 > 50) {
                        handler.post {
                            Toast.makeText(
                                context,
                                "Rule Broken!!\nPlease stop for your own good.",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }
                else {
                    if (catTimeInSeconds > maxTime - 12 && catTimeInSeconds <= maxTime) {
                            notificationManager.sendNotification("Less than 12 secs remaining! Quit now, don't lose the opportunity of helping some one in need.", context)
                    } else if(catTimeInSeconds > maxTime - 24 && catTimeInSeconds <= maxTime-12){
                        notificationManager.sendNotification("Less than 25 secs remaining! Quit now, don't lose the opportunity of helping some one in need.", context)
                    }
                    else if (catLaunches == maxLaunches) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000  || now.timeInMillis <= lastResumeTimeStamp2 + 10000 && blockingScreenView.time_launches_left_text.text!=context.getString(R.string.n,cat)) {
                            notificationManager.sendNotification("No more launches for this app, else you will lose opportunity of doing something noble!", context)
                            blockingScreenView.time_launches_left_text.text=context.getString(R.string.n,cat)
                        }
                    }
                    else if (catTimeInSeconds >= maxTime - 60 && catTimeInSeconds < maxTime - 48) {
                        notificationManager.sendNotification("Less than a min remaining", context)
                    }else if (catTimeInSeconds >= maxTime - 300 && catTimeInSeconds < maxTime - 288) {
                        notificationManager.sendNotification("Less than 5 mins remaining", context)
                    }
                    else if (catLaunches == maxLaunches - 1) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            notificationManager.sendNotification("Only 1 more launch remaining, use with caution", context)
                        }
                    }else if (catLaunches == maxLaunches - 5) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            notificationManager.sendNotification("Only 5 more launches remaining, use with caution", context)
                        }
                    }
                    else if (catTimeInSeconds >= maxTime / 2 && catTimeInSeconds < (maxTime / 2) + 12) {
                        handler.post {
                            Toast.makeText(context, "Half time up", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else if((now.timeInMillis-lastResumeTimeStamp)%120000 < 10000){
                        handler.post {
                            Toast.makeText(context, "${catTimeInSeconds/60} mins spent on this and other $cat apps", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

        }
    }

    private fun getStatsMedium(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {

        if(blockingScreenViewStrict.windowToken!=null){
            wm.removeView(blockingScreenViewStrict)
        }
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
        var lastResumeTimeStamp2:Long = 0L
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
                        if(event.packageName!=pkg){
                            lastResumeTimeStamp = event.timeStamp
                            pkg = event.packageName
                        }
                        lastResumeTimeStamp2=event.timeStamp
                    }
                }
                sortedEvents[event.packageName]?.add(event)
            }
        }
        val notificationManager = ContextCompat.getSystemService(
                context,
        NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()
        if (eval) {
            var appTime=0L
            var appLaunches=0
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
                    if(packageName==pkg){
                        appTime=totalTime
                        appLaunches=launches
                    }
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            if (maxTime > 0) {
                /*val handler = Handler(Looper.getMainLooper())
                val blockingScreenParams: WindowManager.LayoutParams? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE,
                            PixelFormat.OPAQUE
                        )
                    } else {
                        null
                    }
                val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager*/
                val blockingScreenParams: WindowManager.LayoutParams? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE,
                            PixelFormat.OPAQUE
                        )
                    } else {
                        null
                    }
                if (catTimeInSeconds > maxTime || catLaunches > maxLaunches) {
                    if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                        handler.post {
                            blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                            blockingScreenView.image.visibility=View.INVISIBLE
                            blockingScreenView.dont_text.text=context.getString(R.string.rule_broken)
                            blockingScreenView.close_app_text.text =context.getString(R.string.rule_limit_sentence,toHrsMin(maxTime),maxLaunches)
                            blockingScreenView.time_launches_left_text.text=context.getString(R.string.its_sad_sentence,penalties[cat])
                            blockingScreenView.textView18.text =""
                            blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help_yourself)
                            blockingScreenView.textView44.text=""
                            blockingScreenView.app_time.text = (appTime / 60000).toString()
                            blockingScreenView.app_launches.text = appLaunches.toString()
                            blockingScreenView.cat_time.text =
                                (catTimeInSeconds / oneMinuteInSeconds).toString()
                            blockingScreenView.cat_launches.text = catLaunches.toString()
                            blockingScreenView.button.text=context.getString(android.R.string.ok)
                            blockingScreenView.button.setOnClickListener {
                                wm.removeView(blockingScreenView)
                            }
                            if(blockingScreenView.windowToken!=null){
                                wm.removeView(blockingScreenView)
                            }
                            if (blockingScreenParams != null) {
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }

                    }
                    else if((catTimeInSeconds-maxTime)%50 > 40){
                        handler.post {
                            Toast.makeText(context, "Rule Broken!!\nPlease stop for your own good.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    if (catTimeInSeconds > maxTime - 12 && catTimeInSeconds <= maxTime) {
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.close_app_immediately)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t21,12)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if(blockingScreenView.windowToken!=null){
                                    wm.removeView(blockingScreenView)
                                }
                                if (blockingScreenParams != null) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }

                    } else if(catTimeInSeconds > maxTime - 24 && catTimeInSeconds <= maxTime-12){
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.close_app_soon)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t21,24)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if(blockingScreenView.windowToken!=null){
                                    wm.removeView(blockingScreenView)
                                }
                                if (blockingScreenParams != null) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }
                    }
                    else if(catTimeInSeconds > maxTime - 36 && catTimeInSeconds <= maxTime-24){
                        handler.post {
                            blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                            blockingScreenView.image.visibility=View.VISIBLE
                            blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                            blockingScreenView.close_app_text.text=context.getString(R.string.close_app_soon)
                            blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t21,36)
                            blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                            blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                            blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                            blockingScreenView.app_time.text=(appTime/60000).toString()
                            blockingScreenView.app_launches.text=appLaunches.toString()
                            blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                            blockingScreenView.cat_launches.text=catLaunches.toString()
                            blockingScreenView.button.text=context.getString(R.string.i_understand)
                            blockingScreenView.button.setOnClickListener {
                                wm.removeView(blockingScreenView)
                            }
                            val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                            Glide.with(this)
                                .load(missionImgRef)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .apply(
                                    RequestOptions()
                                        .placeholder(R.drawable.ic_imageplaceholder)
                                        .error(R.drawable.ic_imageplaceholder)
                                        .fallback(R.drawable.ic_imageplaceholder)
                                )
                                .into(blockingScreenView.image)
                            if(blockingScreenView.windowToken!=null){
                                wm.removeView(blockingScreenView)
                            }
                            if (blockingScreenParams != null) {
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                        else if (catLaunches == maxLaunches) {
                          if (now.timeInMillis <= lastResumeTimeStamp + 10000  || now.timeInMillis <= lastResumeTimeStamp2 + 10000 && blockingScreenView.time_launches_left_text.text!=context.getString(R.string.n,cat)) {
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.dont_relaunch_app)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t22)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t42)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                    blockingScreenView.time_launches_left_text.text=context.getString(R.string.n,cat)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if(blockingScreenView.windowToken!=null){
                                    wm.removeView(blockingScreenView)
                                }
                                if (blockingScreenParams != null) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }
                        }
                    } else if (catTimeInSeconds >= maxTime - 60 && catTimeInSeconds < maxTime - 48) {
                        notificationManager.sendNotification("Less than a min remaining", context)
                    }else if (catTimeInSeconds >= maxTime - 300 && catTimeInSeconds < maxTime - 288) {
                        handler.post {
                            blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                            blockingScreenView.image.visibility=View.VISIBLE
                            blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                            blockingScreenView.close_app_text.text=context.getString(R.string.close_app_soon)
                            blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t23,5)
                            blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                            blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                            blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                            blockingScreenView.app_time.text=(appTime/60000).toString()
                            blockingScreenView.app_launches.text=appLaunches.toString()
                            blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                            blockingScreenView.cat_launches.text=catLaunches.toString()
                            blockingScreenView.button.text=context.getString(R.string.i_understand)
                            blockingScreenView.button.setOnClickListener {
                                wm.removeView(blockingScreenView)
                            }
                            val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                            Glide.with(this)
                                .load(missionImgRef)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .apply(
                                    RequestOptions()
                                        .placeholder(R.drawable.ic_imageplaceholder)
                                        .error(R.drawable.ic_imageplaceholder)
                                        .fallback(R.drawable.ic_imageplaceholder)
                                )
                                .into(blockingScreenView.image)
                            if(blockingScreenView.windowToken!=null){
                                wm.removeView(blockingScreenView)
                            }
                            if (blockingScreenParams != null) {
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                    else if (catLaunches == maxLaunches - 1) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            notificationManager.sendNotification("Only 1 more launch remaining", context)
                        }
                    }else if (catLaunches == maxLaunches - 5) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 10000) {
                            notificationManager.sendNotification("Only 5 more launches remaining", context)
                        }
                    }
                    else if (catTimeInSeconds >= maxTime / 2 && catTimeInSeconds < (maxTime / 2) + 12) {
                        handler.post {
                            Toast.makeText(context, "Half time up", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else if((now.timeInMillis-lastResumeTimeStamp)%120000 < 10000){
                        handler.post {
                            Toast.makeText(context, "${catTimeInSeconds/60} mins spent on this and other $cat apps", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

        }

    }

    private fun toHrsMin(maxTime: Int): String {
        val hrs=(maxTime/3600)
        val mins=(maxTime%3600)/60
        return when (hrs) {
            0 -> {
                "$mins mins"
            }
            1 -> {
                "$hrs hr $mins mins "
            }
            else -> {
                "$hrs hrs $mins mins "
            }
        }
    }

    private fun getStatsStrict(
        context: Context,
        sortedEvents: Map<String, MutableList<UsageEvents.Event>>,
    ) {

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
        var lastResumeTimeStamp2: Long = 0L
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
                        if(event.packageName!=pkg){
                            lastResumeTimeStamp = event.timeStamp
                            pkg = event.packageName
                        }
                        lastResumeTimeStamp2=event.timeStamp
                    }
                }
                sortedEvents[event.packageName]?.add(event)
            }
        }
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()
        if (eval) {
            var appTime=0L
            var appLaunches=0
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
                    if(pkg==packageName){
                        appTime=totalTime
                        appLaunches=launches
                    }
                }
            }
            val maxTime = timeRules[cat] ?: -1
            val maxLaunches = launchRules[cat] ?: -1
            val catTimeInSeconds = (catTime / 1000).toInt()

            if (maxTime > 0) {
                val blockingScreenParams: WindowManager.LayoutParams? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            PixelFormat.OPAQUE
                        )
                    } else {
                        null
                    }
                if (catTimeInSeconds > maxTime || catLaunches > maxLaunches) {
                    handler.post {
                        blockingScreenViewStrict.user_name_s.text=context.getString(R.string.hey_user,userName)
                        blockingScreenViewStrict.close_app_text_s.text=context.getString(R.string.rule_limit_sentence,toHrsMin(maxTime),maxLaunches)
                        blockingScreenViewStrict.time_launches_left_text_s.text=context.getString(R.string.its_sad_sentence,penalties[cat])
                        blockingScreenViewStrict.app_time_s.text=(appTime/60000).toString()
                        blockingScreenViewStrict.app_launches_s.text=appLaunches.toString()
                        blockingScreenViewStrict.cat_time_s.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                        blockingScreenViewStrict.cat_launches_s.text=catLaunches.toString()
                        if(blockingScreenView.windowToken!=null){
                            wm.removeView(blockingScreenView)
                        }
                        if (blockingScreenParams != null && blockingScreenViewStrict.windowToken==null) {
                            wm.addView(blockingScreenViewStrict, blockingScreenParams)
                        }
                    }

                }
                else {
                    if (catTimeInSeconds > maxTime - 10 && catTimeInSeconds <= maxTime-8) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 9000){
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.close_app_immediately)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t21,10)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if(blockingScreenView.windowToken!=null){
                                    wm.removeView(blockingScreenView)
                                }
                                if (blockingScreenParams != null) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }
                        }
                    }
                    else if(catTimeInSeconds > maxTime - 20 && catTimeInSeconds <= maxTime-18){
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.close_app_soon)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t21,20)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if(blockingScreenView.windowToken!=null){
                                    wm.removeView(blockingScreenView)
                                }
                                if (blockingScreenParams != null ) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }

                    }
                    else if (catLaunches == maxLaunches) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 1000  || now.timeInMillis <= lastResumeTimeStamp2 + 1000 && blockingScreenView.time_launches_left_text.text!=context.getString(R.string.n,cat)) {
                            handler.post {
                                blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                                blockingScreenView.image.visibility=View.VISIBLE
                                blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                                blockingScreenView.close_app_text.text=context.getString(R.string.dont_relaunch_app)
                                blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t22)
                                blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                                blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                                blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t42)
                                blockingScreenView.app_time.text=(appTime/60000).toString()
                                blockingScreenView.app_launches.text=appLaunches.toString()
                                blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                                blockingScreenView.cat_launches.text=catLaunches.toString()
                                blockingScreenView.button.text=context.getString(R.string.i_understand)
                                blockingScreenView.button.setOnClickListener {
                                    wm.removeView(blockingScreenView)
                                    blockingScreenView.time_launches_left_text.text=context.getString(R.string.n,cat)
                                }
                                val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                                Glide.with(this)
                                    .load(missionImgRef)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.ic_imageplaceholder)
                                            .error(R.drawable.ic_imageplaceholder)
                                            .fallback(R.drawable.ic_imageplaceholder)
                                    )
                                    .into(blockingScreenView.image)
                                if (blockingScreenParams != null && blockingScreenView.windowToken==null) {
                                    wm.addView(blockingScreenView, blockingScreenParams)
                                }
                            }
                        }
                    } else if (catTimeInSeconds >= maxTime - 60 && catTimeInSeconds < maxTime - 58) {
                        notificationManager.sendNotification("Less than a min remaining", context)
                    }else if (catTimeInSeconds >= maxTime - 300 && catTimeInSeconds < maxTime - 298) {
                        handler.post {
                            blockingScreenView.user_name.text=context.getString(R.string.hey_user,userName)
                            blockingScreenView.image.visibility=View.VISIBLE
                            blockingScreenView.dont_text.text=context.getString(R.string.don_t_break_the_rule)
                            blockingScreenView.close_app_text.text=context.getString(R.string.close_app_soon)
                            blockingScreenView.time_launches_left_text.text=context.getString(R.string.blocking_screen_t23,5)
                            blockingScreenView.textView18.text=context.getString(R.string.blocking_screen_t3,penalties[cat])
                            blockingScreenView.textView43.text =context.getString(R.string.you_can_still_help)
                            blockingScreenView.textView44.text=context.getString(R.string.blocking_screen_t41)
                            blockingScreenView.app_time.text=(appTime/60000).toString()
                            blockingScreenView.app_launches.text=appLaunches.toString()
                            blockingScreenView.cat_time.text=(catTimeInSeconds/oneMinuteInSeconds).toString()
                            blockingScreenView.cat_launches.text=catLaunches.toString()
                            blockingScreenView.button.text=context.getString(R.string.i_understand)
                            blockingScreenView.button.setOnClickListener {
                                wm.removeView(blockingScreenView)
                            }
                            val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionNamedImages/mission${missionNumber}NamedImage.jpg")
                            Glide.with(this)
                                .load(missionImgRef)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .apply(
                                    RequestOptions()
                                        .placeholder(R.drawable.ic_imageplaceholder)
                                        .error(R.drawable.ic_imageplaceholder)
                                        .fallback(R.drawable.ic_imageplaceholder)
                                )
                                .into(blockingScreenView.image)
                            if(blockingScreenView.windowToken!=null){
                                wm.removeView(blockingScreenView)
                            }
                            if (blockingScreenParams != null) {
                                wm.addView(blockingScreenView, blockingScreenParams)
                            }
                        }
                    }
                    else if (catLaunches == maxLaunches - 1) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 1500) {
                            notificationManager.sendNotification("Only 1 more launch remaining", context)
                        }
                    }else if (catLaunches == maxLaunches - 5) {
                        if (now.timeInMillis <= lastResumeTimeStamp + 1500) {
                            notificationManager.sendNotification("Only 5 more launches remaining", context)
                        }
                    }
                    else if (catTimeInSeconds >= maxTime / 2 && catTimeInSeconds < (maxTime / 2) + 1) {
                        handler.post {
                            Toast.makeText(context, "Half time up", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else if((now.timeInMillis - lastResumeTimeStamp)%120000 < 1500){
                        handler.post {
                            Toast.makeText(context, "${catTimeInSeconds/60} mins spent on this and other $cat apps", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

        }
        else{
            if(blockingScreenView.windowToken!=null){
                wm.removeView(blockingScreenView)
            }
            if(blockingScreenViewStrict.windowToken!=null){
                wm.removeView(blockingScreenViewStrict)
            }
        }
    }

}

