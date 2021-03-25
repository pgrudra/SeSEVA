package com.example.us0

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID=0

fun NotificationManager.sendNotification(messageBody:String, applicationContext: Context){
    val builder=NotificationCompat.Builder(
        applicationContext,applicationContext.getString(R.string.usage_alert_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(applicationContext.getString(R.string.usage_alert_title))
        .setContentText(messageBody)
        .setDefaults(Notification.DEFAULT_ALL)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    notify(NOTIFICATION_ID,builder.build())
}

fun NotificationManager.cancelNotifications(){
    cancel(0)
}