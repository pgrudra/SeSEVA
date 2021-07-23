package com.spandverse.seseva.contributionupdate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.home.usagestats.UsageOverViewViewModel
import kotlinx.coroutines.launch
import java.util.*


class ContributionUpdateViewModel(application: Application): AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val missionDb= AllDatabase.getInstance(application).MissionsDatabaseDao
    private val categoryStatsDb=AllDatabase.getInstance(application).CategoryStatDatabaseDao
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val _ydaReward = MutableLiveData<String>()
    val ydaReward: LiveData<String>
        get() = _ydaReward
    private val _buttonText = MutableLiveData<String>()
    val buttonText: LiveData<String>
        get() = _buttonText
    private val _dailyReward = MutableLiveData<String>()
    val dailyReward: LiveData<String>
        get() = _dailyReward
    private val _weeklyReward = MutableLiveData<String>()
    val weeklyReward: LiveData<String>
        get() = _weeklyReward
    private val _totPenalty = MutableLiveData<String>()
    val totPenalty: LiveData<String>
        get() = _totPenalty

    private val _t1 = MutableLiveData<String>()
    val t1: LiveData<String>
        get() = _t1

    private val _missionName = MutableLiveData<String>()
    val missionName: LiveData<String>
        get() = _missionName
    private val _sponsorName = MutableLiveData<String>()
    val sponsorName: LiveData<String>
        get() = _sponsorName
    private val _goToHome = MutableLiveData<Boolean>()
    val goToHome: LiveData<Boolean>
        get() = _goToHome
    private val _nonZeroMoneyRaised = MutableLiveData<Boolean>()
    val nonZeroMoneyRaised: LiveData<Boolean>
        get() = _nonZeroMoneyRaised
    private val _raisedNothingText = MutableLiveData<String>()
    val raisedNothingText: LiveData<String>
        get() = _raisedNothingText
    private val _penaltiesExpandContractInvisible= MutableLiveData<Boolean>()
    val penaltiesExpandContractInvisible: LiveData<Boolean>
        get()=_penaltiesExpandContractInvisible
    private val _showRewardDetails = MutableLiveData<Boolean>()
    val showRewardDetails: LiveData<Boolean>
        get() = _showRewardDetails
    private val _showPenaltyDetails = MutableLiveData<Boolean>()
    val showPenaltyDetails: LiveData<Boolean>
        get() = _showPenaltyDetails
    private val _showUsageDetails = MutableLiveData<Boolean>()
    val showUsageDetails: LiveData<Boolean>
        get() = _showUsageDetails

    private val _socialST = MutableLiveData<String>()
    val socialST: LiveData<String>
        get() = _socialST
    private val _videoST = MutableLiveData<String>()
    val videoST: LiveData<String>
        get() = _videoST
    private val _gamesST = MutableLiveData<String>()
    val gamesST: LiveData<String>
        get() = _gamesST
    private val _comST = MutableLiveData<String>()
    val comST: LiveData<String>
        get() = _comST
    private val _msnbsST = MutableLiveData<String>()
    val msnbsST: LiveData<String>
        get() = _msnbsST
    private val _othersST = MutableLiveData<String>()
    val othersST: LiveData<String>
        get() = _othersST
    private val _whitelistedST = MutableLiveData<String>()
    val whitelistedST: LiveData<String>
        get() = _whitelistedST
    private val _entertainmentST = MutableLiveData<String>()
    val entertainmentST: LiveData<String>
        get() = _entertainmentST

    private val _socialAL = MutableLiveData<String>()
    val socialAL: LiveData<String>
        get() = _socialAL
    private val _videoAL = MutableLiveData<String>()
    val videoAL: LiveData<String>
        get() = _videoAL
    private val _gamesAL = MutableLiveData<String>()
    val gamesAL: LiveData<String>
        get() = _gamesAL
    private val _comAL = MutableLiveData<String>()
    val comAL: LiveData<String>
        get() = _comAL
    private val _msnbsAL = MutableLiveData<String>()
    val msnbsAL: LiveData<String>
        get() = _msnbsAL
    private val _othersAL = MutableLiveData<String>()
    val othersAL: LiveData<String>
        get() = _othersAL
    private val _whitelistedAL = MutableLiveData<String>()
    val whitelistedAL: LiveData<String>
        get() = _whitelistedAL
    private val _entertainmentAL = MutableLiveData<String>()
    val entertainmentAL: LiveData<String>
        get() = _entertainmentAL

    private val _socialPenalty = MutableLiveData<String>()
    val socialPenalty: LiveData<String>
        get() = _socialPenalty
    private val _videoPenalty = MutableLiveData<String>()
    val videoPenalty: LiveData<String>
        get() = _videoPenalty
    private val _gamesPenalty = MutableLiveData<String>()
    val gamesPenalty: LiveData<String>
        get() = _gamesPenalty
    private val _comPenalty = MutableLiveData<String>()
    val comPenalty: LiveData<String>
        get() = _comPenalty
    private val _msnbsPenalty = MutableLiveData<String>()
    val msnbsPenalty: LiveData<String>
        get() = _msnbsPenalty
    private val _othersPenalty = MutableLiveData<String>()
    val othersPenalty: LiveData<String>
        get() = _othersPenalty
    private val _entertainmentPenalty = MutableLiveData<String>()
    val entertainmentPenalty: LiveData<String>
        get() = _entertainmentPenalty

    private val _totTime = MutableLiveData<String>()
    val totTime: LiveData<String>
        get() = _totTime
    private val _totLaunches = MutableLiveData<String>()
    val totLaunches: LiveData<String>
        get() = _totLaunches
    private val _youRaisedAmount = MutableLiveData<String>()
    val youRaisedAmount: LiveData<String>
        get() = _youRaisedAmount

    fun rewardExpandOrContract() {
        _showRewardDetails.value = _showRewardDetails.value != true
    }
    fun penaltyExpandOrContract() {
        _showPenaltyDetails.value = _showPenaltyDetails.value != true
    }
    fun usageExpandOrContract() {
        _showUsageDetails.value = _showUsageDetails.value != true
    }
    fun toHome(){
        _goToHome.value=true
    }
    private fun inHrsMins(i: Int?): String {
        if(i!=null){
            val hrs:Int=(i/ ONE_HOUR_IN_SECONDS)
            val mins:Int=(i% ONE_HOUR_IN_SECONDS)/60
            when (hrs) {
                0 -> {
                    return if(mins==1)
                        "1 min"
                    else
                        "$mins mins"
                }
                1 -> {
                    return if(mins==1)
                        "1 hr 1 min"
                    else
                        "1 hr $mins mins"
                }
                else -> {
                    return if(mins==1)
                        "$hrs hrs 1 min"
                    else
                        "$hrs hrs $mins mins"
                }
            }
        }
        else return "0 mins"
    }
    init {
        var penalty = 0
        val missionNumber = sharedPref.getInt((R.string.chosen_mission_number).toString(), 0)
        viewModelScope.launch {
            val mission = missionDb.doesMissionExist(missionNumber)
            if (mission != null) {
                val categoryPenalties: HashMap<String, Int> = hashMapOf(
                    "TOTAL" to 0,
                    "SOCIAL" to sharedPref.getInt((R.string.social_penalty).toString(), 0),
                    "COMM. & BROWSING" to sharedPref.getInt(
                        (R.string.communication_penalty).toString(),
                        0
                    ),
                    "GAMES" to sharedPref.getInt((R.string.games_penalty).toString(), 0),
                    "WHITELISTED" to 0,
                    "VIDEO & COMICS" to sharedPref.getInt((R.string.video_penalty).toString(), 0),
                    "ENTERTAINMENT" to sharedPref.getInt(
                        (R.string.entertainment_penalty).toString(),
                        0
                    ),
                    "MSNBS" to sharedPref.getInt((R.string.msnbs_penalty).toString(), 0),
                    "OTHERS" to sharedPref.getInt((R.string.others_penalty).toString(), 0)
                )
                val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
                if(mission.deadline<nowMinusOneDay){
                    _missionName.value = context.getString(R.string.mission_is_accomplished, mission.missionName)
                    _buttonText.value=context.getString(R.string.choose_new_mission)
                }
                else{
                    _missionName.value = context.getString(R.string.you_are_on_2, mission.missionName)
                    _buttonText.value=context.getString(R.string.go_to_home)
                }
                _sponsorName.value =
                    context.getString(R.string.sponsored_by_sponsor_name, mission.sponsorName)
                val dayOfWeek = sharedPref.getInt((R.string.days_after_installation).toString(), 1)
                var ydaRewardVal=0
                val dailyR = sharedPref.getInt((R.string.daily_reward).toString(), 0)
                _dailyReward.value = context.getString(R.string.rs, dailyR)
                if (dayOfWeek == 0) {
                    val weeklyR = sharedPref.getInt((R.string.weekly_reward).toString(), 0)
                    _ydaReward.value = context.getString(R.string.rs, dailyR + weeklyR)
                    _weeklyReward.value = context.getString(R.string.rs, weeklyR)
                    ydaRewardVal=dailyR+weeklyR
                } else {
                    _ydaReward.value = context.getString(R.string.rs, dailyR)
                    ydaRewardVal=dailyR
                }
                val ydaTime: Calendar = Calendar.getInstance()
                ydaTime.set(Calendar.HOUR_OF_DAY, 0)
                ydaTime.set(Calendar.MINUTE, 0)
                ydaTime.set(Calendar.SECOND, 0)
                ydaTime.set(Calendar.MILLISECOND, 0)
                ydaTime.add(Calendar.DATE, -1)
                val dailyCatStats =
                    categoryStatsDb.getCategoryStatsOfChosenDate(ydaTime.timeInMillis)
                if (dailyCatStats != null) {
                    for (i in dailyCatStats) {
                            when (i.categoryName) {
                                "SOCIAL" -> {
                                    _socialST.value=inHrsMins(i.timeSpent)
                                    _socialAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["SOCIAL"] ?: 0
                                        _socialPenalty.value =
                                            context.getString(
                                                R.string.rs,
                                                categoryPenalties["SOCIAL"]
                                            )
                                    }
                                }
                                "COMM. & BROWSING" -> {
                                    _comST.value=inHrsMins(i.timeSpent)
                                    _comAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["COMM. & BROWSING"] ?: 0
                                        _comPenalty.value = context.getString(
                                            R.string.rs,
                                            categoryPenalties["COMM. & BROWSING"]
                                        )
                                    }
                                }
                                "GAMES" -> {
                                    _gamesST.value=inHrsMins(i.timeSpent)
                                    _gamesAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["GAMES"] ?: 0
                                        _gamesPenalty.value =
                                            context.getString(
                                                R.string.rs,
                                                categoryPenalties["GAMES"]
                                            )
                                    }
                                }
                                "VIDEO & COMICS" -> {
                                    _videoST.value=inHrsMins(i.timeSpent)
                                    _videoAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["VIDEO & COMICS"] ?: 0
                                        _videoPenalty.value = context.getString(
                                            R.string.rs,
                                            categoryPenalties["VIDEO & COMICS"]
                                        )
                                    }
                                }
                                "OTHERS" -> {
                                    _othersST.value=inHrsMins(i.timeSpent)
                                    _othersAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["OTHERS"] ?: 0
                                        _othersPenalty.value =
                                            context.getString(
                                                R.string.rs,
                                                categoryPenalties["OTHERS"]
                                            )
                                    }
                                }
                                "MSNBS" -> {
                                    _msnbsST.value=inHrsMins(i.timeSpent)
                                    _msnbsAL.value=(i.appLaunches?:0).toString()
                                    if (i.ruleViolated == true) {
                                        penalty += categoryPenalties["MSNBS"] ?: 0
                                        _msnbsPenalty.value =
                                            context.getString(
                                                R.string.rs,
                                                categoryPenalties["MSNBS"]
                                            )
                                    }
                                }
                                "ENTERTAINMENT"->{
                                    _entertainmentST.value=inHrsMins(i.timeSpent)
                                    _entertainmentAL.value=(i.appLaunches?:0).toString()
                                }
                                "WHITELISTED"->{
                                    _whitelistedST.value=inHrsMins(i.timeSpent)
                                    _whitelistedAL.value=(i.appLaunches?:0).toString()
                                }
                                "TOTAL"->{
                                    _totTime.value=context.getString(R.string.tot_s_t_sentence,inHrsMins(i.timeSpent))
                                    _totLaunches.value=context.getString(R.string.tot_a_l_sentence,i.appLaunches)
                                }
                            }

                    }
                } else {
                    _goToHome.value = true
                }
                if (dayOfWeek == 0) {
                    val weeklyCatStats = categoryStatsDb.getTimeLaunchesDate(
                        "ENTERTAINMENT",
                        ydaTime.timeInMillis - ONE_DAY * 7
                    )
                    var weeklyEntertainmentTime = 0
                    var weeklyEntertainmentLaunches = 0
                    for (i in weeklyCatStats) {
                        weeklyEntertainmentTime += (i.time ?: 0)
                        weeklyEntertainmentLaunches += (i.launches ?: 0)
                    }
                    val entertainmentTimeRule =
                        sharedPref.getInt((R.string.entertainment_max_time).toString(), 0)
                    val entertainmentLaunchesRule =
                        sharedPref.getInt((R.string.entertainment_max_launches).toString(), 0)
                    if (weeklyEntertainmentTime > entertainmentTimeRule || weeklyEntertainmentLaunches > entertainmentLaunchesRule) {
                        penalty += categoryPenalties["ENTERTAINMENT"] ?: 0
                        _entertainmentPenalty.value =
                            context.getString(R.string.rs, categoryPenalties["ENTERTAINMENT"])
                    }
                }
                val userName=sharedPref.getString((R.string.user_name).toString(),"User")?:"User"
                when {
                    penalty==0 -> {
                        _penaltiesExpandContractInvisible.value=true
                        _t1.value=context.getString(R.string.p0,userName)
                        //great work text
                    }
                    penalty>5 -> {
                        _t1.value=context.getString(R.string.p1, userName)
                        //can do better
                    }
                    else -> {
                        //can do much better
                        _t1.value=context.getString(R.string.p2,userName)
                    }
                }
                _totPenalty.value=context.getString(R.string.rs,penalty)
                when {
                    ydaRewardVal>penalty -> {
                        _nonZeroMoneyRaised.value=true
                        _youRaisedAmount.value=context.getString(R.string.rs,ydaRewardVal-penalty)
                    }
                    ydaRewardVal==penalty -> {
                        _nonZeroMoneyRaised.value=false
                        _raisedNothingText.value=context.getString(R.string.raised_nothing_equalled)
                    }
                    else -> {
                        _nonZeroMoneyRaised.value=false
                        //raisedNothingTextSet
                        _raisedNothingText.value=context.getString(R.string.raised_nothing_exceeded)
                    }
                }
            }
                else {
                    _goToHome.value = true
                }



        }
    }

    companion object {
        private const val ONE_DAY:Long=24*60*60*1000
        private const val ONE_HOUR_IN_SECONDS=60*60
    }

}