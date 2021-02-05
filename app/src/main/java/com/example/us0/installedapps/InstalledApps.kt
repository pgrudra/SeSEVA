package com.example.us0.installedapps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.adapters.InstalledAppAdapter
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentInstalledAppsBinding


class InstalledApps : Fragment() {

    private lateinit var binding: FragmentInstalledAppsBinding
    private lateinit var viewModel: InstalledAppsViewModel
    private lateinit var viewModelFactory: InstalledAppsViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_installed_apps,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val pm = requireNotNull(activity?.packageManager)
        val datasource= AllDatabase.getInstance(application).AppDatabaseDao
        viewModelFactory= InstalledAppsViewModelFactory(datasource,application,pm)
        viewModel=ViewModelProvider(this, viewModelFactory).get(InstalledAppsViewModel::class.java)

        binding.lifecycleOwner = this
        binding.installedAppsViewModel=viewModel

        viewModel.goToSignOut.observe(viewLifecycleOwner, Observer<Boolean>{ goToSignOut ->
            if (goToSignOut) {
                findNavController().navigate(InstalledAppsDirections.actionInstalledAppsToSignOutActivity())
                viewModel.onGoToSignOutComplete()
            }
        })
        viewModel.proceed.observe(viewLifecycleOwner,Observer<Boolean>{ proceed->
            if(proceed) {
                viewModel.checkPermission()
            }
            })
        viewModel.goToForegroundService.observe(viewLifecycleOwner,Observer<Boolean>{goToForegroundService->
            if(goToForegroundService){
                findNavController().navigate(InstalledAppsDirections.actionInstalledAppsToForegroundService())
                viewModel.onGoToForegroundServiceComplete()
            }
        })

        viewModel.goToPermissionScreen.observe(viewLifecycleOwner,Observer<Boolean>{goToPermissionScreen->
            if(goToPermissionScreen){
                findNavController().navigate(InstalledAppsDirections.actionInstalledAppsToPermissionFragment())
                viewModel.onGoToPermissionScreenComplete()
            }
        })
        return binding.root
    }

}
