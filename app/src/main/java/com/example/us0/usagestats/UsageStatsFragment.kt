package com.example.us0.usagestats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.databinding.FragmentForegroundBinding
import com.example.us0.databinding.FragmentUsageStatsBinding
import com.example.us0.foregroundnnotifications.ForegroundServiceViewModel
import com.example.us0.foregroundnnotifications.ForegroundServiceViewModelFactory


class UsageStatsFragment : Fragment() {

    private lateinit var binding: FragmentUsageStatsBinding
    private lateinit var viewModel: UsageStatsViewModel
    private lateinit var viewModelFactory: UsageStatsViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_usage_stats,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        viewModelFactory = UsageStatsViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(UsageStatsViewModel::class.java)
        binding.usageStatsViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


}