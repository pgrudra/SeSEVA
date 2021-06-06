package com.example.us0.home.navscreens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.example.us0.*
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentSettingsBinding
import com.example.us0.drawoverotherapps.DOOAFragment
import com.example.us0.foregroundnnotifications.TestService
import com.example.us0.home.DrawerLocker
import com.example.us0.ui.login.NoInternetDialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class SettingsFragment : Fragment(), DeleteAccountDialogFragment.DeleteAccountListener,SignOutDialogFragment.SignOutListener,ClearUsageStatsHistoryDialogFragment.ClearHistoryListener,ManageProfileDialogFragment.ManageProfileListener,GrantDOOADialog.GrantDOOAListener {
    private lateinit var binding:FragmentSettingsBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appContext: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref:SharedPreferences
    private val cloudReference = Firebase.database.reference.child("users")
    override fun onPause() {
        Log.i("SF9","paused")
        super.onPause()
    }

    override fun onDetach() {
        Log.i("SF9","detach")
        super.onDetach()
    }

    override fun onStop() {
        Log.i("SF9","stop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.i("SF9","viewDestroy")
        super.onDestroyView()
    }
    override fun onDestroy() {
        Log.i("SF9","destroy")
        super.onDestroy()
    }
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

        auth= Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(appContext, gso)

        binding.deleteAccountCL.setOnClickListener {  showDeleteAccountConfirmationDialog() }
        binding.signOutCL.setOnClickListener { showSignOutConfirmationDialog() }
        binding.clearUsageHistoryCL.setOnClickListener { showClearUsageHistoryConfirmationDialog() }
        binding.manageProfileCL.setOnClickListener { showManageProfileDialog()  }
        binding.dooaCL.setOnClickListener { findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDOOAFragment()) }
        binding.lightCL.setOnClickListener {
            //stopService()
            with (sharedPref.edit()) {
                this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                this?.apply()
            }
            binding.lightRadio.setImageResource(R.drawable.ic_radio_on)
            binding.mediumRadio.setImageResource(R.drawable.ic_radio_off)
            binding.strictRadio.setImageResource(R.drawable.ic_radio_off)
            binding.lightCL.isEnabled=false
            binding.mediumRadio.isEnabled=true
            binding.strictRadio.isEnabled=true
            //show snackbar
            with (sharedPref.edit()) {
                this?.putInt((com.example.us0.R.string.service_mode).toString(), 1)
                this?.apply()
            }
            startService()
            view?.let {
                Snackbar.make(it, "Light mode enabled successfully", Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.mediumCL.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                //stopService()
                //show snackbar
                with (sharedPref.edit()) {
                    this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_on)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_off)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=false
                binding.strictRadio.isEnabled=true
                with (sharedPref.edit()) {
                    this?.putInt((com.example.us0.R.string.service_mode).toString(), 2)
                    this?.apply()
                }
                startService()
                view?.let {
                    Snackbar.make(it, "Medium mode enabled successfully", Snackbar.LENGTH_SHORT).show()
                }
            }
            else{
                val dialog = GrantDOOADialog()
                val args = Bundle()
                args.putInt("serviceMode", 2)
                dialog.arguments = args
                val fragmentManager=childFragmentManager
                dialog.show(fragmentManager,"Grant Permission")
                //ask DOOA permission
                //on clicking grant permission on dialog,set sharedPref
            }
        }
        binding.strictCL.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                //stopService()
                //show snackbar
                with (sharedPref.edit()) {
                    this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_off)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_on)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=true
                binding.strictRadio.isEnabled=false
                with (sharedPref.edit()) {
                    this?.putInt((com.example.us0.R.string.service_mode).toString(), 3)
                    this?.apply()
                }
                startService()
                view?.let {
                    Snackbar.make(it, "Strict mode enabled successfully", Snackbar.LENGTH_SHORT).show()
                }
            }
            else{
                //ask DOOA permission
                val dialog = GrantDOOADialog()
                val args = Bundle()
                args.putInt("serviceMode", 3)
                dialog.arguments = args
                val fragmentManager=childFragmentManager
                dialog.show(fragmentManager,"Grant Permission")
                //on clicking grant permission on dialog,set sharedPref
            }
        }
        return binding.root
    }

    private fun showManageProfileDialog() {
        val dialog=ManageProfileDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager, "Manage Profile")
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }
    private fun checkPermissions() {
        if(checkUsageAccessPermission()){
            checkDOOAPermissionAndService()
        }
        else {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToPermissionFragment())}

    }

    private fun checkDOOAPermissionAndService() {
        var serviceMode=sharedPref.getInt((R.string.service_mode).toString(),0) ?:0
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)){
            if(serviceMode==0){
                //stopService()
                //mode changed
                with (sharedPref.edit()) {
                    this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with (sharedPref.edit()) {
                    this?.putInt((com.example.us0.R.string.service_mode).toString(), 2)
                    this?.apply()
                }
                startService()
                //start service
                serviceMode=2
            }
            binding.dooaIcon.setImageResource(R.drawable.ic_check_icon)
            binding.dooaCL.isClickable=false
        }
        else{
            /*if(serviceMode==0){
                //stopService()
                //mode changed
                with (sharedPref.edit()) {
                    this?.putInt((com.example.us0.R.string.service_mode).toString(), 1)
                    this?.apply()
                }
                startService()
                serviceMode=1
            }
            else */
                if (serviceMode!=1){
                //stopService()
                //mode changed
                with (sharedPref.edit()) {
                    this?.putBoolean((com.example.us0.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with (sharedPref.edit()) {
                    this?.putInt((com.example.us0.R.string.service_mode).toString(), 1)
                    this?.apply()
                }
                startService()
                //start service
                serviceMode=1
                //start service with 1
                //serviceMode=1
            }
            binding.dooaIcon.setImageResource(R.drawable.ic_arrow_right)
            binding.dooaCL.isClickable=true
        }
        when (serviceMode) {
            1 -> {
                binding.lightRadio.setImageResource(R.drawable.ic_radio_on)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_off)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_off)
                binding.lightCL.isEnabled=false
                binding.mediumRadio.isEnabled=true
                binding.strictRadio.isEnabled=true
            }
            2 -> {
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_on)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_off)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=false
                binding.strictRadio.isEnabled=true
            }
            3 -> {
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_off)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_on)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=true
                binding.strictRadio.isEnabled=false
            }
        }
    }

    private fun startService(){
        Intent(context, TestService::class.java).also{
            it.action= Actions.START.name
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                context?.startForegroundService(it)
            }
            else{
                context?.startService(it)
            }
        }
    }

    private fun checkUsageAccessPermission():Boolean {
        val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appContext.packageName?.let {
                appOps.unsafeCheckOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        } else {
            context?.packageName?.let {
                appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        }
        return mode == AppOpsManager.MODE_ALLOWED
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

        //stop service
        stopService()


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
                        onDeleteAccountComplete(user.uid)
                        Log.i("SF", "try")
                        googleSignInClient.revokeAccess().addOnSuccessListener {
                            Log.i("SF", "revokeAccess")
                        }
                        googleSignInClient.signOut().addOnCompleteListener {
                            Log.i("SF", "gsignout")
                        }
                        toLoginScreen()
                    }
                    catch (e: kotlin.Exception){
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
                                                    onDeleteAccountComplete(user.uid)
                                                    googleSignInClient.revokeAccess().addOnSuccessListener {
                                                        Log.i("SF", "revokeAccess")
                                                    }
                                                    googleSignInClient.signOut().addOnCompleteListener {

                                                    }
                                                    toLoginScreen()
                                                }
                                                catch (e: kotlin.Exception){
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

    private fun stopService() {
        if(getServiceState(appContext)!=ServiceState.STOPPED){
            Intent(appContext, TestService::class.java).also {
                it.action = Actions.STOP.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(it)
                } else {
                    appContext.startService(it)
                }
            }
        }
    }

    private fun setLoadingSymbol() {
        binding.progressBar1.visibility=View.VISIBLE
        binding.skrim.visibility=View.VISIBLE
    }
    private fun onDeleteAccountComplete(uid: String) {
        //remove sharedPref
        sharedPref.edit()?.clear()?.apply()
        //remove workManagers
        WorkManager.getInstance(appContext).cancelAllWork()
        Log.i("SF", "wM removed")

        //remove username and current mission
        cloudReference.child(uid).child("chosenMission").removeValue()
        cloudReference.child(uid).child("username").removeValue()
        Log.i("SF", "userName n mission removed")

    }

    override fun signOut() {
        setLoadingSymbol()
        stopService()
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion {
            sharedPref.edit()?.clear()?.apply()
            // Firebase sign out
            auth.signOut()
            WorkManager.getInstance(appContext).cancelAllWork()
            // Google sign out
            googleSignInClient.signOut().addOnCompleteListener {
                toLoginScreen()
            }
        }

    }
    private fun toLoginScreen(){
        val intentToLoginScreen = Intent(activity, MainActivity::class.java)
        intentToLoginScreen.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentToLoginScreen)
    }
    override fun clearHistory() {
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
        db.CategoryStatDatabaseDao.clear()
        db.StatDataBaseDao.clear()
        }
    }

    override fun changeUsername(userName: String) {
        if(checkInternetConnectivity(appContext)){
                Log.i("ANVM","p")
                insertIntoCloudDatabase(userName)
        }
        else{
            showNoInternetConnectionDialog()
            Log.i("fg","x")
        }
    }
    private fun insertIntoCloudDatabase(userName: String ){
        val userId= auth.currentUser!!.uid
        cloudReference.child(userId).child("username").setValue(userName)
            .addOnSuccessListener{
                insertUsernameIntoFirebase(userName)
            }
            .addOnFailureListener {
            }
    }
    private fun insertUsernameIntoFirebase(userName: String){
        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }
        auth.currentUser!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    insertIntoSharedPref(userName)
                }
            }
    }
    private fun insertIntoSharedPref(userName: String){
        val sharedPref = appContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString((com.example.us0.R.string.user_name).toString(), userName)
            this?.apply()
        }
            view?.let {
                Snackbar.make(it, "Username changed successfully", Snackbar.LENGTH_SHORT).show()
            }
    }



    private fun showNoInternetConnectionDialog() {
        val dialog = NoInternetDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager,"No Internet Connection")
    }

    override fun toDOOAPermission(mode: Int?) {
        mode?.let {
            with (sharedPref.edit()) {
                this?.putInt((com.example.us0.R.string.service_mode).toString(), it)
                this?.apply()
            }
        }
        toDOOAPermissionScreen()
    }

    private fun toDOOAPermissionScreen() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context?.packageName)
        )
        startActivity(intent)
    }


}