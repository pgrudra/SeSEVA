package com.example.us0.home.appintro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.example.us0.R
import com.example.us0.databinding.FragmentIntroToAppBinding


class IntroToApp : Fragment() {

    private lateinit var binding: FragmentIntroToAppBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_to_app, container, false)
binding.button2.setOnClickListener {
    NavHostFragment.findNavController(this).navigate(IntroToAppDirections.actionIntroToApp3ToIntroToApp22())
}


        return binding.root
    }


}