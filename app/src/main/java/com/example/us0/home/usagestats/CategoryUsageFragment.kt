package com.example.us0.home.usagestats

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
import com.example.us0.adapters.StatAdapter
import com.example.us0.databinding.FragmentCategoryUsageBinding
import com.example.us0.foregroundnnotifications.InfoPopUpWindow


class CategoryUsageFragment : Fragment() {
    private lateinit var binding:FragmentCategoryUsageBinding
    private val viewModel:UsageOverViewViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category_usage, container, false)
        binding.usageOverViewViewModel= viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val adapter= StatAdapter(StatAdapter.OnClickListener{
            viewModel.toAppUsageScreen(it)
            })
        Log.i("CUF","tyu")
        viewModel.navigateToSelectedApp.observe(viewLifecycleOwner, Observer {navigate->
            if(navigate) {
                findNavController().navigate(CategoryUsageFragmentDirections.actionCategoryUsageFragmentToAppUsageFragment())
                viewModel.navigateToSelectedAppComplete()
            }
        })
        viewModel.appsInCatList.observe(viewLifecycleOwner, Observer {it->
            if(it.isEmpty()){
                binding.comparativeAnalysisLayout.visibility=View.GONE
                binding.tapToKnowText.visibility=View.GONE
            }
            else{
                binding.comparativeAnalysisLayout.visibility=View.VISIBLE
                binding.tapToKnowText.visibility=View.VISIBLE
            }

        })
        viewModel.screenHeading.observe(viewLifecycleOwner, Observer {heading->
            if(heading=="Today's statistics") {
                binding.i.visibility=View.GONE
            }
            else{
                binding.i.visibility=View.VISIBLE
            }
        })
        viewModel.appsInCatList.observe(viewLifecycleOwner, Observer {
            it?.let { adapter.submitList(it) }
        })
        binding.appRecyclerView.adapter=adapter
        binding.i.setOnClickListener {
            val popUpClass = InfoPopUpWindow()
            it?.let {
                popUpClass.showPopupWindow(it) }
        }
        val manager = GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false)
        binding.appRecyclerView.layoutManager=manager
        return binding.root
    }

}