package com.example.us0.home.lastmission

import android.content.Context
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
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentLastMissionCompletedBinding


class LastMissionCompletedFragment : Fragment() {
    private lateinit var binding:FragmentLastMissionCompletedBinding
    private lateinit var viewModel:LastMissionCompletedViewModel
    private lateinit var viewModelFactory:LastMissionCompletedViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val args=LastMissionCompletedFragmentArgs.fromBundle(requireArguments())
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_mission_completed, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = LastMissionCompletedViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LastMissionCompletedViewModel::class.java)
        binding.lastMissionCompletedViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        viewModel.loadMission(args.missionNumber)

        viewModel.goToHome.observe(viewLifecycleOwner, Observer<Boolean> { goto->
            if(goto){
                findNavController().navigate(LastMissionCompletedFragmentDirections.actionLastMissionCompletedFragmentToPassageFragment())
                viewModel.onGoToHomeComplete()
            }
        })
        return binding.root
    }


}