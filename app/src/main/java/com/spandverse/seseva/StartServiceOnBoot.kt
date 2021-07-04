package com.spandverse.seseva

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.spandverse.seseva.foregroundnnotifications.TestService


class StartServiceOnBoot:BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p0!=null && p1!=null) {
            if (p1.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(p0) == ServiceState.STARTED) {
                Intent(p0, TestService::class.java).also {
                    it.action = Actions.START.name
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        p0.startForegroundService(it)
                    } else {
                        p0.startService(it)
                    }
                }
            }
        }
    }

}