package com.spandverse.seseva.modelspecificpermissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
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
                binding.continueButton.setOnClickListener{
                    findNavController().navigate(AutostartPermissionFragmentDirections.actionAutostartPermissionFragmentToHomeFragment())
                }
                binding.checkAndGrant.setOnClickListener{
                    try {
                        val manufacturer=android.os.Build.MANUFACTURER
                        val intent=Intent()
                        if("xiaomi".equals(manufacturer,true)){
                            intent.component = ComponentName("com.miui.securitycenter","com.miui.permcenter.autostart.AutoStartManagementActivity")
                        }
                        else if("oppo".equals(manufacturer,true)){
                            intent.component = ComponentName("com.coloros.safecenter","com.coloros.safecenter.permission.startup.StartupAppListActivity")
                        }
                        else if("vivo".equals(manufacturer,true)){
                            intent.component = ComponentName("com.vivo.permissionmanager","com.vivo.permissionmanager.activity.BgStartupManagerActivity")
                        }
                        else if("Letv".equals(manufacturer,true)){
                            intent.component = ComponentName("com.letv.android.letvsafe","com.letv.android.letvsafe.AutobootManageActivity")
                        }
                        else if("Honor".equals(manufacturer,true)){
                            intent.component = ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.optimize.process.ProtectActivity")
                        }
                        else{
                            throw RuntimeException("exception with purpose")
                            }
                        val list=context?.packageManager?.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY)
                        list?.let{
                            if(it.size>0){
                                startActivity(intent)
                            }
                        }

                    } catch (e:Exception){
                        findNavController().navigate(AutostartPermissionFragmentDirections.actionAutostartPermissionFragmentToHomeFragment())
                    }
                }
        return binding.root
    }
}