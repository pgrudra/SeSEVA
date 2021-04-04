package com.example.us0.home.feats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.choosemission.ChooseMissionViewModel
import com.example.us0.choosemission.ChooseMissionViewModelFactory
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentFeatsBinding


class FeatsFragment : Fragment() {
    private lateinit var binding: FragmentFeatsBinding
    private lateinit var viewModel: FeatsViewModel
    private lateinit var viewModelFactory: FeatsViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feats, container, false)
        val application = requireNotNull(this.activity).application
        val dataBaseDAO = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory= FeatsViewModelFactory(dataBaseDAO, application)
        viewModel=ViewModelProvider(this, viewModelFactory).get(FeatsViewModel::class.java)
        binding.featsViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}