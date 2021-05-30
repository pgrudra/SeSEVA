package com.example.us0.home.closedmissions

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.us0.R
import com.example.us0.choosemission.DetailMissionArgs


class DetailClosedMissionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mission= DetailClosedMissionFragmentArgs.fromBundle(requireArguments()).selectedMission
Log.i("klo","${mission.toString()}")
        return inflater.inflate(R.layout.fragment_deatil_closed_mission, container, false)
    }
}