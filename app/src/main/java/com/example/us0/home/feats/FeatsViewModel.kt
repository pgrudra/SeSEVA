package com.example.us0.home.feats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.us0.data.SponsorCardContents
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.data.missions.asActiveDomainModel
import java.util.*
import kotlin.collections.HashMap

class FeatsViewModel(
    private val dataBaseDAO: MissionsDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val _navigateToSelectedMission=MutableLiveData<DomainActiveMission?>()
    val navigateToSelectedMission:LiveData<DomainActiveMission?>
        get()=_navigateToSelectedMission
    private val _navigateToSelectedSponsorPage=MutableLiveData<SponsorCardContents?>()
    val navigateToSelectedSponsorPage:LiveData<SponsorCardContents?>
        get()=_navigateToSelectedSponsorPage
    val missions:LiveData<List<DomainActiveMission>> = Transformations.map(dataBaseDAO.getAllMissions()){it.asActiveDomainModel()}
    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    val activeMissions=Transformations.map(dataBaseDAO.getActiveMissionsCount(nowMinusOneDay)){it.toString()}
    val totMoneyRaised=Transformations.map(dataBaseDAO.getTotalMoneyRaised()){ "Rs $it" }
    val totalMissions=Transformations.map(dataBaseDAO.getMissionsCount(-1)){it.toString()}
    val sponsors:LiveData<List<SponsorCardContents>> = Transformations.map(dataBaseDAO.getAllAccomplishedMissions(nowMinusOneDay,-1)){
        val sponsorMissionNumbersList:HashMap<String,MutableList<Int>> = hashMapOf()
        val sponsorMissionList:HashMap<String,MutableList<String>> = hashMapOf()
        val sponsorMoneyList:HashMap<String,Int> = hashMapOf()
        for(i in it){
            if(sponsorMoneyList[i.sponsorName]!=null){
                sponsorMoneyList[i.sponsorName] = sponsorMoneyList[i.sponsorName]!!.plus(i.totalMoneyRaised)
                sponsorMissionNumbersList[i.sponsorName]!!.add(i.missionNumber)
                sponsorMissionList[i.sponsorName]!!.add(i.missionName)
            }
            else{
                sponsorMissionNumbersList[i.sponsorName] = mutableListOf(i.missionNumber)
                sponsorMissionList[i.sponsorName] = mutableListOf(i.missionName)
                sponsorMoneyList[i.sponsorName] = i.totalMoneyRaised
            }
        }
        val list:MutableList<SponsorCardContents> = mutableListOf()
        for(key in sponsorMoneyList.keys){
            list.add(SponsorCardContents(sponsorName = key,totalMoneySponsored = sponsorMoneyList[key]?:0,sponsoredMissions = sponsorMissionList[key]?: emptyList(),sponsoredMissionNumbers = sponsorMissionNumbersList[key]?: emptyList()))
        }
        list
    }
    fun toDetailMission(mission:DomainActiveMission){
        _navigateToSelectedMission.value=mission
    }
    fun toDetailMissionComplete(){
        _navigateToSelectedMission.value=null
    }
    fun toSponsorPage(sponsor:SponsorCardContents){
        _navigateToSelectedSponsorPage.value=sponsor
    }
    fun toSponsorPageComplete(){
        _navigateToSelectedSponsorPage.value=null
    }

}
