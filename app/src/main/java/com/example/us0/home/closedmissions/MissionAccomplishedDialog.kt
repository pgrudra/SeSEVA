package com.example.us0.home.closedmissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.example.us0.R
import kotlinx.android.synthetic.main.mission_accomplished_dialog.view.*

class MissionAccomplishedDialog: DialogFragment() {

    interface MissionAccomplishedDialogListener {
        fun chooseNewMission()
    }
    private fun sendBackResult() {
        val listener = parentFragment as MissionAccomplishedDialogListener
        listener.chooseNewMission()
        dismiss()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mission_accomplished_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val youRaised=arguments?.getInt("you_raised")
        val totalRaised=arguments?.getInt("total_raised")
        view.you_raised.text=getString(R.string.rs,youRaised)
        view.total_raised.text=getString(R.string.rs,totalRaised)
        view.choose_a_new_mission.setOnClickListener{
            sendBackResult()
        }
        dialog?.let{
            it.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.setCanceledOnTouchOutside(false)
            it.setCancelable(false)}
    }


    /*fun newInstance(num: Int): MyDialogFragment? {
        val f = MyDialogFragment()

        // Supply num input as an argument.
        val args = Bundle()
        args.putInt("num", num)
        f.setArguments(args)
        return f
    }*/

    /*companion object {
        fun newInstance(youRaised:Int, totalRaised:Int): MissionAccomplishedDialog {
            val dialog=MissionAccomplishedDialog()
            val args=Bundle()
            args.putInt("you_raised",youRaised)
            args.putInt("total_raised",totalRaised)
            dialog.arguments=args
            return dialog
        }
    }*/
}