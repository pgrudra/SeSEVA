package com.spandverse.seseva

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.data.appcategories.CategoryStat
import com.spandverse.seseva.data.stats.Stat
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
                val minute:Int=(0..2).random()
                val currentTime = Calendar.getInstance()
                val twelveOne = Calendar.getInstance()
                twelveOne.set(Calendar.HOUR_OF_DAY, 0)
                twelveOne.set(Calendar.MINUTE, minute)
                twelveOne.add(Calendar.DATE, 1)
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
        val missionDao=AllDatabase.getInstance(applicationContext).MissionsDatabaseDao
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

        val categoryTimes:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMM. & BROWSING" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO & COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val categoryLaunches:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMM. & BROWSING" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO & COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
        val timeRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"COMM. & BROWSING" to sharedPref.getInt((R.string.communication_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS, "GAMES" to sharedPref.getInt((R.string.games_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"WHITELISTED" to 0,"VIDEO & COMICS" to sharedPref.getInt((R.string.video_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"MSNBS" to sharedPref.getInt((R.string.msnbs_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"OTHERS" to sharedPref.getInt((R.string.others_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS)
        val launchRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_max_launches).toString(),0),"COMM. & BROWSING" to sharedPref.getInt((R.string.communication_max_launches).toString(),0), "GAMES" to sharedPref.getInt((R.string.games_max_launches).toString(),0),"WHITELISTED" to 0,"VIDEO & COMICS" to sharedPref.getInt((R.string.video_max_launches).toString(),0),"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_launches).toString(),0),"MSNBS" to sharedPref.getInt((R.string.msnbs_max_launches).toString(),0),"OTHERS" to sharedPref.getInt((R.string.others_max_launches).toString(),0))
        val categoryPenalties:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_penalty).toString(),0),"COMM. & BROWSING" to sharedPref.getInt((R.string.communication_penalty).toString(),0), "GAMES" to sharedPref.getInt((R.string.games_penalty).toString(),0),"WHITELISTED" to 0,"VIDEO & COMICS" to sharedPref.getInt((R.string.video_penalty).toString(),0),"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_penalty).toString(),0),"MSNBS" to sharedPref.getInt((R.string.msnbs_penalty).toString(),0),"OTHERS" to sharedPref.getInt((R.string.others_penalty).toString(),0))

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
            val timeInSeconds=(totalTime/1000).toInt()
            val app=appDao.isAppExist(packageName)
            if(app!=null) {
                val stat = Stat(packageName = packageName,appCategory = app.appCategory,appName = app.appName,timeSpent = timeInSeconds,appLaunches = launches,date = begin.timeInMillis)
                statDao.insert(stat)
                categoryTimes[app.appCategory]=categoryTimes[app.appCategory]!!+timeInSeconds
                categoryLaunches[app.appCategory]=categoryLaunches[app.appCategory]!!+launches
                categoryTimes["TOTAL"]=categoryTimes["TOTAL"]!!+timeInSeconds
                categoryLaunches["TOTAL"]=categoryLaunches["TOTAL"]!!+launches
            }
        }
        var penaltyToday=0
        val dailyReward=sharedPref.getInt((R.string.daily_reward).toString(), 0)
        var moneyToBeUpdated=sharedPref.getInt((R.string.money_to_be_updated).toString(), 0)
        val pendingMoneyToBeUpdated=moneyToBeUpdated
        moneyToBeUpdated+=dailyReward
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

        if(dayAfterInstallation>5){
            if(entertainmentTime<timeRules[key]!! && entertainmentLaunches<launchRules[key]!! )
            moneyToBeUpdated+=sharedPref.getInt((R.string.weekly_reward).toString(),0)
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
        if (categoryTimes[key]!! > timeRules[key]!! || categoryLaunches[key]!! > launchRules[key]!!) {
            violation = true
            penaltyToday += categoryPenalties[key]!!
        }
        val categoryStat=CategoryStat(categoryName = key,timeSpent = categoryTimes[key],appLaunches = categoryLaunches[key],date = begin.timeInMillis,ruleViolated = violation)
        categoryStatDao.insert(categoryStat)
    }

}
        if(penaltyToday>dailyReward){
            penaltyToday=dailyReward
        }
        moneyToBeUpdated-=penaltyToday
        with(sharedPref.edit()) {
            this?.putInt((R.string.money_to_be_updated).toString(),moneyToBeUpdated)
            this?.apply()
        }
        val choseMission=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)
        val mission=missionDao.doesMissionExist(choseMission)
        if(mission!=null){
            mission.contribution+=moneyToBeUpdated-pendingMoneyToBeUpdated
            missionDao.update(mission)
        }
        //save penalties
        //calculate money raised
    }

    companion object {
        const val ONE_MINUTE_IN_SECONDS = 60
    }
}
