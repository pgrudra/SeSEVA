package com.example.us0.home.askname

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.us0.R
import com.example.us0.checkInternetConnectivity
import com.example.us0.data.missions.MissionsDatabaseDao
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AskNameViewModel(private val database: MissionsDatabaseDao, application: Application) : AndroidViewModel(application)  {
    private val context = getApplication<Application>().applicationContext
    private val user = Firebase.auth.currentUser
    private val cloudDatabase: DatabaseReference = Firebase.database.reference
    private val _nameInsertDone = MutableLiveData<Boolean>()
    val nameInsertDone: LiveData<Boolean>
        get() = _nameInsertDone

    private val _goToNextFragment = MutableLiveData<Boolean>()
    val goToNextFragment: LiveData<Boolean>
        get() = _goToNextFragment

    private val _userName =MutableLiveData<String?>()
    val userName: LiveData<String?>
        get()=_userName

    private val _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean>
        get() = _noInternet

    fun nameInserted(){
        _noInternet.value=false
        _nameInsertDone.value=true

    }

    fun saveEverywhere(userName: String){
        if(checkInternetConnectivity(context)){
            insertIntoSharedPref(userName)
            viewModelScope.launch {
                Log.i("ANVM","p")
                insertIntoCloudDatabase(userName) }

            _nameInsertDone.value=false
            _goToNextFragment.value=true
        }
        else{
            _noInternet.value=true
            Log.i("fg","x")
        }

    }
    private fun insertIntoCloudDatabase(userName: String ){
        val userId= user!!.uid
        cloudDatabase.child("users").child(userId).child("username").setValue(userName)
            .addOnSuccessListener{
                insertUsernameIntoFirebase(userName)
            }
            .addOnFailureListener {
            }
    }
    private fun insertUsernameIntoFirebase(userName: String){
        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener {/* task ->
                if (task.isSuccessful) {
                }*/
            }
    }
    private fun insertIntoSharedPref(userName: String){
        val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString((com.example.us0.R.string.user_name).toString(), userName)
            this?.apply()
        }
        /*val view=
            view?.let {
                Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
            }*/
    }

    fun goToNextFragmentComplete(){
        _goToNextFragment.value=false
    }
    /*private fun checkUserName() {
        //remove multiple providers and make this right
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                if(providerId=="google.com"){
                Log.i("opiul","$providerId")
                _userName.value=profile.displayName
                Log.i("opiul","${_userName.value}")}
            }
        }
    }
    init{
        checkUserName()
    }*/
}