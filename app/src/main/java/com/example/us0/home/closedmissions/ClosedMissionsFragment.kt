package com.example.us0.home.closedmissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentAskNameBinding
import com.example.us0.databinding.FragmentClosedMissionsBinding

class ClosedMissionsFragment : Fragment() {

    private lateinit var binding: FragmentClosedMissionsBinding
    private lateinit var viewModel: ClosedMissionsViewModel
    private lateinit var viewModelFactory: ClosedMissionsViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_closed_missions, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = ClosedMissionsViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ClosedMissionsViewModel::class.java)
        //check Internet, then check name, then check mission
        return binding.root
    }


}