package com.spandverse.seseva.home.yourpreviousmissions

import androidx.lifecycle.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.EventResponse
import com.spandverse.seseva.data.missions.*
import com.spandverse.seseva.deadlineStringToLong
import com.spandverse.seseva.singleValueEvent
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class YPMViewModel(private val database: MissionsDatabaseDao) : ViewModel()  {
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
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
        loadContributedMissions()
    }

    private fun loadContributedMissions() {
        viewModelScope.launch { 
            val downloadedList=database.getDownloadedContributedMissions(0)
            userId?.let {
                val contributedMissionNumbers:MutableList<Int> = arrayListOf()
                val contributionsList:MutableList<Pair<Int,Int>> = arrayListOf()
                when(val contributionsResult=cloudReference.child("users").child(it).child("contributions").singleValueEvent()){
                            is EventResponse.Changed->{
                                val dataSnapshot=contributionsResult.snapshot
                                for (i in dataSnapshot.children) {
                                    contributedMissionNumbers.add(i.key.toString().toInt())
                                    contributionsList.add(Pair(i.key!!.toInt(), i.value.toString().toInt()))
                                }
                            }
                            is EventResponse.Cancelled->{}
                        }
                if(downloadedList!=null){
                    val toDownloadList=contributedMissionNumbers.minus(downloadedList)
                    insertIntoDatabase(toDownloadList,contributionsList)
                }
                else{
                    insertIntoDatabase(contributedMissionNumbers,contributionsList)
                }
                
            }
        }
    }

    private suspend fun insertIntoDatabase(toDownloadList: List<Int>, contributionsList: MutableList<Pair<Int, Int>>) {
        for(i in toDownloadList){
            val accomplishedMissionResult=cloudReference.child("accomplishedMissions").child("i").singleValueEvent()
            val mission=Mission()
            when(accomplishedMissionResult){
                is EventResponse.Changed-> {
                    val dataSnapshot=accomplishedMissionResult.snapshot
                    mission.missionNumber=i
                    mission.missionName=dataSnapshot.child("missionName").value.toString()
                    mission.sponsorName=dataSnapshot.child("sponsorName").value.toString()
                    mission.sponsorNumber=dataSnapshot.child("sponsorNumber").getValue<Int>()?:0
                    mission.missionDescription=dataSnapshot.child("missionDescription").value.toString()
                    mission.reportAvailable=dataSnapshot.child("reportAvailable").getValue<Boolean>()?:false
                    mission.totalMoneyRaised=dataSnapshot.child("moneyRaised").getValue<Int>()?:0
                    mission.contributors=dataSnapshot.child("contributors").getValue<Int>()?:0
                    mission.deadline=dataSnapshot.child("deadline").value.toString().deadlineStringToLong()
                    mission.contribution=contributionsList.find{it.first==i}?.second ?:0
                }
                is EventResponse.Cancelled->{}
            }
            database.insert(mission)

        }

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