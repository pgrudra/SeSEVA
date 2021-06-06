package com.example.us0

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.grant_d_o_o_a_dialog.view.*

class GrantDOOADialog: DialogFragment() {

    interface GrantDOOAListener {
        fun toDOOAPermission(mode: Int?)
    }
    private fun sendBackResult(mode: Int?) {
        val listener = parentFragment as GrantDOOAListener
        listener.toDOOAPermission(mode)
        dismiss()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.grant_d_o_o_a_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mode=arguments?.getInt("serviceMode")
        view.grant_permission.setOnClickListener { sendBackResult(mode) }
        view.cancel_button.setOnClickListener { dismiss() }
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    companion object {
        fun newInstance(): GrantDOOADialog {
            return GrantDOOADialog()
        }
    }
}
