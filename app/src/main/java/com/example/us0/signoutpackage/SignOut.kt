package com.example.us0.signoutpackage

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.us0.R
import com.example.us0.databinding.FragmentSignOutBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient


class SignOut : Fragment() {

    private lateinit var binding: FragmentSignOutBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: SignOutViewModel
    private lateinit var viewModelFactory: SignOutViewModelFactory
    private lateinit var appContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_out, container, false)
        viewModelFactory = SignOutViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(SignOutViewModel::class.java)
        appContext = context?.applicationContext ?: return binding.root
        googleSignInClient = GoogleSignIn.getClient(appContext, viewModel.gso)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOut.setOnClickListener { signOut() }
    }

    private fun signOut() {
        // Firebase sign out
        viewModel.auth.signOut()


        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            NavHostFragment.findNavController(this)
                .navigate(SignOutDirections.actionSignOutToMainActivity())
        }
    }




}


