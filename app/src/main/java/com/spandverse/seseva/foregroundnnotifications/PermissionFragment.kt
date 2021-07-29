package com.spandverse.seseva.foregroundnnotifications

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentPermissionBinding
import com.spandverse.seseva.home.DrawerLocker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.*
import com.spandverse.seseva.ui.login.NoInternetDialogFragment
import kotlinx.coroutines.launch


class PermissionFragment : Fragment(),PermissionMandatoryDialogFragment.PermissionMandatoryDialogListener,DeleteAccountDialogFragment.DeleteAccountListener {

    private lateinit var binding: FragmentPermissionBinding
    private lateinit var viewModel: PermissionViewModel
    private lateinit var viewModelFactory: PermissionViewModelFactory
    private lateinit var appContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_permission,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        viewModelFactory = PermissionViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PermissionViewModel::class.java)
        binding.lifecycleOwner = this
        binding.permissionViewModel = viewModel
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        appContext = context?.applicationContext?: return binding.root
        val pkgManager=appContext.packageManager
        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS
        )
        if(intent.resolveActivity(pkgManager)==null){
            binding.appCompatImageView7.setImageResource(R.drawable.vivo_usage_access_help_guide)
        }
        val termsAndPolicyString= SpannableString(getString(R.string.terms_of_use_and_privacy_policy))
        val termsOfUseText: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://sites.google.com/view/seseva-terms-of-use/home")
                startActivity(i)
            }
        }
        val privacyPolicyText: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://sites.google.com/view/seseva-privacy-policy/home")
                startActivity(i)
            }
        }
        termsAndPolicyString.setSpan(termsOfUseText,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        termsAndPolicyString.setSpan(privacyPolicyText,17,31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        termsAndPolicyString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.disabled_text)),0,12,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        termsAndPolicyString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.disabled_text)),17,31,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.loginTermsAndPrivacyText.apply {
            this.text=termsAndPolicyString
            this.movementMethod= LinkMovementMethod.getInstance()
            this.highlightColor=Color.TRANSPARENT
        }
        binding.neverMind.setOnClickListener { showPermissionMandatoryDialog() }
        viewModel.grantPermission.observe(viewLifecycleOwner, Observer<Boolean> { grantPermission ->
            if (grantPermission) {
                try{
                    val intent1 = Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + context?.packageName)
                    )
                    startActivity(intent1)
                    viewModel.onGrantPermissionComplete()
                }catch (e:Exception){
                    try{
                        startActivity(intent)
                        viewModel.onGrantPermissionComplete()
                    }catch (ex:Exception){
                        val intentToSecurity=Intent(Settings.ACTION_SECURITY_SETTINGS)
                        startActivity(intentToSecurity)
                        viewModel.onGrantPermissionComplete()
                    }
                }
            }
        })
        viewModel.showOrHide.observe(viewLifecycleOwner, Observer<Boolean> { show ->
            if (show) {
                binding.appCompatImageView7.visibility=View.VISIBLE
                binding.imageButton.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.appCompatImageView7.visibility=View.GONE
                binding.imageButton.setImageResource(R.drawable.ic_expand_vector)
            }
        })
        viewModel.toDOOA.observe(viewLifecycleOwner, Observer<Boolean> { toDOOA ->
            if (toDOOA) {
                findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToDOOAFragment())
                viewModel.toDOOAComplete()
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkUsageAccessPermission()
    }

    private fun showPermissionMandatoryDialog(){
        val dialog=PermissionMandatoryDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager, "Permission Mandatory")
    }
    private fun showDeleteAccountConfirmationDialog(){
        val dialog=DeleteAccountDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager,"Delete Account?")
    }
    override fun onClickDeleteAccountButton() {
        showDeleteAccountConfirmationDialog()
    }

    override fun deleteAccount() {
        if(checkInternetConnectivity(appContext)){
        val sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(com.spandverse.seseva.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        val auth= Firebase.auth
        val googleSignInClient = GoogleSignIn.getClient(appContext, gso)
        val user = auth.currentUser
        val cloudReference = Firebase.database.reference.child("users")
        cloudReference.child(user!!.uid).child("username").removeValue()
        val db= AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.SponsorDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion {
            try {
                user.delete().addOnSuccessListener {
                    try {sharedPref.edit()?.clear()?.apply()
                        onDeleteAccountComplete()
                        googleSignInClient.revokeAccess().addOnSuccessListener {
                        }
                        googleSignInClient.signOut().addOnCompleteListener {
                            //delete db
                            //stop service
                            //remove username and current mission
                            //remove workManager
                        }
                        toLoginScreen()
                    } catch (e: kotlin.Exception) {
                        toLoginScreen()
                    }
                }
                    .addOnFailureListener {

                        auth.fetchSignInMethodsForEmail(
                            sharedPref.getString(
                                (R.string.email_address).toString(),
                                "defaultValue"
                            ) ?: "null"
                        )
                            .addOnSuccessListener { result ->
                                val signInMethods = result.signInMethods!!
                                val emailLink=sharedPref.getString(
                                    (R.string.email_link).toString(),
                                    "defaultValue"
                                ) ?: "null"
                                val credential = when {
                                    auth.isSignInWithEmailLink(emailLink) && signInMethods.contains(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD) -> EmailAuthProvider.getCredentialWithLink(
                                        sharedPref.getString(
                                            (R.string.email_address).toString(),
                                            "defaultValue"
                                        ) ?: "null", emailLink
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
                                            user.delete().addOnSuccessListener {
                                                try {
                                                    sharedPref.edit()?.clear()?.apply()
                                                    onDeleteAccountComplete()
                                                    googleSignInClient.revokeAccess()
                                                        .addOnSuccessListener {
                                                        }
                                                    googleSignInClient.signOut()
                                                        .addOnCompleteListener {

                                                        }
                                                    toLoginScreen()
                                                } catch (e: kotlin.Exception) {
                                                    toLoginScreen()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                            }


                    }


            } catch (e: kotlin.Exception) {
            }
        }
    }
        else{
            val dialog = NoInternetDialogFragment()
            val fragmentManager = childFragmentManager
            dialog.show(fragmentManager, "No Internet Connection")
        }
    }
    private fun onDeleteAccountComplete() {
        //remove workManagers
        WorkManager.getInstance(appContext).cancelAllWork()

        //remove username and current mission


    }
    private fun toLoginScreen(){
        val intentToLoginScreen = Intent(activity, MainActivity::class.java)
        intentToLoginScreen.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intentToLoginScreen.putExtra(getString(R.string.delete_action),true)
        startActivity(intentToLoginScreen)
        activity?.finish()
    }
}