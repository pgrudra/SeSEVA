package com.example.us0.home

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.us0.R
import com.example.us0.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.nav_header.view.*


class HomeActivity :AppCompatActivity(),DrawerLocker,
    BottomNavigationView.OnNavigationItemSelectedListener {
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
        checkForUpdate()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        drawerLayout=binding.drawerLayout
val navHostFragment=supportFragmentManager.findFragmentById(R.id.myNavHostFragmentToSignOut) as NavHostFragment
        val navController=navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
binding.bottomNavView.setOnNavigationItemSelectedListener(this)


        //navController.graph.findNode(R.id.detailMission)?.addArgument("selectedMission",NavArgument.Builder().setDefaultValue().build())
       /* navController.addOnDestinationChangedListener { controller, destination, arguments ->


            when(destination.id){
                R.id.detailMission -> {
                    Log.i("HAQW","$destination")

                        Log.i("HAQW","iouyt")

                            Log.i("HAQW","dfgj")
                            //val chosenMission=applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE).getInt((R.string.chosen_mission_number).toString(), 0)
                            //val selectedMission= AllDatabase.getInstance(applicationContext).MissionsDatabaseDao.doesMissionExist(chosenMission)?.asActiveDomainModel()
                            //if(selectedMission!=null){
                    val m=Mission()
                    val d=m.asActiveDomainModel()
                                val argument1= NavArgument.Builder().setDefaultValue(d).build()
                                //destination.addArgument("selectedMission",argument1)
                    destination.addArgument("p",argument1)
                    Log.i("HAQW","${destination.arguments}")
                                Log.i("HAQW","sedtgy")
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

    private fun checkForUpdate() {
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
        Log.i("HAttu","1")
        if(item.itemId==R.id.detailMission){
            Log.i("HAttu","$item")
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
            this@HomeActivity.findNavController(R.id.myNavHostFragmentToSignOut).navigate(item.itemId)

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