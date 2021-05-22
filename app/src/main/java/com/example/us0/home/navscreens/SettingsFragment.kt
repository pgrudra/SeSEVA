package com.example.us0.home.navscreens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.example.us0.*
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentSettingsBinding
import com.example.us0.home.DrawerLocker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class SettingsFragment : Fragment(), DeleteAccountDialogFragment.DeleteAccountListener,SignOutDialogFragment.SignOutListener,ClearUsageStatsHistoryDialogFragment.ClearHistoryListener {
    private lateinit var binding:FragmentSettingsBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appContext: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref:SharedPreferences
    private lateinit var intentToLoginScreen: Intent
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        appContext = context?.applicationContext ?: return binding.root
        sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(com.example.us0.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        intentToLoginScreen = Intent(activity, MainActivity::class.java)
        auth= Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(appContext, gso)

        binding.deleteAccountCL.setOnClickListener {  showDeleteAccountConfirmationDialog() }
        binding.signOutCL.setOnClickListener { showSignOutConfirmationDialog() }
        binding.clearUsageHistoryCL.setOnClickListener { showClearUsageHistoryConfirmationDialog() }
        return binding.root
    }

    private fun showClearUsageHistoryConfirmationDialog() {
        val dialog=ClearUsageStatsHistoryDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager, "Sign out?")
    }

    private fun showSignOutConfirmationDialog() {
        val dialog=SignOutDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager, "Sign out?")
    }

    private fun showDeleteAccountConfirmationDialog() {
        val dialog=DeleteAccountDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager, "Delete Account?")
    }

    override fun deleteAccount() {
        //set loading symbol
        setLoadingSymbol()
        val user = auth.currentUser
        //remove dbs
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion {
            Log.i("SF", "local db clear")
//delete account from firebase and google revoke access, google sign out
            try {
                user?.delete()?.addOnSuccessListener {
                    try {
                        googleSignInClient.revokeAccess().addOnSuccessListener {
                            Log.i("SF", "revokeAccess")
                        }
                        googleSignInClient.signOut().addOnCompleteListener {
                            startActivity(intentToLoginScreen)
                        }
                    }
                    catch (e: kotlin.Exception){
                        Log.i("Delete", "qqq")
                    }
                }
                    ?.addOnFailureListener {
                        val defaultValue = "ddd"
                        Firebase.auth.fetchSignInMethodsForEmail(
                            sharedPref.getString(
                                (R.string.email_address).toString(),
                                defaultValue
                            ) ?: "fff"
                        )
                            .addOnSuccessListener { result ->
                                val signInMethods = result.signInMethods!!
                                val credential = when {
                                    signInMethods.contains(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD) -> EmailAuthProvider.getCredentialWithLink(
                                        sharedPref.getString(
                                            (R.string.email_address).toString(),
                                            defaultValue
                                        ) ?: "fff", sharedPref.getString(
                                            (R.string.email_link).toString(),
                                            defaultValue
                                        ) ?: "fff"
                                    )
                                    GoogleSignIn.getLastSignedInAccount(context) != null -> GoogleAuthProvider.getCredential(
                                        GoogleSignIn.getLastSignedInAccount(context)?.idToken,
                                        null
                                    )
                                    else -> null
                                }
                                if (credential != null) {
                                    user.reauthenticate(credential).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.i("SF", "Reauthenticated.")
                                            user.delete().addOnSuccessListener {
                                                Log.i("SF", "account deleted.")
                                                try {
                                                    googleSignInClient.revokeAccess().addOnSuccessListener {
                                                        Log.i("SF", "revokeAccess")
                                                    }
                                                    googleSignInClient.signOut().addOnCompleteListener {
                                                        onDeleteAccountComplete(user.uid)
                                                    }
                                                }
                                                catch (e: kotlin.Exception){
                                                    Log.i("Delete", "qqq")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("SF", "Error getting sign in methods for user", exception)
                            }


                    }


            } catch (e: kotlin.Exception) {
                Log.i("Delete", "ERROR")
            }
        }

    }

    private fun setLoadingSymbol() {

    }
    private fun onDeleteAccountComplete(uid: String) {
        //remove sharedPref
        sharedPref.edit()?.clear()?.apply()
        //remove workManagers
        WorkManager.getInstance(appContext).cancelAllWork()
        Log.i("SF", "wM removed")

        //remove username and current mission
        val cloudReference = Firebase.database.reference.child("users").child(uid)
        cloudReference.child("chosenMission").removeValue()
        cloudReference.child("username").removeValue()
        Log.i("SF", "userName n mission removed")
        startActivity(intentToLoginScreen)
    }

    override fun signOut() {
        setLoadingSymbol()
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }
        sharedPref.edit()?.clear()?.apply()
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            startActivity(intentToLoginScreen)
        }
    }
    override fun clearHistory() {
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
        db.CategoryStatDatabaseDao.clear()
        db.StatDataBaseDao.clear()
        }
    }


}