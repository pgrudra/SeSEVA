package com.example.us0.choosemission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.FragmentDetailMissionBinding
import com.example.us0.installedapps.DrawerLocker
import com.example.us0.ui.login.NoInternetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DetailMission : Fragment(), NoInternetDialogFragment.NoInternetDialogListener {
    private val cloudImagesReference = Firebase.storage
    private lateinit var binding: FragmentDetailMissionBinding
    private lateinit var viewModel: DetailMissionViewModel
    private lateinit var viewModelFactory: DetailMissionViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail_mission,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val sharedPref = activity?.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val mission:DomainActiveMission = DetailMissionArgs.fromBundle(requireArguments()).selectedMission
        val showImage=DetailMissionArgs.fromBundle(requireArguments()).showImage
        viewModelFactory = DetailMissionViewModelFactory(mission, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailMissionViewModel::class.java)
        if(showImage){
            //binding.Img.visibility=View.VISIBLE
            binding.chooseThisMission.visibility=View.GONE
        }
        binding.selectedMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.title = mission.missionName
        binding.category.text = mission.category
        val misDesLength = mission.missionDescription.length
        if (misDesLength < 151) {
            Log.i("jji", "klop")
            binding.expandOrContract.visibility = View.GONE
            binding.missionDescription.text = mission.missionDescription
        }
        viewModel.drawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
                binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v) }
                (activity as DrawerLocker?)!!.setDrawerEnabled(true)
                Log.i("RF","$visible")
            } else {
                (activity as DrawerLocker?)!!.setDrawerEnabled(false)
            }
        })
        viewModel.showDetailMissionDescription.observe(
            viewLifecycleOwner,
            Observer<Boolean> { show ->
                if (show) {
                    binding.missionDescription.text = mission.missionDescription
                    binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
                } else if (misDesLength > 150) {
                    binding.missionDescription.text = getString(
                        R.string.dots, mission.missionDescription.substring(
                            0,
                            150
                        )
                    )
                    binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                }

            })
        binding.activeContributors.text = mission.usersActive.toString()
        binding.money.text = mission.totalMoneyRaised.toString()
        binding.sponsorName.text = mission.sponsorName
        val reference =
            cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/mission${mission.missionNumber}SponsorLogo.png")
        Glide.with(this)
            .load(reference)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            )
            .into(binding.sponsorLogo)
        binding.sponsorDescription.text = mission.sponsorDescription
        viewModel.knowMore.observe(viewLifecycleOwner, Observer<Boolean> { knowMore ->
            if (knowMore) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(mission.sponsorSite)
                startActivity(i)
                viewModel.toSponsorWebsiteComplete()
            }

        })
        viewModel.toChooseMission.observe(viewLifecycleOwner, Observer<Boolean> { toChooseMission ->
            if (toChooseMission) {
                findNavController().navigate(DetailMissionDirections.actionDetailMissionToChooseMissionFragment())
                viewModel.toChooseMissionComplete()
            }

        })
        viewModel.toRulesFragment.observe(viewLifecycleOwner, Observer<Boolean> { toRulesFragment ->
            if (toRulesFragment) {
                findNavController().navigate(DetailMissionDirections.actionDetailMissionToRulesFragment2())
                viewModel.toRulesFragmentComplete()
            }
        })
        viewModel.toHomeFragment.observe(viewLifecycleOwner, Observer<Boolean> { goto ->
                if (goto) {
                    findNavController().navigate(DetailMissionDirections.actionDetailMissionToHomeFragment())
                    viewModel.thisMissionChosenComplete()
                }
            })
        viewModel.noInternet.observe(viewLifecycleOwner, Observer<Boolean> { noInternet ->
            if (noInternet) {
                showNoInternetConnectionDialog()
            }
        })
        return binding.root
    }

    override fun removeRedBackground(dialog: DialogFragment) {
    }

    private fun showNoInternetConnectionDialog() {
        // Create an instance of the dialog fragment and show it
        Log.i("fg", "sdf")
        val dialog = NoInternetDialogFragment()
        val fragmentManager = childFragmentManager
        dialog.show(fragmentManager, "No Internet Connection")
    }

}