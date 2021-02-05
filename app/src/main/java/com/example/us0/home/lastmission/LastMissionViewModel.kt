package com.example.us0.home.lastmission

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.fonts.FontFamily
import android.graphics.fonts.FontStyle
import android.opengl.Visibility
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
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
    private val _viGreatWork = MutableLiveData<Int>()
    val viGreatWork: LiveData<Int>
        get() = _viGreatWork
    private val _viContribution = MutableLiveData<Int>()
    val viContribution: LiveData<Int>
        get() = _viContribution

    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome
    private val _goToRules = MutableLiveData<Boolean>()
    val goToRules: LiveData<Boolean>
        get() = _goToRules
    private val _goToLastMissionCompleted = MutableLiveData<Int>()
    val goToLastMissionCompleted: LiveData<Int>
        get() = _goToLastMissionCompleted

    private val _lastMissionName = MutableLiveData<String>()
    val lastMissionName: LiveData<String>
        get() = _lastMissionName
    private val _lastMissionSponsorName = MutableLiveData<String>()
    val lastMissionSponsorName: LiveData<String>
        get() = _lastMissionSponsorName
    private val _personalContribution = MutableLiveData<String>()
    val personalContribution: LiveData<String>
        get() = _personalContribution
    private val _totalMoneyRaised = MutableLiveData<String>()
    val totalMoneyRaised: LiveData<String>
        get() = _totalMoneyRaised
    private val _contributors = MutableLiveData<String>()
    val contributors: LiveData<String>
        get() = _contributors
    private val _timeLeft = MutableLiveData<SpannableString>()
    val timeLeft: LiveData<SpannableString>
        get() = _timeLeft
    private val _userName=MutableLiveData<String>()
    val userName:LiveData<String>
        get()=_userName

    init {
        loadUserName()
    }

    private fun loadUserName() {
        val reference0 =
            userId?.let { cloudReference.child("users").child(it).child("username") }
        reference0?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name=dataSnapshot.value
                if(name!=null)
                {
                    _userName.value=name.toString()
                    with (sharedPref?.edit()) {
                        this?.putString((com.example.us0.R.string.user_name).toString(), name.toString())
                        this?.apply()
                    }
                    userName.value?.let { Log.i("ni", it) }
                    loadLastMission()
                }
                else
                    _goToHome.value=true
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                //check internet
            }
        })

    }

    private fun loadLastMission() {
        val reference1 =
            userId?.let { cloudReference.child("users").child(it).child("chosenMission") }
        reference1?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lastMissionNumber = dataSnapshot.value.toString().toIntOrNull()
                if (lastMissionNumber != null) {
                    val referenceDeadline = userId?.let {
                        cloudReference.child("Missions").child((lastMissionNumber.toString()))
                            .child("deadline")
                    }
                    referenceDeadline?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val now = Calendar.getInstance().timeInMillis
                            val deadlineAsDate = dataSnapshot.getValue<String>()
                            val deadlineInMillis =
                                deadlineAsDate?.let { deadlineStringToLong(it).plus(Companion.ONE_DAY) }
                            if (deadlineInMillis != null) {
                                if (deadlineInMillis < now) {
                                    _goToLastMissionCompleted.value = lastMissionNumber
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                        }
                    })
                    val reference2 = userId?.let { cloudReference.child("users").child(it).child("contributions")
                        .child(lastMissionNumber.toString())
                    }
                    reference2?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val m=dataSnapshot.getValue<Int>()
                            if (m != null) {
                                when {
                                    m<50 -> {
                                        _personalContribution.value = "Rs $m"
                                        _viGreatWork.value= View.INVISIBLE
                                        _viContribution.value=View.VISIBLE
                                    }
                                    else -> {
                                        _personalContribution.value = "Rs $m"
                                        _viContribution.value=View.VISIBLE
                                        _viGreatWork.value=View.VISIBLE
                                    }
                                }
                            }
                            else{
                                _viContribution.value=View.INVISIBLE
                                _viGreatWork.value=View.INVISIBLE
                            }



                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                        }
                    })

                    val reference3 = cloudReference.child("Missions").child((lastMissionNumber.toString()))
                    reference3.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val mission = dataSnapshot.getValue<NetworkMission>()
                            _lastMissionName.value=mission?.missionName
                           _lastMissionSponsorName.value=" "+mission?.sponsorName
                            val reference4 = cloudReference.child("Users Active").child(lastMissionNumber.toString())
                            reference4.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    _contributors.value = dataSnapshot.value.toString()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                                }
                            })
                            val now = Calendar.getInstance().timeInMillis
                            val deadlineInMillis =
                                mission?.deadline?.let { deadlineStringToLong(it).plus(Companion.ONE_DAY) }
                            val days = ((deadlineInMillis?.minus(now))?.plus(ONE_DAY)?.div(ONE_DAY))?.toInt()
                            var s="days"
                            if(days==1){
                                s="day"
                            }
                            val spannable= SpannableString("You have $days more $s\n to give your best !!")
                            spannable.setSpan(
                                ForegroundColorSpan(Color.WHITE),9,19+days.toString().length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                            _timeLeft.value= spannable

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                        }
                    })

                    val reference5 = cloudReference.child("Money Raised").child(lastMissionNumber.toString())
                    reference5.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            _totalMoneyRaised.value = dataSnapshot.value.toString()
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("nji", "loadPost:onCancelled", databaseError.toException())
                        }
                    })
                } else {
            _goToHome.value = true
        }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i("nji", "loadPost:onCancelled", databaseError.toException())
            }
        })


            //download last active mission, contributions to that specific mission
            //show status of last active mission
            //Either continue last active mission in case it's still active or choose another mission.
            //If continue last mission: Save it in SharedPref and return Home. Else directly return home.
            //Before returning, set loadData to false


    }

    fun goToHomeComplete() {
        _goToHome.value = false
    }
    fun goToRulesComplete() {
        _goToRules.value = false
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

    fun chooseThisMission(){
            with (sharedPref.edit()) {
                lastMissionNumber?.let {
                    this?.putInt((R.string.chosen_mission_number).toString(),
                        it
                    )
                }
                this?.apply()
            }
        if (checkIfRulesShown()) {
            _goToHome.value = true
        }
    }
    private fun checkIfRulesShown():Boolean {
        val rulesShown = sharedPref?.getBoolean((R.string.rules_shown).toString(), false)
        return if(rulesShown!=true){
            _goToRules.value=true
            false
        } else true

    }


    companion object {
        const val ONE_DAY: Long = 24 * 60 * 60 * 1000
    }
}