package com.example.us0.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.Actions
import com.example.us0.R
import com.example.us0.choosemission.ChooseMissionViewModel
import com.example.us0.choosemission.ChooseMissionViewModelFactory
import com.example.us0.choosemission.DetailMissionViewModel
import com.example.us0.choosemission.DetailMissionViewModelFactory
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentDetailMissionBinding
import com.example.us0.databinding.FragmentHomeBinding
import com.example.us0.foregroundnnotifications.TestService
import com.example.us0.installedapps.InstalledAppsDirections

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        val appDatasource = AllDatabase.getInstance(application).AppDatabaseDao
        val pm = requireNotNull(activity?.packageManager)
        viewModelFactory = HomeViewModelFactory(datasource,appDatasource, application,pm)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.lifecycleOwner=viewLifecycleOwner
        binding.homeViewModel=viewModel
        viewModel.notifyClosedMission.observe(viewLifecycleOwner, Observer {
            if(null!=it){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDetailClosedMissionFragment(it))
                viewModel.notifyClosedMissionComplete()
            }
        })
        viewModel.goToPermissionScreen.observe(viewLifecycleOwner,Observer<Boolean>{goto->
            if(goto){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPermissionFragment())
                viewModel.onGoToPermissionScreenComplete()
            }
        })

        viewModel.goToSignOut.observe(viewLifecycleOwner, Observer<Boolean>{ goToSignOut ->
            if (goToSignOut) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSignOutActivity())
                viewModel.onGoToSignOutComplete()

            }
        })
        viewModel.goToRules.observe(viewLifecycleOwner, Observer<Boolean>{ goToRules ->
            if (goToRules) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRulesFragment2())
                viewModel.onGoToRulesComplete()

            }
        })
        binding.toolbar.title=getString(R.string.app_name)
        return binding.root
    }

}