package com.example.us0.installedapps

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.us0.R
import com.example.us0.databinding.ActivityHomeBinding

class HomeActivity :AppCompatActivity(),DrawerLocker{
    lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout:DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val toolbar=findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        drawerLayout=binding.drawerLayout
val navHostFragment=supportFragmentManager.findFragmentById(R.id.myNavHostFragmentToSignOut) as NavHostFragment
        val navController=navHostFragment.navController
        NavigationUI.setupWithNavController(binding.navView, navController)

        //val appBarConfiguration= AppBarConfiguration(setOf(R.id.homeFragment))
        //NavigationUI.setupWithNavController(toolbar,navController,appBarConfiguration)
        //NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration)
        //NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout)
        //NavigationUI.setupWithNavController()
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.myNavHostFragmentToSignOut) as NavHostFragment
        val navController=navHostFragment.navController
        return NavigationUI.navigateUp(navController,drawerLayout)
    }*/

    override fun openCloseNavigationDrawer(view: View){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
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

}
interface DrawerLocker {
    fun setDrawerEnabled(enabled: Boolean)
    fun openCloseNavigationDrawer(view: View)
}