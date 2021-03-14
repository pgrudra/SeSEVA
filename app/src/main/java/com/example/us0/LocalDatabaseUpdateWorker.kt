package com.example.us0

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.example.us0.data.AllDatabase
import com.example.us0.data.appcategories.CategoryStat
import com.example.us0.data.stats.Stat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class LocalDatabaseUpdateWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
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
                launch(Dispatchers.IO){
                    saveStats(applicationContext,sortedEvents)
                }
                val constraintNet= Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val updateStatsCloudRequest= OneTimeWorkRequestBuilder<CloudDatabaseUpdateWorker>()
                    .setConstraints(constraintNet)
                    .addTag("cloudDateBase")
                    .build()
                val currentTime = Calendar.getInstance()
                val twelveOne = Calendar.getInstance()
                twelveOne.set(Calendar.HOUR_OF_DAY, 0)
                twelveOne.set(Calendar.MINUTE, 1)
                twelveOne.add(Calendar.DATE, 1)
                Log.i("LDUWT", Timestamp(twelveOne.timeInMillis).toString())
                Log.i("LDUWT", Timestamp(currentTime.timeInMillis).toString())
                val timeDiff = twelveOne.timeInMillis - currentTime.timeInMillis
                val updateStatsLocalRequest= OneTimeWorkRequestBuilder<LocalDatabaseUpdateWorker>()
                    .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                    .addTag("localDateBase")
                    .build()

                WorkManager.getInstance(applicationContext).beginUniqueWork("databaseUpdate",
                    ExistingWorkPolicy.APPEND,updateStatsLocalRequest).then(updateStatsCloudRequest).enqueue()

                Result.success()
            }
            catch (e:Exception){
                Result.failure()
            }
        }
    }

    private suspend fun saveStats(applicationContext: Context, sortedEvents: MutableMap<String, MutableList<UsageEvents.Event>>) {

        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val appDao = AllDatabase.getInstance(applicationContext).AppDatabaseDao
        val statDao=AllDatabase.getInstance(applicationContext).StatDataBaseDao
        val categoryStatDao=AllDatabase.getInstance(applicationContext).CategoryStatDatabaseDao
        val sharedPref = applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val end: Calendar = Calendar.getInstance()
        end.set(Calendar.HOUR_OF_DAY, 0)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.set(Calendar.MILLISECOND, 0)
        val begin: Calendar = Calendar.getInstance()
        begin.set(Calendar.HOUR_OF_DAY, 0)
        begin.set(Calendar.MINUTE, 0)
        begin.set(Calendar.SECOND, 0)
        begin.set(Calendar.MILLISECOND, 0)
        begin.add(Calendar.DATE,-1)
        Log.i("LDUWT", "beginTime="+Timestamp(begin.timeInMillis).toString())
        Log.i("LDUWT", "EndTime= "+Timestamp(end.timeInMillis).toString())
        val systemEvents: UsageEvents = usm.queryEvents(
            begin.timeInMillis,
            end.timeInMillis
        )
        while (systemEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            systemEvents.getNextEvent(event)
            val type = event.eventType
            if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                sortedEvents[event.packageName]?.add(event)
            }
        }
        val categoryTimes:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMMUNICATION" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val categoryLaunches:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMMUNICATION" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val timeRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMMUNICATION" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val launchRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMMUNICATION" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val categoryPenalties:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 2,"COMMUNICATION" to 2, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 2,"ENTERTAINMENT" to 2,"MSNBS" to 2,"OTHERS" to 2)

        sortedEvents.forEach { (packageName, events) ->
            // Keep track of the current start and end times
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

                    if (packageName == "com.google.android.youtube") {
                        Log.i("DWN", it.eventType.toString() + " " + Timestamp(it.timeStamp))
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
                }
            }
            if (startTime != 0L && endTime == 0L) {
                totalTime += end.timeInMillis - startTime
            }
            val timeInInt=(totalTime/60000).toInt()
            val app=appDao.isAppExist(packageName)
            if(app!=null) {
                val stat = Stat(packageName = packageName,appCategory = app.appCategory,appName = app.appName,timeSpent = timeInInt,appLaunches = launches,date = begin.timeInMillis)
                statDao.insert(stat)
                categoryTimes[app.appCategory]=categoryTimes[app.appCategory]!!+timeInInt
                categoryLaunches[app.appCategory]=categoryLaunches[app.appCategory]!!+launches
                categoryTimes["TOTAL"]=categoryTimes["TOTAL"]!!+timeInInt
                categoryLaunches["TOTAL"]=categoryLaunches["TOTAL"]!!+launches
                Log.i("LDUWT", "$timeInInt"+"${app.appName}")
            }
        }
        var penaltyToday=0
        var moneyRaisedToday=60
