package com.spandverse.seseva.home.yourpreviousmissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.missions.asActiveDomainModel
import java.util.*

class YPMViewModel(private val database: MissionsDatabaseDao) : ViewModel()  {
    private val _activeMissionsSelected = MutableLiveData<Boolean>()
    val activeMissionsSelected: LiveData<Boolean>
        get() = _activeMissionsSelected
    private val _accomplishedMissionsSelected = MutableLiveData<Boolean>()
    val accomplishedMissionsSelected: LiveData<Boolean>
        get() = _accomplishedMissionsSelected
    private val _listDescriptionText = MutableLiveData<String>()
    val listDescriptionText: LiveData<String>
        get() = _listDescriptionText
    init{
        _listDescriptionText.value="Tap on a mission card to know more"
    }

    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    val activeMissionsToDisplay:LiveData<List<DomainActiveMission>> = Transformations.map(database.getAllActiveMissions(nowMinusOneDay,0)){it.asActiveDomainModel()}
    val accomplishedMissionsToDisplay:LiveData<List<DomainActiveMission>> = Transformations.map(database.getAllAccomplishedMissions(nowMinusOneDay,0)){it.asActiveDomainModel()}

    fun onActiveMissionsButtonClick(){
        _activeMissionsSelected.value=true
        _accomplishedMissionsSelected.value=false
        _listDescriptionText.value="Tap on a mission card to view how much more you can contribute to it"
    }
    fun onAccomplishedMissionsButtonClick(){
        _accomplishedMissionsSelected.value=true
        _activeMissionsSelected.value=false
        _listDescriptionText.value="Tap on a mission card  to know more"
    }


}