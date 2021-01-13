package com.example.us0.home.lastmission

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.data.missions.NetworkMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LastMissionViewModel(private val database: MissionsDatabaseDao, application: Application) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
    var lastMissionNumber: Int? = null

    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome

    private val _lastMissionName = MutableLiveData<String>()
    val lastMissionName: LiveData<String>
        get() = _lastMissionName
    private val _lastMissionDetails = MutableLiveData<String>()
    val lastMissionDetails: LiveData<String>
        get() = _lastMissionDetails

    private val _lastMissionSponsorName = MutableLiveData<String>()
    val lastMissionSponsorName: LiveData<String>
        get() = _lastMissionSponsorName
    private val _lastMissionSponsorDetails = MutableLiveData<String>()
    val lastMissionSponsorDetails: LiveData<String>
        get() = _lastMissionSponsorDetails

    private val _active = MutableLiveData<Boolean>()
    val active: LiveData<Boolean>
        get() = _active

    private val _personalContribution = MutableLiveData<Int>()
    val personalContribution: LiveData<Int>
        get() = _personalContribution

    private val _reportMissionNumber = MutableLiveData<Int>()
    val reportMissionNumber: LiveData<Int>
        get() = _reportMissionNumber

    private val _totalMoneyRaised = MutableLiveData<Int>()
    val totalMoneyRaised: LiveData<Int>
        get() = _totalMoneyRaised
    private val _contributors = MutableLiveData<Int>()
    val contributors: LiveData<Int>
        get() = _contributors
    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String>
        get() = _timeLeft
    private val _otherMission = MutableLiveData<String>()
    val otherMission: LiveData<String>
        get() = _otherMission
    private val _thisMission = MutableLiveData<String>()
    val thisMission: LiveData<String>
        get() = _thisMission


    init {
        loadLastMission()
    }

    private fun loadLastMission() {

        val reference1 =
            userId?.let { cloudReference.child("users").child(it).child("chosenMission") }
        reference1?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lastMissionNumber = dataSnapshot.value.toString().toIntOrNull()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
            }
        })
        if (lastMissionNumber != null) {

            val reference2 = userId?.let { cloudReference.child("users").child(it).child("contributions")
                    .child(lastMissionNumber.toString())
            }
            reference2?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _personalContribution.value = dataSnapshot.getValue<Int>()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                }
            })
            val reference3 = cloudReference.child("missions").child((lastMissionNumber.toString()))
            reference3.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mission = dataSnapshot.getValue<NetworkMission>()
                    _lastMissionName.value=mission?.missionName
                    _lastMissionDetails.value=mission?.missionDescription
                    val now = Calendar.getInstance().timeInMillis
                    val deadlineInMillis =
                        deadlineStringToLong(mission?.deadline!!).plus(Companion.ONE_DAY)
                    _active.value = deadlineInMillis > now
                    if (_active.value == true) {
                        val reference4 = cloudReference.child("Users Active").child(lastMissionNumber.toString())
                        reference4.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                _contributors.value = dataSnapshot.getValue<Int>()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                            }
                        })
                        val days = ((deadlineInMillis - now) / ONE_DAY).toInt()
                        if (days < 10) {
                            _timeLeft.value = "0$days"
                        } else {
                            _timeLeft.value = days.toString()
                        }
                        _otherMission.value=context.getString(R.string.choose_a_different_mission)
                        _thisMission.value=context.getString(R.string.continue_this_mission)
                    }
                    else{
                        _otherMission.value=context.getString(R.string.choose_new_mission)
                        _thisMission.value=context.getString(R.string.see_report)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                }
            })
            val reference5 = cloudReference.child("Money Raised").child(lastMissionNumber.toString())
            reference5.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _totalMoneyRaised.value = dataSnapshot.getValue<Int>()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                }
            })

        } else {
            _goToHome.value = true
        }
        //download last active mission, contributions to that specific mission
        //show status of last active mission
        //Either continue last active mission in case it's still active or choose another mission.
        //If continue last mission: Save it in SharedPref and return Home. Else directly return home.
        //Before returning, set loadData to false


    }

    fun goToHomeComplete() {
        _goToHome.value = false
    }

    private fun deadlineStringToLong(s: String): Long {
        var l: Long = 0L
        val f = SimpleDateFormat("dd-MMM-yyyy")
        try {
            val d: Date? = f.parse(s)
            l = d?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return l
    }

    fun chooseOtherMission(){
        _goToHome.value=true
    }
    fun chooseThisMissionOrReport(){
        if(_active.value!!){
            with (sharedPref.edit()) {
                lastMissionNumber?.let {
                    this?.putInt((R.string.chosen_mission_number).toString(),
                        it
                    )
                }
                this?.apply()
            }
            _goToHome.value=true
        }
        else{
            _reportMissionNumber.value=lastMissionNumber

        }
    }


    companion object {
        const val ONE_DAY: Long = 24 * 60 * 60 * 1000
    }
}
