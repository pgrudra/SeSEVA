package com.example.us0.foregroundnnotifications

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentPermissionBinding


class PermissionFragment : Fragment() {

    private lateinit var binding: FragmentPermissionBinding
    private lateinit var viewModel: PermissionViewModel
    private lateinit var viewModelFactory: PermissionViewModelFactory

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
        //val application = requireNotNull(this.activity).application
        viewModelFactory = PermissionViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(PermissionViewModel::class.java)
        binding.lifecycleOwner = this
        binding.permissionViewModel = viewModel

        viewModel.grantPermission.observe(viewLifecycleOwner, Observer<Boolean> { grantPermission ->
            if (grantPermission) {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                viewModel.onGrantPermissionComplete()
            }

        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val appOps = activity?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            activity?.packageName?.let {
                appOps.unsafeCheckOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        } else {
            activity?.packageName?.let {
                appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        }
        if (mode == AppOpsManager.MODE_ALLOWED) {
            findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToForegroundService())
        }
    }


}