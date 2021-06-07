package com.spandverse.seseva.home.usagestats

import android.app.Application
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
