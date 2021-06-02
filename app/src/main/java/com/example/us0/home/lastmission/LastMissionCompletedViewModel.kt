package com.example.us0.home.lastmission

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
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

class LastMissionCompletedViewModel(private val database: MissionsDatabaseDao, application: Application) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
    private val _enableButton = MutableLiveData<Boolean>()
    val enableButton: LiveData<Boolean>
        get() = _enableButton
    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome
    private val _lastMissionName = MutableLiveData<String>()
    val lastMissionName: LiveData<String>
        get() = _lastMissionName

    private val _lastMissionSponsorName = MutableLiveData<String>()
    val lastMissionSponsorName: LiveData<String>
        get() = _lastMissionSponsorName

    private val _personalContribution = MutableLiveData<SpannableString>()
    val personalContribution: LiveData<SpannableString>
        get() = _personalContribution

    private val _totalMoneyRaised = MutableLiveData<String>()
    val totalMoneyRaised: LiveData<String>
        get() = _totalMoneyRaised
    private val _contributors = MutableLiveData<String>()
    val contributors: LiveData<String>
        get() = _contributors
    private val _userName=MutableLiveData<String>()
    val userName:LiveData<String>
        get()=_userName
init {
    loadUserName()
}

    private fun loadUserName() {
        _userName.value =" "+ sharedPref?.getString((R.string.user_name).toString(), "")
    }

    fun loadMission(missionNumber: Int) {
            val reference2 = userId?.let {
                cloudReference.child("users").child(it).child("contributions")
                    .child(missionNumber.toString())
            }
            reference2?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val personalContri=dataSnapshot.value.toString()
                    val spannable= SpannableString("You raised \nRs $personalContri \nfor this charity")
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context,R.color.primary_text)),11,15+personalContri.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    _personalContribution.value = spannable
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            val reference3 = cloudReference.child("Missions").child((missionNumber.toString()))
            reference3.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mission = dataSnapshot.getValue<NetworkMission>()
                    _lastMissionName.value = mission?.missionName+"met it's goal"
                    _lastMissionSponsorName.value=mission?.sponsorName
                    val reference4 =
                        cloudReference.child("Users Active").child(missionNumber.toString())
                    reference4.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            _contributors.value = dataSnapshot.value.toString()+" total contributors"
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            val reference5 =
                cloudReference.child("Money Raised").child(missionNumber.toString())
            reference5.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _totalMoneyRaised.value = dataSnapshot.value.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        _enableButton.value=true
    }

    fun onGoToHomeComplete() {
        _goToHome.value=false
    }
    fun onGoToHome(){
        _goToHome.value=true
    }
}