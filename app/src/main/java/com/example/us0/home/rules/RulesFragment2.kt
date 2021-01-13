package com.example.us0.home.rules

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
import com.example.us0.choosemission.DetailMissionViewModel
import com.example.us0.choosemission.DetailMissionViewModelFactory
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentRules2Binding
import com.example.us0.ui.login.WelcomeBackViewModel
import com.example.us0.ui.login.WelcomeBackViewModelFactory

class RulesFragment2 : Fragment() {
   private lateinit var binding:FragmentRules2Binding
    private lateinit var viewModel: Rules2ViewModel
    private lateinit var viewModelFactory: Rules2ViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rules2, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = Rules2ViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(Rules2ViewModel::class.java)
        binding.rules2ViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        viewModel.toHomeFragment.observe(viewLifecycleOwner, Observer<Boolean>{goToHome->
            if(goToHome){
                findNavController().navigate(RulesFragment2Directions.actionRulesFragment2ToHomeFragment())
                viewModel.goToHomeComplete()
            }
        })
        return binding.root
    }


}