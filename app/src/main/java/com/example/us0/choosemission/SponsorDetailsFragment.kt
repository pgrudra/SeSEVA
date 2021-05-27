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
    private val cloudImagesReference = Firebase.storage
    private val cloudReference = Firebase.database.reference.child("sponsors")
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
        val sponsorNumber=SponsorDetailsFragmentArgs.fromBundle(requireArguments()).sponsorNumber
        binding.lifecycleOwner = viewLifecycleOwner
        val application = requireNotNull(this.activity).application
        val sponsorDao= AllDatabase.getInstance(application).SponsorDatabaseDao
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val sponsor:Sponsor?=sponsorDao.doesSponsorExist(sponsorNumber)
            if(sponsor==null){
                Log.i("SDF3","$sponsorNumber")
                cloudReference.child(sponsorNumber.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i("SDF1", "${snapshot.child("sponsorName").value.toString()}")
                            Log.i(
                                "SDF2",
                                "${snapshot.child("sponsoredAmount").value.toString().toInt()}"
                            )
                            val mSponsor = Sponsor(
                                sponsorNumber = sponsorNumber,
                                sponsorName = snapshot.child("sponsorName").value.toString(),
                                sponsorDescription = snapshot.child("sponsorDescription").value.toString(),
                                sponsorAddress = snapshot.child("sponsorAddress").value.toString(),
                                sponsorSite = snapshot.child("sponsorSite").value.toString(),
                                missionsSupported = snapshot.child("missionsSupported").value.toString(),
                                sponsoredAmount = snapshot.child("sponsoredAmount").value.toString()
                                    .toInt()
                            )
                            binding.sponsorName.text = mSponsor.sponsorName
                            binding.sponsorDescription.text = mSponsor.sponsorDescription
                            val reference =
                                cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mSponsor.sponsorNumber}Logo.png")
                            Glide.with(this@SponsorDetailsFragment)
                                .load(reference)
                                .apply(
                                    RequestOptions()
                                        .placeholder(R.drawable.ic_launcher_background)
                                        .error(R.drawable.ic_launcher_foreground)
                                )
                                .into(binding.sponsorLogo)
                            if (mSponsor.sponsorSite != null) {
                                binding.toSponsorSite.setOnClickListener {
                                    val i = Intent(Intent.ACTION_VIEW)
                                    i.data = Uri.parse(mSponsor.sponsorSite)
                                    startActivity(i)
                                }
                                binding.toSponsorSite.visibility = View.VISIBLE

                            }
                            mSponsor.sponsorAddress?.let { binding.address.text = it }
                            val list = mSponsor.missionsSupported.split(",").map { it.trim() }
                            val adapter =
                                SponsoredMissionsAdapter(SponsoredMissionsAdapter.OnClickListener {
                                    //download report
                                })
                            adapter.submitList(list)
                            binding.list.adapter = adapter
                            viewLifecycleOwner.lifecycleScope.launch {
                                sponsorDao.insert(mSponsor)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (!checkInternetConnectivity(context!!)) {
                                val dialog = NoInternetDialogFragment()
                                val fragmentManager = childFragmentManager
                                dialog.show(fragmentManager, "No Internet Connection")
                            }
                        }
                    })
            }
            else{
                binding.sponsorName.text=sponsor.sponsorName
                binding.sponsorDescription.text = sponsor.sponsorDescription
                val reference =
                    cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${sponsor.sponsorNumber}Logo.png")
                Glide.with(this@SponsorDetailsFragment)
                    .load(reference)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_foreground)
                    )
                    .into(binding.sponsorLogo)
                if (sponsor.sponsorSite != null) {
                    binding.toSponsorSite.setOnClickListener {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(sponsor?.sponsorSite)
                        startActivity(i)
                    }
                    binding.toSponsorSite.visibility = View.VISIBLE

                }
                sponsor.sponsorAddress?.let { binding.address.text=it }
                val list=sponsor.missionsSupported.split(",").map{it.trim() }
                Log.i("SDF8","$list")
                val adapter=SponsoredMissionsAdapter(SponsoredMissionsAdapter.OnClickListener{
                    //download report
                })
                adapter.submitList(list)
                binding.list.adapter=adapter
            }
            Log.i("SDF","5")

        }
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        return binding.root
    }
}