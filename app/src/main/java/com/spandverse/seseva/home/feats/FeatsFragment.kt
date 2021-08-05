package com.spandverse.seseva.home.feats

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.spandverse.seseva.R
import com.spandverse.seseva.adapters.AllMissionsAdapter
import com.spandverse.seseva.adapters.AllSponsorsAdapter
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentFeatsBinding
import com.spandverse.seseva.home.DrawerLocker
import java.util.*


class FeatsFragment : Fragment() {
    private lateinit var binding: FragmentFeatsBinding
    private lateinit var viewModel: FeatsViewModel
    private lateinit var viewModelFactory: FeatsViewModelFactory
    private lateinit var sharedPref: SharedPreferences
    private lateinit var appContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feats, container, false)
        val application = requireNotNull(this.activity).application
        val missionsDatabaseDAO = AllDatabase.getInstance(application).MissionsDatabaseDao
        val sponsorsDatabaseDAO = AllDatabase.getInstance(application).SponsorDatabaseDao
        viewModelFactory= FeatsViewModelFactory(missionsDatabaseDAO,sponsorsDatabaseDAO, application)
        viewModel=ViewModelProvider(this, viewModelFactory).get(FeatsViewModel::class.java)
        binding.featsViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLoker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
        binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(
            v
        ) }
        drawerLoker!!.setDrawerEnabled(true)
        drawerLoker.displayBottomNavigation(true)
        appContext = context?.applicationContext ?: return binding.root
        sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        with (sharedPref.edit()) {
            this?.putBoolean((R.string.from_rules).toString(), false)
            this?.apply()
        }
        binding.totalMissions.text=(sharedPref.getInt((R.string.total_missions).toString(),1)).toString()
        binding.activeMissions.text=(sharedPref.getInt((R.string.active_missions).toString(),1)).toString()
        binding.outermostConstraintLayout.post {
            val outerLayoutHeight=binding.outermostConstraintLayout.height
            binding.toolbar.post{
                val toolBarHeight=binding.toolbar.height
                binding.dataConstraintLayout.maxHeight=outerLayoutHeight-toolBarHeight
            }
        }
        val scrollView=binding.scrollView
        val missionList=binding.missionsList
        val sponsorsList=binding.sponsorsList
        missionList.isNestedScrollingEnabled=false
        sponsorsList.isNestedScrollingEnabled=false
        scrollView.viewTreeObserver.addOnScrollChangedListener(OnScrollChangedListener {
            if (!scrollView.canScrollVertically(1)) {
                //scroll view is at bottom
                missionList.isNestedScrollingEnabled = true
                sponsorsList.isNestedScrollingEnabled = true
            } else {
                //scroll view is not at bottom
                //missionList.isNestedScrollingEnabled=false
                //sponsorsList.isNestedScrollingEnabled=false
            }
        })

        val missionsButton=binding.missionsButton
        val sponsorsButton=binding.sponsorsButton
        missionsButton.setOnClickListener {
            missionsButton.setBackgroundResource(R.drawable.login_resend_active)
            sponsorsButton.setBackgroundResource(R.drawable.login_resend_inactive)
            missionList.visibility=View.VISIBLE
            sponsorsList.visibility=View.GONE
            binding.activeMissionLegendConstraintLayout.visibility=View.VISIBLE
            context?.let{missionsButton.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.primary_text
                )
            )}
            context?.let{sponsorsButton.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.secondary_text
                )
            )}
        }
        sponsorsButton.setOnClickListener {
            missionsButton.setBackgroundResource(R.drawable.login_resend_inactive)
            sponsorsButton.setBackgroundResource(R.drawable.login_resend_active)
            missionList.visibility=View.GONE
            sponsorsList.visibility=View.VISIBLE
            binding.activeMissionLegendConstraintLayout.visibility=View.GONE
            context?.let{missionsButton.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.secondary_text
                )
            )}
            context?.let{sponsorsButton.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.primary_text
                )
            )}
        }

        viewModel.navigateToSelectedMission.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                val nowMinusOneDay = Calendar.getInstance().timeInMillis - 24 * 60 * 60 * 1000
                if (it.deadline < nowMinusOneDay) {
                    //accomplished
                    findNavController().navigate(FeatsFragmentDirections.actionFeatsFragmentToAccomplishedMissionDetails(it))
                } else {
                    //active
                    findNavController().navigate(FeatsFragmentDirections.actionFeatsFragmentToDetailMission(it,false))
                }
                viewModel.toDetailMissionComplete()
            }
        })
        val missionsAdapter= AllMissionsAdapter(AllMissionsAdapter.OnClickListener {
            viewModel.toDetailMission(
                it
            )
        })
        viewModel.missions.observe(viewLifecycleOwner, Observer {
            it?.let {
                missionsAdapter.submitList(it)
            }
        })
        binding.missionsList.adapter=missionsAdapter
        viewModel.navigateToSelectedSponsorPage.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                findNavController().navigate(FeatsFragmentDirections.actionFeatsFragmentToSponsorDetailsFragment(it))
                viewModel.toSponsorPageComplete()
            }
        })
        val sponsorsAdapter= AllSponsorsAdapter(AllSponsorsAdapter.OnClickListener {
            viewModel.toSponsorPage(
                it.sponsorNumber
            )
        })
        viewModel.sponsors.observe(viewLifecycleOwner, Observer {
            it?.let {
                sponsorsAdapter.submitList(it)
            }
        })
        binding.sponsorsList.adapter=sponsorsAdapter
        return binding.root
    }
}
