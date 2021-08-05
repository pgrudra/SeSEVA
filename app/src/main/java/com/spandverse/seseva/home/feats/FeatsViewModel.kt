package com.spandverse.seseva.home.feats

import android.app.Application
import androidx.lifecycle.*
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.missions.asActiveDomainModel
import com.spandverse.seseva.data.sponsors.PartialSponsor
import com.spandverse.seseva.data.sponsors.Sponsor
import com.spandverse.seseva.data.sponsors.SponsorDatabaseDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class FeatsViewModel(
    missionsDatabaseDao: MissionsDatabaseDao,
    private val sponsorsDatabaseDao: SponsorDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    private val cloudReference = Firebase.database.reference
    private val _navigateToSelectedMission=MutableLiveData<DomainActiveMission?>()
    val navigateToSelectedMission:LiveData<DomainActiveMission?>
        get()=_navigateToSelectedMission
    private val _navigateToSelectedSponsorPage=MutableLiveData<Int?>()
    val navigateToSelectedSponsorPage:LiveData<Int?>
        get()=_navigateToSelectedSponsorPage
    val missions:LiveData<List<DomainActiveMission>> = Transformations.map(missionsDatabaseDao.getAllMissions()){it.asActiveDomainModel()}
    /*private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    private val _activeMissions = MutableLiveData<String>()
    val activeMissions: LiveData<String>
        get() = _activeMissions
    private val _totalMissions = MutableLiveData<String>()
    val totalMissions: LiveData<String>
        get() = _totalMissions*/
    //val activeMissions=Transformations.map(missionsDatabaseDao.getActiveMissionsCount(nowMinusOneDay)){it.toString()}
    val totMoneyRaised=Transformations.map(missionsDatabaseDao.getTotalMoneyRaised()){ "Rs $it" }
    //val totalMissions=Transformations.map(missionsDatabaseDao.getMissionsCount(-1)){it.toString()}
    val sponsors: LiveData<List<Sponsor>> = sponsorsDatabaseDao.getAllSponsors()
        /*dataBaseDAO.getAllAccomplishedMissions(nowMinusOneDay,-1)){
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
        list*/

    fun toDetailMission(mission:DomainActiveMission){
        _navigateToSelectedMission.value=mission
    }
    fun toDetailMissionComplete(){
        _navigateToSelectedMission.value=null
    }
    fun toSponsorPage(sponsorNumber:Int){
        _navigateToSelectedSponsorPage.value=sponsorNumber
    }
    fun toSponsorPageComplete(){
        _navigateToSelectedSponsorPage.value=null
    }
init {
    cloudReference.child("sponsorsCount").addListenerForSingleValueEvent(object :
        ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            getListAndDownload(snapshot.getValue<Int>())
        }

        override fun onCancelled(error: DatabaseError) {
        }
    })
}

    private fun getListAndDownload(sponsorsCount: Int?) {
        val entireList=(1..sponsorsCount!!).toList()
        viewModelScope.launch {
            val downLoadedList=sponsorsDatabaseDao.getDownloadedSponsors()
            if(downLoadedList==null || downLoadedList.isEmpty()){
                downloadSponsors(entireList)
            }
            else{
                downloadSponsors(entireList.minus(downLoadedList))
                refreshDownloadedList(downLoadedList)
            }


        }

    }
    private fun refreshDownloadedList(downLoadedList: List<Int>) {
        for(i in downLoadedList){
cloudReference.child("sponsors").child(i.toString()).child("missionsSponsored").addListenerForSingleValueEvent(object :
    ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        val missionsSponsoredList=if(snapshot.value==null) "" else snapshot.value.toString()
        cloudReference.child("sponsors").child(i.toString()).child("missionAmounts").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val missionAmounts = snapshot.value
                val sponsoredAmount =
                    missionAmounts?.toString()?.split(",")?.map { it.trim().toInt() }
                        ?.sum() ?: 0
                viewModelScope.launch {
                    sponsorsDatabaseDao.update(
                        PartialSponsor(
                            i,
                            missionsSponsored = missionsSponsoredList,
                            missionAmounts = missionAmounts?.toString() ?: "",
                            sponsoredAmount = sponsoredAmount
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCancelled(error: DatabaseError) {
    }
})
        }
    }

    private fun downloadSponsors(toDownloadList: List<Int>) {
        if(toDownloadList.isNotEmpty()){
            for(i in toDownloadList.reversed()){
                cloudReference.child("sponsors").child(i.toString()).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val missionAmounts = snapshot.child("missionAmounts").value
                        val sponsoredAmount =
                            missionAmounts?.toString()?.split(",")?.map { it.trim().toInt() }
                                ?.sum() ?: 0
                        val mSponsor = Sponsor(
                            sponsorNumber = i,
                            sponsorName = snapshot.child("sponsorName").value.toString(),
                            sponsorDescription = snapshot.child("sponsorDescription").value.toString(),
                            sponsorAddress = snapshot.child("sponsorAddress").value.toString(),
                            sponsorSite = snapshot.child("sponsorSite").value.toString(),
                            missionsSponsored = if(snapshot.child("missionsSponsored").value==null) "" else snapshot.child("missionsSponsored").value.toString(),
                            missionAmounts=missionAmounts?.toString() ?: "",
                            sponsoredAmount = sponsoredAmount
                        )
                        viewModelScope.launch {
                            sponsorsDatabaseDao.insert(mSponsor)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
    }
}
