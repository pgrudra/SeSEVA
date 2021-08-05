package com.spandverse.seseva.home.askname

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.data.missions.MissionsDatabaseDao

class AskNameViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AskNameViewModel::class.java)) {
            return AskNameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}