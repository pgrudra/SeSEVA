package com.spandverse.seseva

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.delete_things_dialog.view.*

class SignOutDialogFragment: DialogFragment() {

    interface SignOutListener {
        fun signOut()
    }
    private fun sendBackResult() {
        val listener = parentFragment as SignOutDialogFragment.SignOutListener
        listener.signOut()
        dismiss()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.delete_things_dialog, container)
    }
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners(view)
        setUpBody(view)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private fun setUpClickListeners(view: View){
        view.delete_delete_account_sign_out.setOnClickListener { sendBackResult() }
        view.cancel.setOnClickListener { dismiss() }
    }
    private fun setUpBody(view: View){
        view.body_text.text="Signing out will erase all locally stored data permanently."
        view.delete_delete_account_sign_out.text="SIGN OUT"
    }
    companion object {
        fun newInstance(): SignOutDialogFragment {
            return SignOutDialogFragment()
        }
    }
}