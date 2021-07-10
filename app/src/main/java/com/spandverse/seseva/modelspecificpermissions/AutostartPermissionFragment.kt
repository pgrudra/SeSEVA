package com.spandverse.seseva.modelspecificpermissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentAutostartPermissionBinding
import com.spandverse.seseva.home.DrawerLocker


class AutostartPermissionFragment : Fragment() {
    private lateinit var binding:FragmentAutostartPermissionBinding

            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
                binding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.fragment_autostart_permission,
                    container,
                    false
                )
                binding.lifecycleOwner = this
                val drawerLocker=(activity as DrawerLocker?)
                drawerLocker!!.setDrawerEnabled(false)
                drawerLocker.displayBottomNavigation(false)
                val sharedPref = activity?.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
                with(sharedPref?.edit()) {
                    this?.putBoolean((com.spandverse.seseva.R.string.autostart_permission_asked).toString(), true)
                    this?.apply()
                }
                val infoSheet=binding.infoFragment.root
                val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(infoSheet)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            binding.skrim1.visibility = View.GONE
                            binding.checkAndGrant.isEnabled = true
                            binding.continueButton.isEnabled=true
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
                binding.infoButton.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    binding.skrim1.visibility = View.VISIBLE
                    binding.checkAndGrant.isEnabled = false
                    binding.continueButton.isEnabled=false
                }
                binding.skrim1.setOnClickListener {
                    binding.skrim1.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                binding.continueButton.setOnClickListener{
                    findNavController().navigate(AutostartPermissionFragmentDirections.actionAutostartPermissionFragmentToHomeFragment())
                }
                binding.checkAndGrant.setOnClickListener{
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
                                    intent.component= ComponentName(
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
                                            findNavController().navigate(AutostartPermissionFragmentDirections.actionAutostartPermissionFragmentToHomeFragment())
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
                        findNavController().navigate(AutostartPermissionFragmentDirections.actionAutostartPermissionFragmentToHomeFragment())
                    }
                }
        return binding.root
    }
}