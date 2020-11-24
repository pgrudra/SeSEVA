package com.example.us0.foregroundnnotifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ForegroundServiceViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForegroundServiceViewModel::class.java)) {
            return ForegroundServiceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}