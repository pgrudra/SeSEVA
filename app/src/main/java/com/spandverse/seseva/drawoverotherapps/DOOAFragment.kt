package com.spandverse.seseva.drawoverotherapps

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentDOOABinding
import com.spandverse.seseva.home.DrawerLocker


class DOOAFragment : Fragment() {
    private lateinit var binding: FragmentDOOABinding
    private lateinit var appContext: Context
    private lateinit var sharedPref: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_d_o_o_a,
            container,
            false
        )
        binding.lifecycleOwner = this
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        appContext = context?.applicationContext ?: return binding.root
        sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)){
            if(checkIfDeviceSpecificPermissionNeeded()){
                    findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToAutostartPermissionFragment())
            }
            else{
                with(sharedPref.edit()) {
                    this?.putBoolean((com.spandverse.seseva.R.string.autostart_permission_asked).toString(), true)
                    this?.apply()
                }
                findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment())
            }

        }

        binding.allow.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    // permission not granted...
                        try{
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context?.packageName)
                            )
                            startActivityForResult(
                                intent,
                                REQUEST_OVERLAY_PERMISSION
                            )
                        }catch (e:Exception){
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            startActivityForResult(
                                intent,
                                REQUEST_OVERLAY_PERMISSION
                            )
                        }

                }
            }
        }
        binding.askMeLater.setOnClickListener {
            if(checkIfDeviceSpecificPermissionNeeded()){
                findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToAutostartPermissionFragment())
            }
            else{
                findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment())
            } }

        return binding.root
    }

    private fun checkIfDeviceSpecificPermissionNeeded(): Boolean {
        val manufacturer=android.os.Build.MANUFACTURER
        return if(sharedPref.getBoolean((R.string.autostart_permission_asked).toString(), false))
            false
        else "xiaomi".equals(manufacturer,true) ||
                "oppo".equals(manufacturer,true) ||
                "vivo".equals(manufacturer,true) ||
                "Letv".equals(manufacturer,true) ||
                "Honor".equals(manufacturer,true)||
                "asus".equals(manufacturer,true)||
                "nokia".equals(manufacturer,true)

    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (!Settings.canDrawOverlays(context)) {
                // You don't have permission
                //checkPermission()
            } else {
                if(checkIfDeviceSpecificPermissionNeeded()){
                    findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToAutostartPermissionFragment())
                }
                else{
                    findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment())
                }
            }
        }
    }
   /* private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context?.packageName)
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            }
        }
    }*/


    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 5469
    }
}