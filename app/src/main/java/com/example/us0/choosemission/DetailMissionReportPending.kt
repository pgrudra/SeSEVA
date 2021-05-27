package com.example.us0.choosemission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.FragmentDetailMissionReportPendingBinding


class DetailMissionReportPending : Fragment() {
    private lateinit var binding: FragmentDetailMissionReportPendingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail_mission_report_pending,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }


}