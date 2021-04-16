package com.example.us0.home.lastmission

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.missions.MissionsDatabaseDao

class LastMissionCompletedViewModelFactory(private val dataSource: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LastMissionCompletedViewModel::class.java)) {
            return LastMissionCompletedViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}