package com.example.us0.installedapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.ActivityHomeBinding

class HomeActivity :AppCompatActivity(){
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

    }


}