package com.example.us0.home.rules

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.apps.AppDataBaseDao

class Rules2ViewModelFactory(private val dataSource: AppDataBaseDao, private val application: Application, private val pm: PackageManager): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Rules2ViewModel::class.java)) {
            return Rules2ViewModel(dataSource,application,pm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}