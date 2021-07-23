package com.spandverse.seseva.home.navscreens

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.*
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentSettingsBinding
import com.spandverse.seseva.foregroundnnotifications.TestService
import com.spandverse.seseva.home.DrawerLocker
import com.spandverse.seseva.ui.login.NoInternetDialogFragment
import kotlinx.coroutines.launch


class SettingsFragment : Fragment(), DeleteAccountDialogFragment.DeleteAccountListener,SignOutDialogFragment.SignOutListener,ClearUsageStatsHistoryDialogFragment.ClearHistoryListener,ManageProfileDialogFragment.ManageProfileListener,GrantDOOADialog.GrantDOOAListener,ReAuthenticateAndDeleteFragment.ReAuthenticateAndDeleteDialogListener {
    private lateinit var binding:FragmentSettingsBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appContext: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref:SharedPreferences
    private val cloudReference = Firebase.database.reference.child("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            .requestIdToken(com.spandverse.seseva.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        auth= Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(appContext, gso)
        binding.deleteAccountCL.setOnClickListener {  showDeleteAccountConfirmationDialog() }
        binding.signOutCL.setOnClickListener { showSignOutConfirmationDialog() }
        binding.clearUsageHistoryCL.setOnClickListener { showClearUsageHistoryConfirmationDialog() }
        binding.manageProfileCL.setOnClickListener { showManageProfileDialog()  }
        binding.lightInfo.setOnClickListener {
            val popUpClass = ModeInfoPopUpWindow()
            popUpClass.showPopupWindow(it)
        }
        binding.mediumInfo.setOnClickListener {
            val popUpClass = ModeInfoPopUpWindow()
            popUpClass.showPopupWindow(it)
        }
        binding.strictInfo.setOnClickListener {
            val popUpClass = ModeInfoPopUpWindow()
            popUpClass.showPopupWindow(it)
        }
        binding.dooaCL.setOnClickListener { findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDOOAFragment()) }
        binding.autostartCL.setOnClickListener {
            try {
                val manufacturer=android.os.Build.MANUFACTURER
                when {
                    "xiaomi".equals(manufacturer,true) -> {
                        val intent=Intent()
                        intent.component = ComponentName("com.miui.securitycenter","com.miui.permcenter.autostart.AutoStartManagementActivity")
                        val list=context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        list?.let{
                            if(it.size>0){
                                startActivity(intent)
                            }
                        }
                    }
                    "oppo".equals(manufacturer,true) -> {
                        try {
                            val intent = Intent()
                            intent.component=ComponentName(
                                "com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                            )
                            startActivity(intent)
                        } catch (e: java.lang.Exception) {
                            try {
                                val intent = Intent()
                                intent.component=ComponentName(
                                    "com.oppo.safe",
                                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                                )
                                startActivity(intent)
                            } catch (ex: java.lang.Exception) {
                                try {
                                    val intent = Intent()
                                    intent.component=ComponentName(
                                        "com.coloros.safecenter",
                                        "com.coloros.safecenter.startupapp.StartupAppListActivity"
                                    )
                                    startActivity(intent)
                                } catch (exx: java.lang.Exception) {
                                }
                            }
                        }



                        /*val intent=Intent()
                        intent.component = ComponentName("com.coloros.safecenter","com.coloros.safecenter.permission.startup.StartupAppListActivity")
                        val list=context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        list?.let{
                            if(it.size>0){
                                startActivity(intent)
                            }
                        }*/
                    }
                    "vivo".equals(manufacturer,true) -> {
                        try {
                            val intent=Intent()
                            intent.component = ComponentName("com.iqoo.secure",
                                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                            startActivity(intent)
                        } catch (e:Exception) {
                            try {
                                val intent=Intent()
                                intent.component = ComponentName ("com.vivo.permissionmanager",
                                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                                )
                                startActivity(intent)
                            } catch (ex:Exception) {
                                try {
                                    val intent=Intent()
                                    intent.setClassName(
                                        "com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                                    )
                                    startActivity(intent)
                                } catch (exx:Exception) {
                                    view?.let {
                                        Snackbar.make(it, "Could not proceed to Settings", Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        //intent.component = ComponentName("com.vivo.permissionmanager","com.vivo.permissionmanager.activity.BgStartupManagerActivity")
                    }
                    "Letv".equals(manufacturer,true) -> {
                        val intent=Intent()
                        intent.component = ComponentName("com.letv.android.letvsafe","com.letv.android.letvsafe.AutobootManageActivity")
                        val list=context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        list?.let{
                            if(it.size>0){
                                startActivity(intent)
                            }
                        }
                    }
                    "Honor".equals(manufacturer,true) -> {
                        val intent=Intent()
                        intent.component = ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.optimize.process.ProtectActivity")
                        val list=context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        list?.let{
                            if(it.size>0){
                                startActivity(intent)
                            }
                        }
                    }
                    "asus".equals(manufacturer,true)->{
                        val intent=Intent()
                        intent.component = ComponentName("com.asus.mobilemanager","com.asus.mobilemanager.powersaver.PowerSaverSettings")
                        try{
                            startActivity(intent)
                        }catch (e:java.lang.Exception){}

                    }

                    "nokia".equals(manufacturer,true)->{
                        val intent=Intent()
                        intent.component = ComponentName("com.evenwell.powersaving.g3","com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity")
                        try{
                            startActivity(intent)
                        }catch (e:java.lang.Exception){}
                    }
                }


            } catch (e:Exception){
            }
        }
        binding.lightCL.setOnClickListener {
            //stopService()
            with (sharedPref.edit()) {
                this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
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
                this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 1)
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
                    this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_on)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_off)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=false
                binding.strictRadio.isEnabled=true
                with (sharedPref.edit()) {
                    this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 2)
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
                    this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                binding.lightRadio.setImageResource(R.drawable.ic_radio_off)
                binding.mediumRadio.setImageResource(R.drawable.ic_radio_off)
                binding.strictRadio.setImageResource(R.drawable.ic_radio_on)
                binding.lightCL.isEnabled=true
                binding.mediumRadio.isEnabled=true
                binding.strictRadio.isEnabled=false
                with (sharedPref.edit()) {
                    this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 3)
                    this?.apply()
                }
                with (sharedPref.edit()) {
                    this?.putBoolean((R.string.show_strict_banner).toString(),false)
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

    override fun onStart() {
        super.onStart()
        if(!sharedPref.getBoolean((R.string.authenticate_to_delete).toString(),false))
            checkPermissions()
        if(checkIfDeviceSpecificPermissionNeeded())
            binding.autostartCL.visibility=View.VISIBLE
    }

    private fun checkIfDeviceSpecificPermissionNeeded():Boolean {
        val manufacturer=android.os.Build.MANUFACTURER
        return "xiaomi".equals(manufacturer,true) ||
                "oppo".equals(manufacturer,true) ||
                "vivo".equals(manufacturer,true) ||
                "Letv".equals(manufacturer,true) ||
                "Honor".equals(manufacturer,true) ||
                "asus".equals(manufacturer,true)||
                "nokia".equals(manufacturer,true)
    }

    private fun checkPermissions() {
        if(checkUsageAccessPermission()){
            checkDOOAPermissionAndService()
        }
        else {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToPermissionFragment())}
    }

    private fun checkDOOAPermissionAndService() {
        var serviceMode= sharedPref.getInt((R.string.service_mode).toString(),0)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)){
            if(serviceMode==0){
                //stopService()
                //mode changed
                with (sharedPref.edit()) {
                    this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with (sharedPref.edit()) {
                    this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 2)
                    this?.apply()
                }
                startService()
                //start service
                serviceMode=2
            }
            else{
                startService()
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
                    this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
                    this?.apply()
                }
                with (sharedPref.edit()) {
                    this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), 1)
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
        if(checkInternetConnectivity(appContext)) {
            //set loading symbol
            setLoadingSymbol()
            val user = auth.currentUser
            //stop service
            stopService()
            //remove dbs

            user?.uid?.let {
                cloudReference.child(it).child("username").removeValue()}


//delete account from firebase and google revoke access, google sign out
                try {
                    user?.delete()?.addOnSuccessListener {
                        try {
                            googleSignInClient.revokeAccess().addOnSuccessListener {
                            }
                            googleSignInClient.signOut().addOnCompleteListener {
                            }
                            onDeleteAccountComplete(user.uid)
                            //toLoginScreen()
                        } catch (e: kotlin.Exception) {
                            removeLoadingSymbol()
                            view?.let {
                                Snackbar.make(it, "Failed to delete account", Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                        ?.addOnFailureListener {
                            Firebase.auth.fetchSignInMethodsForEmail(
                                sharedPref.getString(
                                    (R.string.email_address).toString(),
                                    "defaultValue"
                                ) ?: "null"
                            )
                                .addOnSuccessListener { result ->
                                    val signInMethods = result.signInMethods!!
                                    val emailLink = sharedPref.getString(
                                        (R.string.email_link).toString(),
                                        "defaultValue"
                                    ) ?: "null"
                                    if(auth.isSignInWithEmailLink(emailLink) && signInMethods.contains(
                                            EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD
                                        )){
                                        val credential=EmailAuthProvider.getCredentialWithLink(
                                            sharedPref.getString(
                                                (R.string.email_address).toString(),
                                                "defaultValue"
                                            ) ?: "null", emailLink
                                        )
                                        user.reauthenticateAndRetrieveData(credential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    user.delete().addOnSuccessListener {
                                                        try {

                                                            googleSignInClient.revokeAccess()
                                                                .addOnSuccessListener {
                                                                }
                                                            googleSignInClient.signOut()
                                                                .addOnCompleteListener {
                                                                }
                                                            onDeleteAccountComplete(user.uid)
                                                            //toLoginScreen()
                                                        } catch (e: kotlin.Exception) {
                                                            removeLoadingSymbol()
                                                            view?.let {
                                                                Snackbar.make(it, "Failed to delete account", Snackbar.LENGTH_SHORT)
                                                                    .show()
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val dialog = ReAuthenticateAndDeleteFragment()
                                                    val fragmentManager=childFragmentManager
                                                    removeLoadingSymbol()
                                                    dialog.show(fragmentManager, "RNDE")
                                                }
                                            }
                                    }
                                    else if(GoogleSignIn.getLastSignedInAccount(context) != null){
                                        val credential=GoogleAuthProvider.getCredential(
                                            GoogleSignIn.getLastSignedInAccount(context)?.idToken,
                                            null
                                        )
                                        user.reauthenticate(credential).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                user.delete().addOnSuccessListener {
                                                    try {
                                                        googleSignInClient.revokeAccess()
                                                            .addOnSuccessListener {
                                                            }
                                                        googleSignInClient.signOut()
                                                            .addOnCompleteListener {
                                                            }
                                                        onDeleteAccountComplete(user.uid)
                                                        //toLoginScreen()
                                                    } catch (e: kotlin.Exception) {
                                                        removeLoadingSymbol()
                                                        view?.let {
                                                            Snackbar.make(it, "Failed to delete account", Snackbar.LENGTH_SHORT)
                                                                .show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                val dialog = ReAuthenticateAndDeleteFragment()
                                                val fragmentManager=childFragmentManager
                                                removeLoadingSymbol()
                                                dialog.show(fragmentManager, "RNDG")
                                            }
                                        }
                                    }
                                   /* val credential = when {
                                        auth.isSignInWithEmailLink(emailLink) && signInMethods.contains(
                                            EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD
                                        ) -> EmailAuthProvider.getCredentialWithLink(
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
                                        user.reauthenticateAndRetrieveData(credential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    user.delete().addOnSuccessListener {
                                                        try {
                                                            onDeleteAccountComplete(user.uid)
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
                                                } else {

                                                }
                                            }
                                    }*/

                                }
                                .addOnFailureListener {
                                }


                        }


                } catch (e: kotlin.Exception) {
                }


        }
        else{
            val dialog = NoInternetDialogFragment()
            val fragmentManager = childFragmentManager
            dialog.show(fragmentManager, "No Internet Connection")
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
    private fun removeLoadingSymbol() {
        binding.progressBar1.visibility=View.GONE
        binding.skrim.visibility=View.GONE
    }
    private fun onDeleteAccountComplete(uid: String) {
        //remove sharedPref
        val db = AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.SponsorDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion { sharedPref.edit()?.clear()?.apply()
            //remove workManagers
            WorkManager.getInstance(appContext).cancelAllWork()
            //remove username and current mission
            toLoginScreen(true)
        }

    }

    override fun signOut() {
        setLoadingSymbol()
        stopService()
        val db=AllDatabase.getInstance(appContext)
        viewLifecycleOwner.lifecycleScope.launch {
            db.AppDatabaseDao.clear()
            db.CategoryStatDatabaseDao.clear()
            db.MissionsDatabaseDao.clear()
            db.SponsorDatabaseDao.clear()
            db.StatDataBaseDao.clear()
        }.invokeOnCompletion {
            sharedPref.edit()?.clear()?.apply()
            // Firebase sign out
            auth.signOut()
            WorkManager.getInstance(appContext).cancelAllWork()
            // Google sign out
            googleSignInClient.signOut().addOnCompleteListener {
                toLoginScreen(false)
            }
        }

    }
    private fun toLoginScreen(deleteAccount:Boolean){
        val intentToLoginScreen = Intent(activity, MainActivity::class.java)
        intentToLoginScreen.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if(deleteAccount){
            intentToLoginScreen.putExtra(getString(R.string.delete_action),true)
        }
        else{
            intentToLoginScreen.putExtra(getString(R.string.signout_action),true)
        }
        startActivity(intentToLoginScreen)
        activity?.finish()
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
                insertIntoCloudDatabase(userName)
        }
        else{
            showNoInternetConnectionDialog()
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
            this?.putString((com.spandverse.seseva.R.string.user_name).toString(), userName)
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
                this?.putInt((com.spandverse.seseva.R.string.service_mode).toString(), it)
                this?.apply()
            }
            with (sharedPref.edit()) {
                this?.putBoolean((com.spandverse.seseva.R.string.mode_changed).toString(), true)
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

    override fun reAuthenticateAndDelete(dialog: DialogFragment) {
        /*if(dialog.tag=="RNDE"){

        }
        else{
            googleSignInToDelete()
        }*/
        otherEmailSignInToDelete()
    }
    private fun otherEmailSignInToDelete() {
        val emailId =sharedPref.getString((R.string.email_address).toString(),"")?:""
        if (emailId != "") {
            sendSignInLinkToDelete(emailId, buildActionCodeSettings())
        } else {
            removeLoadingSymbol()
            view?.let {
                Snackbar.make(it, "Failed to delete account", Snackbar.LENGTH_SHORT)
                    .show()
            }
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
                "com.spandverse.seseva",
                false, /* installIfNotAvailable */
                "22" /* minimumVersion */
            )
        }
    }
    private fun sendSignInLinkToDelete(email: String, actionCodeSettings: ActionCodeSettings) {
        // [START auth_send_sign_in_link]
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //showLinkVerificationScreenToDelete()
                    with (sharedPref.edit()) {
                        this?.putBoolean((com.spandverse.seseva.R.string.authenticate_to_delete).toString(), true)
                        this?.apply()
                    }
                    removeLoadingSymbol()
                    Snackbar.make(binding.root, "Please check your email and click on the sign in link sent to re-authenticate and delete your account", Snackbar.LENGTH_INDEFINITE)
                            .show()
                } else {
                    removeLoadingSymbol()
                    if(checkInternetConnectivity(appContext)){
                        view?.let {
                            Snackbar.make(it, "Failed to delete account", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else{
                        showNoInternetConnectionDialog()
                    }
                }
            }
        // [END auth_send_sign_in_link]
    }
    /*private fun googleSignInToDelete() {
        val googleSignInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(googleSignInIntent, RC_SIGN_IN)
    }
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 4564
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)!!

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {

                // Google Sign In failed, update UI appropriately
                removeLoadingSymbol()
                if(checkInternetConnectivity(appContext)) {

                    view?.let {
                        Snackbar.make(it, "Re-Authentication Failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
                else{

                    showNoInternetConnectionDialog()
                }
            }
        }
    }*/


    /*private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    //delete account

                    with (sharedPref.edit()) {
                        this?.putBoolean((com.spandverse.seseva.R.string.authenticate_to_delete).toString(), true)
                        this?.apply()
                    }
                    val user = auth.currentUser
                    user?.delete()?.addOnSuccessListener {
                        try {
                            googleSignInClient.revokeAccess().addOnSuccessListener {
                            }
                            googleSignInClient.signOut().addOnCompleteListener {
                            }
                            onDeleteAccountComplete(user.uid)

                        } catch (e: kotlin.Exception) {
                            removeLoadingSymbol()

                            view?.let {
                                Snackbar.make(it, "Re-Authentication Failed", Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                        removeLoadingSymbol()

                    if(checkInternetConnectivity(appContext)) {
                        view?.let {
                            Snackbar.make(it, "Re-Authentication Failed", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else{
                        showNoInternetConnectionDialog()
                    }
                    binding.progressBar1.visibility=View.GONE
                    binding.skrim.visibility=View.GONE
                }

            }
    }*/
}