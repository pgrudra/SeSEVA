package com.example.us0.installedapps

import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class InstalledAppsViewModel: ViewModel() {

    private val _goToSignOut=MutableLiveData<Boolean>()
    val goToSignOut:LiveData<Boolean>
    get()=_goToSignOut

    fun onGoTOSignOut(){
        _goToSignOut.value=true
    }
    fun onGoTOSignOutComplete(){
        _goToSignOut.value=false
    }

}