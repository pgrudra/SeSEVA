package com.spandverse.seseva.home

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
import com.spandverse.seseva.databinding.FragmentPassageBinding


class PassageFragment : Fragment() {

    private lateinit var binding: FragmentPassageBinding
    private lateinit var viewModel: PassageViewModel
    private lateinit var viewModelFactory: PassageViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_passage, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = PassageViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PassageViewModel::class.java)
        binding.lifecycleOwner=this
        //check Internet, then check name, then check mission
        viewModel.goToAskNameFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(PassageFragmentDirections.actionPassageFragmentToAskName())
                viewModel.goToAskNameFragmentComplete()
            }
        })
        viewModel.goToChooseMissionFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
               findNavController().navigate(PassageFragmentDirections.actionPassageFragmentToChooseMissionFragment())
                viewModel.goToChosenMissionFragmentComplete()
            }
        })
        viewModel.goToLastMissionFragment.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(PassageFragmentDirections.actionPassageFragmentToLastMissionFragment())
                viewModel.goToLastMissionFragmentComplete()
            }
        })
        viewModel.goToHomeFragment.observe(viewLifecycleOwner,Observer<Boolean>{goto->
            if(goto){
                findNavController().navigate(PassageFragmentDirections.actionPassageFragmentToHomeFragment())
                viewModel.goToHomeFragmentComplete()
            }
        })
        viewModel.goToRules.observe(viewLifecycleOwner,Observer<Boolean>{goto->
            if(goto){
                findNavController().navigate(PassageFragmentDirections.actionPassageFragmentToRulesFragment2())
                viewModel.goToRulesComplete()
            }
        })
        return binding.root
    }



}