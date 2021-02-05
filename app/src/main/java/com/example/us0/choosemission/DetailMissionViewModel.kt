package com.example.us0.choosemission

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class DetailMissionViewModel(mission:DomainActiveMission, application: Application):AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _selectedMission=MutableLiveData<DomainActiveMission>()
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val cloudReference = Firebase.database.reference
    private val user = Firebase.auth.currentUser
    val selectedMission:LiveData<DomainActiveMission>
        get()=_selectedMission
    private val _showDetailMissionDescription=MutableLiveData<Boolean>()
    val showDetailMissionDescription:LiveData<Boolean>
        get()=_showDetailMissionDescription
    private val _trigger=MutableLiveData<SpannableString>()
    val trigger:LiveData<SpannableString>
        get()=_trigger
    private val _daysLeft=MutableLiveData<String>()
    val daysLeft:LiveData<String>
        get()=_daysLeft
    private val _knowMore=MutableLiveData<Boolean>()
    val knowMore:LiveData<Boolean>
        get()=_knowMore
    private val _noInternet=MutableLiveData<Boolean>()
    val noInternet:LiveData<Boolean>
        get()=_noInternet
    private val _thisMissionChosen=MutableLiveData<Boolean>()
    val thisMissionChosen:LiveData<Boolean>
        get()=_thisMissionChosen
    private val _toChooseMission=MutableLiveData<Boolean>()
    val toChooseMission:LiveData<Boolean>
        get()=_toChooseMission
    private val _toRulesFragment=MutableLiveData<Boolean>()
    val toRulesFragment:LiveData<Boolean>
        get()=_toRulesFragment
    private val _toHomeFragment=MutableLiveData<Boolean>()
    val toHomeFragment:LiveData<Boolean>
        get()=_toHomeFragment
    init {
    _selectedMission.value=mission
    _showDetailMissionDescription.value=false
    makeTriggerText()

}

    private fun makeTriggerText() {
        val now= Calendar.getInstance().timeInMillis
        val intDaysLeft=((_selectedMission.value!!.deadline-now+ ONE_DAY)/ Companion.ONE_DAY)+1
        if(intDaysLeft<10){
            _daysLeft.value= "0$intDaysLeft"
        }
        else{
            _daysLeft.value= intDaysLeft.toString()
        }
        val possibleMoney=intDaysLeft*6
        val spannable=SpannableString("Your chance to add\nRs $possibleMoney more\nbefore mission closes !!")
        spannable.setSpan(ForegroundColorSpan(Color.WHITE),19,27+possibleMoney.toString().length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        _trigger.value= spannable
//18,26+possibleMoney.toString().length
    }

fun toSponsorWebsite(){
    _knowMore.value=true
}
    fun toSponsorWebsiteComplete(){
        _knowMore.value=false
    }
    fun thisMissionChosen(){
        if(!checkInternetConnectivity()){
            _noInternet.value=true
        }
        else {
            val userId = user!!.uid
            cloudReference.child("users").child(userId).child("chosenMission")
                .setValue(_selectedMission.value!!.missionNumber)
                .addOnSuccessListener {
                    Log.i("IOIO", "PASS")
                    with(sharedPref?.edit()) {
                        this?.putInt(
                            (R.string.chosen_mission_number).toString(),
                            _selectedMission.value!!.missionNumber
                        )
                        this?.apply()
                    }
                    if (checkIfRulesShown()) {
                        _toHomeFragment.value = true
                    }
                }
                .addOnFailureListener {
                    Log.i("IOIO", "FAIL")

                }
        }
    }

    private fun checkIfRulesShown():Boolean {
            val rulesShown = sharedPref?.getBoolean((R.string.rules_shown).toString(), false)
            return if(rulesShown!=true){
                _toRulesFragment.value=true
                false
            } else true

    }
fun toRulesFragmentComplete(){
    _toRulesFragment.value=false
}
    fun thisMissionChosenComplete(){
_toHomeFragment.value=false
    }
    fun toChooseMission(){
        _toChooseMission.value=true
    }
    fun toChooseMissionComplete(){
        _toChooseMission.value=false
    }
    fun expandOrContract(){
        _showDetailMissionDescription.value = _showDetailMissionDescription.value != true
    }
    private fun checkInternetConnectivity(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    null
                }
            return if (capabilities != null) {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                false
            }
        }
        else return false
    }

    companion object {
        const val ONE_DAY=24*60*60*1000
    }

}