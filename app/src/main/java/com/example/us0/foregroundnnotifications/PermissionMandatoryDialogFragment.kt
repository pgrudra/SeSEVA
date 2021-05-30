package com.example.us0.foregroundnnotifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.example.us0.R
import kotlinx.android.synthetic.main.fragment_permission_mandatory_dialog.view.*


class PermissionMandatoryDialogFragment:DialogFragment() {

    interface PermissionMandatoryDialogListener {
        fun onClickDeleteAccountButton()
    }
    private fun sendBackResult() {
        val listener = parentFragment as PermissionMandatoryDialogListener?
        listener!!.onClickDeleteAccountButton()
        dismiss()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permission_mandatory_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners(view)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private fun setUpClickListeners(view: View){
        view.delete_account.setOnClickListener {sendBackResult()  }
        view.ok.setOnClickListener { dismiss() }
    }
    companion object {
        fun newInstance(): PermissionMandatoryDialogFragment {
            return PermissionMandatoryDialogFragment()
        }
    }
}

