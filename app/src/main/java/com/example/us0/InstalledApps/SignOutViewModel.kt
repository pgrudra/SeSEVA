package com.example.us0.InstalledApps


import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignOutViewModel:ViewModel() {
    val token = MutableLiveData<Int>()
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var auth: FirebaseAuth
    fun getOAuth2ClientId() {

        token.value = com.example.us0.R.string.default_web_client_id
    }
    fun getGso(appContext: Context) {
        getOAuth2ClientId()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token.value.toString())
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(appContext, gso)

        auth = Firebase.auth

        //installedApps()
    }

    fun signOut(signOut: SignOut) {
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            NavHostFragment.findNavController(signOut)
                .navigate(SignOutDirections.actionSignOutToMainActivity())
        }
    }


}

