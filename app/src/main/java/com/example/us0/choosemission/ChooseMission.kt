package com.example.us0.choosemission

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.adapters.ActiveMissionsAdapter
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentChooseMissionBinding
import com.example.us0.home.DrawerLocker

class ChooseMission : Fragment() {
    private lateinit var binding: FragmentChooseMissionBinding
    private lateinit var viewModel: ChooseMissionViewModel
    private lateinit var viewModelFactory: ChooseMissionViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_choose_mission,
            container,
            false
        )
        val appContext = context?.applicationContext ?: return binding.root
        val sharedPref = appContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = ChooseMissionViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChooseMissionViewModel::class.java)
        binding.chooseMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.navigateToSelectedMission.observe(viewLifecycleOwner, Observer {
            if(null!=it) {
                findNavController().navigate(ChooseMissionDirections.actionChooseMissionFragmentToDetailMission(it))
                viewModel.toDetailMissionComplete()
            }
        })
        val adapter=ActiveMissionsAdapter(ActiveMissionsAdapter.OnClickListener{viewModel.toDetailMission(it)})
        viewModel.activeMissions.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        viewModel.drawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible &&sharedPref.getInt((R.string.chosen_mission_number).toString(), 0) !=0) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
                binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v) }
                (activity as DrawerLocker?)!!.setDrawerEnabled(true)
                val r=activity?.resources
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F,r?.displayMetrics)
                binding.toolbar.titleMarginStart=0
                Log.i("RF","$visible")
            } else {
                (activity as DrawerLocker?)!!.setDrawerEnabled(false)
            }
        })
        binding.toolbar.title=getString(R.string.titlebar_choose_your_mission)
        binding.activeMissionsList.adapter=adapter
        return binding.root
    }
}