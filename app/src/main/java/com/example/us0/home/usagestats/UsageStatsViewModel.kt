package com.example.us0.home.usagestats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class UsageStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _stats = MutableLiveData<String>()
    val stats: LiveData<String>
        get() = _stats

}
