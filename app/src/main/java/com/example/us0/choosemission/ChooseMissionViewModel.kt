package com.example.us0.choosemission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.data.missions.MissionsDatabaseDao

class ChooseMissionViewModel(
    private val database: MissionsDatabaseDao,
    application: Application,
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    init {
        getActiveMissions()

    }
    private val _toInstalledApps = MutableLiveData<Boolean>()
    val toInstalledApps: LiveData<Boolean>
        get() = _toInstalledApps

    fun toNextFragment(){
        _toInstalledApps.value=true
    }
    fun toNextFragmentComplete(){
        _toInstalledApps.value=false
    }

    private fun getActiveMissions(){

    }
}