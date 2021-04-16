package com.example.us0.home.usagestats

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class UsageOverViewViewModelFactory( private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageOverViewViewModel::class.java)) {
            return UsageOverViewViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
