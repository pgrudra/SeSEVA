package com.example.us0.home.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentProfileBinding
import com.example.us0.home.DrawerLocker
import com.example.us0.home.feats.FeatsViewModel
import com.example.us0.home.feats.FeatsViewModelFactory


class ProfileFragment : Fragment() {
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
        drawerLoker!!.setDrawerEnabled(true)
        drawerLoker.displayBottomNavigation(true)
        return binding.root
    }
}