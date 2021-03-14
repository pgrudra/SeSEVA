package com.example.us0.home

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.apps.AppDataBaseDao
import com.example.us0.data.missions.MissionsDatabaseDao

class HomeViewModelFactory(private val dataSource: MissionsDatabaseDao,private val appDataSource: AppDataBaseDao, private val application: Application, private val pm: PackageManager): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dataSource,appDataSource,application,pm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}