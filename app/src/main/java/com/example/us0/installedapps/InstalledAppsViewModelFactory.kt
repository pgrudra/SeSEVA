package com.example.us0.installedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InstalledAppsViewModelFactory:ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InstalledAppsViewModel::class.java)) {
            return InstalledAppsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}