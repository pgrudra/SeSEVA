package com.example.us0.drawoverotherapps

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentDOOABinding
import com.example.us0.home.DrawerLocker


class DOOAFragment : Fragment() {
    private lateinit var binding: FragmentDOOABinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)){
            findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment())
        }
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_d_o_o_a,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        binding.lifecycleOwner = this
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)

        binding.allow.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    // permission not granted...
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context?.packageName)
                    )
                    startActivityForResult(
                        intent,
                        REQUEST_OVERLAY_PERMISSION
                    )
                }
            } else {
                Log.i("DOOAF", "should never occur")
            }
        }
        binding.askMeLater.setOnClickListener { findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment()) }

        return binding.root
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (!Settings.canDrawOverlays(context)) {
                // You don't have permission
                //checkPermission()
            } else {
                findNavController().navigate(DOOAFragmentDirections.actionDOOAFragmentToHomeFragment())
            }
        }
    }
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context?.packageName)
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            }
        }
    }


    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 5469
    }
}