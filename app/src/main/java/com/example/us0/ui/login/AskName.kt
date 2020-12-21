package com.example.us0.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.us0.R
import com.example.us0.databinding.FragmentAskNameBinding
import com.example.us0.foregroundnnotifications.PermissionViewModel
import com.example.us0.foregroundnnotifications.PermissionViewModelFactory


class AskName : Fragment() {

    private lateinit var binding: FragmentAskNameBinding
    private lateinit var viewModel: AskNameViewModel
    private lateinit var viewModelFactory: AskNameViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ask_name, container, false)
        val application = requireNotNull(this.activity).application
        viewModelFactory = AskNameViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AskNameViewModel::class.java)
        binding.askNameViewModel=viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.checkUserName()
        viewModel.nameInsertDone.observe(viewLifecycleOwner, Observer<Boolean>{nameInserted->
            if(nameInserted){
                val userName=binding.editTextTextPersonName.text.toString()
                Log.i("Lo",userName)
                viewModel.saveEverywhere(userName)
            }
        })
        viewModel.goToNextFragment.observe(viewLifecycleOwner,Observer<Boolean>{go->
            if(go){
                NavHostFragment.findNavController(this).navigate(AskNameDirections.actionAskNameToInstalledAppsActivity())
                viewModel.goToNextFragmentComplete()
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}