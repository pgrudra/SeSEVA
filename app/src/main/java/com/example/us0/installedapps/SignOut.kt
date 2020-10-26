package com.example.us0.installedapps

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignOut : Fragment() {

    private lateinit var binding: FragmentSignOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: SignOutViewModel
    private lateinit var viewModelFactory: SignOutViewModelFactory
    lateinit var appContext: Context
    var arrayAdapter: ArrayAdapter<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_out, container, false)

        viewModelFactory = SignOutViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(SignOutViewModel::class.java)
        appContext = context?.applicationContext ?: return binding.root

       /*installedApps()*/
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
        //viewModel.signOut(this)


        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            NavHostFragment.findNavController(this)
                .navigate(SignOutDirections.actionSignOutToMainActivity())
        }
    }


    private fun installedApps() {
        val list = activity?.packageManager?.getInstalledPackages(0)
        var appMutableList:MutableList<String?>?=null
        if (list != null) {
            for (i in list.indices) {
                val packageInfo = list.get(i)
                if (packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {

                    val appName =
                        activity?.packageManager?.let { packageInfo.applicationInfo.loadLabel(it).toString() }


                    if (appName != null) {
                        Log.i("Sign", appName)
                        appMutableList?.add(appName)
                    }

                    val appContext = context?.applicationContext ?: return
                    arrayAdapter = ArrayAdapter(
                        appContext,
                        R.layout.support_simple_spinner_dropdown_item, list as List<*>
                    )
                    binding.listView.adapter = arrayAdapter
                }
            }

        }
    }

}


