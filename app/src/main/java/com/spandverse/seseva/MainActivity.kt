package com.spandverse.seseva

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.spandverse.seseva.databinding.ActivityMainBinding


class MainActivity: AppCompatActivity(){

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*FirebaseApp.initializeApp(*//*context=*//* this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance())

        val intent=intent
        val emailLink=intent.data.toString()
        val sharedPref = getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString((R.string.email_link).toString(), emailLink)
            apply()
        }*/
        val intent=intent
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if(intent.extras?.getBoolean("Link Verification Failed",false) == true){
            Snackbar.make(
                binding.root,
                "Link Verification Failed.",
                Snackbar.LENGTH_SHORT
            ).show()

        }
    }

}