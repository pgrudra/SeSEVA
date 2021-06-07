package com.spandverse.seseva.home.lastmission
import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.Mission
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.missions.NetworkMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
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
    var rulesNumber:Int=0
    private val _viGreatWork = MutableLiveData<Int>()
    val viGreatWork: LiveData<Int>
        get() = _viGreatWork
    private val _viContribution = MutableLiveData<Int>()
    val viContribution: LiveData<Int>
        get() = _viContribution
    private val _enableDifferentMissionButton= MutableLiveData<Boolean>()
    val enableDifferentMissionButton: LiveData<Boolean>
        get() = _enableDifferentMissionButton
    private val _enableChooseThisMissionButton= MutableLiveData<Boolean>()
    val enableChooseThisMissionButton: LiveData<Boolean>
        get() = _enableChooseThisMissionButton
    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome
    private val _goToChooseMission = MutableLiveData<Boolean>()
    val goToChooseMission: LiveData<Boolean>
        get() = _goToChooseMission
    private val _goToRules = MutableLiveData<Boolean>()
    val goToRules: LiveData<Boolean>
        get() = _goToRules
    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean>
        get() = _showProgress
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
                        this?.putString((com.spandverse.seseva.R.string.user_name).toString(), name.toString())
                        this?.apply()
                    }
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
                    var deadlineLong=0L
                        val referenceDeadline = userId?.let {
                            cloudReference.child("deadlines").child((lastMissionNumber.toString()))
                        }
                        referenceDeadline?.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val now = Calendar.getInstance().timeInMillis
                                val deadlineAsDate = dataSnapshot.getValue<String>()
                                val deadlineInMillis =
                                    deadlineAsDate?.let { deadlineStringToLong(it).plus(ONE_DAY) }
                                if (deadlineInMillis != null) {
                                    deadlineLong=deadlineInMillis
                                    if (deadlineInMillis < now) {
                                        _goToLastMissionCompleted.value = lastMissionNumber
                                    }
                                    else{
                                        _enableDifferentMissionButton.value=true
                                            viewModelScope.launch {
                                                val missionToBeSaved = Mission()
                                                missionToBeSaved.missionNumber= lastMissionNumber as Int
                                                missionToBeSaved.deadline=deadlineLong
                                                val reference2 = userId?.let {
                                                    cloudReference.child("users").child(it).child("contributions")
                                                        .child(lastMissionNumber.toString())
                                                }
                                                reference2?.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        val m = dataSnapshot.getValue<Int>()
                                                        if (m != null) {
                                                            missionToBeSaved.contribution=m
                                                            when {
                                                                m < 50 -> {
                                                                    _personalContribution.value = "Rs $m"
                                                                    _viGreatWork.value = View.INVISIBLE
                                                                    _viContribution.value = View.VISIBLE
                                                                }
                                                                else -> {
                                                                    _personalContribution.value = "Rs $m"
                                                                    _viContribution.value = View.VISIBLE
                                                                    _viGreatWork.value = View.VISIBLE
                                                                }
                                                            }
                                                        } else {
                                                            _viContribution.value = View.INVISIBLE
                                                            _viGreatWork.value = View.INVISIBLE
                                                        }
                                                        val reference3 =
                                                            cloudReference.child("Missions")
                                                                .child((lastMissionNumber.toString()))
                                                        reference3.addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                                val mission = dataSnapshot.getValue<NetworkMission>()
                                                                _lastMissionName.value = mission?.missionName
                                                                _lastMissionSponsorName.value = " " + mission?.sponsorName
                                                                rulesNumber = mission?.rulesNumber?.toInt() ?: 0
                                                                Log.i("LMVM","lMNValue=${_lastMissionName.value}")
                                                                missionToBeSaved.missionName= mission?.missionName ?: ""
                                                                Log.i("LMVM","mTBSmN=${missionToBeSaved.missionName}")
                                                                missionToBeSaved.rulesNumber=rulesNumber
                                                                missionToBeSaved.sponsorName= mission?.sponsorName ?: ""
                                                                missionToBeSaved.sponsorDescription= mission?.sponsorDescription ?: ""
                                                                missionToBeSaved.missionDescription= mission?.missionDescription ?: ""
                                                                missionToBeSaved.missionCategory= mission?.category ?: ""
                                                                missionToBeSaved.goal=mission?.goal ?:""
                                                                missionToBeSaved.sponsorNumber=mission?.sponsorNumber ?:0
                                                                val reference4 = cloudReference.child("Users Active")
                                                                    .child(lastMissionNumber.toString())
                                                                reference4.addListenerForSingleValueEvent(object :
                                                                    ValueEventListener {
                                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                                        val contri=dataSnapshot.value.toString()
                                                                        _contributors.value = contri
                                                                        if(contri.toIntOrNull()!=null){
                                                                            missionToBeSaved.usersActive=contri.toInt()
                                                                            Log.i("1LMVM","$contri")
                                                                        }
                                                                        val reference5 =
                                                                            cloudReference.child("Money Raised")
                                                                                .child(lastMissionNumber.toString())
                                                                        reference5.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                                                val tMR=dataSnapshot.value.toString()
                                                                                _totalMoneyRaised.value = tMR
                                                                                if(tMR.toIntOrNull()!=null){
                                                                                    missionToBeSaved.totalMoneyRaised=tMR.toInt()
                                                                                }
                                                                                Log.i("LMVM","wholeMission=$missionToBeSaved")
                                                                                viewModelScope.launch { database.insert(missionToBeSaved) }

                                                                            }

                                                                            override fun onCancelled(databaseError: DatabaseError) {
                                                                                Log.i(
                                                                                    "nji",
                                                                                    "loadPost:onCancelled",
                                                                                    databaseError.toException()
                                                                                )
                                                                            }
                                                                        })
                                                                    }

                                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                                        Log.i(
                                                                            "nji",
                                                                            "loadPost:onCancelled",
                                                                            databaseError.toException()
                                                                        )
                                                                    }
                                                                })
                                                                //val now = Calendar.getInstance().timeInMillis
                                                                val deadlineInMillis2 =
                                                                    mission?.deadline?.let {
                                                                        deadlineStringToLong(it).plus(
                                                                            ONE_DAY
                                                                        )
                                                                    }
                                                                val days = ((deadlineInMillis2?.minus(now))?.plus(
                                                                    ONE_DAY
                                                                )
                                                                    ?.div(ONE_DAY))?.toInt()
                                                                var s = "days"
                                                                if (days == 1) {
                                                                    s = "day"
                                                                }
                                                                val spannable =
                                                                    SpannableString("You have $days more $s\n to give your best !!")
                                                                spannable.setSpan(
                                                                    ForegroundColorSpan(
                                                                        ContextCompat.getColor(
                                                                            context,
                                                                            R.color.primary_text
                                                                        )
                                                                    ), 9, 19 + days.toString().length,
                                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                                                )

                                                                _timeLeft.value = spannable

                                                            }

                                                            override fun onCancelled(databaseError: DatabaseError) {
                                                                Log.i(
                                                                    "nji",
                                                                    "loadPost:onCancelled",
                                                                    databaseError.toException()
                                                                )
                                                            }
                                                        })

                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                        Log.i(
                                                            "nji",
                                                            "loadPost:onCancelled",
                                                            databaseError.toException()
                                                        )
                                                    }
                                                })

                                            }
                                        _enableChooseThisMissionButton.value=true

                                    }
                                }
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
        sharedPref.edit().remove((R.string.chosen_mission_number).toString()).apply()
        _goToChooseMission.value=true

    }
    fun chooseOtherMissionComplete(){
        _goToChooseMission.value=false
    }

    fun chooseThisMission(){
        _showProgress.value=true
            with (sharedPref.edit()) {
                lastMissionNumber?.let {
                    this?.putInt((R.string.chosen_mission_number).toString(),
                        it
                    )
                }
                this?.apply()
            }
        with (sharedPref.edit()) {
            this?.putBoolean((R.string.update_contributions_cloud).toString(),true)
            this?.apply()
        }
        with(sharedPref?.edit()) {
            this?.putInt(
                (R.string.rules_number).toString(),
                rulesNumber
            )
            this?.apply()
        }
            _goToHome.value = true
    }
    /*private fun checkIfRulesShown():Boolean {
        val rulesShown = sharedPref?.getBoolean((R.string.rules_shown).toString(), false)
        return if(rulesShown!=true){
            _goToRules.value=true
            false
        } else true

    }*/


    companion object {
        const val ONE_DAY: Long = 24 * 60 * 60 * 1000
    }
}
