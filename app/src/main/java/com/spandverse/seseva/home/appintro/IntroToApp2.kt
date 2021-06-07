package com.spandverse.seseva.home.appintro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentIntroToApp2Binding



class IntroToApp2 : Fragment() {

    private lateinit var binding: FragmentIntroToApp2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_to_app2, container, false)
binding.button.setOnClickListener {
    findNavController().navigate(IntroToApp2Directions.actionIntroToApp22ToPassageFragment())
}
        return binding.root
    }


}