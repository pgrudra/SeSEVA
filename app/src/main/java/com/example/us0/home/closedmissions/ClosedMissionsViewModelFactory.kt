package com.example.us0.home.closedmissions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.home.HomeViewModel

class ClosedMissionsViewModelFactory(private val dataSource: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClosedMissionsViewModel::class.java)) {
            return ClosedMissionsViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}