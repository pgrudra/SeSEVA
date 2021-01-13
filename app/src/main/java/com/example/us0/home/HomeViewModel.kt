package com.example.us0.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.us0.data.missions.MissionsDatabaseDao

class HomeViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application) {
}