package com.spandverse.seseva.contributionupdate

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spandverse.seseva.foregroundnnotifications.PermissionViewModel


class ContributionUpdateViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContributionUpdateViewModel::class.java)) {
            return ContributionUpdateViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}