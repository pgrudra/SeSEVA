package com.example.us0.home.rules

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.MissionsDatabaseDao

class Rules2ViewModel(private val database: MissionsDatabaseDao, application: Application) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val _toHomeFragment = MutableLiveData<Boolean>()
    val toHomeFragment: LiveData<Boolean>
        get() = _toHomeFragment
    init {
        with (sharedPref?.edit()) {
            this?.putBoolean((R.string.rules_shown).toString(), true)
            this?.apply()
        }
    }
    fun goToHome(){
        _toHomeFragment.value=true
    }
    fun goToHomeComplete(){
        _toHomeFragment.value=false
    }


}
