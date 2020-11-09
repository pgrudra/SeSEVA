package com.example.us0.foregroundnnotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.us0.R
import com.example.us0.databinding.FragmentForegroundBinding


class ForegroundService : Fragment() {
    companion object {
        fun newInstance() = ForegroundService()
    }

    private lateinit var binding: FragmentForegroundBinding
    private lateinit var viewModel: ForegroundServiceViewModel
    private lateinit var viewModelFactory: ForegroundServiceViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_foreground,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        viewModelFactory= ForegroundServiceViewModelFactory(application)
        viewModel=ViewModelProvider(this, viewModelFactory).get(ForegroundServiceViewModel::class.java)
        binding.lifecycleOwner = this
        binding.foregroundServiceViewModel=viewModel
        viewModel.startServiceBoolean.observe(viewLifecycleOwner, Observer<Boolean>{ startService->
            if(startService){
                viewModel.startService()
                viewModel.onStartServiceComplete()
            }

        })
        viewModel.stopServiceBoolean.observe(viewLifecycleOwner, Observer<Boolean>{ stopService->
            if(stopService){
                viewModel.stopService()
                viewModel.onStopServiceComplete()
            }

        })
        return binding.root
    }


}