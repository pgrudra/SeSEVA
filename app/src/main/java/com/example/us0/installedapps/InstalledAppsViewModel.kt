package com.example.us0.installedapps


import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.lifecycle.*
import com.example.us0.data.AppAndCategory
import com.example.us0.data.AppDataBaseDao
import com.example.us0.formatApps
import kotlinx.coroutines.launch
import java.util.*


class InstalledAppsViewModel(
    val database: AppDataBaseDao,
    application: Application,
    private val pm: PackageManager
) : AndroidViewModel(application) {

    init {
        getApps()
    }

    private val _goToSignOut = MutableLiveData<Boolean>()
    val goToSignOut: LiveData<Boolean>
        get() = _goToSignOut

    val apps=database.getAll()

    val appsString=Transformations.map(apps){apps-> formatApps(apps,application.resources) }
    private fun getApps() {
        viewModelScope.launch {
            val main = Intent(Intent.ACTION_MAIN, null)
            main.addCategory(Intent.CATEGORY_LAUNCHER)
            val launchables = pm.queryIntentActivities(main, 0)
            Collections.sort(
                launchables,
                ResolveInfo.DisplayNameComparator(pm)
            )
            val app_name_list = ArrayList<String>()
            val app_package_list = ArrayList<String>()
            for (item in launchables) {
                try {
                    val package_name: String = item.activityInfo.packageName
                    val app_name = pm.getApplicationLabel(
                        pm.getApplicationInfo(
                            package_name, PackageManager.GET_META_DATA
                        )
                    ) as String
                    var copy = false
                    for (i in app_name_list.indices) {
                        if (package_name == app_package_list[i]) copy = true
                    }
                    if (!copy) {
                        app_name_list.add(app_name)
                        app_package_list.add(package_name)
                    }
                    Log.i("II", "package = <" + package_name + "> name = <" + app_name + ">");
                } catch (e: Exception) {
                }
            }
            for (i in app_name_list.indices){
                val app=AppAndCategory()
                app.appName=app_name_list[i]
                app.packageName=app_package_list[i]
                insertIntoDatabase(app)
            }
        }
    }

    private suspend fun insertIntoDatabase(app:AppAndCategory) {
        database.insert((app))
    }


    fun onGoToSignOut() {
        _goToSignOut.value = true
    }

    fun onGoToSignOutComplete() {
        _goToSignOut.value = false
    }

}