package com.example.us0.ui.login

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.us0.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AskNameViewModel(application: Application) : AndroidViewModel(application)  {
    private val context = getApplication<Application>().applicationContext
    private val user = Firebase.auth.currentUser
    private val _nameInsertDone = MutableLiveData<Boolean>()
    val nameInsertDone: LiveData<Boolean>
        get() = _nameInsertDone

    private val _goToNextFragment = MutableLiveData<Boolean>()
    val goToNextFragment: LiveData<Boolean>
        get() = _goToNextFragment

    private val _userName =MutableLiveData<String>()
    val userName: LiveData<String>
        get()=_userName

    fun nameInserted(){
        _nameInsertDone.value=true
    }

    fun saveEverywhere(userName: String){
        insertIntoSharedPref((userName))
        insertUsernameIntoFirebase(userName)
        insertIntoCloudDatabase(userName)
        _nameInsertDone.value=false
        _goToNextFragment.value=true
    }

    private fun insertUsernameIntoFirebase(userName:String){

        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("LL",userName)
                }
            }
    }



    private fun insertIntoCloudDatabase(userName: String){
        val userId=user!!.uid
        var database: DatabaseReference = Firebase.database.reference
        database.child("users").child(userId).child("username").setValue(userName)
            .addOnSuccessListener{Log.i("IOIO","PASS")}
            .addOnFailureListener { Log.i("IOIO","FAIL") }
    }

    private fun insertIntoSharedPref(userName: String){
        val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString((R.string.user_name).toString(), userName)
            this?.apply()
        }
    }

    fun goToNextFragmentComplete(){
        _goToNextFragment.value=false
    }
    fun checkUserName() {
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid
                val name = profile.displayName
                _userName.value=name?:"User Name"
            }
        }
    }

}