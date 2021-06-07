package com.spandverse.seseva.home.yourpreviousmissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.data.missions.MissionsDatabaseDao

class YPMViewModelFactory(private val dataSource: MissionsDatabaseDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YPMViewModel::class.java)) {
            return YPMViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}