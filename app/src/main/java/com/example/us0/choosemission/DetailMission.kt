package com.example.us0.choosemission

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.databinding.FragmentDetailMissionBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DetailMission : Fragment() {
    private val cloudImagesReference= Firebase.storage
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
        val mission=DetailMissionArgs.fromBundle(requireArguments()).selectedMission
        viewModelFactory = DetailMissionViewModelFactory(mission, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailMissionViewModel::class.java)
        binding.selectedMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.title=mission.missionName
        binding.category.text=mission.category
        val misDesLength=mission.missionDescription.length
        if(misDesLength<268){
            Log.i("jji", "klop")
            binding.expandOrContract.visibility=View.GONE
            binding.missionDescription.text=mission.missionDescription
        }
        viewModel.showDetailMissionDescription.observe(
            viewLifecycleOwner,
            Observer<Boolean> { show ->
                if (show) {
                    binding.missionDescription.text = mission.missionDescription
                    binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
                } else if (misDesLength > 267) {
                    binding.missionDescription.text = getString(
                        R.string.dots, mission.missionDescription.substring(
                            0,
                            267
                        )
                    )
                    binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                }

            })
        binding.activeContributors.text=mission.usersActive.toString()
        binding.money.text=mission.totalMoneyRaised.toString()
        binding.sponsorName.text=mission.sponsorName
        val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/mission${mission.missionNumber}SponsorLogo.png")
        Glide.with(this)
            .load(reference)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            )
            .into(binding.sponsorLogo)
        binding.sponsorDescription.text=mission.sponsorDescription
        viewModel.knowMore.observe(viewLifecycleOwner, Observer<Boolean> { knowMore ->
            if (knowMore) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(mission.sponsorSite)
                startActivity(i)
                viewModel.toSponsorWebsiteComplete()
            }

        })
        viewModel.toChooseMission.observe(viewLifecycleOwner,Observer<Boolean>{toChooseMission->
            if(toChooseMission){
                findNavController().navigate(DetailMissionDirections.actionDetailMissionToChooseMissionFragment())
                viewModel.toChooseMissionComplete()
            }

        })
        return binding.root
    }

}