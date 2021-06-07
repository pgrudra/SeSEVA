package com.spandverse.seseva.home.lastmission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentLastMissionCompletedBinding


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
        viewModel.enableButton.observe(viewLifecycleOwner, Observer<Boolean> { enable->
            if(enable){
                binding.progressBar1.visibility=View.GONE
                binding.skrim.visibility=View.GONE
                binding.button4.isEnabled=true
                binding.chooseNewMission.isEnabled=true
            }
        })
        viewModel.reportAvailable.observe(viewLifecycleOwner, Observer<Boolean> { reportAvailable->
            if(reportAvailable){
                binding.button4.visibility=View.VISIBLE
                binding.reportPendingText.visibility=View.GONE
            }
        })
        return binding.root
    }


}