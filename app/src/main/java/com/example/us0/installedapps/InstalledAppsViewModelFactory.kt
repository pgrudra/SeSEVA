package com.example.us0.installedapps

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.apps.AppDataBaseDao

class InstalledAppsViewModelFactory(private val dataSource: AppDataBaseDao, private val application: Application, private val pm:PackageManager):ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstalledAppsViewModel::class.java)) {
            return InstalledAppsViewModel(dataSource,application,pm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}