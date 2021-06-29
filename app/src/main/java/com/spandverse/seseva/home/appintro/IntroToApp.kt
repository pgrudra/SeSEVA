package com.spandverse.seseva.home.appintro

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentIntroToAppBinding


class IntroToApp : Fragment() {
    private lateinit var binding: FragmentIntroToAppBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_to_app, container, false)
        val sharedPref = context?.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val userName=sharedPref?.getString((R.string.user_name).toString(),"User")?:"User"
binding.continueButton.setOnClickListener {
    NavHostFragment.findNavController(this).navigate(IntroToAppDirections.actionIntroToApp3ToIntroToApp22())
}
        binding.introTextBody.text=getString(R.string.intro_1_text,userName)


        return binding.root
    }


}