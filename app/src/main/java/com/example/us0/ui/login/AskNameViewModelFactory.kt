package com.example.us0.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AskNameViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AskNameViewModel::class.java)) {
            return AskNameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}