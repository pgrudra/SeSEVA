package com.spandverse.seseva.home.appintro

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.home.askname.AskNameViewModel


class IntroToAppViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IntroToAppViewModel::class.java)) {
            return IntroToAppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}