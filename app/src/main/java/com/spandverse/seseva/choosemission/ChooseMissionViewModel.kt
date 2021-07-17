package com.spandverse.seseva.choosemission

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.os.Process
import android.util.Log
import androidx.lifecycle.*
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.ui.login.LoginViewModel
import kotlinx.coroutines.launch
import java.util.*

class ChooseMissionViewModel(
    private val database: MissionsDatabaseDao,
    application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    //private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val timer: CountDownTimer
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
    private val _navigateToSelectedMission=MutableLiveData<DomainActiveMission?>()
    val navigateToSelectedMission:LiveData<DomainActiveMission?>
        get()=_navigateToSelectedMission
    private val _drawer = MutableLiveData<Boolean>()
    val drawer: LiveData<Boolean>
        get() = _drawer
    private val _expandBottomSheet = MutableLiveData<Boolean>()
    val expandBottomSheet: LiveData<Boolean>
        get() = _expandBottomSheet
    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    val activeMissions:LiveData<List<DomainActiveMission>> = Transformations.map(database.getAllActiveMissions(nowMinusOneDay,-1)){it -> it.asActiveDomainModel()}
    fun toDetailMission(mission:DomainActiveMission){
       _navigateToSelectedMission.value=mission
    }
    fun toDetailMissionComplete(){
        _navigateToSelectedMission.value=null
    }
    companion object {
        private const val ONE_SECOND = 1000L
        private const val COUNTDOWN_TIME = 7000L
    }
    private fun checkAndLoad(){
        viewModelScope.launch {
            val loadedList=database.getActiveDownloadedMissions(nowMinusOneDay)
            val entireList:MutableList<Int> = arrayListOf()
            val moneyRaisedList:MutableList<Pair<Int,Int>> = arrayListOf()
            val moneyRaisedReference=cloudReference.child("moneyRaised")
            moneyRaisedReference.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children){
                        entireList.add(i.key.toString().toInt())
                        moneyRaisedList.add(Pair(i.key.toString().toInt(),i.value.toString().toInt()))
                    }
                    if(loadedList!=null){
                        val toDownloadList=entireList.minus(loadedList)
                        insertIntoDatabase(toDownloadList.reversed(),loadedList.reversed(),moneyRaisedList)
                    }
                    else{
                        insertIntoDatabase(entireList.reversed(),null,moneyRaisedList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
    private fun insertIntoDatabase(list: List<Int>,loadedList: List<Int>?,moneyRaisedList:MutableList<Pair<Int,Int>>){
        val contributorsList:MutableList<Pair<Int,Int>> = arrayListOf()
        val reference=cloudReference.child("contributors")
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(missionSnapShot in dataSnapshot.children ){
                    contributorsList.add(Pair(missionSnapShot.key!!.toInt(),missionSnapShot.value.toString().toInt()))
                }
                val contributionsList:MutableList<Pair<Int,Int>> = arrayListOf()
                val contributionsReference= userId?.let { cloudReference.child("users").child(it).child("contributions") }
                contributionsReference?.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(dSnapshot: DataSnapshot) {
                        for(i in dSnapshot.children){
                            contributionsList.add(Pair(i.key!!.toInt(),i.value.toString().toInt()))
                        }
                        for (i in list){
                            val missionReference=cloudReference.child("Missions").child(i.toString())
                            missionReference.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val mission:Mission? =snapshot.getValue<NetworkMission>()?.asDatabaseModel()
                                    val primaryKey= snapshot.key?.toInt() ?: 0
                                    mission?.missionNumber=primaryKey
                                    mission?.contributors= contributorsList.find{it.first==primaryKey}?.second ?:0
                                    val now: Calendar = Calendar.getInstance()
                                    mission?.missionActive=now.timeInMillis<= mission?.deadline!!
                                    mission.totalMoneyRaised=moneyRaisedList.find{it.first==primaryKey}?.second ?:0
                                    mission.contribution=contributionsList.find { it.first==primaryKey }?.second ?:0

                                    viewModelScope.launch { database.insert(mission) }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }
                        if(loadedList!=null){
                            viewModelScope.launch {
                                for (i in loadedList) {
                                    val mission = database.doesMissionExist(i)
                                    mission?.contributors=contributorsList.find{it.first==i}?.second ?:0
                                    mission?.totalMoneyRaised=moneyRaisedList.find{it.first==i}?.second ?:0
                                    mission?.let { database.update(it) }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })



            }

            override fun onCancelled(databaseError: DatabaseError) {
            }

        })


       /* val missionsReference=cloudReference.child("Missions")
        missionsReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (missionSnapshot in dataSnapshot.children) {
                    val i=missionSnapshot.getValue<NetworkMission>()
                    val mission: Mission? =i?.asDatabaseModel()

                    val primaryKey= missionSnapshot.key?.toInt() ?: 0
                    mission?.missionNumber=primaryKey
                    mission?.usersActive= usersActiveList.find{it.first==primaryKey}?.second ?:0
                    val now: Calendar = Calendar.getInstance()
                    mission?.missionActive=now.timeInMillis<= mission?.deadline!!
                    mission?.totalMoneyRaised=moneyRaisedList.find{it.first==primaryKey}?.second ?:0
                    mission?.contribution=contributionsList.find { it.first==primaryKey }?.second ?:0

                    Log.i("nji","$mission")

                    insertOrUpdate(mission)

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })*/

    }
    /*private suspend fun insert(mission: Mission){
        database.insert(mission)
    }
    private suspend fun update(mission: Mission){
        database.update(mission)
    }
    fun insertOrUpdate(mission:Mission){
        viewModelScope.launch{
            val m=database.doesMissionExist(mission.missionNumber)
            if(m!=null){
                update(mission)
            }
            else if (mission.missionNumber!=0){
                insert(mission)}
        }
        //set it to true later
        with(sharedPref?.edit()) {
            this?.putBoolean((R.string.missions_updated_today).toString(), true)
            this?.apply()}
    }*/
    init {
        checkAndLoad()
        checkUsageAccessPermission()
      timer = object : CountDownTimer(
            COUNTDOWN_TIME, ONE_SECOND
        ) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onTimeUp()
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        // Cancel the timer
        timer.cancel()
    }
fun startCountDown(){
    timer.start()
}
    private fun onTimeUp() {
        _expandBottomSheet.value=true
    }

    private fun checkUsageAccessPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.packageName?.let {
                appOps.unsafeCheckOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        } else {
            context?.packageName?.let {
                appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        }
        _drawer.value = mode == AppOpsManager.MODE_ALLOWED
    }
}