package com.example.us0.signoutpackage


import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignOutViewModel:ViewModel() {
    var gso:GoogleSignInOptions

    var auth: FirebaseAuth
    init {
        gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestIdToken(com.example.us0.R.string.default_web_client_id.toString())
           .requestEmail()
           .build()

        auth = Firebase.auth
    }

}

