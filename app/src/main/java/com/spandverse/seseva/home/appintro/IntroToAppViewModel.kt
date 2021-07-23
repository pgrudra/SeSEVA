package com.spandverse.seseva.home.appintro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.internal.t

class IntroToAppViewModel(application: Application) : AndroidViewModel(application) {
    private val _slideNumber = MutableLiveData<Int>()
    val slideNumber: LiveData<Int>
        get() = _slideNumber
    private val _goToNextScreen = MutableLiveData<Boolean>()
    val goToNextScreen: LiveData<Boolean>
        get() = _goToNextScreen
    init{
        _slideNumber.value=0
    }
    fun previousSlide(){
        _slideNumber.value = _slideNumber.value?.minus(1)
    }
    fun nextSlide(){
        _slideNumber.value = _slideNumber.value?.plus(1)?:1
    }
    fun nextScreen(){
        _goToNextScreen.value= true
    }
    fun nextScreenComplete(){
        _goToNextScreen.value= false
    }
}