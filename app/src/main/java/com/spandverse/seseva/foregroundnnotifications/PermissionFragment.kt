package com.spandverse.seseva.foregroundnnotifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.spandverse.seseva.DeleteAccountDialogFragment
import com.spandverse.seseva.MainActivity
import com.spandverse.seseva.R
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
            .requestIdToken(com.spandverse.seseva.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        val auth= Firebase.auth
        val googleSignInClient = GoogleSignIn.getClient(appContext, gso)
        val user = auth.currentUser
        val cloudReference = Firebase.database.reference.child("users")
        cloudReference.child(user!!.uid).child("chosenMission").removeValue().addOnSuccessListener { Log.i("SF", "${user.uid}") }
        cloudReference.child(user.uid).child("username").removeValue().addOnSuccessListener {  Log.i("SF", "username removed")}
        Log.i("SF", "P ${user.uid}")
        val db= AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.SponsorDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion {
            try {
                Log.i("SF", "trtr")
                user?.delete()?.addOnSuccessListener {
                    try {sharedPref.edit()?.clear()?.apply()
                        onDeleteAccountComplete()
                        Log.i("SF", "hjkhgj")
                        googleSignInClient.revokeAccess().addOnSuccessListener {
                            Log.i("SF", "revokeAccess")
                        }
                        googleSignInClient.signOut().addOnCompleteListener {
                            //delete db
                            //stop service
                            //remove username and current mission
                            //remove workManager
                        }
                        toLoginScreen()
                    } catch (e: kotlin.Exception) {
                        Log.i("Delete", "qqq")
                        toLoginScreen()
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
                                                    sharedPref.edit()?.clear()?.apply()
                                                    onDeleteAccountComplete()
                                                    googleSignInClient.revokeAccess()
                                                        .addOnSuccessListener {
                                                            Log.i("SF", "revokeAccess")
                                                        }
                                                    googleSignInClient.signOut()
                                                        .addOnCompleteListener {

                                                        }
                                                    toLoginScreen()
                                                } catch (e: kotlin.Exception) {
                                                    Log.i("Delete", "qqq")
                                                    toLoginScreen()
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
    private fun onDeleteAccountComplete() {
        //remove workManagers
        WorkManager.getInstance(appContext).cancelAllWork()

        //remove username and current mission


    }
    private fun toLoginScreen(){
        val intentToLoginScreen = Intent(activity, MainActivity::class.java)
        intentToLoginScreen.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentToLoginScreen)
        activity?.finish()
    }
}