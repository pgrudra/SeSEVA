package com.example.us0.home.lastmission

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
import com.example.us0.databinding.FragmentLastMissionBinding


class LastMissionFragment : Fragment() {

    private lateinit var binding: FragmentLastMissionBinding
    private lateinit var viewModel: LastMissionViewModel
    private lateinit var viewModelFactory: LastMissionViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_mission, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = LastMissionViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LastMissionViewModel::class.java)
        binding.lastMissionViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
viewModel.goToHome.observe(viewLifecycleOwner, Observer<Boolean> { goto->
    if(goto){
        findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToPassageFragment())
        viewModel.goToHomeComplete()
    }
})
        viewModel.goToRules.observe(viewLifecycleOwner, Observer<Boolean> { goto->
            if(goto){
                findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToRulesFragment2())
                viewModel.goToRulesComplete()
            }
        })
        viewModel.goToLastMissionCompleted.observe(viewLifecycleOwner, Observer<Int> { number->
                findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToLastMissionCompletedFragment(number))
        })


       return binding.root
    }

}