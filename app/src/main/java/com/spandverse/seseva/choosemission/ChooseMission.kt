package com.spandverse.seseva.choosemission

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
import com.spandverse.seseva.R
import com.spandverse.seseva.adapters.ActiveMissionsAdapter
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentChooseMissionBinding
import com.spandverse.seseva.home.DrawerLocker

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
                Log.i("SDF45","$it")
                findNavController().navigate(ChooseMissionDirections.actionChooseMissionFragmentToDetailMission(it))
                viewModel.toDetailMissionComplete()
            }
        })
        val chosenMissionNumber=sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0)?:0
        val adapter=ActiveMissionsAdapter(ActiveMissionsAdapter.OnClickListener{viewModel.toDetailMission(it)})
        viewModel.activeMissions.observe(viewLifecycleOwner, Observer {
            Log.i("CM","a$it")
            val list = it.filter { mission -> mission.missionNumber!= chosenMissionNumber}
            if(list.isNotEmpty()){
                Log.i("CM","b$list")
                Log.i("CM","$chosenMissionNumber")
                adapter.submitList(list)
            }
        })
        val drawerLocker=(activity as DrawerLocker?)
        viewModel.drawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible && chosenMissionNumber !=0) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
                binding.toolbar.setNavigationOnClickListener {
                    activity?.onBackPressed()
                }
                drawerLocker!!.setDrawerEnabled(true)
                drawerLocker.displayBottomNavigation(true)
                val r=activity?.resources
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F,r?.displayMetrics)
                binding.toolbar.titleMarginStart=0
            } else {
                (activity as DrawerLocker?)!!.setDrawerEnabled(false)
                drawerLocker!!.displayBottomNavigation(false)
            }
        })
        binding.toolbar.title=getString(R.string.titlebar_choose_your_mission)
        binding.activeMissionsList.adapter=adapter
        return binding.root
    }
}