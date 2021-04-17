package com.example.us0.home.yourpreviousmissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.data.missions.asActiveDomainModel
import java.util.*

class YPMViewModel(private val database: MissionsDatabaseDao) : ViewModel()  {
    private val _activeMissionsSelected = MutableLiveData<Boolean>()
    val activeMissionsSelected: LiveData<Boolean>
        get() = _activeMissionsSelected
    private val _accomplishedMissionsSelected = MutableLiveData<Boolean>()
    val accomplishedMissionsSelected: LiveData<Boolean>
        get() = _accomplishedMissionsSelected
    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    var missionsToDisplay= Transformations.map(database.getAllActiveMissions(nowMinusOneDay,0)){it.asActiveDomainModel()}
    fun onActiveMissionsButtonClick(){
        _activeMissionsSelected.value=true
        _accomplishedMissionsSelected.value=false
        missionsToDisplay= Transformations.map(database.getAllActiveMissions(nowMinusOneDay,0)){it.asActiveDomainModel()}
    }
    fun onAccomplishedMissionsButtonClick(){
        _accomplishedMissionsSelected.value=true
        _activeMissionsSelected.value=false
        missionsToDisplay= Transformations.map(database.getAllAccomplishedMissions(nowMinusOneDay,0)){it.asActiveDomainModel()}
    }


}