package com.spandverse.seseva.home

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.spandverse.seseva.*
import com.spandverse.seseva.R
import com.spandverse.seseva.data.apps.AppAndCategory
import com.spandverse.seseva.data.apps.AppDataBaseDao
import com.spandverse.seseva.data.missions.*
import com.spandverse.seseva.foregroundnnotifications.TestService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class HomeViewModel(private val database: MissionsDatabaseDao, private val appDatabase: AppDataBaseDao, application: Application, private val pm: PackageManager) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
    private val _goToPermissionScreen = MutableLiveData<Boolean>()
    val goToPermissionScreen:LiveData<Boolean>
        get()=_goToPermissionScreen
    private val _goToMissionsScreen =MutableLiveData<Boolean>()
    val goToMissionsScreen:LiveData<Boolean>
        get() = _goToMissionsScreen
    private val _goToRules = MutableLiveData<Boolean>()
    val goToRules: LiveData<Boolean>
        get() = _goToRules
    private val _showOverlayPermissionBanner = MutableLiveData<Boolean>()
    val showOverlayPermissionBanner: LiveData<Boolean>
        get() = _showOverlayPermissionBanner
    private val _showStrictModeBanner = MutableLiveData<Boolean>()
    val showStrictModeBanner: LiveData<Boolean>
        get() = _showStrictModeBanner
    private val _checkForUpdate = MutableLiveData<Boolean>()
    val checkForUpdate: LiveData<Boolean>
        get() = _checkForUpdate
    private val _goToProfile = MutableLiveData<Boolean>()
    val goToProfile: LiveData<Boolean>
        get() = _goToProfile
    private val _accomplishedMissionYouRaised = MutableLiveData<Int>()
    val accomplishedMissionYouRaised: LiveData<Int>
        get() = _accomplishedMissionYouRaised
    private val _accomplishedMissionTotalRaised = MutableLiveData<Int>()
    val accomplishedMissionTotalRaised: LiveData<Int>
        get() = _accomplishedMissionTotalRaised
    private val _missionName = MutableLiveData<String>()
    val missionName: LiveData<String>
        get() = _missionName
    private val _moneyRaised = MutableLiveData<String>()
    val moneyRaised: LiveData<String>
        get() = _moneyRaised
    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    val activeMissions=Transformations.map(database.getActiveMissionsCount(nowMinusOneDay)){ it?.toString() ?: "0" }
    val totalMoneyRaised=Transformations.map(database.getTotalMoneyRaised()){it?.toString() ?: "0"}
    val totalMissions=Transformations.map(database.getMissionsCount(-1)){it?.toString() ?: "0"}
    private val _levelName = MutableLiveData<String>()
    val levelName: LiveData<String>
        get() = _levelName
    private val _timeSpent = MutableLiveData<String>()
    val timeSpent: LiveData<String>
        get() = _timeSpent
   /* private val _totalMoneyRaised = MutableLiveData<String>()
    val totalMoneyRaised: LiveData<String>
        get() = _totalMoneyRaised*/
    /*private val _totalMissions = MutableLiveData<String>()
    val totalMissions: LiveData<String>
        get() = _totalMissions*/
    private val _appLaunches = MutableLiveData<String>()
    val appLaunches: LiveData<String>
        get() = _appLaunches

    private val _goToUsageOverview = MutableLiveData<Boolean>()
    val goToUsageOverview: LiveData<Boolean>
        get() = _goToUsageOverview
    init{
        checkPermission()
    }

    private fun notifyAndServiceAndRefreshAppsDatabase() {
        viewModelScope.launch {
            val m=database.doesMissionExist(1)
            restartService()
            notifyMissionAccomplishedOrAppUpdate()
            displayThings()
            refreshAppsDatabase()
        }
    }

    private suspend fun displayThings() {
        displayProfileRelatedThings()
        displayUsageStatsRelatedThings()
        displayBannerIfApplicable()
        displayMissionsRelatedThings()
    }

    private fun displayBannerIfApplicable() {
        Log.i("HVM1","1")
        val count=sharedPref.getInt((R.string.count).toString(),0)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.i("HVM1","2")
            if(count%4==2){
                //show banner
                Log.i("HVM1","3")
                _showOverlayPermissionBanner.value=true
            }
        }
        else{
            val showStrictBanner=sharedPref.getBoolean((R.string.show_strict_banner).toString(),true)
            if(showStrictBanner && count%4==0){
                //show banner
                //upon enabling strict mode, put showStrictBanner as false
            }
        }
    }

    private suspend fun displayMissionsRelatedThings() {
        val loadedList=database.getDownloadedMissions()
        val entireList:MutableList<Int> = arrayListOf()
        val moneyRaisedList:MutableList<Pair<Int,Int>> = arrayListOf()
        val moneyRaisedReference=cloudReference.child("moneyRaised")
        moneyRaisedReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //_totalMissions.value= snapshot.childrenCount.toString()
                //var totalRaisedMoney=0
                for(i in snapshot.children) {
                    //totalRaisedMoney += i.getValue<Int>() ?: 0
                    entireList.add(i.key.toString().toInt())
                    moneyRaisedList.add(Pair(i.key.toString().toInt(),i.value.toString().toInt()))
                }
                //_totalMoneyRaised.value = "Rs $totalRaisedMoney"
                    if(loadedList!=null){
                        val toDownloadList=entireList.minus(loadedList)
                        insertIntoDatabase(toDownloadList,loadedList,moneyRaisedList)
                    }
                    else{
                        insertIntoDatabase(entireList,null,moneyRaisedList)
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                //_totalMoneyRaised.value="Rs"+Transformations.map(database.getTotalMoneyRaised()){it.toString()}
            }
        })

        val accomplishedEntireList:MutableList<Int> = arrayListOf()
        val accomplishedMoneyRaisedResult=cloudReference.child("accomplishedMoneyRaised").singleValueEvent()
        when(accomplishedMoneyRaisedResult){
            is EventResponse.Changed->{
                val dataSnapshot=accomplishedMoneyRaisedResult.snapshot
                for(i in dataSnapshot.children) {
                    //totalRaisedMoney += i.getValue<Int>() ?: 0
                    accomplishedEntireList.add(i.key.toString().toInt())
                }
            }
            is EventResponse.Cancelled->{}
        }
        if(loadedList!=null){
            val toDownloadList=accomplishedEntireList.minus(loadedList)
            insertAccomplishedMissionsIntoDatabase(toDownloadList)
        }
        else{
            insertAccomplishedMissionsIntoDatabase(accomplishedEntireList)
        }

       /* val deadlinesReference=cloudReference.child("deadlines")
        val now = Calendar.getInstance()
        deadlinesReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeMissions=0
                for(i in snapshot.children){
                    if(now.timeInMillis < deadlineStringToLong(i.value.toString())){
                        activeMissions+=1
                        _activeMissions.value=activeMissions.toString()
                        //update deadlines
                        }

                }
            } override fun onCancelled(error: DatabaseError) {

            }
        })*/
    }

    private suspend fun insertAccomplishedMissionsIntoDatabase(toDownloadList: List<Int>) {
        val contributionsList:MutableList<Pair<Int,Int>> = arrayListOf()
        userId?.let {
            when(val contributionsResult=cloudReference.child("users").child(userId).child("contributions").singleValueEvent()){
                is EventResponse.Changed->{
                    for(j in contributionsResult.snapshot.children){
                        contributionsList.add(Pair(j.getValue<Int>()?:0,j.getValue<Int>()?:0))
                    }
                }
                is EventResponse.Cancelled->{}
            }
        }
        for(i in toDownloadList){
            val accomplishedMissionResult=cloudReference.child("accomplishedMissions").child("i").singleValueEvent()
            val mission=Mission()
            when(accomplishedMissionResult){
                is EventResponse.Changed-> {
                    val dataSnapshot=accomplishedMissionResult.snapshot
                    mission.missionNumber=i
                    mission.missionName=dataSnapshot.child("missionName").value.toString()
                    mission.sponsorName=dataSnapshot.child("sponsorName").value.toString()
                    mission.sponsorNumber=dataSnapshot.child("sponsorNumber").getValue<Int>()?:0
                    mission.missionDescription=dataSnapshot.child("missionDescription").value.toString()
                    mission.reportAvailable=dataSnapshot.child("reportAvailable").getValue<Boolean>()?:false
                    mission.totalMoneyRaised=dataSnapshot.child("moneyRaised").getValue<Int>()?:0
                    mission.contributors=dataSnapshot.child("contributors").getValue<Int>()?:0
                    mission.deadline=dataSnapshot.child("deadline").value.toString().deadlineStringToLong()
                    mission.contribution=contributionsList.find{it.first==i}?.second ?:0
                }
                is EventResponse.Cancelled->{}
            }
            database.insert(mission)

        }
    }

    private fun insertIntoDatabase(list: List<Int>, loadedList: List<Int>?, moneyRaisedList:MutableList<Pair<Int,Int>>) {
        val contributorsList:MutableList<Pair<Int,Int>> = arrayListOf()
        val contributorsReference=cloudReference.child("contributors")
        contributorsReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(missionSnapShot in snapshot.children ){
                    contributorsList.add(Pair(missionSnapShot.key!!.toInt(),missionSnapShot.value.toString().toInt()))
                }
                val check=sharedPref.getBoolean((R.string.update_contributions_cloud).toString(),false)
                if(check){
                    val contributionsList:MutableList<Pair<Int,Int>> = arrayListOf()
                    val contributionsReference= userId?.let { cloudReference.child("users").child(it).child("contributions") }
                    contributionsReference?.get()?.addOnSuccessListener { dataSnapshot ->
                        for (i in dataSnapshot.children) {
                            contributionsList.add(Pair(i.key!!.toInt(), i.value.toString().toInt()))
                        }
                        with (sharedPref.edit()) {
                            this?.putBoolean((R.string.update_contributions_cloud).toString(),false)
                            this?.apply()
                        }
                        for(i in list){
                            val missionReference=cloudReference.child("Missions").child(i.toString())
                            missionReference.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val mission:Mission? =snapshot.getValue<NetworkMission>()?.asDatabaseModel()
                                    val primaryKey= snapshot.key?.toInt() ?: 0
                                    mission?.missionNumber=primaryKey
                                    mission?.totalMoneyRaised= moneyRaisedList.find{it.first==primaryKey}?.second ?:0
                                    val now: Calendar = Calendar.getInstance()
                                    mission?.missionActive=now.timeInMillis<= mission?.deadline!!
                                    mission.contributors=contributorsList.find{it.first==primaryKey}?.second ?:0
                                    viewModelScope.launch { database.insert(mission) }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                        if(loadedList!=null){
                            viewModelScope.launch {
                                for (i in loadedList) {
                                    val mission = database.doesMissionExist(i)
                                    mission?.contributors=contributorsList.find{it.first==i}?.second ?:0
                                    mission?.totalMoneyRaised=moneyRaisedList.find{it.first==i}?.second ?:0
                                    mission?.let { database.update(it) }
                                }
                            }
                        }
                    }
                }
                else{
                    for(i in list){
                        val missionReference=cloudReference.child("Missions").child(i.toString())
                        missionReference.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val mission:Mission? =snapshot.getValue<NetworkMission>()?.asDatabaseModel()
                                val primaryKey= snapshot.key?.toInt() ?: 0
                                mission?.missionNumber=primaryKey
                                mission?.totalMoneyRaised= moneyRaisedList.find{it.first==primaryKey}?.second ?:0
                                val now: Calendar = Calendar.getInstance()
                                //may need to remove this
                                mission?.missionActive=now.timeInMillis<= mission?.deadline!!
                                mission.contributors=contributorsList.find{it.first==primaryKey}?.second ?:0
                                viewModelScope.launch { database.insert(mission) }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                    if(loadedList!=null){
                        viewModelScope.launch {
                            for (i in loadedList) {
                                val mission = database.doesMissionExist(i)
                                mission?.contributors=contributorsList.find{it.first==i}?.second ?:0
                                mission?.totalMoneyRaised=moneyRaisedList.find{it.first==i}?.second ?:0
                                mission?.let { database.update(it) }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    /*private fun deadlineStringToLong(deadline:String):Long{
        var l:Long=0L
        val f = SimpleDateFormat("dd-MMM-yyyy")
        try {
            val d: Date = f.parse(deadline)
            l= d.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return l
    }*/
    private suspend fun displayUsageStatsRelatedThings() {
        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
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
        val timeRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"COMMUNICATION" to sharedPref.getInt((R.string.communication_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS, "GAMES" to sharedPref.getInt((R.string.games_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to sharedPref.getInt((R.string.video_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"MSNBS" to sharedPref.getInt((R.string.msnbs_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS,"OTHERS" to sharedPref.getInt((R.string.others_max_time).toString(),0)* ONE_MINUTE_IN_SECONDS)
        val launchRules:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_max_launches).toString(),0),"COMMUNICATION" to sharedPref.getInt((R.string.communication_max_launches).toString(),0), "GAMES" to sharedPref.getInt((R.string.games_max_launches).toString(),0),"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to sharedPref.getInt((R.string.video_max_launches).toString(),0),"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_launches).toString(),0),"MSNBS" to sharedPref.getInt((R.string.msnbs_max_launches).toString(),0),"OTHERS" to sharedPref.getInt((R.string.others_max_launches).toString(),0))
        val categoryPenalties:HashMap<String,Int> = hashMapOf("TOTAL" to 0,"SOCIAL" to sharedPref.getInt((R.string.social_penalty).toString(),0),"COMMUNICATION" to sharedPref.getInt((R.string.communication_penalty).toString(),0), "GAMES" to sharedPref.getInt((R.string.games_penalty).toString(),0),"WHITELISTED" to 0,"VIDEO_PLAYERS_N_COMICS" to sharedPref.getInt((R.string.video_penalty).toString(),0),"ENTERTAINMENT" to sharedPref.getInt((R.string.entertainment_penalty).toString(),0),"MSNBS" to sharedPref.getInt((R.string.msnbs_penalty).toString(),0),"OTHERS" to sharedPref.getInt((R.string.others_penalty).toString(),0))

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
                totalTime += now.timeInMillis - startTime
            }
            val timeInSeconds = (totalTime / 1000).toInt()
            val app=appDatabase.isAppExist(packageName)
            if(app!=null) {
                categoryTimes[app.appCategory]=categoryTimes[app.appCategory]!!+timeInSeconds
                categoryLaunches[app.appCategory]=categoryLaunches[app.appCategory]!!+launches
                categoryTimes["TOTAL"]=categoryTimes["TOTAL"]!!+timeInSeconds
                categoryLaunches["TOTAL"]=categoryLaunches["TOTAL"]!!+launches

            }
        }
        val hrs:Int=(categoryTimes["TOTAL"]!!/ ONE_HOUR_IN_SECONDS)
        val mins:Int=(categoryTimes["TOTAL"]!! % ONE_HOUR_IN_SECONDS)/60
        _appLaunches.value=categoryLaunches["TOTAL"].toString()
        if(hrs!=0){
            _timeSpent.value="$hrs hrs $mins mins"
        }
        else{
            _timeSpent.value="$mins minutes"
        }
        val daysAfterInstallation= sharedPref.getInt((R.string.days_after_installation).toString(), 0)
        if(daysAfterInstallation==6){
            val key="ENTERTAINMENT"
            var entertainmentTime=sharedPref.getInt((R.string.entertainment_time).toString(), 0)
            var entertainmentLaunches=sharedPref.getInt((R.string.entertainment_launches).toString(), 0)
            entertainmentTime+=categoryTimes[key]!!
            entertainmentLaunches+=categoryLaunches[key]!!
            if(entertainmentTime>=timeRules[key]!! || entertainmentLaunches>=launchRules[key]!!){
                //statement that today is weekly bonus day, but unfortunately you have broken the rule set for Entertainment apps
            }
            else{
                //statement that today is weekly bonus day, and you have a chance of raising Rs x more if you follow rule till today midnight
            }
        }
        else{
            var penaltyToday=0
            val dailyReward=sharedPref.getInt((R.string.daily_reward).toString(), 0)
            for(key in categoryTimes.keys) {
                if (key != "ENTERTAINMENT") {
                    if (categoryTimes[key]!! >= timeRules[key]!! || categoryLaunches[key]!! >= launchRules[key]!!) {
                        penaltyToday += categoryPenalties[key]!!
                    }

                }
            }
            if(penaltyToday>=dailyReward){
                //statement
            }
            else{
                //Lost your chance to raise Rs $penaltyToday, but still can contribute Rs dailyReward-penaltyToday today.
            }
        }

    }


    private suspend fun displayProfileRelatedThings() {
        val choseMission=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)
        val mission=database.doesMissionExist(choseMission)
        if(mission!=null){
            _missionName.value=mission.missionName
            _moneyRaised.value="Rs ${mission.contribution}"
        }

    }

    private fun restartService() {
        val serviceMode=sharedPref.getInt((R.string.service_mode).toString(),0) ?:0
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
            if (serviceMode == 0) {
                //stopService()
                    //mode changed
                with(sharedPref?.edit()) {
                    this?.putInt((R.string.service_mode).toString(), 2)
                    this?.apply()
                }
            }
        }
        else{
            //ask DOOA through banner
            /*if(serviceMode==0){
                //stopService()
                //mode changed
                with (sharedPref.edit()) {
                    this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with(sharedPref?.edit()) {
                    this?.putInt((R.string.service_mode).toString(), 1)
                    this?.apply()
                }
            }
            else */
                if(serviceMode!=1){
                //mode changed
                with (sharedPref.edit()) {
                    this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with(sharedPref?.edit()) {
                    this?.putInt((R.string.service_mode).toString(), 1)
                    this?.apply()
                }
                //put service to light mode
            }
        }
        startService()
    }
    private fun stopService() {
        if(getServiceState(context)!=ServiceState.STOPPED){
            Intent(context, TestService::class.java).also {
                it.action = Actions.STOP.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                } else {
                    context.startService(it)
                }
            }
        }
    }
    //on clicking grant per on banner, take to DOOA per, on returning, show dialog for medium or strict mode
    private fun startService(){
        Intent(context, TestService::class.java).also{
            it.action= Actions.START.name
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                context?.startForegroundService(it)
            }
            else{
                context?.startService(it)
            }
        }
    }

    private suspend fun refreshAppsDatabase() {
        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables = pm.queryIntentActivities(main, 0)
        val appPackageList = ArrayList<String>()

        if(checkInternet()){
            withContext(Dispatchers.IO) {
                val otherCategory:List<AppAndCategory>? =appDatabase.getCatApps("OTHERS")
                if(otherCategory!=null){
                    for(i in otherCategory){
                        val nameOfPackage: String =i.packageName
                        val queryUrl = "${GOOGLE_URL}$nameOfPackage&hl=en"
                        val category = try {
                            val document = Jsoup.connect(queryUrl).get()

                            val text = document?.select("a[itemprop=genre]")
                            if (text == null) {
                                "OTHERS"
                            }
                            val href = text?.attr("abs:href")
                            if (href != null) {

                                if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                                    href.substring(
                                        href.indexOf(CATEGORY_STRING) + CAT_SIZE,
                                        href.length
                                    )
                                } else {
                                    "OTHERS"
                                }
                            } else {
                                "OTHERS"
                            }
                        } catch (e: Exception) {
                            "OTHERS"
                        }
                        if(category!="OTHERS") {
                            i.appCategory = allotGroup(category)
                            appDatabase.update(i)
                        }
                    }
                }
                for (item in launchables) {
                    val nameOfPackage: String = item.activityInfo.packageName
                    appPackageList.add(nameOfPackage)
                    val checkApp = appDatabase.isAppExist(nameOfPackage)
                    if (checkApp == null) {
                        val queryUrl = "${GOOGLE_URL}$nameOfPackage&hl=en"
                        val category = try {
                            val document = Jsoup.connect(queryUrl).get()

                            val text = document?.select("a[itemprop=genre]")
                            if (text == null) {
                                "OTHERS"
                            }
                            val href = text?.attr("abs:href")
                            if (href != null) {

                                if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                                    href.substring(
                                        href.indexOf(CATEGORY_STRING) + CAT_SIZE,
                                        href.length
                                    )
                                } else {
                                    "OTHERS"
                                }
                            } else {
                                "OTHERS"
                            }
                        } catch (e: Exception) {
                            "OTHERS"
                        }
                        val app = AppAndCategory()
                        app.packageName = nameOfPackage
                        app.appName = pm.getApplicationLabel(
                            pm.getApplicationInfo(
                                nameOfPackage, PackageManager.GET_META_DATA
                            )
                        ) as String
                        app.appCategory = allotGroup(category)
                        appDatabase.insert(app)
                    }
                }
                val databaseList = appDatabase.getLaunchablesList()
                val deletedApps = databaseList.minus(appPackageList)
                if (deletedApps.isNotEmpty()) {
                    for (i in deletedApps) {
                        appDatabase.deleteApp(i)
                    }
                }

            }
        }
        else {
            withContext(Dispatchers.IO) {
                for (item in launchables) {
                    val nameOfPackage: String = item.activityInfo.packageName
                    appPackageList.add(nameOfPackage)
                }
                val databaseList = appDatabase.getLaunchablesList()
                val deletedApps = databaseList.minus(appPackageList)
                if (deletedApps.isNotEmpty()) {
                    for (i in deletedApps) {
                        appDatabase.deleteApp(i)
                    }
                }
            }
        }
    }
    private fun checkInternet():Boolean{
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    null
                }
            return if (capabilities != null) {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                false
            }
        }
        else return false
    }

    private suspend fun notifyMissionAccomplishedOrAppUpdate() {
        val chosenMissionNumber=sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0) ?: 0
        val chosenMission=database.doesMissionExist(chosenMissionNumber)
        chosenMission?.let {
            if(it.deadline<nowMinusOneDay){
                sharedPref?.edit()?.remove((R.string.chosen_mission_number).toString())?.apply()
                _accomplishedMissionTotalRaised.value=chosenMission.totalMoneyRaised
                _accomplishedMissionYouRaised.value=chosenMission.contribution
            }
            else{
                //check for app update
                _checkForUpdate.value=true
            }
        }
    }
    private fun checkPermission(){
        val appOps = context
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), context.packageName
            )
        }
        if(mode == AppOpsManager.MODE_ALLOWED) {
            with(sharedPref?.edit()) {
                this?.putBoolean((R.string.onboarding_done).toString(), true)
                this?.apply()
            }
            val constraintNet= Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val categoryRefreshRequest= PeriodicWorkRequestBuilder<CategoryRefreshWorker>(1,
                TimeUnit.DAYS)
                .setConstraints(constraintNet)
                .setInitialDelay(12, TimeUnit.HOURS)
                .addTag(context.getString(R.string.category_refresh))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("categoryRefresh",
                ExistingPeriodicWorkPolicy.KEEP,categoryRefreshRequest)
val minute:Int=(0..2).random()
            val currentTime= Calendar.getInstance()
            val twelveOne=Calendar.getInstance()
            twelveOne.set(Calendar.HOUR_OF_DAY,0)
            twelveOne.set(Calendar.MINUTE,minute)
            twelveOne.add(Calendar.DATE,1)
            val timeDiff=twelveOne.timeInMillis-currentTime.timeInMillis
            val updateStatsLocalRequest= OneTimeWorkRequestBuilder<LocalDatabaseUpdateWorker>()
                .setInitialDelay(timeDiff,TimeUnit.MILLISECONDS)
                .addTag("localDateBase")
                .build()
            val updateStatsCloudRequest=OneTimeWorkRequestBuilder<CloudDatabaseUpdateWorker>()
                .setConstraints(constraintNet)
                .addTag("cloudDateBase")
                .build()
            WorkManager.getInstance(context).beginUniqueWork("databaseUpdate",ExistingWorkPolicy.KEEP,updateStatsLocalRequest).then(updateStatsCloudRequest).enqueue()
            notifyAndServiceAndRefreshAppsDatabase()
        }
        else {
            _goToPermissionScreen.value = true
        }

    }
    fun onGoToPermissionScreenComplete(){
        _goToPermissionScreen.value=false
    }

    fun onGoToMissionsScreen() {
        _goToMissionsScreen.value=true
       /* viewModelScope.launch {
            retrieveChosenMission()
        }*/
    }

   /* private suspend fun retrieveChosenMission() {
        val chosenMissionNumber=sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0) ?: 0
        val chosenMission=database.doesMissionExist(chosenMissionNumber)?.asActiveDomainModel()

        if(chosenMission!=null){
            _goToDetailsScreen.value=chosenMission
        }
        else{
            //this case must never come
            _goToChooseMission.value=true
        }
    }*/


    fun onGoToMissionsScreenComplete() {
        _goToMissionsScreen.value=false
    }

    fun onGoToRules(){
        _goToRules.value=true
    }
    fun onGoToRulesComplete(){
        _goToRules.value=false
    }
    fun onGoToProfile(){
        _goToProfile.value=true
    }
    fun onGoToProfileComplete(){
        _goToProfile.value=false
    }
    fun onGoToUsageOverview(){
        _goToUsageOverview.value=true
    }
    fun onGoToUsageOverviewComplete(){
        _goToUsageOverview.value=false
    }

    fun enableMediumMode() {
        with (sharedPref.edit()) {
            this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
            this?.apply()
        }
        with (sharedPref.edit()) {
            this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 2)
            this?.apply()
        }
        startService()
    }

    fun enableStrictMode() {
        with (sharedPref.edit()) {
            this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
            this?.apply()
        }
        with (sharedPref.edit()) {
            this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 3)
            this?.apply()
        }
        with (sharedPref.edit()) {
            this?.putBoolean((R.string.show_strict_banner).toString(),false)
            this?.apply()
        }
        startService()
    }

    fun showOverlayPermissionBannerComplete() {
        _showOverlayPermissionBanner.value=false
    }

    fun showStrictModeBannerComplete() {
        _showStrictModeBanner.value=false
    }

    companion object {
        private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
        private const val CAT_SIZE = 9
        private const val CATEGORY_STRING = "category/"
        private const val ONE_DAY=24*60*60*1000
        private const val ONE_HOUR_IN_SECONDS=60*60
        private const val ONE_MINUTE_IN_SECONDS = 60
    }
}