for(key in categoryTimes.keys){

    if(key=="ENTERTAINMENT"){
        var violatedToday=false
        var violatedAlready=false
        val dayAfterInstallation= sharedPref.getInt((R.string.days_after_installation).toString(), 0)
        var entertainmentTime=sharedPref.getInt((R.string.entertainment_time).toString(), 0)
        var entertainmentLaunches=sharedPref.getInt((R.string.entertainment_launches).toString(), 0)
        if(entertainmentTime>=timeRules[key]!! || entertainmentLaunches>=launchRules[key]!!){
            violatedAlready=true
        }
        entertainmentLaunches+=categoryLaunches[key]!!
        entertainmentTime+=categoryTimes[key]!!

        if(entertainmentTime>=timeRules[key]!! || entertainmentLaunches>=launchRules[key]!!){
            if(!violatedAlready){
                violatedToday=true
            }
        }

        if(dayAfterInstallation>6){
            if(entertainmentTime<timeRules[key]!! && entertainmentLaunches<launchRules[key]!! )
            moneyRaisedToday+=4
            with(sharedPref.edit()) {
                this?.putInt((R.string.days_after_installation).toString(), 0)
                this?.apply()
            }
            with(sharedPref.edit()) {
                this?.putInt((R.string.entertainment_time).toString(), 0)
                this?.apply()
            }
            with(sharedPref.edit()) {
                this?.putInt((R.string.entertainment_launches).toString(), 0)
                this?.apply()
            }
        }
        else{
            with(sharedPref.edit()) {
                this?.putInt((R.string.days_after_installation).toString(), dayAfterInstallation+1)
                this?.apply()
            }
            with(sharedPref.edit()) {
                this?.putInt((R.string.entertainment_time).toString(), entertainmentTime)
                this?.apply()
            }
            with(sharedPref.edit()) {
                this?.putInt((R.string.entertainment_launches).toString(), entertainmentLaunches)
                this?.apply()
            }
        }
        val categoryStat=CategoryStat(categoryName = key,timeSpent = categoryTimes[key],appLaunches = categoryLaunches[key],date = begin.timeInMillis,ruleViolated = violatedToday)
        categoryStatDao.insert(categoryStat)
    }
    else {
        var violation:Boolean?=null
        if (categoryTimes[key]!! >= timeRules[key]!! || categoryLaunches[key]!! >= launchRules[key]!!) {
            violation = true
            penaltyToday += categoryPenalties[key]!!
        }
        val categoryStat=CategoryStat(categoryName = key,timeSpent = categoryTimes[key],appLaunches = categoryLaunches[key],date = begin.timeInMillis,ruleViolated = violation)
        categoryStatDao.insert(categoryStat)
    }

}
        moneyRaisedToday-=penaltyToday
        with(sharedPref.edit()) {
            this?.putInt((R.string.money_raised_today).toString(), moneyRaisedToday)
            this?.apply()
        }
        Log.i("LDUWT", "MONEY_RAISED=$moneyRaisedToday Penalty=$penaltyToday")
        //save penalties
        //calculate money raised
    }
}