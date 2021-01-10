package com.example.us0.home

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
import com.example.us0.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = HomeViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.homeViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        //check Internet, then check name, then check mission
        viewModel.goToAskNameFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAskName())
                viewModel.goToAskNameFragmentComplete()
            }
        })
        viewModel.goToChooseMissionFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseMissionFragment())
                viewModel.goToChosenMissionFragmentComplete()
            }
        })
        viewModel.connectedToNetwork.observe(viewLifecycleOwner, Observer<Boolean> {internetAvailable->
            if(!internetAvailable){
                //show no internet
                }
        })
        viewModel.goToLastMissionFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLastMissionFragment())
                viewModel.goToLastMissionFragmentComplete()
            }
        })
        return binding.root
    }


}