package com.spandverse.seseva.choosemission

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.spandverse.seseva.R
import com.spandverse.seseva.adapters.ActiveMissionsAdapter
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentChooseMissionBinding
import com.spandverse.seseva.home.DrawerLocker

class ChooseMission : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var binding: FragmentChooseMissionBinding
    private lateinit var viewModel: ChooseMissionViewModel
    private lateinit var viewModelFactory: ChooseMissionViewModelFactory
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var showBotSheet=true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_choose_mission,
            container,
            false
        )
        val appContext = context?.applicationContext ?: return binding.root
        val sharedPref =
            appContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao

        viewModelFactory = ChooseMissionViewModelFactory(datasource, application)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ChooseMissionViewModel::class.java)
        binding.chooseMissionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.navigateToSelectedMission.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                findNavController().navigate(
                    ChooseMissionDirections.actionChooseMissionFragmentToDetailMission(
                        it
                    )
                )
                viewModel.toDetailMissionComplete()
            }
        })
        val chosenMissionNumber = sharedPref?.getInt((R.string.chosen_mission_number).toString(), 0) ?: 0
        val adapter = ActiveMissionsAdapter(ActiveMissionsAdapter.OnClickListener {
            viewModel.toDetailMission(it)
        })
        if (sharedPref?.getBoolean((R.string.mison_i_sown).toString(), false) != true) {
            viewModel.startCountDown()
            with (sharedPref.edit()) {
                this?.putBoolean((com.spandverse.seseva.R.string.mison_i_sown).toString(), true)
                this?.apply()
            }
        }
        viewModel.activeMissions.observe(viewLifecycleOwner, Observer {
            val list = it.filter { mission -> mission.missionNumber != chosenMissionNumber }
            if (list.isNotEmpty()) {
                binding.progressBar1.visibility = View.GONE
                adapter.submitList(list)
            }
        })
        val drawerLocker = (activity as DrawerLocker?)
        viewModel.drawer.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible && chosenMissionNumber != 0) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
                binding.toolbar.setNavigationOnClickListener {
                    activity?.onBackPressed()
                }
                drawerLocker!!.setDrawerEnabled(true)
                drawerLocker.displayBottomNavigation(true)
                val r = activity?.resources
                val px =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F, r?.displayMetrics)
                binding.toolbar.titleMarginStart = 0
            } else {
                (activity as DrawerLocker?)!!.setDrawerEnabled(false)
                drawerLocker!!.displayBottomNavigation(false)
            }
        })
        val infoSheet = binding.infoFragment.root
        bottomSheetBehavior = BottomSheetBehavior.from(infoSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.skrim1.visibility = View.GONE
                    //mission item no sensing
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        viewModel.expandBottomSheet.observe(viewLifecycleOwner, Observer<Boolean> { expand ->
            if (expand && showBotSheet) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.skrim1.visibility = View.VISIBLE
            }
        })
        binding.skrim1.setOnClickListener {
            binding.skrim1.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        /*val closeButton =
            activity?.findViewById<AppCompatImageButton>(R.id.choose_mission_close_info)
        closeButton?.setOnClickListener {
            binding.skrim1.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }*/
        binding.toolbar.inflateMenu(R.menu.choose_mission_info)
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.activeMissionsList.adapter = adapter
        return binding.root
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.skrim1.visibility = View.VISIBLE
        showBotSheet=false
        return true
    }
    /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.i("CMVMw","4")
        inflater.inflate(R.menu.choose_mission_info,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.choose_mission_info_button->{
                Log.i("CMVMw","5")
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.skrim1.visibility = View.VISIBLE
                true
            }
            else-> false
        }
    }
*/
}