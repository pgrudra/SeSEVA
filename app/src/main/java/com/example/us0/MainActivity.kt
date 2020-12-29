package com.example.us0

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.us0.databinding.ActivityMainBinding
import com.example.us0.ui.login.LinkVerificationFragment


class MainActivity: AppCompatActivity(){

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent=intent
        val emailLink=intent.data.toString()
        val sharedPref = getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString((R.string.email_link).toString(), emailLink)
            apply()
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}