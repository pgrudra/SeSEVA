package com.spandverse.seseva

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.spandverse.seseva.ui.login.NoInternetDialogFragment


class ReAuthenticateAndDeleteFragment : DialogFragment() {
    internal lateinit var listener: ReAuthenticateAndDeleteDialogListener

    interface ReAuthenticateAndDeleteDialogListener{
        fun reAuthenticateAndDelete(dialog:DialogFragment)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val sharedPref =
                activity?.getSharedPreferences(
                    (R.string.shared_pref).toString(),
                    Context.MODE_PRIVATE
                )
            val emailAddress = sharedPref?.getString((R.string.email_address).toString(), "null") ?: "null"
            val body = getString(R.string.reauthenticate_and_delete_body, emailAddress)
            val builder =
                AlertDialog.Builder(it, android.R.style.Theme_DeviceDefault_Dialog_MinWidth)
            builder.setMessage(body)
                .setTitle(R.string.reauthenticate_and_delete_title)
                .setPositiveButton(R.string.reauthenticate_and_delete_button,
                    DialogInterface.OnClickListener { _, _ ->
                        listener.reAuthenticateAndDelete(this)
                    })
                .setNegativeButton(android.R.string.cancel, null)
            // Create the AlertDialog object and return it
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = parentFragment as ReAuthenticateAndDeleteFragment.ReAuthenticateAndDeleteDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }



}

