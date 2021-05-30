package com.example.us0.choosemission

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.us0.data.sponsors.SponsorDatabaseDao

class SponsorDetailsViewModelFactory(private val sponsorNumber:Int,private val sponsorDatabaseDao:SponsorDatabaseDao, private val application: Application): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SponsorDetailsViewModel::class.java)) {
            return SponsorDetailsViewModel(sponsorNumber,sponsorDatabaseDao,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}