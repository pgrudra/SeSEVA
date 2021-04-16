package com.example.us0.home.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.us0.R
import com.example.us0.data.missions.MissionsDatabaseDao
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val dataBaseDAO: MissionsDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val _levelName = MutableLiveData<String>()
    val levelName: LiveData<String>
        get() = _levelName
    private val _daysLeft = MutableLiveData<String>()
    val daysLeft: LiveData<String>
        get() = _daysLeft
    private val _showDetailMissionDescription = MutableLiveData<Boolean>()
    val showDetailMissionDescription: LiveData<Boolean>
        get() = _showDetailMissionDescription

    fun expandOrContract(){
        _showDetailMissionDescription.value = _showDetailMissionDescription.value != true
    }
    fun toChooseMission(){

    }
    init {
        val currentMissionNumber=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)
        viewModelScope.launch {
            val currentMission=dataBaseDAO.doesMissionExist(currentMissionNumber)
            if(currentMission!=null){

            }
        }
    }
}