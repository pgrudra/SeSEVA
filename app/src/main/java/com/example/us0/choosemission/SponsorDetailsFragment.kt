package com.example.us0.choosemission

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.adapters.SponsoredMissionsAdapter
import com.example.us0.checkInternetConnectivity
import com.example.us0.data.AllDatabase
import com.example.us0.data.sponsors.Sponsor
import com.example.us0.databinding.FragmentSponsorDetailsBinding
import com.example.us0.home.DrawerLocker
import com.example.us0.ui.login.NoInternetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch


class SponsorDetailsFragment : Fragment() {
    private lateinit var binding: FragmentSponsorDetailsBinding
    private lateinit var viewModel: SponsorDetailsViewModel
    private lateinit var viewModelFactory: SponsorDetailsViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sponsor_details,
            container,
            false
        )
        val sponsorNumber = SponsorDetailsFragmentArgs.fromBundle(requireArguments()).sponsorNumber
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val sponsorDao = AllDatabase.getInstance(application).SponsorDatabaseDao
        val drawerLocker = (activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        viewModelFactory = SponsorDetailsViewModelFactory(sponsorNumber,sponsorDao, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SponsorDetailsViewModel::class.java)
        binding.sponsorDetailsViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.showEntireMissionsList.observe(viewLifecycleOwner, Observer<Boolean> { show ->
            if (show) {
                binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
                viewModel.displayEntireList()
            } else {
                binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                viewModel.displayShortList()
            }
                }
        )
        viewModel.expandContractVisibility.observe(viewLifecycleOwner,{visible ->
            if(visible){
                binding.expandOrContract.visibility=View.VISIBLE
            }
            else{
                binding.expandOrContract.visibility=View.GONE
            }
        })
        viewModel.showNoInternetDialog.observe(viewLifecycleOwner,{show ->
            if(show){
                val dialog = NoInternetDialogFragment()
                val fragmentManager = childFragmentManager
                dialog.show(fragmentManager, "No Internet Connection")
            }
        })
        viewModel.logoReference.observe(viewLifecycleOwner,{it ->
            Glide.with(this)
                .load(it)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                )
                .into(binding.sponsorLogo)
        })
        binding.toSponsorSite.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(viewModel.sponsorSite.value)
            startActivity(i)
        }
        viewModel.sponsorSite.observe(viewLifecycleOwner,{it ->
            if(it!=null){
                binding.toSponsorSite.visibility=View.VISIBLE
            }
        })
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        return binding.root
    }
}