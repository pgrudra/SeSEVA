package com.example.us0.InstalledApps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SignOutViewModelFactory():ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignOutViewModel::class.java)) {
            return SignOutViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}