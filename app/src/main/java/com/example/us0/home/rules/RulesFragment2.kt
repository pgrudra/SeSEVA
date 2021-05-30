package com.example.us0.home.rules

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.us0.R
import com.example.us0.adapters.InstalledAppAdapter
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentRules2Binding
import com.example.us0.foregroundnnotifications.InfoPopUpWindow
import com.example.us0.home.DrawerLocker
import com.example.us0.ui.login.NoInternetDialogFragment


class RulesFragment2 : Fragment(),NoInternetDialogFragment.NoInternetDialogListener,View.OnClickListener {
    private lateinit var binding:FragmentRules2Binding
    private lateinit var viewModel: Rules2ViewModel
    private lateinit var viewModelFactory: Rules2ViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rules2, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).AppDatabaseDao
        val pm = requireNotNull(activity?.packageManager)
        val elevationSelectedCard=5* context?.resources?.displayMetrics?.density!!
        val elevationUnSelectedCard=3* context?.resources?.displayMetrics?.density!!
       viewModelFactory = Rules2ViewModelFactory(datasource, application, pm)
        viewModel = ViewModelProvider(this, viewModelFactory).get(Rules2ViewModel::class.java)
        binding.rules2ViewModel=viewModel
        viewModel.social.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.socialAppsList.visibility = View.VISIBLE
                binding.social.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.iSocial.visibility = View.VISIBLE
                binding.social.elevation = elevationSelectedCard
                binding.socialExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.socialAppsList.visibility = View.GONE
                binding.social.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.iSocial.visibility = View.GONE
                binding.social.elevation = elevationUnSelectedCard
                binding.socialExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.communication.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.communicationAppsList.visibility = View.VISIBLE
                binding.iCommunication.visibility = View.VISIBLE
                binding.communication.elevation = elevationSelectedCard
                binding.communication.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.communicationExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.communicationAppsList.visibility = View.GONE
                binding.iCommunication.visibility = View.GONE
                binding.communication.elevation = elevationUnSelectedCard
                binding.communication.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.communicationExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.games.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.gamesAppsList.visibility = View.VISIBLE
                binding.iGames.visibility = View.VISIBLE
                binding.games.elevation = elevationSelectedCard
                binding.games.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.gamesExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.gamesAppsList.visibility = View.GONE
                binding.iGames.visibility = View.GONE
                binding.games.elevation = elevationUnSelectedCard
                binding.games.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.gamesExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.video.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.videoAppsList.visibility = View.VISIBLE
                binding.iVideo.visibility = View.VISIBLE
                binding.video.elevation = elevationSelectedCard
                binding.video.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.videoExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.videoAppsList.visibility = View.GONE
                binding.iVideo.visibility = View.GONE
                binding.video.elevation = elevationUnSelectedCard
                binding.video.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.videoExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.msnbs.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.msnbsAppsList.visibility = View.VISIBLE
                binding.iMsnbs.visibility = View.VISIBLE
                binding.msnbs.elevation = elevationSelectedCard
                binding.msnbs.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.msnbsExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.msnbsAppsList.visibility = View.GONE
                binding.iMsnbs.visibility = View.GONE
                binding.msnbs.elevation = elevationUnSelectedCard
                binding.msnbs.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.msnbsExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.whitelisted.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.whitelistedAppsList.visibility = View.VISIBLE
                binding.iWhitelisted.visibility = View.VISIBLE
                binding.whitelisted.elevation = elevationSelectedCard
                binding.whitelisted.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.whitelistedExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.whitelistedAppsList.visibility = View.GONE
                binding.iWhitelisted.visibility = View.GONE
                binding.whitelisted.elevation = elevationUnSelectedCard
                binding.whitelisted.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.whitelistedExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.others.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.othersAppsList.visibility = View.VISIBLE
                binding.iOthers.visibility = View.VISIBLE
                binding.others.elevation = elevationSelectedCard
                binding.others.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.othersExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.othersAppsList.visibility = View.GONE
                binding.iOthers.visibility = View.GONE
                binding.others.elevation = elevationUnSelectedCard
                binding.others.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.othersExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.entertainment.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand) {
                binding.entertainmentAppsList.visibility = View.VISIBLE
                binding.iEntertainment.visibility = View.VISIBLE
                binding.entertainment.elevation = elevationSelectedCard
                binding.entertainment.setBackgroundResource(R.drawable.all_corner_rounded_highlighted_4dp)
                binding.entertainmentExpandOrContract.setImageResource(R.drawable.ic_collapse_vector)
            } else {
                binding.entertainmentAppsList.visibility = View.GONE
                binding.iEntertainment.visibility = View.GONE
                binding.entertainment.elevation = elevationUnSelectedCard
                binding.entertainment.setBackgroundResource(R.drawable.all_corner_rounded_4dp)
                binding.entertainmentExpandOrContract.setImageResource(R.drawable.ic_expand_vector)

            }
        })
        viewModel.toHomeFragment.observe(viewLifecycleOwner, Observer<Boolean> { goToHome ->
            if (goToHome) {
                findNavController().navigate(RulesFragment2Directions.actionRulesFragment2ToHomeFragment())
                viewModel.goToHomeComplete()
            }
        })
        viewModel.daily.observe(viewLifecycleOwner, Observer<Boolean> { daily ->
            if (daily) {
                binding.dailyRulesButton.setBackgroundResource(R.drawable.all_corner_rounded_16dp_primary)
                binding.weeklyRulesButton.setBackgroundResource(R.drawable.all_corner_rounded_16dp_disabled)
                context?.let{binding.dailyRulesButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.weeklyRulesButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
                binding.dailyLinearLayout.visibility = View.VISIBLE
                binding.weeklyLinearLayout.visibility = View.GONE
            }
        })
        viewModel.weekly.observe(viewLifecycleOwner, Observer<Boolean> { weekly ->
            if (weekly) {
                binding.dailyRulesButton.setBackgroundResource(R.drawable.all_corner_rounded_16dp_disabled)
                binding.weeklyRulesButton.setBackgroundResource(R.drawable.all_corner_rounded_16dp_primary)
                context?.let{binding.weeklyRulesButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.dailyRulesButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
                binding.dailyLinearLayout.visibility = View.GONE
                binding.weeklyLinearLayout.visibility = View.VISIBLE
            }
        })
        viewModel.scrimVisible.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (!visible) {
                binding.introScrim.visibility = View.GONE
                binding.toHome.isEnabled = true
                //binding.toHome.visibility=View.GONE
            }
        })
        viewModel.iUnderstandRulesVisible.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (!visible) {
                binding.toHome.visibility=View.GONE
            }
            if(visible){
                binding.toHome.visibility=View.VISIBLE
            }
        })
        //If scrim becomes invisible and toolbar is present and iUnder... is invisible, then show bottom navigation.
        viewModel.toolBarNDrawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            val drawerLoker=(activity as DrawerLocker?)
            if (visible) {
                binding.toolbar.visibility = View.VISIBLE
                binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
                binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v) }
                drawerLoker!!.setDrawerEnabled(true)
                drawerLoker.displayBottomNavigation(true)
                val params=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                val r=activity?.resources
                val px16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16F,r?.displayMetrics).toInt()
                params.setMargins(0,px16,0,0)
                binding.textView5.layoutParams = params
            } else {

                drawerLoker!!.setDrawerEnabled(false)
                drawerLoker.displayBottomNavigation(false)
                binding.toolbar.visibility = View.GONE
                binding.toHome.visibility=View.VISIBLE
                Log.i("RF","$visible")
            }
        })
        Log.i("fg", "sdf")
        viewModel.noInternet.observe(viewLifecycleOwner, Observer<Boolean> { noInternet ->
            if (noInternet) {
                showNoInternetConnectionDialog()
                viewModel.makeNoInternetFalse()
            }
        })

        val socialAdapter= InstalledAppAdapter()
        viewModel.socialApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                socialAdapter.submitList(
                    it
                )
            }
        })
        binding.socialAppsList.adapter=socialAdapter
        val communicationAdapter= InstalledAppAdapter()
        viewModel.communicationApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                communicationAdapter.submitList(
                    it
                )
            }
        })
        binding.communicationAppsList.adapter=communicationAdapter
        val gamesAdapter= InstalledAppAdapter()
        viewModel.gamesApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                gamesAdapter.submitList(
                    it
                )
            }
        })
        binding.gamesAppsList.adapter=gamesAdapter
        val videoAdapter= InstalledAppAdapter()
        viewModel.videoApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                videoAdapter.submitList(
                    it
                )
            }
        })
        binding.videoAppsList.adapter=videoAdapter
        val msnbsAdapter=InstalledAppAdapter()
        viewModel.msnbsApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                msnbsAdapter.submitList(
                    it
                )
            }
        })
        binding.msnbsAppsList.adapter=msnbsAdapter
        val whitelistedAdapter=InstalledAppAdapter()
        viewModel.whitelistedApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                whitelistedAdapter.submitList(
                    it
                )
            }
        })
        binding.whitelistedAppsList.adapter=whitelistedAdapter
        val othersAdapter=InstalledAppAdapter()
        viewModel.otherApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                othersAdapter.submitList(
                    it
                )
            }
        })
        binding.othersAppsList.adapter=othersAdapter
        val entertainmentAdapter= InstalledAppAdapter()
        viewModel.entertainmentApps.observe(viewLifecycleOwner, Observer {
            it?.let {
                entertainmentAdapter.submitList(
                    it
                )
            }
        })
        binding.entertainmentAppsList.adapter=entertainmentAdapter
        binding.lifecycleOwner = this
        val socialManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val communicationManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val gamesManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val videoManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val msnbsManager=GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val whitelistedManager=GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val otherManager=GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        val entertainmentManager = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)

        binding.socialAppsList.layoutManager=socialManager
        binding.communicationAppsList.layoutManager=communicationManager
        binding.gamesAppsList.layoutManager=gamesManager
        binding.videoAppsList.layoutManager=videoManager
        binding.entertainmentAppsList.layoutManager=entertainmentManager
        binding.msnbsAppsList.layoutManager=msnbsManager
        binding.whitelistedAppsList.layoutManager=whitelistedManager
        binding.othersAppsList.layoutManager=otherManager

        binding.iSocial.setOnClickListener(this)
        binding.iCommunication.setOnClickListener(this)
        binding.iGames.setOnClickListener(this)
        binding.iVideo.setOnClickListener(this)
        binding.iEntertainment.setOnClickListener(this)
        binding.iMsnbs.setOnClickListener(this)
        binding.iWhitelisted.setOnClickListener(this)
        binding.iOthers.setOnClickListener(this)
        //viewModel.initiate()
        return binding.root
    }
    private fun showNoInternetConnectionDialog() {
        // Create an instance of the dialog fragment and show it
        Log.i("fg", "sdf")
        val dialog = NoInternetDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager, "No Internet Connection")
    }

    override fun removeRedBackground(dialog: DialogFragment) {

    }

    override fun onClick(p0: View?) {
        val popUpClass = InfoPopUpWindow()
        p0?.let {
            popUpClass.showPopupWindow(it) }
    }

}
