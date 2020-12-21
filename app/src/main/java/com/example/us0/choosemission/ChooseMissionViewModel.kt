package com.example.us0.choosemission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.us0.data.missions.MissionsDatabaseDao

class ChooseMissionViewModel(
    private val database: MissionsDatabaseDao,
    application: Application,
) : AndroidViewModel(application) {

}