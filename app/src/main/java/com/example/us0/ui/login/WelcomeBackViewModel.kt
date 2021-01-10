package com.example.us0.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.data.missions.MissionsDatabaseDao

class WelcomeBackViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _goToHomeActivity = MutableLiveData<Boolean>()
    val goToHomeActivity: LiveData<Boolean>
        get() = _goToHomeActivity
    private val _userName=MutableLiveData<String>()
    val userName:LiveData<String>
        get()=_userName

    fun toHomeActivity(){
        _goToHomeActivity.value=true
    }
    fun goToHomeActivityComplete(){
        _goToHomeActivity.value=false
    }

    fun setUserName(userName: String) {
_userName.value= "welcome back $userName"
    }
}