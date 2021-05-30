package com.example.us0.home.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.missions.MissionsDatabaseDao

class ProfileViewModelFactory(private val dataBaseDAO: MissionsDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(dataBaseDAO,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}