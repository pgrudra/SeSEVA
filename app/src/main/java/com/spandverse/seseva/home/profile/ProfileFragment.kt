package com.spandverse.seseva.home.profile

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentProfileBinding
import com.spandverse.seseva.home.DrawerLocker
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class ProfileFragment : Fragment() {
    private val cloudImagesReference = Firebase.storage
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        val application = requireNotNull(this.activity).application
        val dataBaseDAO = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory= ProfileViewModelFactory(dataBaseDAO, application)
        viewModel= ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        binding.profileViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLoker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
        binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v) }
        binding.toSponsor.setOnClickListener {
                viewModel.sponsorNumber.value?.let{
                    findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSponsorDetailsFragment(it))
                }
        }
        drawerLoker!!.setDrawerEnabled(true)
        drawerLoker.displayBottomNavigation(true)
        viewModel.currentMissionNumber.observe(viewLifecycleOwner, Observer {
            if(null!=it){
                val reference1 = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${it}Image.jpg")
                Glide.with(this)
                    .load(reference1)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_imageplaceholder)
                            .error(R.drawable.ic_imageplaceholder)
                    )
                    .into(binding.missionImage)
                val reference2 =
                    cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${viewModel.sponsorNumber.value}Logo.png")
                Glide.with(this)
                    .load(reference2)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_sponsor)
                            .error(R.drawable.ic_sponsor)
                            .fallback(R.drawable.ic_sponsor)
                    )
                    .into(binding.sponsorLogo)
                binding.previousMissionsButton.isEnabled=true
            }
        })
        viewModel.makeExpandOrContractIconVisible.observe(viewLifecycleOwner, Observer { show->
            if(show){
                binding.expandOrContract.visibility=View.VISIBLE
            }
        })
        viewModel.showDetailMissionDescription.observe(viewLifecycleOwner, Observer { expand->
            if(expand){
                binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            }
            else{
                binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
            }
        })
        viewModel.goToChooseMission.observe(viewLifecycleOwner, Observer { go->
            if(go){
                findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToChooseMissionFragment())
                viewModel.toChooseMissionComplete()
            }
        })
        viewModel.goToYourPreviousMissions.observe(viewLifecycleOwner, Observer { go->
            if(go){
                findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToYourPreviousMissionsFragment(viewModel.getCurrentMission()))
                viewModel.toYourPreviousMissionsComplete()
            }
        })
        return binding.root
    }
}