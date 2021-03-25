package com.example.us0.home

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.us0.*
import com.example.us0.R
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.data.apps.AppDataBaseDao
import com.example.us0.data.missions.*
import com.example.us0.foregroundnnotifications.TestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

class HomeViewModel(private val database: MissionsDatabaseDao, private val appDatabase: AppDataBaseDao, application: Application, private val pm: PackageManager) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _notifyClosedMission = MutableLiveData<DomainClosedMission?>()
    val notifyClosedMission : LiveData<DomainClosedMission?>
         get() = _notifyClosedMission
    private val _goToPermissionScreen = MutableLiveData<Boolean>()
    val goToPermissionScreen:LiveData<Boolean>
        get()=_goToPermissionScreen

    private val _goToSignOut = MutableLiveData<Boolean>()
    val goToSignOut: LiveData<Boolean>
        get() = _goToSignOut
    private val _goToRules = MutableLiveData<Boolean>()
    val goToRules: LiveData<Boolean>
        get() = _goToRules
    init{
        checkPermission()
    }
    fun notifyClosedMissionComplete(){
        _notifyClosedMission.value=null
    }

    private fun notifyAndServiceAndRefreshAppsDatabase() {
        viewModelScope.launch {
            refreshAppsDatabase()
            notifyMissionClosedIfAny()
            startService()
        }
    }

    private fun startService() {
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
        Log.i("HVM","refreshApp")
        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables = pm.queryIntentActivities(main, 0)
        val appPackageList = ArrayList<String>()

        if(checkInternet()){
            withContext(Dispatchers.IO) {
                val otherCategory:List<AppAndCategory>? =appDatabase.getCatApps("OTHERS")
                Log.i("HVM","$otherCategory")
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

    private suspend fun notifyMissionClosedIfAny() {
val mission=database.notifyIfClosed(true)
        if(mission!=null){
            mission.missionCompleteNotification=false
            database.update(mission)
            _notifyClosedMission.value= mission.asClosedDomainModel()

        }
        else{


            //checkIfServiceActive()
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
            Log.i("LODR", Timestamp(twelveOne.timeInMillis).toString())
            Log.i("LODP", Timestamp(currentTime.timeInMillis).toString())
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
        Log.i("HVM","here")
            notifyAndServiceAndRefreshAppsDatabase()
        }
        else {
            _goToPermissionScreen.value = true
        }

    }
    fun onGoToPermissionScreenComplete(){
        _goToPermissionScreen.value=false
    }

    fun onGoToSignOut() {
        _goToSignOut.value = true
    }

    fun onGoToSignOutComplete() {
        _goToSignOut.value = false
    }

    fun onGoToRules(){
        _goToRules.value=true
    }
    fun onGoToRulesComplete(){
        _goToRules.value=false
    }
    companion object {
        private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
        private const val CAT_SIZE = 9
        private const val CATEGORY_STRING = "category/"

    }
}