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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignOut : Fragment() {

    private lateinit var binding: FragmentSignOutBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: SignOutViewModel
    private lateinit var viewModelFactory: SignOutViewModelFactory
    private lateinit var appContext: Context
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_out, container, false)
        viewModelFactory = SignOutViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(SignOutViewModel::class.java)
        appContext = context?.applicationContext ?: return binding.root
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(com.example.us0.R.string.default_web_client_id.toString())
            .requestEmail()
            .build()

        auth= Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(appContext, gso)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOut.setOnClickListener { signOut() }
        binding.deleteAccount.setOnClickListener { deleteAccount() }
    }

    private fun signOut() {
        val sharedPref =
            activity?.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        sharedPref?.edit()?.clear()?.apply()
        // Firebase sign out
        auth.signOut()


        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            NavHostFragment.findNavController(this)
                .navigate(SignOutDirections.actionSignOutToMainActivity())
        }
    }

    private fun deleteAccount() {
        val sharedPref =
            activity?.getSharedPreferences(
                (R.string.shared_pref).toString(),
                Context.MODE_PRIVATE
            )
        sharedPref?.edit()?.clear()?.apply()
        val user = auth.currentUser
      try {
            Log.i("Delete", "swe")
           // auth.signOut()
          if(user==null){
              Log.i("Delete","OPK")
          }
            user?.delete()?.addOnCompleteListener {

                NavHostFragment.findNavController(this)
                    .navigate(SignOutDirections.actionSignOutToMainActivity())
            }


        } catch (e: kotlin.Exception) {
            Log.i("Delete", "ERROR")
        }
        try{googleSignInClient.revokeAccess().addOnSuccessListener {  Log.i("Delete","revokeAccess") }}
        catch(e:kotlin.Exception){
            Log.i("Delete", "qqq")
        }

    }


}


