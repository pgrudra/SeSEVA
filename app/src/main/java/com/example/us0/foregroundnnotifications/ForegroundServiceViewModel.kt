package com.example.us0.foregroundnnotifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.us0.R


class ForegroundServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _startServiceBoolean = MutableLiveData<Boolean>()
    val startServiceBoolean: LiveData<Boolean>
        get() = _startServiceBoolean

    private val _stopServiceBoolean = MutableLiveData<Boolean>()
    val stopServiceBoolean: LiveData<Boolean>
        get() = _stopServiceBoolean

    private val _getUsageStats = MutableLiveData<Boolean>()
    val getUsageStats: LiveData<Boolean>
        get() = _getUsageStats

    fun onGetUsageStats(){
        _getUsageStats.value=true
        Log.i("OPOP", "dest")
    }
    fun onGetUsageStatsComplete(){
        _getUsageStats.value=false
    }

    fun onStartService() {
        _startServiceBoolean.value = true
    }

    fun onStartServiceComplete() {
        _startServiceBoolean.value = false
    }

    fun onStopServiceComplete() {
        _stopServiceBoolean.value = false
    }

    fun onStopService() {
        _stopServiceBoolean.value = true
    }

    fun startService() {
        val serviceIntent = Intent(context, TestService::class.java)
        serviceIntent.putExtra("inputExtra", "Serve")
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(context, TestService::class.java)
        context.stopService(serviceIntent)
    }

    init {
        createChannel(
            context.getString(R.string.foreground_service_notification_channel_id),
            context.getString(R.string.foreground_service_notification_channel_name)
        )
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