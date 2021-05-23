package com.example.us0

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.manage_profile_dialog.view.*

class ManageProfileDialogFragment: DialogFragment() {

    interface ManageProfileListener {
        fun changeUsername(userName: String)
    }
    private fun sendBackResult(userName: String) {
        val listener = parentFragment as ManageProfileDialogFragment.ManageProfileListener
        listener.changeUsername(userName)
        dismiss()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.manage_profile_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners(view)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private fun setUpClickListeners(view: View){
        view.done_button.setOnClickListener{
            val userName = view.edit_box.text.toString()
            if (userName != "") {
                Log.i("Lo", userName)
                sendBackResult(userName)
            } else {
                makeErrorBackground(view)
            }
             }
        view.cancel_button.setOnClickListener { dismiss() }
    }

    private fun makeErrorBackground(view: View) {
        view.edit_box.setBackgroundResource(R.drawable.login_email_edit_box_error)
        view.errorMessage.visibility=View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            view.edit_box.setBackgroundResource(R.drawable.login_email_edit_box_active)
            view.errorMessage.visibility = View.INVISIBLE
        }, 2500)
    }

    companion object {
        fun newInstance(): ManageProfileDialogFragment {
            return ManageProfileDialogFragment()
        }
    }
}