package com.example.us0.ui.login

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.us0.R
import com.example.us0.data.missions.Mission
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.data.missions.NetworkMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.*

class AskNameViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application)  {
    private val context = getApplication<Application>().applicationContext
    private val user = Firebase.auth.currentUser
    private lateinit var cloudDatabase: DatabaseReference
    private val _nameInsertDone = MutableLiveData<Boolean>()
    val nameInsertDone: LiveData<Boolean>
        get() = _nameInsertDone

    private val _goToNextFragment = MutableLiveData<Boolean>()
    val goToNextFragment: LiveData<Boolean>
        get() = _goToNextFragment

    private val _userName =MutableLiveData<String?>()
    val userName: LiveData<String?>
        get()=_userName

    fun nameInserted(){
        _nameInsertDone.value=true
    }

    fun saveEverywhere(userName: String){
        insertIntoSharedPref((userName))
        insertUsernameIntoFirebase(userName)
        insertIntoCloudDatabase(userName)
        _nameInsertDone.value=false
        _goToNextFragment.value=true
    }

    private fun insertUsernameIntoFirebase(userName:String){

        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("LL",userName)
                }
            }
    }



    private fun insertIntoCloudDatabase(userName: String){
        val userId=user!!.uid
        var database: DatabaseReference = Firebase.database.reference
        database.child("users").child(userId).child("username").setValue(userName)
            .addOnSuccessListener{Log.i("IOIO","PASS")}
            .addOnFailureListener { Log.i("IOIO","FAIL") }
    }

    private fun insertIntoSharedPref(userName: String){
        val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString((R.string.user_name).toString(), userName)
            this?.apply()
        }
    }

    fun goToNextFragmentComplete(){
        _goToNextFragment.value=false
    }
    fun checkUserName() {
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid
                Log.i("opiul","$providerId")
                _userName.value=profile.displayName
                Log.i("opiul","${_userName.value}")
            }
        }
    }
    init{
        loadActiveMissions()
    }

    private fun loadActiveMissions(){
        GlobalScope.launch{
            insertIntoDatabase()
        }
    }
    private suspend fun insertIntoDatabase(){
        cloudDatabase = Firebase.database.reference

        val usersActiveList:MutableList<Pair<Int,Int>> = arrayListOf()
        val usersActiveReference=cloudDatabase.child("Users Active")
        usersActiveReference.addListenerForSingleValueEvent(object:ValueEventListener{
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
        val moneyRaisedReference=cloudDatabase.child("Money Raised")
        moneyRaisedReference.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(missionSnapShot in dataSnapshot.children ){
                    moneyRaisedList.add(Pair(missionSnapShot.key!!.toInt(),missionSnapShot.value.toString().toInt()))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
            }

        })

        val missionsReference=cloudDatabase.child("Missions")
        missionsReference.addListenerForSingleValueEvent(object:ValueEventListener{
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

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })

    }
}