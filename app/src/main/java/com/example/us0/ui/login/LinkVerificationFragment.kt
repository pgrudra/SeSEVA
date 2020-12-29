package com.example.us0.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentLinkVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LinkVerificationFragment : Fragment() {


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    private val viewModel:LoginViewModel by activityViewModels()
    private lateinit var binding:FragmentLinkVerificationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_link_verification,
            container,
            false
        )
        binding.loginViewModel=viewModel
binding.lifecycleOwner=this
        viewModel.backToLoginScreen.observe(viewLifecycleOwner, Observer{ backToLoginScreen->
            if(backToLoginScreen){
                Log.i("ff","OPOP")
findNavController().navigate(LinkVerificationFragmentDirections.actionLinkVerificationFragmentToLoginFragment())
                viewModel.backToLoginScreenComplete()
            }
        })
        viewModel.enableResendButton.observe(viewLifecycleOwner, Observer { enableResend->
            if(enableResend){
                binding.resend.isEnabled=true
                binding.resend.setBackgroundResource(R.drawable.login_resend_active)
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if(currentUser!=null){
findNavController().navigate(LinkVerificationFragmentDirections.actionLinkVerificationFragmentToAskName())
        }

    }


}