package com.example.us0.ui.login


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.adapters.OnBoardingAdapter
import com.example.us0.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(), View.OnClickListener,NoInternetDialogFragment.NoInternetDialogListener {

    private val viewModel:LoginViewModel by activityViewModels()
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

        val appContext = context?.applicationContext ?: return binding.root
        mgoogleSignInClient = GoogleSignIn.getClient(appContext, gso)
        auth = Firebase.auth

        Log.i("jio","${auth.currentUser}")

        verifySignInLink()
        val adapter = OnBoardingAdapter()
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager)
        { tab, position -> }.attach()

        binding.loginViewModel=viewModel
        binding.lifecycleOwner=this
        viewModel.resendEmail.observe(viewLifecycleOwner, Observer{ resend->
            if(resend){
                Log.i("io","qw")
                viewModel.resendComplete()
                binding.progressBar1.visibility=View.VISIBLE
                val sharedPref =
                    activity?.getSharedPreferences(
                        (R.string.shared_pref).toString(),
                        Context.MODE_PRIVATE
                    )
                val repeatEmail =
                    sharedPref?.getString((R.string.email_address).toString(), "") ?: ""
                sendSignInLink(repeatEmail, buildActionCodeSettings())

            }
        })
        putEmail()
        return binding.root
    }
    private fun putEmail(){
        val sharedPref =
            activity?.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        val repeatEmail =
            sharedPref?.getString((R.string.email_address).toString(), "") ?: ""

        viewModel.putEmailAddress(repeatEmail)
    }

    private fun otherEmailSignIn() {
        val emailId = binding.editTextEmailAddress.text.toString()
        if (emailId != "") {
binding.progressBar1.visibility=View.VISIBLE
            val sharedPref =
                activity?.getSharedPreferences(
                    (R.string.shared_pref).toString(),
                    Context.MODE_PRIVATE
                )
            with(sharedPref?.edit()) {
                this?.putString((R.string.email_address).toString(), emailId)
                this?.apply()
            }
            sendSignInLink(emailId, buildActionCodeSettings())
        } else {
            makeErrorBackground(true)
            binding.errorMessage.visibility=View.VISIBLE
        }
    }

    private fun buildActionCodeSettings(): ActionCodeSettings {
        return actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://www.unslave0.com"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.us0",
                true, /* installIfNotAvailable */
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
                    binding.progressBar1.visibility=View.GONE
                    showLinkVerificationScreen()

                } else {
                    binding.progressBar1.visibility=View.GONE

                    val sharedPref =
                        activity?.getSharedPreferences(
                            (R.string.shared_pref).toString(),
                            Context.MODE_PRIVATE
                        )
                    if(checkInternetConnectivity()){
                        makeErrorBackground(true)
                        Log.i("OPOP", "qqq")
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                    else{
                        Log.i("OPOP", "hjh")
                        makeErrorBackground(false)
                        showNoInternetConnectionDialog()
                    }
                    sharedPref?.edit()?.clear()?.apply()
                }
            }
        // [END auth_send_sign_in_link]
    }
    private fun checkInternetConnectivity():Boolean{
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    null
                }
            return if (capabilities != null) {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                false
            }
        }
        else return false
    }
    private fun showNoInternetConnectionDialog() {
        // Create an instance of the dialog fragment and show it

        val dialog = NoInternetDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager,"No Internet Connection")
    }
    private fun showLinkVerificationScreen(){
        viewModel.disableResendButton()
findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLinkVerificationFragment())
    }

    private fun verifySignInLink() {
        val sharedPref =
            activity?.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val defaultValue = "ddd"
        val emailLink =
            sharedPref?.getString((R.string.email_link).toString(), defaultValue) ?: "fff"
        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            binding.progressBar1.visibility=View.VISIBLE
            val email =
                sharedPref?.getString((R.string.email_address).toString(), defaultValue) ?: ""
            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link!")
                        val result = task.result
                        val user = auth.currentUser
                        binding.progressBar1.visibility=View.GONE
                        if (result?.additionalUserInfo?.isNewUser!!) {
                            Log.i("MN", "kj")
                        } else {
                            Log.i("MN", "UI")
                        }
                        sharedPref?.edit()?.remove((R.string.email_address).toString())?.apply()
                        Log.i("sdfa","1")
                        updateUI(user)
                        // You can access the new user via result.getUser()
                        // Additional user info profile *not* available via:
                        // result.getAdditionalUserInfo().getProfile() == null
                        // You can check if the user is new or existing:
                        // result.getAdditionalUserInfo().isNewUser()
                    } else {
                        Log.e(TAG, "Error signing in with email link", task.exception)
                        view?.let {
                            Snackbar.make(
                                it,
                                "Authentication Failed.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        binding.progressBar1.visibility=View.GONE
                        Log.i("sdfa","2")
                        updateUI(null)
                    }
                }
        }
        // [END auth_verify_sign_in_link]
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        Log.i("sdfa","onstart")
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        Log.i("sdfa","3")
        Log.i("sdfa","ff${currentUser?.displayName}")
        updateUI(currentUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleSignInButton.setOnClickListener(this)
        binding.otherEmailButton.setOnClickListener(this)
        binding.editTextEmailAddress.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                makeActiveBackground()
            }
        }

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
                if(checkInternetConnectivity()){
                    Log.i("zxc", "true")
                    view?.let {
                        Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    }
                    Log.i("sdfa","4")
                    updateUI(null)}
                else{
                    Log.i("zxc", "false")
                    showNoInternetConnectionDialog()
                    Log.i("sdfa","5")
                    updateUI(null)
                }
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
                    val result = task.result
                    if (result?.additionalUserInfo?.isNewUser!!) {
                        Log.i("MN", "kj")
                    } else {
                        Log.i("MN", "UI")

                    }
                    val user = auth.currentUser
                    Log.i("sdfa","6")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // ...
                    if(checkInternetConnectivity()){
                        Log.i("zxc", "true")
                    view?.let {
                        Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    }
                        Log.i("sdfa","7")
                    updateUI(null)}
                    else{
                        Log.i("zxc", "false")
                        showNoInternetConnectionDialog()
                        Log.i("sdfa","8")
                        updateUI(null)
                    }
                }

            }
    }


    private fun updateUI(user: FirebaseUser?) {
        Log.i("sdfa","9")
        if (user != null) {
            Log.i("sdfa","${user.metadata}")
            findNavController(this).navigate(LoginFragmentDirections.actionLoginFragmentToAskName())
        } else {
        }
    }

    private fun makeActiveBackground() {
        binding.editTextEmailAddress.setBackgroundResource(R.drawable.login_email_edit_box_active)
        binding.underline.visibility = View.GONE
    }

    private fun makeErrorBackground(emailError:Boolean) {
        binding.editTextEmailAddress.setBackgroundResource(R.drawable.login_email_edit_box_error)
        binding.otherEmailButton.setBackgroundResource(R.drawable.login_other_sign_in_error_button)
        if(emailError) {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                binding.editTextEmailAddress.setBackgroundResource(R.drawable.login_email_edit_box_active)
                binding.otherEmailButton.setBackgroundResource(R.drawable.login_other_sign_in_button)
                binding.errorMessage.visibility = View.INVISIBLE

            }, 2500)
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 4564
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.google_sign_in_button -> googleSignIn()
            //R.id.editTextEmailAddress -> makeActiveBackground()
            R.id.other_email_button -> otherEmailSignIn()

        }
    }

    override fun removeRedBackground(dialog: DialogFragment) {
        binding.editTextEmailAddress.setBackgroundResource(R.drawable.login_email_edit_box_active)
        binding.otherEmailButton.setBackgroundResource(R.drawable.login_other_sign_in_button)
    }


}
