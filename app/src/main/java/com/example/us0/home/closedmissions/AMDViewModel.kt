package com.example.us0.home.closedmissions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.data.missions.MissionsDatabaseDao

class AMDViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application)  {
    private val _showDetailMissionDescription=MutableLiveData<Boolean>()
    val showDetailMissionDescription:LiveData<Boolean>
        get()=_showDetailMissionDescription

}