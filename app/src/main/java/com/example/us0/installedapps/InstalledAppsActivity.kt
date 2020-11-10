package com.example.us0.installedapps

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.ActivityInstalledAppsBinding

class InstalledAppsActivity :AppCompatActivity(){
    lateinit var binding: ActivityInstalledAppsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_installed_apps)

    }


}