package com.example.us0.foregroundnnotifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PermissionViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            return PermissionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

