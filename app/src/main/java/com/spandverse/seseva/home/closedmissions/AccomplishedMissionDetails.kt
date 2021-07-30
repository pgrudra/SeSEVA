package com.spandverse.seseva.home.closedmissions

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.databinding.FragmentAccomplishedMissionsDetailsBinding
import com.spandverse.seseva.home.DrawerLocker
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.spandverse.seseva.checkInternetConnectivity
import com.spandverse.seseva.ui.login.NoInternetDialogFragment

class AccomplishedMissionDetails : Fragment(), NoInternetDialogFragment.NoInternetDialogListener {
    private val cloudStorageReference = Firebase.storage
    private lateinit var binding: FragmentAccomplishedMissionsDetailsBinding
    private lateinit var viewModel: AMDViewModel
    private lateinit var appContext: Context
    private lateinit var sharedPref: SharedPreferences
    private lateinit var viewModelFactory: AMDViewModelFactory
    private lateinit var downLoadManager:DownloadManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_accomplished_missions_details, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        val mission: DomainActiveMission = AccomplishedMissionDetailsArgs.fromBundle(requireArguments()).selectedMission
        viewModelFactory = AMDViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AMDViewModel::class.java)
        //check Internet, then check name, then check mission
        binding.amdViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(true)
        appContext = context?.applicationContext ?: return binding.root
        sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        val misDesLength = mission.missionDescription.length
        if (misDesLength < 226) {
            binding.expandOrContract.visibility = View.GONE
            binding.accomplishedMissionDescription.text = mission.missionDescription
        }
        viewModel.showDetailMissionDescription.observe(
            viewLifecycleOwner,
            Observer<Boolean> { show ->
                if (show) {
                    binding.accomplishedMissionDescription.text = mission.missionDescription
                    binding.expandOrContract.setImageResource(R.drawable.ic_collapse_vector)
                } else if (misDesLength > 225) {
                    binding.accomplishedMissionDescription.text = getString(
                        R.string.dots, mission.missionDescription.substring(
                            0,
                            225
                        )
                    )
                    binding.expandOrContract.setImageResource(R.drawable.ic_expand_vector)
                }

            })
        binding.missionName.text=getString(R.string.mission_is_accomplished,mission.missionName)
        binding.accomplishedMissionDescription.text=mission.missionDescription
        binding.amountRaised.text=getString(R.string.rs,mission.totalMoneyRaised)
        binding.contributors.text=mission.contributors.toString()
        binding.accomplishedOn.text=mission.deadlineAsDateShort
        binding.contribution.text=getString(R.string.rs,mission.contribution)
        binding.sponsorName.text=mission.sponsorName
        binding.toSponsor.setOnClickListener {
            findNavController().navigate(AccomplishedMissionDetailsDirections.actionAccomplishedMissionDetailsToSponsorDetailsFragment(mission.sponsorNumber))
        }
        val reference = cloudStorageReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mission.sponsorNumber}Logo.png")
        Glide.with(this)
            .load(reference)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_sponsor)
                    .error(R.drawable.ic_sponsor)
            )
            .into(binding.sponsorLogo)
        if(mission.reportAvailable){
            binding.downloadReportButton.text=getString(R.string.download_report)
            binding.downloadReportButton.isEnabled=true
            context?.let { binding.downloadReportButton.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary)) }
            binding.downloadReportButton.setBackgroundResource(R.drawable.login_change_email)
        }
        binding.downloadReportButton.setOnClickListener {
                val reportReference = cloudStorageReference.reference.child("missionReports/mission${mission.missionNumber}Report.pdf")
                reportReference.downloadUrl.addOnSuccessListener {
                    val url=it.toString()
                    downloadFile("${mission.missionName} Report",url)
                }.addOnFailureListener {
                        if(!checkInternetConnectivity(requireContext())){
                            showNoInternetConnectionDialog()
                        }
                        else{
                            Toast.makeText(context,"Something went wrong",Toast.LENGTH_LONG).show()
                        }
                }
        }
        return binding.root
    }

    private fun downloadFile(
        fileName: String,
        url: String
    ) {
        downLoadManager=appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri= Uri.parse(url)
        val request=DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setTitle(fileName)
        request.setDestinationInExternalFilesDir(appContext,Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")
        with(sharedPref.edit()) {
            this?.putLong((com.spandverse.seseva.R.string.report_download_id).toString(), downLoadManager.enqueue(request))
            this?.apply()
        }
    }

    private fun showNoInternetConnectionDialog() {
        val dialog = NoInternetDialogFragment()
        val fragmentManager = childFragmentManager
        dialog.show(fragmentManager, "No Internet Connection")
    }
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadID=sharedPref.getLong((R.string.report_download_id).toString(),0)
            if (downloadID == id) {
                val uri=downLoadManager.getUriForDownloadedFile(downloadID)
                val intentToView=Intent(Intent.ACTION_VIEW)
                intentToView.setDataAndType(uri,"application/pdf")
                intentToView.flags=Intent.FLAG_GRANT_READ_URI_PERMISSION
                try{startActivity(intentToView)
                }catch (e:ActivityNotFoundException){
                    Toast.makeText(appContext,"No application available to view the report PDF",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun removeRedBackground(dialog: DialogFragment) {
    }

    override fun onResume() {
        super.onResume()
        try{
            appContext.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }catch (e:Exception){
            context?.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    override fun onPause() {
        try{
            appContext.unregisterReceiver(onDownloadComplete)
        }catch (e:Exception){
            try{
                context?.unregisterReceiver(onDownloadComplete)
            }catch (e:Exception){
            }
        }
        super.onPause()
    }
}