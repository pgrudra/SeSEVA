package com.example.us0.choosemission

import android.app.Application
import android.graphics.Color
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import java.util.*

class DetailMissionViewModel(mission:DomainActiveMission, application: Application):AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _selectedMission=MutableLiveData<DomainActiveMission>()
    val selectedMission:LiveData<DomainActiveMission>
        get()=_selectedMission
    private val _showDetailMissionDescription=MutableLiveData<Boolean>()
    val showDetailMissionDescription:LiveData<Boolean>
        get()=_showDetailMissionDescription
    private val _trigger=MutableLiveData<SpannableString>()
    val trigger:LiveData<SpannableString>
        get()=_trigger
    private val _daysLeft=MutableLiveData<String>()
    val daysLeft:LiveData<String>
        get()=_daysLeft
    private val _knowMore=MutableLiveData<Boolean>()
    val knowMore:LiveData<Boolean>
        get()=_knowMore
    private val _thisMissionChosen=MutableLiveData<Boolean>()
    val thisMissionChosen:LiveData<Boolean>
        get()=_thisMissionChosen
    private val _toChooseMission=MutableLiveData<Boolean>()
    val toChooseMission:LiveData<Boolean>
        get()=_toChooseMission

    init {
    _selectedMission.value=mission
    _showDetailMissionDescription.value=false
    makeTriggerText()

}

    private fun makeTriggerText() {
        val now= Calendar.getInstance().timeInMillis
        val intDaysLeft=((_selectedMission.value!!.deadline-now+ ONE_DAY)/ Companion.ONE_DAY)+1
        if(intDaysLeft<10){
            _daysLeft.value= "0$intDaysLeft"
        }
        else{
            _daysLeft.value= intDaysLeft.toString()
        }
        val possibleMoney=intDaysLeft*6
        val spannable=SpannableString("Your chance to add\nRs $possibleMoney more\nbefore mission closes !!")
        spannable.setSpan(ForegroundColorSpan(Color.WHITE),19,27+possibleMoney.toString().length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        _trigger.value= spannable
//18,26+possibleMoney.toString().length
    }

fun toSponsorWebsite(){
    _knowMore.value=true
}
    fun toSponsorWebsiteComplete(){
        _knowMore.value=false
    }
    fun thisMissionChosen(){

    }
    fun thisMissionChosenComplete(){

    }
    fun toChooseMission(){
        _toChooseMission.value=true
    }
    fun toChooseMissionComplete(){

    }
    fun expandOrContract(){
        _showDetailMissionDescription.value = _showDetailMissionDescription.value != true
    }

    companion object {
        const val ONE_DAY=24*60*60*1000
    }

}