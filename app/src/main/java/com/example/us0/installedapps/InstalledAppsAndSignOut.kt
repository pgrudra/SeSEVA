package com.example.us0.installedapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.ActivityInstalledAppsAndSignOutBinding

class InstalledAppsAndSignOut : AppCompatActivity() {
    lateinit var binding: ActivityInstalledAppsAndSignOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_installed_apps_and_sign_out)

    }
}