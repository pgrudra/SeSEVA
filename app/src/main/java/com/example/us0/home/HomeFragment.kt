package com.example.us0.home

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentHomeBinding
import com.example.us0.home.closedmissions.MissionAccomplishedDialog

class HomeFragment : Fragment(),MissionAccomplishedDialog.MissionAccomplishedDialogListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory
    private lateinit var drawerLayout: DrawerLayout
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
        val appDatasource = AllDatabase.getInstance(application).AppDatabaseDao
        val pm = requireNotNull(activity?.packageManager)
        viewModelFactory = HomeViewModelFactory(datasource,appDatasource, application,pm)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.lifecycleOwner=viewLifecycleOwner
        binding.homeViewModel=viewModel
        //context?.let { binding.usageStatistics.paint.shader=LinearGradient(0,0,0,20,ContextCompat.getColor(it,R.color.t1),null,Shader.TileMode.CLAMP) }
        (activity as DrawerLocker?)!!.displayBottomNavigation(false)

        viewModel.goToPermissionScreen.observe(viewLifecycleOwner,Observer<Boolean>{goto->
            if(goto){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPermissionFragment())
                viewModel.onGoToPermissionScreenComplete()
            }
        })
        viewModel.accomplishedMissionYouRaised.observe(viewLifecycleOwner,Observer<Int>{contribution->
            if(contribution!=null){
                val totalRaised=viewModel.accomplishedMissionTotalRaised.value?:0
                val dialog= MissionAccomplishedDialog()
                val args=Bundle()
                args.putInt("you_raised",contribution)
                args.putInt("total_raised",totalRaised)
                dialog.arguments=args
                val fraManager=childFragmentManager
                dialog.show(fraManager, "accomplished_mission_dialog ")
            }
        })
        viewModel.goToMissionsScreen.observe(viewLifecycleOwner, Observer {go->
            if(go) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFeatsFragment())
                viewModel.onGoToMissionsScreenComplete()
            }
        })

        viewModel.goToRules.observe(viewLifecycleOwner, Observer<Boolean>{ goToRules ->
            if (goToRules) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRulesFragment2())
                viewModel.onGoToRulesComplete()
            }
        })
        viewModel.goToProfile.observe(viewLifecycleOwner, Observer<Boolean>{ goToFeats ->
            if (goToFeats) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
                viewModel.onGoToProfileComplete()
            }
        })
        viewModel.goToUsageOverview.observe(viewLifecycleOwner, Observer<Boolean>{ goToUsageOverview ->
            if (goToUsageOverview) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToUsageOverViewFragment())
                viewModel.onGoToUsageOverviewComplete()
            }
        })
        //binding.toolbar.title=getString(R.string.app_name)
        binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
        binding.toolbar.setNavigationOnClickListener { v-> (activity as HomeActivity).openCloseNavigationDrawer(v)}
        //binding.toolbar.na
        return binding.root
    }

    override fun chooseNewMission() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseMissionFragment())
    }

}