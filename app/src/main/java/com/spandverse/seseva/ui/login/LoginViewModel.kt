package com.spandverse.seseva.ui.login

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel


class LoginViewModel() : ViewModel() {
    private val timer: CountDownTimer
    companion object {

        // Time when the game is over
        private const val DONE = 0L

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time for the game
        private const val COUNTDOWN_TIME = 45000L

    }
    private val _backToLoginScreen= MutableLiveData<Boolean>()
    val backToLoginScreen: LiveData<Boolean>
        get() = _backToLoginScreen

    private val _enableResendButton= MutableLiveData<Boolean>()
    val enableResendButton: LiveData<Boolean>
        get() = _enableResendButton

    private val _resendEmail= MutableLiveData<Boolean>()
    val resendEmail: LiveData<Boolean>
        get() = _resendEmail

    private val _emailAddress =MutableLiveData<String?>()
    val emailAddress: LiveData<String?>
        get()=_emailAddress

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime
    val currentTimeString = Transformations.map(currentTime) { time ->
       if(time.toInt()!=0) {"You can resend in ${time}s"}
        else ""
    }

    fun changeEmail(){
    _backToLoginScreen.value=true
}
    fun backToLoginScreenComplete(){
        _backToLoginScreen.value=false
    }

    fun resend(){
        _resendEmail.value=true
        _backToLoginScreen.value=true
    }
fun resendComplete(){
    _resendEmail.value=false
    _backToLoginScreen.value=false
}
    fun putEmailAddress(email:String){
        if(email==""){
            _emailAddress.value=null
        }
        else{
            _emailAddress.value=email
        }
    }
    fun onTimeUp(){
        _enableResendButton.value=true
    }
    fun disableResendButton(){
        _enableResendButton.value=false

        timer.start()
    }
    init {

        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished/ONE_SECOND)
            }
            override fun onFinish() {
                _currentTime.value = DONE
onTimeUp()
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the timer
        timer.cancel()
    }

}