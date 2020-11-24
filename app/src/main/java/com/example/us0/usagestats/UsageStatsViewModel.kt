package com.example.us0.usagestats

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.foregroundnnotifications.stat
import java.sql.Timestamp
import java.util.*


class UsageStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _stats = MutableLiveData<String>()
    val stats: LiveData<String>
        get() = _stats

}
