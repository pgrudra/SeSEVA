package com.example.us0.installedapps

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentInstalledAppsBinding
import java.util.*


class InstalledApps : Fragment() {

    private lateinit var binding: FragmentInstalledAppsBinding
    private lateinit var viewModel: InstalledAppsViewModel
    private lateinit var viewModelFactory: InstalledAppsViewModelFactory
    var arrayAdapter: ArrayAdapter<*>? = null

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
        viewModelFactory= InstalledAppsViewModelFactory()
        viewModel=ViewModelProvider(this, viewModelFactory).get(InstalledAppsViewModel::class.java)

        viewModel.goToSignOut.observe(viewLifecycleOwner, Observer { goToSignOut ->
            if (goToSignOut) {
                findNavController().navigate(InstalledAppsDirections.actionInstalledAppsToSignOutActivity())
                viewModel.onGoTOSignOutComplete()
            }
        })

        binding.installedAppsViewModel=viewModel
        /*installedApps()
        r()*/
        n()
        return binding.root
    }

    private fun installedApps() {
        val list = activity?.packageManager?.getInstalledPackages(0)
        var appMutableList: MutableList<String?>? = null
        if (list != null) {
            Log.i("II", list.size.toString())
            for (i in list.indices) {
                val packageInfo = list.get(i)
                if (packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {

                    val appName =
                        activity?.packageManager?.let {
                            packageInfo.applicationInfo.loadLabel(it).toString()
                        }


                    if (appName != null) {
                        //Log.i("II", appName)
                        appMutableList?.add(appName)
                    }

                    val appContext = context?.applicationContext ?: return
                    arrayAdapter = ArrayAdapter(
                        appContext,
                        R.layout.support_simple_spinner_dropdown_item, list as List<*>
                    )
                    binding.listView.adapter = arrayAdapter
                }
            }

        }
    }
    fun r(){
        val pm: PackageManager = activity?.packageManager ?: return
//get a list of installed apps.
//get a list of installed apps.
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        var totApps=packages.size
        var packagesWithCat=0
        var apwtname=0
        for (packageInfo in packages) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                /*Log.i("II","Installed package :" + packageInfo.packageName)
                Log.i("II", "Installed package :" + packageInfo.category)*/
                if(packageInfo.category!=-1){packagesWithCat+=1}
                totApps+=1
            }
            else{
                /*Log.i("pp", "Installed package :" + packageInfo.packageName)*/
            }
            var apNam=packageInfo.loadLabel(pm).toString()

            if(apNam!=null){
                /*Log.i("II", apNam)*/
                apwtname+=1
            }
            /*Log.i("II", "Source dir : " + packageInfo.sourceDir)
            Log.i("II", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName))*/
        }
        Log.i("II", apwtname.toString())
        Log.i("II", totApps.toString())
        Log.i("II", packagesWithCat.toString())
    }

    fun n(){
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
        Log.i("II", launchables.size.toString())
        /*val pm: PackageManager = getPackageManager()
        val main = Intent(Intent.ACTION_MAIN, null)

        main.addCategory(Intent.CATEGORY_LAUNCHER)

        val launchables = pm.queryIntentActivities(main, 0)

        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(pm)
        )

        adapter = AppAdapter(pm, launchables)
        setListAdapter(adapter)*/
    }

}
