package com.example.us0.signoutpackage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.ActivitySignOutBinding

class SignOutActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_out)

    }
}