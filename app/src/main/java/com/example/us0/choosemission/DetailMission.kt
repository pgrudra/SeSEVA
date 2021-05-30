package com.example.us0.choosemission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.example.us0.home.DrawerLocker
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
        val showDifferentMissionButton=DetailMissionArgs.fromBundle(requireArguments()).showDifferentMissionButton
        viewModelFactory = DetailMissionViewModelFactory(mission, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailMissionViewModel::class.java)
        binding.selectedMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val contribution=mission.contribution
            val color= context?.let { ContextCompat.getColor(it,R.color.colorPrimary) }
            if(color!=null){
                if(contribution!=0){
                    binding.textView22.setTextColor(color)
                }
                else{
                    binding.textView2.setTextColor(color)
                }
            }
        if(contribution==0){
            binding.textView22.visibility=View.GONE
        }
        if(!showDifferentMissionButton){
            binding.chooseADifferentMission.visibility=View.GONE
        }
        viewModel.makeTriggerText(contribution)
        binding.category.text = mission.category
        binding.missionName.text=mission.missionName
        val misDesLength = mission.missionDescription.length
        if (misDesLength < 151) {
            binding.expandOrContract.visibility = View.GONE
            binding.missionDescription.text = mission.missionDescription
        }
        viewModel.drawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            val drawerLocker=(activity as DrawerLocker?)
            if (visible && sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0)?:0 !=0) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
                binding.toolbar.setNavigationOnClickListener {
                    activity?.onBackPressed()
                }
                drawerLocker!!.setDrawerEnabled(true)
                drawerLocker.displayBottomNavigation(true)
            } else {
                drawerLocker!!.setDrawerEnabled(false)
                drawerLocker.displayBottomNavigation(false)
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
        binding.goal.text=mission.goal
        val reference =
            cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mission.sponsorNumber}Logo.png")
        Glide.with(this)
            .load(reference)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            )
            .into(binding.sponsorLogo)
        viewModel.goToSponsorScreen.observe(viewLifecycleOwner, Observer<Boolean> { go ->
            if (go) {
                findNavController().navigate(DetailMissionDirections.actionDetailMissionToSponsorDetailsFragment(mission.sponsorNumber))
                viewModel.toSponsorScreenComplete()
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
                    Log.i("DM5","jgpu")
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