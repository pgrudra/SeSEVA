package com.spandverse.seseva.home.closedmissions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.data.missions.MissionsDatabaseDao

class AMDViewModelFactory(private val dataSource: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AMDViewModel::class.java)) {
            return AMDViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}