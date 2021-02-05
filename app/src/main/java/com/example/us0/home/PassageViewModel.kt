package com.example.us0.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.MissionsDatabaseDao

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
        val rulesShown = sharedPref?.getBoolean((R.string.rules_shown).toString(), false)
        return if(rulesShown!=true){
            _goToRules.value=true
            false
        } else true

    }
    fun goToRulesComplete(){
        _goToRules.value=true
    }

    init{

            if(sharedPref?.getBoolean((R.string.load_data).toString(), false) == true){
                with (sharedPref.edit()) {
                    this?.putBoolean((R.string.load_data).toString(),false)
                    this?.apply()
                }
                Log.i("hone","load data")
                toLastMission()

            }
            else{
                Log.i("hone","else")
                if(!checkUserNameInSharedPref()){
                    Log.i("hone","kjui")
                    checkChosenMissionInSharedPref()
                }
                   }


    }


}