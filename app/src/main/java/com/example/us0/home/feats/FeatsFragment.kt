package com.example.us0.home.feats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.adapters.AllMissionsAdapter
import com.example.us0.adapters.AllSponsorsAdapter
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentFeatsBinding
import com.example.us0.home.DrawerLocker
import java.util.*


class FeatsFragment : Fragment() {
    private lateinit var binding: FragmentFeatsBinding
    private lateinit var viewModel: FeatsViewModel
    private lateinit var viewModelFactory: FeatsViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feats, container, false)
        val application = requireNotNull(this.activity).application
        val dataBaseDAO = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory= FeatsViewModelFactory(dataBaseDAO, application)
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
            binding.listDescriptionText.text="List of missions hosted on SeSeva"
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
            binding.listDescriptionText.text="List of companies that have fulfilled their pledges towards missions hosted on SeSeva"
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
                } else {
                    //active
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
                //goToSponsorPage
                viewModel.toSponsorPageComplete()
            }
        })
        val sponsorsAdapter= AllSponsorsAdapter(AllSponsorsAdapter.OnClickListener {
            viewModel.toSponsorPage(
                it
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
