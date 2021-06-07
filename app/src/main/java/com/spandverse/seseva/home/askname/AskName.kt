package com.spandverse.seseva.home.askname

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.spandverse.seseva.R
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentAskNameBinding
import com.spandverse.seseva.home.DrawerLocker
import com.spandverse.seseva.ui.login.NoInternetDialogFragment


class AskName : Fragment(),NoInternetDialogFragment.NoInternetDialogListener {

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
        val datasource = AllDatabase.getInstance(application).MissionsDatabaseDao
        viewModelFactory = AskNameViewModelFactory(datasource, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AskNameViewModel::class.java)
        binding.askNameViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        //nav drawer disable
        viewModel.nameInsertDone.observe(viewLifecycleOwner, Observer<Boolean> { nameInserted ->
            if (nameInserted) {
                val userName = binding.editTextTextPersonName.text.toString()
                if (userName != "") {
                    Log.i("Lo", userName)
                    viewModel.saveEverywhere(userName)
                } else {
                    makeErrorBackground()
                }
            }
        })
        viewModel.goToNextFragment.observe(viewLifecycleOwner, Observer<Boolean> { go ->
            if (go) {
                binding.editTextTextPersonName.hideKeyboard()
                Log.i("AN5","$go")
                NavHostFragment.findNavController(this)
                    .navigate(AskNameDirections.actionAskNameToIntroToApp3())
                viewModel.goToNextFragmentComplete()
            }
        })
        viewModel.noInternet.observe(viewLifecycleOwner, Observer<Boolean> { noInternet ->
            if (noInternet) {
              showNoInternetConnectionDialog()
                Log.i("fg","b")
            }
        })
        binding.editTextTextPersonName.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    makeActiveBackground()
                }
            }

        return binding.root
    }
    fun View.focus() {
        requestFocus()
        showKeyboard()
    }
    fun View.showKeyboard() { // Or View.showKeyboard()
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
    }
    fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    private fun makeErrorBackground() {
        binding.editTextTextPersonName.setBackgroundResource(R.drawable.login_email_edit_box_error)
        binding.usernameButton.setBackgroundResource(R.drawable.login_other_sign_in_error_button)

            binding.errorMessage.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                binding.editTextTextPersonName.setBackgroundResource(R.drawable.login_email_edit_box_active)
                binding.usernameButton.setBackgroundResource(R.drawable.login_other_sign_in_button)
                binding.errorMessage.visibility = View.INVISIBLE

            }, 2500)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        binding.editTextTextPersonName.focus()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun makeActiveBackground() {
        binding.editTextTextPersonName.setBackgroundResource(R.drawable.login_email_edit_box_active)
        binding.underline.visibility = View.GONE
    }
    private fun showNoInternetConnectionDialog() {
        // Create an instance of the dialog fragment and show it
        Log.i("fg","sdf")
        val dialog = NoInternetDialogFragment()
        val fragmentManager=childFragmentManager
        dialog.show(fragmentManager,"No Internet Connection")
    }

    override fun removeRedBackground(dialog: DialogFragment) {
    }
}