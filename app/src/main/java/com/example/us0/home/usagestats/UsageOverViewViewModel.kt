package com.example.us0.home.usagestats

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.us0.AppsCategoryType
import com.example.us0.CategoryRuleStatus
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.data.appcategories.AppsCategory
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.data.stats.Stat
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class UsageOverViewViewModel(application: Application) : AndroidViewModel(application) {
    private val appDatabaseDao = AllDatabase.getInstance(application).AppDatabaseDao
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val _navigateToSelectedCategory=MutableLiveData<AppsCategory?>()
    val navigateToSelectedCategory:LiveData<AppsCategory?>
        get()=_navigateToSelectedCategory
    private val _navigateToSelectedApp=MutableLiveData<Boolean>()
    val navigateToSelectedApp:LiveData<Boolean>
        get()=_navigateToSelectedApp
    private val _listOfCats=MutableLiveData<List<AppsCategory>>()
    val listOfCats:LiveData<List<AppsCategory>>
        get()=_listOfCats
    private val _screenHeading = MutableLiveData<String>()
    val screenHeading: LiveData<String>
        get() = _screenHeading
    private val _totalTimeSpent = MutableLiveData<String>()
    val totalTimeSpent: LiveData<String>
        get() = _totalTimeSpent
    private val _totalAppLaunches = MutableLiveData<String>()
    val totalAppLaunches: LiveData<String>
        get() = _totalAppLaunches
    private val _totalTimeSpentCatwise = MutableLiveData<String>()
    val totalTimeSpentCatwise: LiveData<String>
        get() = _totalTimeSpentCatwise
    private val _totalAppLaunchesCatwise = MutableLiveData<String>()
    val totalAppLaunchesCatwise: LiveData<String>
        get() = _totalAppLaunchesCatwise
    private val _appTimeSpent = MutableLiveData<String>()
    val appTimeSpent: LiveData<String>
        get() = _appTimeSpent
    private val _appLaunches = MutableLiveData<String>()
    val appLaunches: LiveData<String>
        get() = _appLaunches
    private val _mostUsedAppName = MutableLiveData<String>()
    val mostUsedAppName: LiveData<String>
        get() = _mostUsedAppName
    private val _catNameForCatScreen = MutableLiveData<String>()
    val catNameForCatScreen: LiveData<String>
        get() = _catNameForCatScreen
    private val _catNameForAppScreen = MutableLiveData<String>()
    val catNameForAppScreen: LiveData<String>
        get() = _catNameForAppScreen
    private val _appNameForAppScreen = MutableLiveData<String>()
    val appNameForAppScreen: LiveData<String>
        get() = _appNameForAppScreen
    private val _appPackageNameForAppScreen = MutableLiveData<String>()
    val appPackageNameForAppScreen: LiveData<String>
        get() = _appPackageNameForAppScreen
    private val _mostUsedAppCatwiseName = MutableLiveData<String>()
    val mostUsedAppCatwiseName: LiveData<String>
        get() = _mostUsedAppCatwiseName
    private val _mostLaunchedAppName = MutableLiveData<String>()
    val mostLaunchedAppName: LiveData<String>
        get() = _mostLaunchedAppName
    private val _mostLaunchedAppCatwiseName = MutableLiveData<String>()
    val mostLaunchedAppCatwiseName: LiveData<String>
        get() = _mostLaunchedAppCatwiseName
    private val _appsInCatList=MutableLiveData<List<Stat>>()
    val appsInCatList:LiveData<List<Stat>>
        get() = _appsInCatList
    private val _appsListEmptyOrApplications = MutableLiveData<String>()
    val appsListEmptyOrApplications: LiveData<String>
        get() = _appsListEmptyOrApplications
    private val _totalTimeSpentPieChartVisible=MutableLiveData<Boolean>()
    val totalTimeSpentPieChartVisible:LiveData<Boolean>
        get()=_totalTimeSpentPieChartVisible
    private val _totalAppLaunchesPieChartVisible=MutableLiveData<Boolean>()
    val totalAppLaunchesPieChartVisible:LiveData<Boolean>
        get()=_totalAppLaunchesPieChartVisible
    private val _catTimeSpentPieChartVisible=MutableLiveData<Boolean>()
    val catTimeSpentPieChartVisible:LiveData<Boolean>
        get()=_catTimeSpentPieChartVisible
    private val _catAppLaunchesPieChartVisible=MutableLiveData<Boolean>()
    val catAppLaunchesPieChartVisible:LiveData<Boolean>
        get()=_catAppLaunchesPieChartVisible

    private val _processingDataForPieChartDone=MutableLiveData<Boolean>()
    val processingDataForPieChartDone:LiveData<Boolean>
        get()=_processingDataForPieChartDone

    val categoryTimes:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"OTHERS" to 0,"WHITELISTED" to 0, "GAMES" to 0,"MSNBS" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"COMMUNICATION" to 0,"SOCIAL" to 0)
    val categoryLaunches:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to 0,"COMMUNICATION" to 0, "GAMES" to 0,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to 0,"ENTERTAINMENT" to 0,"MSNBS" to 0,"OTHERS" to 0)
    val timeRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref!!.getInt((R.string.social_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"COMMUNICATION" to sharedPref.getInt((R.string.communication_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS, "GAMES" to sharedPref.getInt((R.string.games_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to sharedPref.getInt((R.string.video_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"MSNBS" to sharedPref.getInt((R.string.msnbs_max_time).toString(),0)*ONE_MINUTE_IN_SECONDS,"OTHERS" to sharedPref.getInt((R.string.others_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS)
    val launchRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref!!.getInt((R.string.social_max_launches).toString(),0),"COMMUNICATION" to sharedPref.getInt((R.string.communication_max_launches).toString(),0), "GAMES" to sharedPref.getInt((R.string.games_max_launches).toString(),0),"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to sharedPref.getInt((R.string.video_max_launches).toString(),0),"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_launches).toString(),0),"MSNBS" to sharedPref.getInt((R.string.msnbs_max_launches).toString(),0),"OTHERS" to sharedPref.getInt((R.string.others_max_launches).toString(),0))
    var entertainmentTime=sharedPref!!.getInt((R.string.entertainment_time).toString(), 0)
    var entertainmentLaunches=sharedPref!!.getInt((R.string.entertainment_launches).toString(), 0)

    private val appStats:MutableList<Stat> = arrayListOf()
    fun toCatUsageScreen(appsCategory: AppsCategory) {
        val cat=appsCategory.categoryName
        if(cat=="ENTERTAINMENT"){
            _screenHeading.value="This week's statistics"
        }
        else{
            _screenHeading.value="Today's statistics"
        }
        _catNameForCatScreen.value=cat
        val list=appStats.filter { it.appCategory==cat }
        if(list.isNotEmpty()){
        _mostLaunchedAppCatwiseName.value=list.maxByOrNull { it.appLaunches!! }!!.appName!!
        _appsInCatList.value=list.sortedBy {it.timeSpent }.reversed()
        _mostUsedAppCatwiseName.value=list.maxByOrNull { it.timeSpent!! }!!.appName!!
        _totalAppLaunchesCatwise.value=categoryLaunches[cat].toString()
        _totalTimeSpentCatwise.value=inHrsMins(categoryTimes[cat]!!)
        _appsListEmptyOrApplications.value="Applications"}
        else{
            _mostLaunchedAppCatwiseName.value="---"
            _appsInCatList.value= emptyList()
            _mostUsedAppCatwiseName.value="---"
            _totalAppLaunchesCatwise.value="---"
            _totalTimeSpentCatwise.value="---"
            _appsListEmptyOrApplications.value="You have no apps belonging to this category"
        }
        _navigateToSelectedCategory.value=appsCategory
        //work on this
    }
    fun toAppUsageScreen(stat: Stat) {
        _appNameForAppScreen.value=stat.appName!!
        _catNameForAppScreen.value=stat.appCategory!!
        _appTimeSpent.value=inHrsMins(stat.timeSpent!!)
        _appLaunches.value=stat.appLaunches.toString()
        _appPackageNameForAppScreen.value=stat.packageName!!
        _navigateToSelectedApp.value=true
    }
    fun goToMaxUsedAppUsageFragment(){
        val stat=appStats.maxByOrNull { it.timeSpent!! }
        _appNameForAppScreen.value=stat?.appName!!
        _catNameForAppScreen.value=stat.appCategory!!
        _appTimeSpent.value=inHrsMins(stat.timeSpent!!)
        _appLaunches.value=stat.appLaunches.toString()
        _appPackageNameForAppScreen.value=stat.packageName!!
        _navigateToSelectedApp.value=true
    }
    fun goToMaxLaunchedAppUsageFragment(){
        val stat=appStats.maxByOrNull { it.appLaunches!! }
        _appNameForAppScreen.value=stat?.appName!!
        _catNameForAppScreen.value=stat.appCategory!!
        _appTimeSpent.value=inHrsMins(stat.timeSpent!!)
        _appLaunches.value=stat.appLaunches.toString()
        _appPackageNameForAppScreen.value=stat.packageName!!
        _navigateToSelectedApp.value=true
    }
    fun goToMaxUsedAppCatwiseUsageFragment(){
        val stat=appStats.filter { it.appCategory==_catNameForCatScreen.value }.maxByOrNull { it.timeSpent!! }
        _appNameForAppScreen.value=stat?.appName!!
        _catNameForAppScreen.value=stat.appCategory!!
        _appTimeSpent.value=inHrsMins(stat.timeSpent!!)
        _appLaunches.value=stat.appLaunches.toString()
        _appPackageNameForAppScreen.value=stat.packageName!!
        _navigateToSelectedApp.value=true
    }
    fun goToMaxLaunchedAppCatwiseUsageFragment(){
        val stat=appStats.filter { it.appCategory==_catNameForCatScreen.value }.maxByOrNull { it.appLaunches!! }
        _appNameForAppScreen.value=stat?.appName!!
        _catNameForAppScreen.value=stat.appCategory!!
        _appTimeSpent.value=inHrsMins(stat.timeSpent!!)
        _appLaunches.value=stat.appLaunches.toString()
        _appPackageNameForAppScreen.value=stat.packageName!!
        _navigateToSelectedApp.value=true
    }
    fun navigateToSelectedAppComplete(){
        _navigateToSelectedApp.value=false
    }
 init {
        _screenHeading.value="Today's statistics"

     val mainHandler= Handler(Looper.getMainLooper())
     mainHandler.post(object :Runnable{
         override fun run() {
             viewModelScope.launch {
                 val list:MutableList<AppsCategory> = arrayListOf()
                 //catStats.add(CategoryStat(categoryName = "SOCIAL",timeSpent = 0,appLaunches = 9,ruleViolated = false,date = 0L))
                appStats.clear()
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                     categoryTimes.replaceAll { _, _ -> 0 }
                     categoryLaunches.replaceAll { _, _ -> 0 }
                 }
                 else{
                     for(key in categoryTimes.keys){
                         categoryTimes[key]=0
                         categoryLaunches[key]=0
                     }
                 }
                 _processingDataForPieChartDone.value=false
                 val packages=appDatabaseDao.getList()
                 val mapPkgWithAppNCat:MutableList<Pair<String, AppAndCategory>> = arrayListOf()
                 val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
                 if (packages != null) {
                     for (item in packages){
                         sortedEvents[item.packageName] = mutableListOf()
                         mapPkgWithAppNCat.add(Pair(item.packageName,item))
                     }
                 }
                 val now = Calendar.getInstance()
                 val begin: Calendar = Calendar.getInstance()
                 begin.set(Calendar.HOUR_OF_DAY, 0)
                 begin.set(Calendar.MINUTE, 0)
                 begin.set(Calendar.SECOND, 0)
                 begin.set(Calendar.MILLISECOND, 0)
                 val systemEvents: UsageEvents = usm.queryEvents(begin.timeInMillis, now.timeInMillis)
                 while (systemEvents.hasNextEvent()) {
                     val event = UsageEvents.Event()
                     systemEvents.getNextEvent(event)
                     val type = event.eventType
                     if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                         sortedEvents[event.packageName]?.add(event)
                     }
                 }

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
                         totalTime += now.timeInMillis - startTime
                     }
                     val timeInSeconds = (totalTime / 1000).toInt()

                     val app=mapPkgWithAppNCat.find{it.first==packageName}?.second!!
                     appStats.add(Stat(packageName = packageName,appName = app.appName,appCategory = app.appCategory,timeSpent = timeInSeconds,appLaunches = launches,date = now.timeInMillis))
                     categoryTimes[app.appCategory]=categoryTimes[app.appCategory]!!+timeInSeconds
                     categoryLaunches[app.appCategory]=categoryLaunches[app.appCategory]!!+launches
                     categoryTimes["TOTAL"]=categoryTimes["TOTAL"]!!+timeInSeconds
                     categoryLaunches["TOTAL"]=categoryLaunches["TOTAL"]!!+launches

                 }
                 _totalAppLaunches.value=categoryLaunches["TOTAL"].toString()
                 _totalTimeSpent.value=inHrsMins(categoryTimes["TOTAL"]!!)
                 _mostLaunchedAppName.value=appStats.maxByOrNull { it->it.appLaunches!! }!!.appName!!
                 _mostUsedAppName.value=appStats.maxByOrNull { it->it.timeSpent!! }!!.appName!!

                 for(key in categoryTimes.toSortedMap().keys) {

                     if (key != "ENTERTAINMENT" && key!="TOTAL") {
                         if (categoryTimes[key]!! >= timeRules[key]!! || categoryLaunches[key]!! >= launchRules[key]!!) {
                             val appsCategory=AppsCategory(key,AppsCategoryType.WEEKLY,CategoryRuleStatus.BROKEN)
                             list.add(appsCategory)
                         }
                         else if(categoryTimes[key]!! >= timeRules[key]!!-60 || categoryLaunches[key]!! >= launchRules[key]!!-2){
                             val appsCategory=AppsCategory(key,AppsCategoryType.WEEKLY,CategoryRuleStatus.WARNING)
                             list.add(appsCategory)
                         }
                         else{
                             val appsCategory=AppsCategory(key,AppsCategoryType.WEEKLY,CategoryRuleStatus.SAFE)
                             list.add(appsCategory)
                         }

                     }
                 }

                 val entertainmentKey="ENTERTAINMENT"
                 val eT=entertainmentTime+categoryTimes[entertainmentKey]!!
                 val eL=entertainmentLaunches+categoryLaunches[entertainmentKey]!!
                 /*entertainmentTime+=categoryTimes[entertainmentKey]!!
                 entertainmentLaunches+=categoryLaunches[entertainmentKey]!!*/
                 if(eT>=timeRules[entertainmentKey]!! || eL>=launchRules[entertainmentKey]!!){
                     val entertainmentAppsCategory=AppsCategory(entertainmentKey,AppsCategoryType.WEEKLY,CategoryRuleStatus.BROKEN)
                     list.add(entertainmentAppsCategory)
                 }
                 else if(eT>=timeRules[entertainmentKey]!!-60 || eL>=launchRules[entertainmentKey]!!-2){
                     val entertainmentAppsCategory=AppsCategory(entertainmentKey,AppsCategoryType.WEEKLY,CategoryRuleStatus.WARNING)//statement that today is weekly bonus day, and you have a chance of raising Rs x more if you follow rule till today midnight
                     list.add(entertainmentAppsCategory)
                 }
                 else{
                     val entertainmentAppsCategory=AppsCategory(entertainmentKey,AppsCategoryType.WEEKLY,CategoryRuleStatus.SAFE)
                     list.add(entertainmentAppsCategory)
                 }
                 _listOfCats.value=list
                 _processingDataForPieChartDone.value=true
             }
             mainHandler.postDelayed(this, HALF_MINUTE_IN_MILLIS)
         }
     })

    }

    private fun inHrsMins(i: Int): String {
        val hrs:Int=(i/ ONE_HOUR_IN_SECONDS)
        val mins:Int=(i% ONE_HOUR_IN_SECONDS)/60
        when (hrs) {
            0 -> {
                return if(mins==1)
                    "1 min"
                else
                    "$mins mins"
            }
            1 -> {
                return if(mins==1)
                    "1 hr 1 min"
                else
                    "1 hr $mins mins"
            }
            else -> {
                return if(mins==1)
                    "$hrs hrs 1 min"
                else
                    "$hrs hrs $mins mins"
            }
        }
    }
    fun totalTimeSpentConstraintLayoutClicked(){
        _totalTimeSpentPieChartVisible.value = _totalTimeSpentPieChartVisible.value != true
        Log.i("UOVVM","test")
    }
    fun catTimeSpentConstraintLayoutClicked(){
        _catTimeSpentPieChartVisible.value = _catTimeSpentPieChartVisible.value != true
    }
    fun totalAppLaunchesConstraintLayoutClicked(){
        _totalAppLaunchesPieChartVisible.value = _totalAppLaunchesPieChartVisible.value != true
    }
    fun catAppLaunchesConstraintLayoutClicked(){
        _catAppLaunchesPieChartVisible.value = _catAppLaunchesPieChartVisible.value != true
    }



    /*fun provideSharedPref(sharedPref: SharedPreferences?) {
        timeRules["SOCIAL"]=sharedPref!!.getInt((R.string.social_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["COMMUNICATION"]=sharedPref.getInt((R.string.communication_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["GAMES"]=sharedPref.getInt((R.string.games_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["VIDEO"]=sharedPref.getInt((R.string.video_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["VIDEO_PLAYERS_N_COMICS"]=sharedPref.getInt((R.string.video_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["ENTERTAINMENT"]=sharedPref.getInt((R.string.entertainment_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        timeRules["MSNBS"]=sharedPref.getInt((R.string.msnbs_max_time).toString(),0)*ONE_MINUTE_IN_SECONDS
        timeRules["OTHERS"]=sharedPref.getInt((R.string.others_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS
        launchRules["SOCIAL"]=sharedPref.getInt((R.string.social_max_launches).toString(),0)
        launchRules["COMMUNICATION"]=sharedPref.getInt((R.string.communication_max_launches).toString(),0)
        launchRules["GAMES"]=sharedPref.getInt((R.string.games_max_launches).toString(),0)
        launchRules["VIDEO_PLAYERS_N_COMICS"]=sharedPref.getInt((R.string.video_max_launches).toString(),0)
        launchRules["ENTERTAINMENT"]=sharedPref.getInt((R.string.entertainment_launches).toString(),0)
        launchRules["MSNBS"]=sharedPref.getInt((R.string.msnbs_max_launches).toString(),0)
        launchRules["OTHERS"]=sharedPref.getInt((R.string.others_max_launches).toString(),0)
        entertainmentTime=sharedPref.getInt((R.string.entertainment_time).toString(), 0)
        entertainmentLaunches=sharedPref.getInt((R.string.entertainment_launches).toString(), 0)
    }*/

    companion object {
        private const val ONE_DAY=24*60*60*1000
        private const val ONE_HOUR_IN_SECONDS=60*60
        private const val ONE_MINUTE_IN_SECONDS = 60
        private const val HALF_MINUTE_IN_MILLIS:Long =30000
    }
}
