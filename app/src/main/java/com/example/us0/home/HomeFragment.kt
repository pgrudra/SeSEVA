package com.example.us0.home

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
import com.example.us0.choosemission.DetailMissionViewModel
import com.example.us0.choosemission.DetailMissionViewModelFactory
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentDetailMissionBinding
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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = HomeViewModelFactory(datasource, application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        return binding.root
    }

}