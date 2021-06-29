package com.spandverse.seseva

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.home.HomeActivity
import kotlinx.coroutines.launch

class DynamicLinkActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val cloudReference = Firebase.database.reference.child("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(com.spandverse.seseva.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val sharedPref = getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        auth = Firebase.auth
        val intent=intent
        val emailLink=intent.data.toString()
        with (sharedPref.edit()) {
            putString((R.string.email_link).toString(), emailLink)
            apply()
        }
        val deleteAccount=sharedPref.getBoolean((R.string.authenticate_to_delete).toString(),false)?:false
        if(deleteAccount){
            verifySignInLinkToDelete(emailLink)
        }
        else{
            verifySignInLink(emailLink)
        }
    }

    private fun verifySignInLinkToDelete(emailLink: String) {
        val sharedPref =
            getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        if (auth.isSignInWithEmailLink(emailLink)) {
            val email = sharedPref?.getString((R.string.email_address).toString(), "") ?: ""
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    val user = auth.currentUser
                    user?.delete()?.addOnSuccessListener {
                        try {
                            googleSignInClient.revokeAccess().addOnSuccessListener {
                            }
                            googleSignInClient.signOut().addOnCompleteListener {
                            }
                            val db = AllDatabase.getInstance(this)
                            this.lifecycleScope.launch {
                                db.AppDatabaseDao.clear()
                                db.CategoryStatDatabaseDao.clear()
                                db.MissionsDatabaseDao.clear()
                                db.SponsorDatabaseDao.clear()
                                db.StatDataBaseDao.clear()
                            }.invokeOnCompletion { sharedPref.edit()?.clear()?.apply()
                                //remove workManagers
                                WorkManager.getInstance(this).cancelAllWork()
                               /* //remove username and current mission
                                cloudReference.child(user.uid).child("chosenMission").removeValue()
                                cloudReference.child(user.uid).child("username").removeValue()*/
                                goToLoginScreenOnDelete()
                            }

                        } catch (e: kotlin.Exception) {
                            val db = AllDatabase.getInstance(this)
                            this.lifecycleScope.launch {
                                db.AppDatabaseDao.clear()
                                db.CategoryStatDatabaseDao.clear()
                                db.MissionsDatabaseDao.clear()
                                db.SponsorDatabaseDao.clear()
                                db.StatDataBaseDao.clear()
                            }
                            sharedPref.edit()?.clear()?.apply()
                            WorkManager.getInstance(this).cancelAllWork()
                            goToLoginScreenOnDelete()
                        }
                    }
                }
        }
    }

    private fun goToLoginScreenOnDelete() {
        val intent= Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        finish()
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