package com.example.us0.foregroundnnotifications

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.us0.R
import com.example.us0.installedapps.InstalledAppsActivity


/*fun NotificationManager.sendNotifications(messageBody:String,appContext: Context){
    val NOTIFICATION_ID = 0
    val REQUEST_CODE = 0
    val FLAGS = 0
    val builder = NotificationCompat.Builder(
        appContext,
        appContext.getString(R.string.foreground_service_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(appContext.getString(R.string.notification_title))
        .setContentText(messageBody)
    notify(NOTIFICATION_ID, builder.build())
}*/

class TestService: Service(){
    val NOTIFICATION_ID = 1
    val REQUEST_CODE = 0
    val FLAGS = 0
    override fun onBind(p0: Intent?): IBinder? {
        return  null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("inputExtra")


        val pendingIntent: PendingIntent =
            Intent(this, InstalledAppsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, FLAGS)
            }

        val notification = NotificationCompat.Builder(
            this,
            getString(R.string.foreground_service_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(input)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
