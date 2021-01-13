package com.example.us0.home.rules

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.us0.R
import com.example.us0.databinding.FragmentRules1Binding


class RulesFragment1 : Fragment() {
    private lateinit var binding:FragmentRules1Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rules1, container, false)
binding.seeRulesButton.setOnClickListener {
findNavController().navigate(RulesFragment1Directions.actionRulesFragment1ToRulesFragment2())
}
        return binding.root
    }

}