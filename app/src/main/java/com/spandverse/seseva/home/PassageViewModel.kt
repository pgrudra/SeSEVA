package com.spandverse.seseva.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.MissionsDatabaseDao

class PassageViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application)  {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)

    private val _goToAskNameFragment = MutableLiveData<Boolean>()
    val goToAskNameFragment: LiveData<Boolean>
        get() = _goToAskNameFragment

    private val _goToChooseMissionFragment = MutableLiveData<Boolean>()
    val goToChooseMissionFragment: LiveData<Boolean>
        get() = _goToChooseMissionFragment

    private val _goToLastMissionFragment = MutableLiveData<Boolean>()
    val goToLastMissionFragment: LiveData<Boolean>
        get() = _goToLastMissionFragment
    private val _goToHomeFragment = MutableLiveData<Boolean>()
    val goToHomeFragment: LiveData<Boolean>
        get() = _goToHomeFragment
    private val _goToRules = MutableLiveData<Boolean>()
    val goToRules: LiveData<Boolean>
        get() = _goToRules


    private fun checkUserNameInSharedPref():Boolean {
        //if null, goto choose mission fragment
        val userName = sharedPref?.getString((R.string.user_name).toString(), null)
        return if(userName==null){
            _goToAskNameFragment.value=true
            true
        } else false
    }
    private fun checkChosenMissionInSharedPref() {
        //if 0, goto choose mission fragment
        val chosenMissionNumber = sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0)?:0
        if(chosenMissionNumber==0){
            _goToChooseMissionFragment.value=true
        }
        else if(checkIfRulesShown()){
            _goToHomeFragment.value=true
        }
    }
    fun goToHomeFragmentComplete(){
        _goToHomeFragment.value=false
    }
    private fun toLastMission() {
        _goToLastMissionFragment.value=true
    }
    fun goToLastMissionFragmentComplete(){
        _goToLastMissionFragment.value=false
    }
    fun goToAskNameFragmentComplete(){
        _goToAskNameFragment.value=false
    }
    fun goToChosenMissionFragmentComplete(){
        _goToChooseMissionFragment.value=false
    }
    private fun checkIfRulesShown():Boolean {
       /* val rulesShown = sharedPref?.getBoolean((R.string.rules_shown).toString(), false)
        return if(rulesShown!=true){
            _goToRules.value=true
            false
        } else true*/
        val rN=sharedPref.getInt((R.string.rules_number).toString(),-1)
        val sRN=sharedPref.getInt((R.string.saved_rules_number).toString(),-2)
        return if(rN==sRN){
            true
        } else{
            _goToRules.value=true
            false
        }

    }
    fun goToRulesComplete(){
        _goToRules.value=false
    }

    init{
        /*context?.let { FirebaseApp.initializeApp(*//*context=*//* it) }
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance())*/
            if(sharedPref?.getBoolean((R.string.load_data).toString(), false) == true){
                with (sharedPref.edit()) {
                    this?.putBoolean((R.string.load_data).toString(),false)
                    this?.apply()
                }
                toLastMission()
            }
            else{
                if(!checkUserNameInSharedPref()){
                    checkChosenMissionInSharedPref()
                }
                   }


    }


}