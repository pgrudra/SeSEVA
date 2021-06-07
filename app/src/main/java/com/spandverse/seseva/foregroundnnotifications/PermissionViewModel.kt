package com.spandverse.seseva.foregroundnnotifications


import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Process

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PermissionViewModel(application: Application): AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private val _grantPermission= MutableLiveData<Boolean>()
    val grantPermission:LiveData<Boolean>
        get()=_grantPermission
    private val _disclosureVisible= MutableLiveData<Boolean>()
    val disclosureVisible:LiveData<Boolean>
        get()=_disclosureVisible
    private val _helpVisible= MutableLiveData<Boolean>()
    val helpVisible:LiveData<Boolean>
        get()=_helpVisible
    private val _toDOOA=MutableLiveData<Boolean>()
    val toDOOA:LiveData<Boolean>
        get()=_toDOOA
    fun onGrantPermission(){
        _grantPermission.value=true
    }
    fun onGrantPermissionComplete(){
        _grantPermission.value=false
    }

    fun usageDisclosure(){
        _disclosureVisible.value=true
    }
    fun usageDisclosureGone(){
        _disclosureVisible.value=false
    }

    fun checkUsageAccessPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.packageName?.let {
                appOps.unsafeCheckOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        } else {
            context?.packageName?.let {
                appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        }
        if (mode == AppOpsManager.MODE_ALLOWED) {
            /*Intent(context, TestService::class.java).also{
                it.action= Actions.START.name
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    context.startForegroundService(it)
                }
                else{
                    context.startService(it)
                }
            }*/
            _toDOOA.value=true
        }
    }

fun toDOOAComplete(){
    _toDOOA.value=false
}

}