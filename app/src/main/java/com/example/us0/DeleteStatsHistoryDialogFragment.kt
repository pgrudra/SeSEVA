package com.example.us0

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.delete_things_dialog.view.*

class DeleteStatsHistoryDialogFragment:DialogFragment() {

    interface DeleteStatsHistoryListener{
        fun deleteStatsHistory()
    }
    private fun sendBackResult() {
        val listener = parentFragment as DeleteStatsHistoryListener
        listener.deleteStatsHistory()
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
        view.delete.setOnClickListener { sendBackResult() }
        view.cancel.setOnClickListener { dismiss() }
    }
    private fun setUpBody(view: View){
        view.body_text.text="Clearing data removes all previous usage stats permanently."
    }
    companion object {
        fun newInstance(): DeleteStatsHistoryDialogFragment {
            return DeleteStatsHistoryDialogFragment()
        }
    }
}