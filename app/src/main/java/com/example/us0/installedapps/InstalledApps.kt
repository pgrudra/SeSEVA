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
        val adapter= InstalledAppAdapter()
        viewModel.apps.observe(viewLifecycleOwner, Observer { it?.let{adapter.submitList(it)} })
        binding.installedAppsList.adapter=adapter
        return binding.root
    }

    /*fun n(){
        val pm: PackageManager = activity?.packageManager ?: return
        val main=Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables = pm.queryIntentActivities(main, 0)
        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(pm)
        )
        val app_name_list = ArrayList<String>()
        val app_package_list = ArrayList<String>()
        for(item in launchables){
            try {
                val package_name: String = item.activityInfo.packageName
                val app_name = pm.getApplicationLabel(
                    pm.getApplicationInfo(
                        package_name, PackageManager.GET_META_DATA
                    )
                ) as String
                var copy = false
                for (i in app_name_list.indices) {
                    if (package_name == app_package_list[i]) copy = true
                }
                if (!copy) {
                    app_name_list.add(app_name)
                    app_package_list.add(package_name)
                }
                Log.i("CH", "package = <" + package_name + "> name = <" + app_name + ">");
            } catch (e: Exception) {
            }
        }

    }*/

}
