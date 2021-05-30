package com.example.us0.choosemission

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.data.missions.MissionsDatabaseDao

class DetailMissionViewModelFactory(private val mission:DomainActiveMission, private val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailMissionViewModel::class.java)) {
            return DetailMissionViewModel(mission,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}