package com.spandverse.seseva.home.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentReportBinding

class ReportFragment : Fragment() {

    private lateinit var binding:FragmentReportBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val args= ReportFragmentArgs.fromBundle(requireArguments())

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report, container, false)
        return binding.root
    }

}