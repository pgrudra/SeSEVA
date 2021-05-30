package com.example.us0.home.closedmissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.choosemission.DetailMissionArgs
import com.example.us0.data.AllDatabase
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.FragmentAccomplishedMissionsDetailsBinding
import com.example.us0.home.DrawerLocker
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AccomplishedMissionDetails : Fragment() {
    private val cloudImagesReference = Firebase.storage
    private lateinit var binding: FragmentAccomplishedMissionsDetailsBinding
    private lateinit var viewModel: AMDViewModel
    private lateinit var viewModelFactory: AMDViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_accomplished_missions_details, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        val mission: DomainActiveMission = AccomplishedMissionDetailsArgs.fromBundle(requireArguments()).selectedMission
        viewModelFactory = AMDViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AMDViewModel::class.java)
        //check Internet, then check name, then check mission
        binding.amdViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(true)
        val misDesLength = mission.missionDescription.length
        if (misDesLength < 151) {
            binding.expandOrContract.visibility = View.GONE
            binding.accomplishedMissionDescription.text = mission.missionDescription
        }
        viewModel.showDetailMissionDescription.observe(
            viewLifecycleOwner,
            Observer<Boolean> { show ->
                if (show) {
                    binding.accomplishedMissionDescription.text = mission.missionDescription
                    binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
                } else if (misDesLength > 150) {
                    binding.accomplishedMissionDescription.text = getString(
                        R.string.dots, mission.missionDescription.substring(
                            0,
                            150
                        )
                    )
                    binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                }

            })
        binding.missionName.text=getString(R.string.mission_is_accomplished,mission.missionName)
        binding.accomplishedMissionDescription.text=mission.missionDescription
        binding.amountRaised.text=getString(R.string.rs,mission.totalMoneyRaised)
        binding.contributors.text=mission.usersActive.toString()
        binding.accomplishedOn.text=mission.deadlineAsDateShort
        binding.contribution.text=getString(R.string.rs,mission.contribution)
        binding.sponsorName.text=mission.sponsorName
        binding.toSponsor.setOnClickListener {
            findNavController().navigate(AccomplishedMissionDetailsDirections.actionAccomplishedMissionDetailsToSponsorDetailsFragment(mission.sponsorNumber))
        }
        val reference = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mission.sponsorNumber}Logo.png")
        Glide.with(this)
            .load(reference)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            )
            .into(binding.sponsorLogo)
        if(mission.reportAvailable){
            binding.downloadReportButton.text="DOWNLOAD REPORT"
            binding.downloadReportButton.setBackgroundResource(R.drawable.login_change_email)
        }
        return binding.root
    }


}