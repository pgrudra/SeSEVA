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

    private val _connectedToNetwork = MutableLiveData<Boolean>()
    val connectedToNetwork: LiveData<Boolean>
        get() = _connectedToNetwork

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
        else{
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

    init{
        if(checkInternetConnectivity()){
            _connectedToNetwork.value=true
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
        else{
            _connectedToNetwork.value=false
        }
    }


}