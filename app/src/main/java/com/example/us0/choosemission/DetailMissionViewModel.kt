package com.example.us0.choosemission

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Process
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class DetailMissionViewModel(mission:DomainActiveMission, application: Application):AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val cloudReference = Firebase.database.reference
    private val user = Firebase.auth.currentUser
    private val _selectedMission=MutableLiveData<DomainActiveMission>()
    val selectedMission:LiveData<DomainActiveMission>
        get()=_selectedMission
    private val _showDetailMissionDescription=MutableLiveData<Boolean>()
    val showDetailMissionDescription:LiveData<Boolean>
        get()=_showDetailMissionDescription
    private val _trigger=MutableLiveData<SpannableString>()
    val trigger:LiveData<SpannableString>
        get()=_trigger
    private val _trigger2=MutableLiveData<SpannableString>()
    val trigger2:LiveData<SpannableString>
        get()=_trigger2
    private val _daysLeft=MutableLiveData<String>()
    val daysLeft:LiveData<String>
        get()=_daysLeft
    private val _chooseMissionButtonText=MutableLiveData<String>()
    val chooseMissionButtonText:LiveData<String>
        get()=_chooseMissionButtonText
    private val _goToSponsorScreen=MutableLiveData<Boolean>()
    val goToSponsorScreen:LiveData<Boolean>
        get()=_goToSponsorScreen
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
    private val _drawer=MutableLiveData<Boolean>()
    val drawer:LiveData<Boolean>
        get()=_drawer
    init {
    _selectedMission.value=mission
    _showDetailMissionDescription.value=false
    checkUsageAccessPermission()
        setChooseMissionButtonText()
}

    private fun setChooseMissionButtonText() {
        if(sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0)?:0 != 0){
            _chooseMissionButtonText.value=context.getString(R.string.switch_to_this_mission)

        }
        else{
            _chooseMissionButtonText.value=context.getString(R.string.choose_this_mission)
        }
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

  fun makeTriggerText( contribution: Int) {
      val now= Calendar.getInstance().timeInMillis
      val intDaysLeft=((_selectedMission.value!!.deadline-now+ ONE_DAY)/ Companion.ONE_DAY)+1
      if(intDaysLeft<10){
          _daysLeft.value= "0$intDaysLeft"
      }
      else{
          _daysLeft.value= intDaysLeft.toString()
      }
      var possibleMoney=0
          cloudReference.child("rules").child(_selectedMission.value!!.rulesNumber.toString())
              .child("dailyReward").addListenerForSingleValueEvent(object :
              ValueEventListener {
              override fun onDataChange(snapshot: DataSnapshot) {
                  val dailyReward = snapshot.value.toString().toInt()
                  cloudReference.child("rules")
                      .child(_selectedMission.value!!.rulesNumber.toString()).child("weeklyReward")
                      .addListenerForSingleValueEvent(object :
                          ValueEventListener {
                          override fun onDataChange(snapshot: DataSnapshot) {
                              val weeklyReward = snapshot.value.toString().toInt()
                              possibleMoney =
                                  (dailyReward * intDaysLeft + weeklyReward * (intDaysLeft / 7)).toInt()
                              furtherTriggerText(contribution,possibleMoney)
                          }

                          override fun onCancelled(error: DatabaseError) {
                              if(!checkInternetConnectivity()){
                                  _noInternet.value=true
                              }
                          }
                      })
              }

              override fun onCancelled(error: DatabaseError) {
                  if(!checkInternetConnectivity()){
                      _noInternet.value=true
                  }
              }
          })



    }

    private fun furtherTriggerText(contribution: Int, possibleMoney: Int) {
        if(contribution==0){
            val spannable=SpannableString("Your chance to add\nRs $possibleMoney more\nbefore mission closes")
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.primary_text)),19,27+possibleMoney.toString().length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            _trigger.value= spannable
        }
        else{
            val spannable=SpannableString("You raised\nRs $contribution")
            spannable.setSpan(RelativeSizeSpan(2f),10,14+contribution.toString().length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.secondary_text)),0,10,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            _trigger.value=spannable
            val spannable2=SpannableString("Your chance to add Rs $possibleMoney more\nbefore mission closes")
            spannable2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.primary_text)),19,27+possibleMoney.toString().length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            _trigger2.value=spannable2
        }
//18,26+possibleMoney.toString().length
    }

    fun toSponsorScreen(){
    _goToSponsorScreen.value=true
}
    fun toSponsorScreenComplete(){
        _goToSponsorScreen.value=false
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
                    Log.i("DMVM","${_selectedMission.value!!.missionNumber}")
                    with(sharedPref?.edit()) {
                        this?.putInt(
                            (R.string.rules_number).toString(),
                            _selectedMission.value!!.rulesNumber
                        )
                        this?.apply()
                    }
                    Log.i("DMVM","${_selectedMission.value!!.rulesNumber}")

                    _toRulesFragment.value=true
                    /*if (checkIfRulesShown()) {
                        _toHomeFragment.value = true
                    }*/
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