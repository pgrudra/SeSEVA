package com.spandverse.seseva.foregroundnnotifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PermissionViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            return PermissionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

