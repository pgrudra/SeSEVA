package com.spandverse.seseva

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.manage_profile_dialog.view.*
import kotlinx.android.synthetic.main.t_o_u_dialog.view.*

class TOUDialogFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.t_o_u_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.cancel_button_t_o_u.setOnClickListener { dismiss() }
    }

    companion object {
        fun newInstance(): TOUDialogFragment {
            return TOUDialogFragment()
        }
    }
}