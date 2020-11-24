package com.example.us0.ui.login

import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(),View.OnClickListener {

    private lateinit var viewModel: LoginViewModel
    private lateinit var viewModelFactory: LoginViewModelFactory
    private lateinit var auth: FirebaseAuth
    private lateinit var mgoogleSignInClient: GoogleSignInClient
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        viewModelFactory= LoginViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
        val appContext = context?.applicationContext ?: return binding.root
        mgoogleSignInClient = GoogleSignIn.getClient(appContext, gso)
        auth = Firebase.auth
        verifySignInLink()
        return binding.root
    }

    private fun otherEmailSignIn(){
        val emailId = binding.editTextTextEmailAddress.text.toString()
        val sharedPref = activity?.getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString((R.string.email_address).toString(), emailId)
            this?.apply()
        }
        sendSignInLink(emailId, buildActionCodeSettings())
    }
    private fun buildActionCodeSettings():ActionCodeSettings {
        return actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://www.unslave0.com"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.us0",
                false, /* installIfNotAvailable */
                "21" /* minimumVersion */
            )
        }
    }

    private fun sendSignInLink(email: String, actionCodeSettings: ActionCodeSettings) {
        // [START auth_send_sign_in_link]
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("OPOP", "Email sent.")
                }
                else{

                }
            }
        // [END auth_send_sign_in_link]
    }

    private fun verifySignInLink() {
        val sharedPref = activity?.getSharedPreferences((R.string.shared_pref).toString(),Context.MODE_PRIVATE)
        val defaultValue="ddd"
        val emailLink = sharedPref?.getString((R.string.email_link).toString(), defaultValue)?:"fff"
        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            
            val email = sharedPref?.getString((R.string.email_address).toString(), defaultValue)?:""
            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link!")
                        val result = task.result
                        val user = auth.currentUser
                        updateUI(user)
                        // You can access the new user via result.getUser()
                        // Additional user info profile *not* available via:
                        // result.getAdditionalUserInfo().getProfile() == null
                        // You can check if the user is new or existing:
                        // result.getAdditionalUserInfo().isNewUser()
                    } else {
                        Log.e(TAG, "Error signing in with email link", task.exception)
                        view?.let { Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show() }
                        updateUI(null)
                    }
                }
        }
        // [END auth_verify_sign_in_link]
    }



    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        checkCurrentUser()
    }
    private fun checkCurrentUser(){
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleSignInButton.setOnClickListener(this)
        binding.otherEmailButton.setOnClickListener(this)

    }

    private fun googleSignIn() {
        val googleSignInIntent: Intent = mgoogleSignInClient.signInIntent
        startActivityForResult(googleSignInIntent, RC_SIGN_IN)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // ...
                    view?.let { Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show() }
                    updateUI(null)
                }

            }
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToInstalledAppsActivity())
        }
        else{}
    }



    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 4564
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.google_sign_in_button -> googleSignIn()

            R.id.other_email_button -> otherEmailSignIn()
        }
    }
}
