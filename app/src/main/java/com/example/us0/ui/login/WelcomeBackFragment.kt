package com.example.us0.ui.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentAskNameBinding
import com.example.us0.databinding.FragmentWelcomeBackBinding
import com.example.us0.home.HomeFragmentDirections
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class WelcomeBackFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBackBinding
    private lateinit var viewModel: WelcomeBackViewModel
    private lateinit var viewModelFactory: WelcomeBackViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_back, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = WelcomeBackViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WelcomeBackViewModel::class.java)
        binding.welcomeBackViewModel=viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        val args=WelcomeBackFragmentArgs.fromBundle(requireArguments())
        viewModel.setUserName(args.userName)
        viewModel.goToHomeActivity.observe(viewLifecycleOwner, Observer<Boolean> {goto->
            if(goto){
                findNavController().navigate(WelcomeBackFragmentDirections.actionWelcomeBackFragmentToHomeActivity())
                viewModel.goToHomeActivityComplete()
            }
        })
        return binding.root
    }


}