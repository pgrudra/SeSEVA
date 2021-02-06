package com.example.us0.foregroundnnotifications


import android.app.AppOpsManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.us0.R

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
    private val _toHome=MutableLiveData<Boolean>()
    val toHome:LiveData<Boolean>
        get()=_toHome
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
            createChannel(
                context.getString(R.string.foreground_service_notification_channel_id),
                context.getString(R.string.foreground_service_notification_channel_name)
            )
            val serviceIntent = Intent(context, TestService::class.java)
            serviceIntent.putExtra("inputExtra", "Serve")
            ContextCompat.startForegroundService(context, serviceIntent)
            _toHome.value=true
        }
    }
fun toHomeComplete(){
    _toHome.value=false
}
    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundServiceNotificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(foregroundServiceNotificationChannel)
        }
    }

}