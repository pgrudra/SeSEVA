package com.example.us0.home.usagestats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.databinding.FragmentAppUsageBinding
import com.example.us0.databinding.FragmentCategoryUsageBinding


class AppUsageFragment : Fragment() {
    private lateinit var binding: FragmentAppUsageBinding
    private val viewModel:UsageOverViewViewModel by activityViewModels()
    //private lateinit var viewModelFactory: AppUsageViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_usage, container, false)
        binding.usageOverViewViewModel=viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.appPackageNameForAppScreen.observe(viewLifecycleOwner, Observer {
            binding.appIcon.setImageDrawable(context?.packageManager?.getApplicationIcon(it))
        })
        return binding.root
    }
}