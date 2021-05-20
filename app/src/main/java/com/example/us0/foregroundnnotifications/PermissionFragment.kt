package com.example.us0.foregroundnnotifications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.us0.DeleteAccountDialogFragment
import com.example.us0.R
import com.example.us0.databinding.FragmentPermissionBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback


class PermissionFragment : Fragment(),PermissionMandatoryDialogFragment.PermissionMandatoryDialogListener,DeleteAccountDialogFragment.DeleteAccountListener {

    private lateinit var binding: FragmentPermissionBinding
    private lateinit var viewModel: PermissionViewModel
    private lateinit var viewModelFactory: PermissionViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_permission,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        viewModelFactory = PermissionViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PermissionViewModel::class.java)
        binding.lifecycleOwner = this
        binding.permissionViewModel = viewModel
        val disclosureSheet = binding.disclosureFragment.root
        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(disclosureSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.skrim1.visibility = View.GONE
                    binding.allow.isEnabled = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        binding.neverMind.setOnClickListener { showPermissionMandatoryDialog() }
        viewModel.grantPermission.observe(viewLifecycleOwner, Observer<Boolean> { grantPermission ->
            if (grantPermission) {
                val intent = Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + context?.packageName)
                )
                startActivity(intent)
                viewModel.onGrantPermissionComplete()
            }
        })
        viewModel.disclosureVisible.observe(viewLifecycleOwner, Observer<Boolean> { visible ->
            if (visible) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.skrim1.visibility = View.VISIBLE
                binding.allow.isEnabled = false
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.skrim1.visibility = View.GONE
                binding.allow.isEnabled = true
            }
        })
        viewModel.toDOOA.observe(viewLifecycleOwner, Observer<Boolean> { toDOOA ->
            if (toDOOA) {
                findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToDOOAFragment())
                viewModel.toDOOAComplete()
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkUsageAccessPermission()
    }

    private fun showPermissionMandatoryDialog(){
        val dialog=PermissionMandatoryDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager, "Permission Mandatory")
    }
    private fun showDeleteAccountConfirmationDialog(){
        val dialog=DeleteAccountDialogFragment()
        val fraManager=childFragmentManager
        dialog.show(fraManager,"Delete Account?")
    }
    override fun onClickDeleteAccountButton() {
        showDeleteAccountConfirmationDialog()
    }

    override fun deleteAccount() {
        //delete account
    }
}