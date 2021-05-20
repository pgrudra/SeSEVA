package com.example.us0.drawoverotherapps

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.Actions
import com.example.us0.foregroundnnotifications.TestService

class  DOOAViewModel(application: Application): AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _grantPermission= MutableLiveData<Boolean>()
    val grantPermission: LiveData<Boolean>
        get()=_grantPermission
    private val _disclosureVisible= MutableLiveData<Boolean>()
    val disclosureVisible: LiveData<Boolean>
        get()=_disclosureVisible
    private val _helpVisible= MutableLiveData<Boolean>()
    val helpVisible: LiveData<Boolean>
        get()=_helpVisible
    private val _toHome= MutableLiveData<Boolean>()
    val toHome: LiveData<Boolean>
        get()=_toHome
    fun onGrantPermission(){
        _grantPermission.value=true
    }
    fun onGrantPermissionComplete(){
        _grantPermission.value=false
    }

    fun usageDisclosure(){
        _disclosureVisible.value=true
    }
    fun usageDisclosureGone(){
        _disclosureVisible.value=false
    }

}