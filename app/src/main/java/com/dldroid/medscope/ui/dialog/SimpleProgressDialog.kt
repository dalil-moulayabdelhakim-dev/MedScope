package com.dldroid.medscope.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.dldroid.medscope.R

class SimpleProgressDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.dialog)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_simple_progress, null)
        builder.setView(view)
        builder.setCancelable(false)
        return builder.create()
    }
}