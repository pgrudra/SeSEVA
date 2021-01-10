package com.example.us0.ui.login

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.us0.R


class NoInternetDialogFragment : DialogFragment() {
    internal lateinit var listener: NoInternetDialogListener

    interface NoInternetDialogListener{
        fun removeRedBackground(dialog:DialogFragment)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it,android.R.style.Theme_DeviceDefault_Dialog_MinWidth)
            builder.setMessage(R.string.no_internet_message)
                .setTitle(R.string.no_internet_title)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.removeRedBackground(this)
                    })


            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = parentFragment as NoInternetDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }



}

