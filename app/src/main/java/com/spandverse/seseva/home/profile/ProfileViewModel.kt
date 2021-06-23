package com.spandverse.seseva.home.profile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.data.missions.Mission
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.missions.asActiveDomainModel
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(
    private val dataBaseDAO: MissionsDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val _goToChooseMission = MutableLiveData<Boolean>()
    val goToChooseMission: LiveData<Boolean>
        get() = _goToChooseMission
    private val _goToYourPreviousMissions = MutableLiveData<Boolean>()
    val goToYourPreviousMissions: LiveData<Boolean>
        get() = _goToYourPreviousMissions
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName
    private val _levelName = MutableLiveData<String>()
    val levelName: LiveData<String>
        get() = _levelName
    private val _daysLeft = MutableLiveData<String>()
    val daysLeft: LiveData<String>
        get() = _daysLeft
    private val _showDetailMissionDescription = MutableLiveData<Boolean>()
    val showDetailMissionDescription: LiveData<Boolean>
        get() = _showDetailMissionDescription
    private val _makeExpandOrContractIconVisible = MutableLiveData<Boolean>()
    val makeExpandOrContractIconVisible: LiveData<Boolean>
        get() = _makeExpandOrContractIconVisible
    val totalContribution= Transformations.map(dataBaseDAO.getTotalContribution()){it.toString()}
    val missionsSupported=Transformations.map(dataBaseDAO.getMissionsCount(0)){it.toString()}
    private val _currentMissionNumber = MutableLiveData<Int>()
    val currentMissionNumber: LiveData<Int>
        get() = _currentMissionNumber
    private val _currentMissionName = MutableLiveData<String>()
    val currentMissionName: LiveData<String>
        get() = _currentMissionName
    private val _currentMissionDescription = MutableLiveData<String>()
    val currentMissionDescription: LiveData<String>
        get() = _currentMissionDescription
    private val _currentMissionSponsorName = MutableLiveData<String>()
    val currentMissionSponsorName: LiveData<String>
        get() = _currentMissionSponsorName
    private val _currentMissionSponsorDescription = MutableLiveData<String>()
    val currentMissionSponsorDescription: LiveData<String>
        get() = _currentMissionSponsorDescription
    private val _goal = MutableLiveData<String>()
    val goal: LiveData<String>
        get() = _goal
    private val _amountRaised = MutableLiveData<String>()
    val amountRaised: LiveData<String>
        get() = _amountRaised
    private val _contributors = MutableLiveData<String>()
    val contributors: LiveData<String>
        get() = _contributors
    private val _contribution = MutableLiveData<String>()
    val contribution: LiveData<String>
        get() = _contribution
    private val _category = MutableLiveData<String>()
    val category: LiveData<String>
        get() = _category
    private val _sponsorNumber = MutableLiveData<Int>()
    val sponsorNumber: LiveData<Int>
        get() = _sponsorNumber
    //private val _missionDescription=MutableLiveData<String>()
    var missionDescription=""
    private lateinit var currentMission: Mission
        //get() = _missionDescription

    fun expandOrContract(){
        _showDetailMissionDescription.value = _showDetailMissionDescription.value != true
        if(_showDetailMissionDescription.value==true){
            _currentMissionDescription.value=missionDescription//_missionDescription.value
        }
        else{
            _currentMissionDescription.value=context.getString(
                R.string.dots, missionDescription.substring(
                    0,
                    225
                )
            )
        }
    }
    fun toChooseMission(){
        _goToChooseMission.value=true
    }
    fun toChooseMissionComplete(){
        _goToChooseMission.value=false
    }
    fun toYourPreviousMissions(){
        _goToYourPreviousMissions.value=true
    }
    fun toYourPreviousMissionsComplete(){
        _goToYourPreviousMissions.value=false
    }

    fun getCurrentMission(): DomainActiveMission {
        return currentMission.asActiveDomainModel()
    }

    init {
        //_userName.value=sharedPref.getString((R.string.user_name).toString(),"User")
        val userLevel=sharedPref.getInt((R.string.user_level).toString(),1)
        _levelName.value=when(userLevel){
            1->"Sevak"
            2->"Super Sevak"
            else->"Sevak"
        }
        _userName.value=sharedPref.getString((R.string.user_name).toString(),"Username")
        val currentMissionNumber=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)
        viewModelScope.launch {
            currentMission= dataBaseDAO.doesMissionExist(currentMissionNumber)!!
            _sponsorNumber.value=currentMission.sponsorNumber
            _currentMissionNumber.value=currentMission.missionNumber
            _currentMissionName.value=currentMission.missionName
            missionDescription=currentMission.missionDescription
            if(missionDescription.length<226){
                _currentMissionDescription.value=missionDescription
            }
            else{
                _makeExpandOrContractIconVisible.value=true
                val k=context.getString(
                    R.string.dots, missionDescription.substring(
                        0,
                        225
                    )
                )
                _currentMissionDescription.value=k
            }
            _currentMissionSponsorName.value=currentMission.sponsorName
            _goal.value=currentMission.goal
            _amountRaised.value=currentMission.totalMoneyRaised.toString()
            _contributors.value=currentMission.contributors.toString()
            _contribution.value="Rs " + currentMission.contribution.toString()
            _category.value=currentMission.missionCategory
            val now= Calendar.getInstance().timeInMillis
            val intDaysLeft=((currentMission.deadline-now+ ONE_DAY)/ ONE_DAY).toInt()+1
            if(intDaysLeft<10){
                _daysLeft.value= "0$intDaysLeft"
            }
            else{
                _daysLeft.value= intDaysLeft.toString()
            }
        }
    }
    companion object {
        const val ONE_DAY=24*60*60*1000
    }
}