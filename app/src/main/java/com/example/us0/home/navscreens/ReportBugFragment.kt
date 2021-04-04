package com.example.us0.home.navscreens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.FragmentReportBugBinding


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