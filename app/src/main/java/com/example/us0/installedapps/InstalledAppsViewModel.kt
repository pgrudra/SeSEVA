package com.example.us0.installedapps


import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Process
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.us0.data.AppAndCategory
import com.example.us0.data.AppDataBaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.*


class InstalledAppsViewModel(
    val database: AppDataBaseDao,
    application: Application,
    private val pm: PackageManager
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    init {
        getApps()

    }

    private val _goToSignOut = MutableLiveData<Boolean>()
    val goToSignOut: LiveData<Boolean>
        get() = _goToSignOut

    private  val _goToPermissionScreen=MutableLiveData<Boolean>()
    val goToPermissionScreen: LiveData<Boolean>
        get()=_goToPermissionScreen

    private val _proceed=MutableLiveData<Boolean>()
    val proceed:LiveData<Boolean>
    get()=_proceed

    private val _goToForegroundService=MutableLiveData<Boolean>()
    val goToForegroundService:LiveData<Boolean>
        get()=_goToForegroundService


    val apps = database.getAll()

    //val appsString=Transformations.map(apps){apps-> formatApps(apps,application.resources) }
    private fun getApps() {
        viewModelScope.launch {
            insertIntoDatabase()
        }
    }

    private suspend fun insertIntoDatabase() {

        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables = pm.queryIntentActivities(main, 0)
        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(pm)
        )
        val app_name_list = ArrayList<String>()
        val app_package_list = ArrayList<String>()
        //var previous_package_name:String?=null
        for (item in launchables) {
            try {
                val package_name: String = item.activityInfo.packageName
                val app_name = pm.getApplicationLabel(
                    pm.getApplicationInfo(
                        package_name, PackageManager.GET_META_DATA
                    )
                ) as String

                /*if (package_name != previous_package_name) {
                    val app = AppAndCategory()
                    app.appName = app_name
                    app.packageName = package_name
                    database.insert(app)
                    previous_package_name = package_name
                }*/
                var copy = false
                for (i in app_name_list.indices) {
                    if (package_name == app_package_list[i]) copy = true
                }
                if (!copy) {
                    app_name_list.add(app_name)
                    app_package_list.add(package_name)
                }
            } catch (e: Exception) {
            }
        }
        for (i in app_name_list.indices) {
            val app = AppAndCategory()
            app.appName = app_name_list[i]
            app.packageName = app_package_list[i]
            val checkApp = database.isAppExist(app_package_list[i])
            if (checkApp == null) {
                val queryUrl = GOOGLE_URL + app.packageName + "&hl=en"
                app.appCategory = try {

                    val document = withContext(Dispatchers.IO) {
                        Jsoup.connect(queryUrl).get()
                    }
                    val text = document?.select("a[itemprop=genre]")
                    if (text == null) {
                        Log.i("N", "text is null")
                        "Others"
                    }
                    val href = text?.attr("abs:href")
                    if (href != null) {

                        if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                            Log.i("H", "$href")
                            href.substring(
                                href.indexOf(CATEGORY_STRING) + CAT_SIZE,
                                href.length
                            )
                        } else {
                            "Others"
                        }
                    } else {
                        "Others"
                    }
                } catch (e: kotlin.Exception) {
                    "Others"
                }

                database.insert(app)
            } else if (checkApp.appCategory == "") {
                val queryUrl = GOOGLE_URL + app.packageName + "&hl=en"
                checkApp.appCategory = try {

                    val document = withContext(Dispatchers.IO) {
                        Jsoup.connect(queryUrl).get()
                    }
                    val text = document?.select("a[itemprop=genre]")
                    if (text == null) {
                        Log.i("N", "text is null")
                        "Others"
                    }
                    val href = text?.attr("abs:href")
                    if (href != null) {

                        if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                            Log.i("H", "$href")
                            href.substring(
                                href.indexOf(CATEGORY_STRING) + CAT_SIZE,
                                href.length
                            )
                        } else {
                            "Others"
                        }
                    } else {
                        "Others"
                    }
                } catch (e: kotlin.Exception) {
                    "Others"
                }
                database.update(checkApp)
            }

        }
    }

    fun checkPermission(){

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
            _goToForegroundService.value = true
            onProceedComplete()

        }
else {
            _goToPermissionScreen.value = true
            onProceedComplete()
        }

    }
    fun onGoToPermissionScreenComplete(){
        _goToPermissionScreen.value=false
    }


    fun onGoToSignOut() {
        _goToSignOut.value = true
        Log.i("P", "${apps.value}")
    }

    fun onGoToSignOutComplete() {
        _goToSignOut.value = false
    }
    fun onProceed(){
        _proceed.value=true
    }
    fun onProceedComplete(){
        _proceed.value=false
    }
    fun onGoToForegroundServiceComplete(){
        _goToForegroundService.value=false
    }

    companion object {
        private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
        private const val CAT_SIZE = 9
        private const val CATEGORY_STRING = "category/"
        private const val TEST = "https://play.google.com/store/apps/details?id=com.tinder"
    }


}