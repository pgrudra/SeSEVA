package com.spandverse.seseva.home.lastmission

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class LastMissionCompletedViewModel(private val database: MissionsDatabaseDao, application: Application) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val userId = Firebase.auth.currentUser?.uid
    private val cloudReference = Firebase.database.reference
    private val _enableButton = MutableLiveData<Boolean>()
    val enableButton: LiveData<Boolean>
        get() = _enableButton
    private val _downLoadReport = MutableLiveData<Boolean>()
    val downLoadReport: LiveData<Boolean>
        get() = _downLoadReport
    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome
    private val _reportAvailable = MutableLiveData<Boolean>()
    val reportAvailable: LiveData<Boolean>
        get() = _reportAvailable
    private val _lastMissionName = MutableLiveData<String>()
    val lastMissionName: LiveData<String>
        get() = _lastMissionName
    private val _missionName = MutableLiveData<String>()
    val missionName: LiveData<String>
        get() = _missionName
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
        _userName.value =context.getString(R.string.welcome_back,sharedPref?.getString((R.string.user_name).toString(), "")?:"")
    }

    fun loadMission(missionNumber: Int) {
            val reference2 = userId?.let {
                cloudReference.child("users").child(it).child("contributions")
                    .child(missionNumber.toString())
            }
            reference2?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val personalContri=dataSnapshot.value.toString()
                    val spannable= SpannableString("You raised Rs $personalContri for this mission")
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),10,14+personalContri.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    _personalContribution.value = spannable
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
            cloudReference.child("accomplishedMissions").child((missionNumber.toString())).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val missionName=dataSnapshot.child("missionName").value.toString()
                    _missionName.value=missionName
                    val deadline=dataSnapshot.child("deadline").value.toString().replace("-"," " )
                    _reportAvailable.value=dataSnapshot.child("reportAvailable").getValue<Boolean>()
                    _lastMissionName.value=context.getString(R.string.mission_n_deadline,missionName,deadline)
                    _contributors.value=dataSnapshot.child("contributors").value.toString()
                    _totalMoneyRaised.value=dataSnapshot.child("moneyRaised").value.toString()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }

            })
        _enableButton.value=true
    }
    fun downloadReport(){
        _downLoadReport.value=true
    }
    fun downloadReportComplete(){
        _downLoadReport.value=true
    }
    fun onGoToHomeComplete() {
        _goToHome.value=false
    }
    fun onGoToHome(){
        _goToHome.value=true
    }
}