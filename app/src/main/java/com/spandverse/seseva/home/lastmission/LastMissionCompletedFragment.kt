package com.spandverse.seseva.home.lastmission

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.spandverse.seseva.R
import com.spandverse.seseva.checkInternetConnectivity
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentLastMissionCompletedBinding
import com.spandverse.seseva.ui.login.NoInternetDialogFragment


class LastMissionCompletedFragment : Fragment(), NoInternetDialogFragment.NoInternetDialogListener {
    private lateinit var binding:FragmentLastMissionCompletedBinding
    private lateinit var viewModel:LastMissionCompletedViewModel
    private lateinit var viewModelFactory:LastMissionCompletedViewModelFactory
    private lateinit var appContext: Context
    private lateinit var sharedPref: SharedPreferences
    private lateinit var downLoadManager:DownloadManager
    private val cloudStorageReference = Firebase.storage
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val args=LastMissionCompletedFragmentArgs.fromBundle(requireArguments())
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_mission_completed, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = LastMissionCompletedViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LastMissionCompletedViewModel::class.java)
        binding.lastMissionCompletedViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        appContext = context?.applicationContext ?: return binding.root
        sharedPref =
            appContext.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        viewModel.loadMission(args.missionNumber)
        viewModel.goToHome.observe(viewLifecycleOwner, Observer<Boolean> { goto->
            if(goto){
                findNavController().navigate(LastMissionCompletedFragmentDirections.actionLastMissionCompletedFragmentToPassageFragment())
                viewModel.onGoToHomeComplete()
            }
        })
        viewModel.enableButton.observe(viewLifecycleOwner, Observer<Boolean> { enable->
            if(enable){
                binding.progressBar1.visibility=View.GONE
                binding.skrim.visibility=View.GONE
                binding.button4.isEnabled=true
                binding.chooseNewMission.isEnabled=true
            }
        })
        viewModel.reportAvailable.observe(viewLifecycleOwner, Observer<Boolean> { reportAvailable->
            if(reportAvailable){
                binding.button4.visibility=View.VISIBLE
                binding.reportPendingText.visibility=View.GONE
            }
        })
        viewModel.downLoadReport.observe(viewLifecycleOwner, Observer<Boolean> { download->
            if(download){
                val reportReference = cloudStorageReference.reference.child("missionReports/mission${args.missionNumber}Report.pdf")
                reportReference.downloadUrl.addOnSuccessListener {
                    val url=it.toString()
                    downloadFile("${viewModel.missionName} Report",url)
                }.addOnFailureListener {
                    if(!checkInternetConnectivity(requireContext())){
                        showNoInternetConnectionDialog()
                    }
                    else{
                        Toast.makeText(context,"Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.downloadReportComplete()
            }
        })
        appContext.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        return binding.root
    }
    private fun showNoInternetConnectionDialog() {
        val dialog = NoInternetDialogFragment()
        val fragmentManager = childFragmentManager
        dialog.show(fragmentManager, "No Internet Connection")
    }
    private fun downloadFile(
        fileName: String,
        url: String
    ) {
        downLoadManager=appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri= Uri.parse(url)
        val request= DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setTitle(fileName)
        request.setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")
        with(sharedPref.edit()) {
            this?.putLong((com.spandverse.seseva.R.string.report_download_id).toString(), downLoadManager.enqueue(request))
            this?.apply()
        }
    }

    override fun removeRedBackground(dialog: DialogFragment) {
    }
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadID=sharedPref.getLong((R.string.report_download_id).toString(),0)
            if (downloadID == id) {
                val uri=downLoadManager.getUriForDownloadedFile(downloadID)
                val intentToView= Intent(Intent.ACTION_VIEW)
                intentToView.setDataAndType(uri,"application/pdf")
                intentToView.flags= Intent.FLAG_GRANT_READ_URI_PERMISSION
                try{startActivity(intentToView)
                }catch (e: ActivityNotFoundException){
                    Toast.makeText(appContext,"No application available to view the report PDF",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        appContext.unregisterReceiver(onDownloadComplete)
    }
}