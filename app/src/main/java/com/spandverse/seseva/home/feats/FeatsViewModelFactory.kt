package com.spandverse.seseva.home.feats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.sponsors.SponsorDatabaseDao

class FeatsViewModelFactory(private val missionsDataBaseDAO: MissionsDatabaseDao,private val sponsorsDataBaseDAO:SponsorDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeatsViewModel::class.java)) {
            return FeatsViewModel(missionsDataBaseDAO,sponsorsDataBaseDAO,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}