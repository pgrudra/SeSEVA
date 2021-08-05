package com.spandverse.seseva.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import kotlinx.android.synthetic.main.nav_header.view.*


class HomeActivity :AppCompatActivity(),DrawerLocker,
    BottomNavigationView.OnNavigationItemSelectedListener,
    NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout:DrawerLayout
    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val installStateUpdatedListener: InstallStateUpdatedListener by lazy {
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbarForCompleteUpdate()
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    appUpdateManager.unregisterListener(this)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val toolbar=findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar)
        /*val appCheck=sharedPref.getBoolean((R.string.app_check_done).toString(),false)
        if(!appCheck){
            FirebaseApp.initializeApp( this)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance())
            with (sharedPref.edit()) {
                this?.putBoolean((R.string.app_check_done).toString(),true)
                this?.apply()
            }
        }*/
        FirebaseApp.initializeApp( this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance())
        val sharedPref=getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val count=sharedPref.getInt((R.string.count).toString(),0)
        with (sharedPref.edit()) {
            this?.putInt((R.string.count).toString(),count+1)
            this?.apply()
        }
        checkForUpdate(count+1)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        drawerLayout=binding.drawerLayout
val navHostFragment=supportFragmentManager.findFragmentById(R.id.myNavHostFragmentToSignOut) as NavHostFragment
        val navController=navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navView, navController)
        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.itemIconTintList=null
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
        //binding.bottomNavView.selectedItemId=R.id.homeFragment
        //binding.bottomNavView.menu.getItem(0).isChecked = true
binding.bottomNavView.setOnNavigationItemSelectedListener(this)
binding.bottomNavView.itemIconTintList=null
        //navController.graph.findNode(R.id.detailMission)?.addArgument("selectedMission",NavArgument.Builder().setDefaultValue().build())
       /* navController.addOnDestinationChangedListener { controller, destination, arguments ->


            when(destination.id){
                R.id.detailMission -> {



                            //val chosenMission=applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE).getInt((R.string.chosen_mission_number).toString(), 0)
                            //val selectedMission= AllDatabase.getInstance(applicationContext).MissionsDatabaseDao.doesMissionExist(chosenMission)?.asActiveDomainModel()
                            //if(selectedMission!=null){
                    val m=Mission()
                    val d=m.asActiveDomainModel()
                                val argument1= NavArgument.Builder().setDefaultValue(d).build()
                                //destination.addArgument("selectedMission",argument1)
                    destination.addArgument("p",argument1)

                                *//*val args:Bundle=Bundle()
                                args.putParcelable("selectedMission",selectedMission)
                                args.putBoolean("showImage",true)
                                this@HomeActivity.findNavController(R.id.myNavHostFragmentToSignOut).navigate(item.itemId,args)*//*
                            //}


                }

}
        }*/
        /*val header=binding.navView.getHeaderView(0)
        val userName=this.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE).getString((R.string.user_name).toString(),"User")
        header.user_name.text=userName
        */
       /* val closeButton=findViewById<ImageButton>(R.id.close_nav_drawer)
        closeButton.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }*/
        //val appBarConfiguration= AppBarConfiguration(setOf(R.id.homeFragment))
        //NavigationUI.setupWithNavController(toolbar,navController,appBarConfiguration)
        //NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration)
        //NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout)
        //NavigationUI.setupWithNavController()
    }
    private fun checkForUpdate(count: Int) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                && (appUpdateInfo.clientVersionStalenessDays() ?: -1) >=DAYS_FOR_FLEXIBLE_UPDATE
            ) {
                appUpdateManager.registerListener(installStateUpdatedListener)
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )

                // Displays the snackbar notification and call to action.
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            binding.root,
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                setActionTextColor(getColor(R.color.colorPrimary))
            }
            show()
        }
    }

    override fun onStop() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            }

    }
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.detailMission){

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO){
                    val chosenMission=applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE).getInt((R.string.chosen_mission_number).toString(), 0)
                    val selectedMission= AllDatabase.getInstance(applicationContext).MissionsDatabaseDao.doesMissionExist(chosenMission)?.asActiveDomainModel()
                    if(selectedMission!=null){
                        val args:Bundle=Bundle()
                        args.putParcelable("selectedMission",selectedMission)
                        args.putBoolean("showImage",true)
                        this@HomeActivity.findNavController(R.id.myNavHostFragmentToSignOut).navigate(item.itemId,args)
                    }
                }
            }
            return true
        }
        else{return super.onOptionsItemSelected(item)}
    }*/



    /*override fun displayUserName(userName: String) {
        val header=binding.navView.getHeaderView(0)
        header.user_name.text=userName
    }*/

    /*override fun displayLevel(level: Int) {
        val header=binding.navView.getHeaderView(0)
        when(level){
            1->{header.level.text="p"}
            2->{header.level.text="p"}
            else->{header.level.text="p"}
        }
    }*/

    override fun openCloseNavigationDrawer(view: View){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    override fun displayBottomNavigation(display: Boolean) {
        if(display)
            binding.bottomNavView.visibility=View.VISIBLE
        else
            binding.bottomNavView.visibility=View.GONE
    }
    override fun setDrawerEnabled(enabled: Boolean){
        val lockMode=if(enabled){
            DrawerLayout.LOCK_MODE_UNLOCKED
        }
        else{
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        }
        drawerLayout.setDrawerLockMode(lockMode)
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        /*if(item.itemId==R.id.detailMission){
            val args=Bundle()
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO){
                    val chosenMission=applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE).getInt((R.string.chosen_mission_number).toString(), 0)
                    val selectedMission= AllDatabase.getInstance(applicationContext).MissionsDatabaseDao.doesMissionExist(chosenMission)?.asActiveDomainModel()
                    if(selectedMission!=null){
                        args.putParcelable("selectedMission",selectedMission)
                        args.putBoolean("showImage",true)
                    }
                }
            }.invokeOnCompletion { this@HomeActivity.findNavController(R.id.myNavHostFragmentToSignOut).navigate(item.itemId,args) }
        }*/
        when(item.itemId){
            R.id.reportBugFragment ->{
                val intent= Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("spandverse@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_bug_in,getString(R.string.app_version)))
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
            R.id.becomeSponsor ->{
                val intent= Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("spandverse@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.interest_become_sponsor))
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
            R.id.inviteFriends->{
                val link="https://seseva.page.link/invite"
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, link)
                startActivity(Intent.createChooser(intent, "Share Link"))
            }
            R.id.rateUs->{
                val manager = ReviewManagerFactory.create(this)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(this, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            binding.root.let {
                                Snackbar.make(it, "Thanks for your time", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.root.let {
                            Snackbar.make(it, "Something went wrong", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            R.id.FAQFragment->{}
            else ->this@HomeActivity.findNavController(R.id.myNavHostFragmentToSignOut).navigate(item.itemId)
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    companion object {
        const val DAYS_FOR_FLEXIBLE_UPDATE=4
        const val MY_REQUEST_CODE=22
    }

}


interface DrawerLocker {
    fun setDrawerEnabled(enabled: Boolean)
    fun openCloseNavigationDrawer(view: View)
    //fun displayUserName(userName:String)
    //fun displayLevel(level:Int)
    fun displayBottomNavigation(display: Boolean)
}