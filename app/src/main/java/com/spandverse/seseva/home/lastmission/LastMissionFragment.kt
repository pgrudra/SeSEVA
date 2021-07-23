package com.spandverse.seseva.home.lastmission

import android.os.Bundle
import android.text.SpannableString
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentLastMissionBinding
import com.spandverse.seseva.home.DrawerLocker


class LastMissionFragment : Fragment() {

    private lateinit var binding: FragmentLastMissionBinding
    private lateinit var viewModel: LastMissionViewModel
    private lateinit var viewModelFactory: LastMissionViewModelFactory
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { FirebaseApp.initializeApp(*//*context=*//* it) }
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance())
    }*/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_mission, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = LastMissionViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LastMissionViewModel::class.java)
        binding.lastMissionViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        binding.progressBar1.visibility=View.VISIBLE
        binding.skrim.visibility=View.VISIBLE
        (activity as DrawerLocker?)!!.displayBottomNavigation(false)
viewModel.goToHome.observe(viewLifecycleOwner, Observer<Boolean> { goto->
    if(goto){
        findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToPassageFragment())
        viewModel.goToHomeComplete()
    }
})
        viewModel.showProgress.observe(viewLifecycleOwner, Observer<Boolean> { show->
            if(show){
                binding.skrim.visibility=View.VISIBLE
                binding.progressBar1.visibility=View.VISIBLE
            }
        })
        viewModel.timeLeft.observe(viewLifecycleOwner, Observer<SpannableString> {
                binding.trigger.visibility=View.VISIBLE

        })
        viewModel.goToChooseMission.observe(viewLifecycleOwner, Observer<Boolean> { goto->
            if(goto){
                binding.skrim.visibility=View.VISIBLE
                binding.progressBar1.visibility=View.VISIBLE
                findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToChooseMissionFragment())
                viewModel.chooseOtherMissionComplete()
            }
        })
        viewModel.enableChooseThisMissionButton.observe(viewLifecycleOwner, Observer<Boolean> { enable->
            if(enable){
                binding.chooseThisMission.isEnabled=true
                binding.progressBar1.visibility=View.GONE
                binding.skrim.visibility=View.GONE
            }
        })
        viewModel.enableDifferentMissionButton.observe(viewLifecycleOwner, Observer<Boolean> { enable->
            binding.chooseADifferentMission.isEnabled = enable
            binding.skrimDark.visibility=View.GONE
        })
        /*viewModel.goToRules.observe(viewLifecycleOwner, Observer<Boolean> { goto->
            if(goto){
                binding.skrim.visibility=View.VISIBLE
                binding.progressBar1.visibility=View.VISIBLE

                findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToRulesFragment2())
                viewModel.goToRulesComplete()
            }
        })*/
        viewModel.goToLastMissionCompleted.observe(viewLifecycleOwner, Observer<Int> { number->
                findNavController().navigate(LastMissionFragmentDirections.actionLastMissionFragmentToLastMissionCompletedFragment(number))
        })


       return binding.root
    }

}