package com.spandverse.seseva.choosemission

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentSponsorDetailsBinding
import com.spandverse.seseva.home.DrawerLocker
import com.spandverse.seseva.ui.login.NoInternetDialogFragment


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
        viewModel.missionsSponsoredVisibility.observe(viewLifecycleOwner,{visible ->
            if(visible){
                binding.missionsSupportedText.visibility=View.VISIBLE
            }
            else{
                binding.missionsSupportedText.visibility=View.GONE
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
                        .placeholder(R.drawable.ic_sponsor)
                        .error(R.drawable.ic_sponsor)
                )
                .into(binding.sponsorLogo)
        })
        viewModel.hideProgress.observe(viewLifecycleOwner,{hide ->
            if(hide){
                binding.progressBar1.visibility=View.GONE
                binding.skrim.visibility=View.GONE
            }
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