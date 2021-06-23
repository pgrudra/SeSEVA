package com.spandverse.seseva

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.home.HomeActivity

class DynamicLinkActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        val intent=intent
        val emailLink=intent.data.toString()
        val sharedPref = getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString((R.string.email_link).toString(), emailLink)
            apply()
        }
        verifySignInLink(emailLink)
    }
    private fun goToHomeFragment() {
        val intent= Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        finish()
    }
    private fun verifySignInLink(emailLink: String) {
        val sharedPref = getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        if (auth.isSignInWithEmailLink(emailLink)) {
            val email = sharedPref?.getString((R.string.email_address).toString(), "") ?: ""
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        if (result.additionalUserInfo?.isNewUser!!) {
                            goToHomeFragment()
                        } else {
                            with(sharedPref?.edit()) {
                                this?.putBoolean((com.spandverse.seseva.R.string.load_data).toString(), true)
                                this?.apply()
                            }
                            goToHomeFragment()
                        }
                    } else {
                        goToLoginFragment()
                    }
                }
        }
        // [END auth_verify_sign_in_link]
    }

    private fun goToLoginFragment() {
        val intent= Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("Link Verification Failed",true)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        finish()
    }
}