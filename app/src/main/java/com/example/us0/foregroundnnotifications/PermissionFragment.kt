package com.example.us0.foregroundnnotifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.DeleteAccountDialogFragment
import com.example.us0.MainActivity
import com.example.us0.R
import com.example.us0.databinding.FragmentPermissionBinding
import com.example.us0.home.DrawerLocker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


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
        val disclosureSheet = binding.disclosureFragment.root
        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(disclosureSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.skrim1.visibility = View.GONE
                    binding.allow.isEnabled = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        binding.neverMind.setOnClickListener { showPermissionMandatoryDialog() }
        viewModel.grantPermission.observe(viewLifecycleOwner, Observer<Boolean> { grantPermission ->
            if (grantPermission) {
                val intent = Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + context?.packageName)
                )
                startActivity(intent)
                viewModel.onGrantPermissionComplete()
            }
        })
        viewModel.disclosureVisible.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.skrim1.visibility = View.VISIBLE
                binding.allow.isEnabled = false
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.skrim1.visibility = View.GONE
                binding.allow.isEnabled = true
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
        val sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(com.example.us0.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        val intentToLoginScreen = Intent(activity, MainActivity::class.java)
        val auth= Firebase.auth
        val googleSignInClient = GoogleSignIn.getClient(appContext, gso)
        sharedPref.edit()?.clear()?.apply()

        val user = auth.currentUser
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
                                                    startActivity(intentToLoginScreen)
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