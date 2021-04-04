package com.example.us0.home.feats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.choosemission.ChooseMissionViewModel
import com.example.us0.data.missions.MissionsDatabaseDao

class FeatsViewModelFactory(private val dataBaseDAO: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeatsViewModel::class.java)) {
            return FeatsViewModel(dataBaseDAO,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}