package com.spandverse.seseva.home

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentHomeBinding
import com.spandverse.seseva.drawoverotherapps.DOOAFragment
import com.spandverse.seseva.drawoverotherapps.DOOAFragmentDirections
import com.spandverse.seseva.home.closedmissions.MissionAccomplishedDialog

class HomeFragment : Fragment(),MissionAccomplishedDialog.MissionAccomplishedDialogListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: HomeViewModelFactory
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback=activity?.onBackPressedDispatcher?.addCallback(this,object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                activity!!.finish()
            }

        })
    }

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
        /*val fm=activity?.supportFragmentManager
        fm?.let{
            for (i in 0 until it.backStackEntryCount) {
                it.popBackStack()
                Log.i("HF","poi")
            }
        }*/

        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        val appDatasource = AllDatabase.getInstance(application).AppDatabaseDao
        val pm = requireNotNull(activity?.packageManager)
        viewModelFactory = HomeViewModelFactory(datasource, appDatasource, application, pm)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        binding.lifecycleOwner=viewLifecycleOwner
        binding.homeViewModel=viewModel
        binding.mainContainer.transitionToStart()
        //context?.let { binding.usageStatistics.paint.shader=LinearGradient(0,0,0,20,ContextCompat.getColor(it,R.color.t1),null,Shader.TileMode.CLAMP) }
        (activity as DrawerLocker?)!!.displayBottomNavigation(false)

        viewModel.goToPermissionScreen.observe(viewLifecycleOwner, Observer<Boolean> { goto ->
            if (goto) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPermissionFragment())
                viewModel.onGoToPermissionScreenComplete()
            }
        })
        viewModel.accomplishedMissionYouRaised.observe(
            viewLifecycleOwner,
            Observer<Int> { contribution ->
                if (contribution != null) {
                    val totalRaised = viewModel.accomplishedMissionTotalRaised.value ?: 0
                    val dialog = MissionAccomplishedDialog()
                    val args = Bundle()
                    args.putInt("you_raised", contribution)
                    args.putInt("total_raised", totalRaised)
                    dialog.arguments = args
                    val fraManager = childFragmentManager
                    dialog.show(fraManager, "accomplished_mission_dialog ")
                }
            })
        viewModel.goToMissionsScreen.observe(viewLifecycleOwner, Observer { go ->
            if (go) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFeatsFragment())
                viewModel.onGoToMissionsScreenComplete()
            }
        })
        /*viewModel.checkForUpdate.observe(viewLifecycleOwner, Observer {t->
            if(t) {

                viewModel.onGoToMissionsScreenComplete()
            }
        })*/
        viewModel.goToRules.observe(viewLifecycleOwner, Observer<Boolean> { goToRules ->
            if (goToRules) {
                binding.rulebookBg.isPressed=true
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRulesFragment2())
                viewModel.onGoToRulesComplete()
            }
            else{
                binding.rulebookBg.isPressed=false
            }
        })
        viewModel.goToProfile.observe(viewLifecycleOwner, Observer<Boolean> { goToFeats ->
            if (goToFeats) {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
                viewModel.onGoToProfileComplete()
            }
        })
        viewModel.goToUsageOverview.observe(
            viewLifecycleOwner,
            Observer<Boolean> { goToUsageOverview ->
                if (goToUsageOverview) {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToUsageOverViewFragment())
                    viewModel.onGoToUsageOverviewComplete()
                }
            })
        viewModel.showOverlayPermissionBanner.observe(viewLifecycleOwner, Observer<Boolean> { show ->
                if (show) {
                    Log.i("HVM1","4")
                   binding.bannerBody.text=getString(R.string.overlay_permission_banner_body)
                    binding.positive.text="GRANT PERMISSION"
                    binding.positive.setOnClickListener {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context?.packageName)
                        )
                        startActivityForResult(
                            intent,
                            REQUEST_OVERLAY_PERMISSION
                        )
                    }
                    binding.mainContainer.transitionToEnd()
                    viewModel.showOverlayPermissionBannerComplete()
                }
            })
        viewModel.showStrictModeBanner.observe(viewLifecycleOwner, Observer<Boolean> { show ->
            if (show) {
                binding.bannerBody.text=getString(R.string.strict_mode_banner_body)
                binding.positive.text="ENABLE STRICT MODE"
                binding.positive.setOnClickListener {
                    binding.mainContainer.transitionToStart()
                    viewModel.enableStrictMode()
                    view?.let {
                        Snackbar.make(it, "Strict mode enabled successfully", Snackbar.LENGTH_SHORT).show()
                    }
                }
                binding.mainContainer.transitionToEnd()
                viewModel.showStrictModeBannerComplete()
            }
        })
        binding.negative.setOnClickListener{
            binding.mainContainer.transitionToStart()
        }
        //binding.toolbar.title=getString(R.string.app_name)
        binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
        binding.toolbar.setNavigationOnClickListener { v-> (activity as HomeActivity).openCloseNavigationDrawer(
            v
        )}
        //binding.toolbar.na
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.stopHandler()
        viewModel.runHandler()
    }
    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(context)){
                binding.mainContainer.transitionToStart()
                viewModel.enableMediumMode()
                view?.let {
                    Snackbar.make(it, "Medium mode enabled successfully", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun chooseNewMission() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseMissionFragment())
    }
    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 5469
    }
}