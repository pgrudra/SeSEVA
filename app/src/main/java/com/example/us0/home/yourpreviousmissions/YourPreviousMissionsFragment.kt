package com.example.us0.home.yourpreviousmissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.adapters.PlainMissionsCardAdapter
import com.example.us0.data.AllDatabase
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.FragmentYourPreviousMissionsBinding
import com.example.us0.home.DrawerLocker
import java.util.*


class YourPreviousMissionsFragment : Fragment() {
    private lateinit var binding: FragmentYourPreviousMissionsBinding
    private lateinit var viewModel: YPMViewModel
    private lateinit var viewModelFactory: YPMViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_your_previous_missions, container, false)
        val application = requireNotNull(this.activity).application
        val currentMission: DomainActiveMission = YourPreviousMissionsFragmentArgs.fromBundle(requireArguments()).currentMission
        val dataBaseDAO = AllDatabase.getInstance(application).MissionsDatabaseDao
        val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
        viewModelFactory= YPMViewModelFactory(dataBaseDAO)
        viewModel= ViewModelProvider(this, viewModelFactory).get(YPMViewModel::class.java)
        binding.ypmViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(YourPreviousMissionsFragmentDirections.actionYourPreviousMissionsFragmentToProfileFragment())
        }
        viewModel.accomplishedMissionsSelected.observe(viewLifecycleOwner, Observer { accomplishedSelected->
            if(accomplishedSelected){
                binding.accomplishedMissionsButton.setBackgroundResource(R.drawable.login_resend_active)
                binding.activeMissionsButton.setBackgroundResource(R.drawable.login_resend_inactive)
                context?.let{binding.accomplishedMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.activeMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
            }
        })
        viewModel.activeMissionsSelected.observe(viewLifecycleOwner, Observer { activeSelected->
            if(activeSelected){
                binding.activeMissionsButton.setBackgroundResource(R.drawable.login_resend_active)
                binding.accomplishedMissionsButton.setBackgroundResource(R.drawable.login_resend_inactive)
                context?.let{binding.activeMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.accomplishedMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
            }
        })
        val adapter=PlainMissionsCardAdapter(PlainMissionsCardAdapter.OnClickListener{
            if(it.deadline>nowMinusOneDay){
                findNavController().navigate(YourPreviousMissionsFragmentDirections.actionYourPreviousMissionsFragmentToDetailMission(it,true))
            }
            else{
                //download report
            }})
        viewModel.missionsToDisplay.observe(viewLifecycleOwner, Observer {
            val list=it.filter { mission -> mission!=currentMission }
            if(list.isNotEmpty()){
                adapter.submitList(it)
                binding.noCardsText.visibility=View.GONE
            }
            else{
                binding.noCardsText.visibility=View.VISIBLE
            }
        })
        binding.missionsList.adapter=adapter
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(true)

        return binding.root
    }


}