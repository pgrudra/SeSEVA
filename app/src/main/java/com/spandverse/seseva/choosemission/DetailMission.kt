package com.spandverse.seseva.choosemission

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.databinding.FragmentDetailMissionBinding
import com.spandverse.seseva.home.DrawerLocker
import com.spandverse.seseva.ui.login.NoInternetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DetailMission : Fragment(), NoInternetDialogFragment.NoInternetDialogListener {
    private val cloudImagesReference = Firebase.storage
    private lateinit var binding: FragmentDetailMissionBinding
    private lateinit var viewModel: DetailMissionViewModel
    private lateinit var viewModelFactory: DetailMissionViewModelFactory
    private lateinit var appContext: Context
    private lateinit var sharedPref: SharedPreferences
    private var previousCMission:Int=0
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
        appContext = context?.applicationContext ?: return binding.root
        sharedPref = appContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val application = requireNotNull(this.activity).application
        val mission:DomainActiveMission = DetailMissionArgs.fromBundle(requireArguments()).selectedMission
        val showDifferentMissionButton=DetailMissionArgs.fromBundle(requireArguments()).showDifferentMissionButton
        viewModelFactory = DetailMissionViewModelFactory(mission, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailMissionViewModel::class.java)
        binding.selectedMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val contribution=mission.contribution
        /*val color= context?.let { ContextCompat.getColor(it,R.color.colorPrimary) }
        if(color!=null){
            if(contribution!=0){
                binding.textView22.setTextColor(color)
            }
            else{
                binding.textView2.setTextColor(color)
            }
        }*/
        if(contribution==0){
            binding.textView22.visibility=View.GONE
        }
        if(!showDifferentMissionButton){
            binding.chooseADifferentMission.visibility=View.GONE
        }
        val chosenMissionNumber=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)?:0
        previousCMission=chosenMissionNumber
        val cTM=binding.chooseThisMission
        if(chosenMissionNumber==mission.missionNumber){
            cTM.setBackgroundResource(R.drawable.disabled_button)
            cTM.isEnabled=false
            context?.let { cTM.setTextColor(ContextCompat.getColor(it, R.color.disabled_text)) }
        }
        else{
            cTM.setBackgroundResource(R.drawable.login_resend_active)
            cTM.isEnabled=true
            context?.let { cTM.setTextColor(ContextCompat.getColor(it, R.color.nav_color)) }
        }
        viewModel.makeTriggerText(contribution)
        binding.category.text = mission.category
        binding.missionName.text=mission.missionName
        val misDesLength = mission.missionDescription.length
        if (misDesLength < 226) {
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
                val layOutParams=binding.chooseADifferentMission.layoutParams as ViewGroup.MarginLayoutParams
                layOutParams.bottomMargin=40
                binding.chooseADifferentMission.layoutParams=layOutParams
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
                } else if (misDesLength > 225) {
                    binding.missionDescription.text = getString(
                        R.string.dots, mission.missionDescription.substring(
                            0,
                            225
                        )
                    )
                    binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                }

            })

        binding.activeContributors.text = mission.contributors.toString()
        binding.money.text = mission.totalMoneyRaised.toString()
        binding.sponsorName.text = mission.sponsorName
        binding.goal.text=mission.goal
        val reference =
            cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mission.sponsorNumber}Logo.png")
        Glide.with(this)
            .load(reference)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_sponsor)
                    .error(R.drawable.ic_sponsor)
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
                val notificationManager: NotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(2)
                findNavController().navigate(DetailMissionDirections.actionDetailMissionToRulesFragment2())
                viewModel.toRulesFragmentComplete()
            }
        })

        viewModel.somethingWentWrong.observe(viewLifecycleOwner, Observer<Boolean> { wentWrong ->
            if (wentWrong) {
                Snackbar.make(binding.root,
                    "Something went wrong, please try again later",
                    Snackbar.LENGTH_SHORT
                ).show()
                 viewModel.hideSnackbar()
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
        viewModel.progressVisibility.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible) {
                binding.progressBar1.visibility=View.VISIBLE
                binding.skrim.visibility=View.VISIBLE
            }
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.skrim.visibility=View.GONE
        binding.progressBar1.visibility=View.GONE
        if(sharedPref.getBoolean((R.string.from_rules).toString(),false)) {
            with(sharedPref.edit()) {
                this?.putInt(
                    (R.string.chosen_mission_number).toString(),
                    previousCMission
                )
                this?.apply()
            }
            binding.chooseThisMission.setBackgroundResource(R.drawable.login_resend_active)
            binding.chooseThisMission.isEnabled = true
            context?.let {
                binding.chooseThisMission.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.nav_color
                    )
                )
            }
        }
    }
    override fun removeRedBackground(dialog: DialogFragment) {
    }

    private fun showNoInternetConnectionDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = NoInternetDialogFragment()
        val fragmentManager = childFragmentManager
        dialog.show(fragmentManager, "No Internet Connection")
    }

}