package com.spandverse.seseva

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.manage_profile_dialog.view.*
import kotlinx.android.synthetic.main.p_p_dialog.view.*

class PPDialogFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.p_p_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.cancel_button_p_p.setOnClickListener { dismiss() }
    }

    companion object {
        fun newInstance(): PPDialogFragment {
            return PPDialogFragment()
        }
    }
}