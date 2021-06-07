package com.spandverse.seseva

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.spandverse.seseva.databinding.ActivityMainBinding


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