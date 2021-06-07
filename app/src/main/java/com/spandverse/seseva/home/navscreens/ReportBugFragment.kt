package com.spandverse.seseva.home.navscreens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentReportBugBinding


class ReportBugFragment : Fragment() {
    private lateinit var binding:FragmentReportBugBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_bug, container, false)

        return binding.root
    }
}