package com.example.us0.usagestats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class UsageStatsViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageStatsViewModel::class.java)) {
            return UsageStatsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
