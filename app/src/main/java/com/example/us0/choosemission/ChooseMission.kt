package com.example.us0.choosemission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.adapters.ActiveMissionsAdapter
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentChooseMissionBinding
import com.example.us0.databinding.FragmentInstalledAppsBinding
import com.example.us0.foregroundnnotifications.ForegroundServiceDirections
import com.example.us0.installedapps.InstalledAppsViewModel
import com.example.us0.installedapps.InstalledAppsViewModelFactory

class ChooseMission : Fragment() {
    private lateinit var binding: FragmentChooseMissionBinding
    private lateinit var viewModel: ChooseMissionViewModel
    private lateinit var viewModelFactory: ChooseMissionViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_choose_mission,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = ChooseMissionViewModelFactory(datasource, application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ChooseMissionViewModel::class.java)
        binding.chooseMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.navigateToSelectedMission.observe(viewLifecycleOwner, Observer {
            if(null!=it) {
                findNavController().navigate(ChooseMissionDirections.actionChooseMissionFragmentToDetailMission(it))
                viewModel.toDetailMissionComplete()
            }
        })
        val adapter=ActiveMissionsAdapter(ActiveMissionsAdapter.OnClickListener{viewModel.toDetailMission(it)})
        viewModel.activeMissions.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        binding.toolbar.title=getString(R.string.titlebar_choose_your_mission)
        binding.activeMissionsList.adapter=adapter
        return binding.root
    }
}