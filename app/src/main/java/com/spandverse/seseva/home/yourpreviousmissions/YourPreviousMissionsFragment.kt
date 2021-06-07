package com.spandverse.seseva.home.yourpreviousmissions

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
import com.spandverse.seseva.R
import com.spandverse.seseva.adapters.AccomplishedMissionsAdapter
import com.spandverse.seseva.adapters.ActiveMissions2Adapter
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.databinding.FragmentYourPreviousMissionsBinding
import com.spandverse.seseva.home.DrawerLocker
import java.util.*


class YourPreviousMissionsFragment : Fragment() {
    private lateinit var binding: FragmentYourPreviousMissionsBinding
    private lateinit var viewModel: YPMViewModel
    private lateinit var viewModelFactory: YPMViewModelFactory
    private var zeroAvailableCards=true
    private var zeroAccomplishedCards=true
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
            activity?.onBackPressed()
        }
        viewModel.accomplishedMissionsSelected.observe(viewLifecycleOwner, Observer { accomplishedSelected->
            if(accomplishedSelected){
                binding.accomplishedMissionsButton.setBackgroundResource(R.drawable.login_resend_active)
                binding.activeMissionsButton.setBackgroundResource(R.drawable.login_resend_inactive)
                binding.activeMissionsList.visibility=View.GONE
                if(zeroAccomplishedCards){
                    binding.noCardsText.visibility = View.VISIBLE
                }
                else{
                    binding.noCardsText.visibility = View.GONE
                }
                binding.accomplishedMissionsList.visibility=View.VISIBLE
                binding.legendsCL.visibility=View.VISIBLE
                context?.let{binding.accomplishedMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.activeMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
            }
        })
        viewModel.activeMissionsSelected.observe(viewLifecycleOwner, Observer { activeSelected->
            if(activeSelected){
                binding.activeMissionsButton.setBackgroundResource(R.drawable.login_resend_active)
                binding.accomplishedMissionsButton.setBackgroundResource(R.drawable.login_resend_inactive)
                if(zeroAvailableCards){
                    binding.noCardsText.visibility = View.VISIBLE
                }
                else{
                    binding.noCardsText.visibility = View.GONE
                }
                binding.activeMissionsList.visibility=View.VISIBLE
                binding.accomplishedMissionsList.visibility=View.GONE
                binding.legendsCL.visibility=View.GONE
                context?.let{binding.activeMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
                context?.let{binding.accomplishedMissionsButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))}
            }
        })
        val activeMissionsAdapter=ActiveMissions2Adapter(ActiveMissions2Adapter.OnClickListener{
                findNavController().navigate(YourPreviousMissionsFragmentDirections.actionYourPreviousMissionsFragmentToDetailMission(it))
            })
        val accomplishedMissionsAdapter=AccomplishedMissionsAdapter(AccomplishedMissionsAdapter.OnClickListener{
            //add argument
            findNavController().navigate(YourPreviousMissionsFragmentDirections.actionYourPreviousMissionsFragmentToAccomplishedMissionDetails(it))
        })
        viewModel.activeMissionsToDisplay.observe(viewLifecycleOwner, Observer {
            val list = it.filter { mission -> mission != currentMission }
            zeroAvailableCards = if (list.isNotEmpty()) {
                activeMissionsAdapter.submitList(list)
                false
            } else{
                true
            }
        })
        viewModel.accomplishedMissionsToDisplay.observe(viewLifecycleOwner, Observer {
            val list = it.filter { mission -> mission != currentMission }
            if (list.isNotEmpty()) {
                accomplishedMissionsAdapter.submitList(list)
                binding.noCardsText.visibility = View.GONE
                zeroAccomplishedCards=false
            } else {
                binding.accomplishedMissionsList.visibility=View.GONE
                binding.noCardsText.visibility = View.VISIBLE
                zeroAccomplishedCards=true
            }
        })
        binding.activeMissionsList.adapter=activeMissionsAdapter
        binding.accomplishedMissionsList.adapter=accomplishedMissionsAdapter
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(true)

        return binding.root
    }


}