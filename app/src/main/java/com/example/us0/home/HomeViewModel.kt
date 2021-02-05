package com.example.us0.home

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.us0.data.missions.*
import kotlinx.coroutines.launch

class HomeViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application) {
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
    init{
        notifyPermissionService()
    }
    fun notifyClosedMissionComplete(){
        _notifyClosedMission.value=null
    }

    private fun notifyPermissionService() {
        viewModelScope.launch {
            notifyMissionClosedIfAny()
        }
    }

    private suspend fun notifyMissionClosedIfAny() {
val mission=database.notifyIfClosed(true)
        if(mission!=null){
            mission.missionCompleteNotification=false
            database.update(mission)
            _notifyClosedMission.value= mission.asClosedDomainModel()

        }
        else{

            checkPermission()
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
           /* _goToForegroundService.value = true
            onProceedComplete()*/

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
}