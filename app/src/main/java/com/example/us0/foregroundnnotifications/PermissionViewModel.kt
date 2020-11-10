package com.example.us0.foregroundnnotifications


import android.app.Application
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel(): ViewModel() {
    //private val context = getApplication<Application>().applicationContext

    private val _grantPermission= MutableLiveData<Boolean>()
    val grantPermission:LiveData<Boolean>
        get()=_grantPermission




    fun onGrantPermission(){
        _grantPermission.value=true

    }
    fun onGrantPermissionComplete(){
        _grantPermission.value=false
        Log.i("O","KJO")
    }


}