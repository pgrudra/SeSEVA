package com.example.us0.home.feats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.us0.data.missions.MissionsDatabaseDao

class FeatsViewModel(
    private val dataBaseDAO: MissionsDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

}
