package com.example.us0.drawoverotherapps

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DOOAViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DOOAViewModel::class.java)) {
            return DOOAViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}