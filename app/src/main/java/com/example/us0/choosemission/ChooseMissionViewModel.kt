package com.example.us0.choosemission

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.lifecycle.*
import com.example.us0.R
import com.example.us0.data.missions.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import java.util.*

class ChooseMissionViewModel(
    private val database: MissionsDatabaseDao,
    application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val cloudReference = Firebase.database.reference

    private val _navigateToSelectedMission=MutableLiveData<DomainActiveMission?>()
    val navigateToSelectedMission:LiveData<DomainActiveMission?>
        get()=_navigateToSelectedMission
    private val _drawer = MutableLiveData<Boolean>()
    val drawer: LiveData<Boolean>
        get() = _drawer
    private val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
    val activeMissions:LiveData<List<DomainActiveMission>> = Transformations.map(database.getAllActiveMissions(nowMinusOneDay)){it.asActiveDomainModel()}
    fun toDetailMission(mission:DomainActiveMission){
       _navigateToSelectedMission.value=mission
    }
    fun toDetailMissionComplete(){
        _navigateToSelectedMission.value=null
    }

    private fun checkIfUpdated(){
            val missionsUpdatedToday:Boolean =
                sharedPref?.getBoolean((R.string.missions_updated_today).toString(), false) ?: false
            if(!missionsUpdatedToday)
                {Log.i("nji","iikkjo")
                    insertIntoDatabase()}

    }
    private  fun insertIntoDatabase(){
        val usersActiveList:MutableList<Pair<Int,Int>> = arrayListOf()
        val usersActiveReference=cloudReference.child("Users Active")
        usersActiveReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(missionSnapShot in dataSnapshot.children ){
                    usersActiveList.add(Pair(missionSnapShot.key!!.toInt(),missionSnapShot.value.toString().toInt()))
                    Log.i("nji","$usersActiveList")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
            }
        })

        val moneyRaisedList:MutableList<Pair<Int,Int>> = arrayListOf()
        val moneyRaisedReference=cloudReference.child("Money Raised")
        moneyRaisedReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(missionSnapShot in dataSnapshot.children ){
                    moneyRaisedList.add(Pair(missionSnapShot.key!!.toInt(),missionSnapShot.value.toString().toInt()))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
            }

        })

        val missionsReference=cloudReference.child("Missions")
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
                    Log.i("nji","$mission")

                    insertOrUpdate(mission)

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }
    private suspend fun insert(mission: Mission){
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
    }
    init {
        Log.i("nji","hhuuh")
        checkIfUpdated()
        checkUsageAccessPermission()
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