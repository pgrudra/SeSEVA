package com.example.us0.home.usagestats

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.us0.R
import com.example.us0.adapters.AppsCategoryBriefAdapter
import com.example.us0.databinding.FragmentUsageOverViewBinding


class UsageOverviewFragment : Fragment() {

    private lateinit var binding: FragmentUsageOverViewBinding
    private val viewModel:UsageOverViewViewModel by activityViewModels{UsageOverViewViewModelFactory(requireNotNull(this.activity).application)}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_usage_over_view,
            container,
            false
        )
        binding.usageOverViewViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val adapter=AppsCategoryBriefAdapter(AppsCategoryBriefAdapter.OnClickListener{
            viewModel.toCatUsageScreen(it)
            findNavController().navigate(UsageOverviewFragmentDirections.actionUsageOverViewFragmentToCategoryUsageFragment())
        })
        viewModel.navigateToSelectedApp.observe(viewLifecycleOwner, Observer {navigate->
            if(navigate) {
                findNavController().navigate(UsageOverviewFragmentDirections.actionUsageOverViewFragmentToAppUsageFragment())
                viewModel.navigateToSelectedAppComplete()
            }
        })
        viewModel.listOfCats.observe(viewLifecycleOwner, Observer { it->
            it?.let { adapter.submitList(it) }
        })

        binding.catRecyclerView.adapter=adapter
        val manager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        binding.catRecyclerView.layoutManager=manager
        return binding.root
    }


}