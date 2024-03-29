package com.spandverse.seseva.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.data.missions.MissionsDatabaseDao

class PassageViewModelFactory(private val dataSource: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PassageViewModel::class.java)) {
            return PassageViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}